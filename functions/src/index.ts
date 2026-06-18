import { onCall, HttpsError } from "firebase-functions/v2/https";
import { onDocumentWritten } from "firebase-functions/v2/firestore";
import * as functionsV1 from "firebase-functions/v1";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { getFirestore, FieldValue } from "firebase-admin/firestore";
import { getDatabase } from "firebase-admin/database";
import { getAuth } from "firebase-admin/auth";
import { initializeApp } from "firebase-admin/app";

initializeApp();
const db = getFirestore();

export const removeAdminFromCourse = onCall(async (request) => {
    if (!request.auth) {
        throw new HttpsError("unauthenticated", "Must be signed in.");
    }

    const { email, courseId } = request.data as { email: string; courseId: string };

    if (!email || !courseId) {
        throw new HttpsError("invalid-argument", "email and courseId are required.");
    }

    const courseRef = db.collection("courses").doc(courseId);
    const courseSnap = await courseRef.get();

    if (!courseSnap.exists) {
        throw new HttpsError("not-found", "Course not found.");
    }

    const callerEmail = request.auth.token.email;
    const adminIDs: string[] = courseSnap.data()?.adminIDs ?? [];

    if (!callerEmail || !adminIDs.includes(callerEmail)) {
        throw new HttpsError("permission-denied", "You are not an admin of this course.");
    }

    // Find the user by email and update their adminCourses
    const usersSnap = await db.collection("users")
        .where("email", "==", email)
        .limit(1)
        .get();

    const batch = db.batch();

    batch.update(courseRef, {
        adminIDs: FieldValue.arrayRemove(email),
    });

    if (!usersSnap.empty) {
        batch.update(usersSnap.docs[0].ref, {
            adminCourses: FieldValue.arrayRemove(courseId),
            lastUpdated: FieldValue.serverTimestamp(),
        });
    }

    await batch.commit();

    return { success: true };
});

export const syncUserLastUpdated = onDocumentWritten("users/{userId}", async (event) => {
    const before = event.data?.before?.data();
    const after = event.data?.after?.data();

    // Skip creates and deletes — only care about updates
    if (!before || !after) return;

    // Strip lastUpdated from both snapshots and compare everything else
    const { lastUpdated: _a, ...beforeRest } = before;
    const { lastUpdated: _b, ...afterRest } = after;

    // If nothing other than lastUpdated changed, bail out (avoids infinite loop)
    if (JSON.stringify(beforeRest) === JSON.stringify(afterRest)) return;

    await event.data!.after.ref.update({
        lastUpdated: FieldValue.serverTimestamp(),
    });
});

export const cleanupOrphanedGames = onDocumentWritten("users/{userId}", async (event) => {
    const before: string[] = event.data?.before?.data()?.gameIDs ?? [];
    const after: string[] = event.data?.after?.data()?.gameIDs ?? [];

    const removedIds = before.filter((id) => !after.includes(id));
    if (removedIds.length === 0) return;

    const batch = db.batch();
    let hasDeletes = false;

    for (const gameId of removedIds) {
        const owners = await db.collection("users")
            .where("gameIDs", "array-contains", gameId)
            .limit(1)
            .get();

        if (owners.empty) {
            batch.delete(db.collection("games").doc(gameId));
            hasDeletes = true;
        }
    }

    if (hasDeletes) await batch.commit();
});

export const clearTierDowngradeData = onDocumentWritten("courses/{courseId}", async (event) => {
    const before = event.data?.before?.data();
    const after = event.data?.after?.data();

    if (!before || !after) return;

    const tierBefore: number = before.tier ?? 1;
    const tierAfter: number = after.tier ?? 1;

    // Only act when tier is newly 1 and was previously 2 or 3
    if (tierAfter !== 1 || tierBefore <= 1) return;

    const courseId = event.params.courseId;

    // Clear tier 2/3 fields and disable leaderboard on the course doc
    await event.data!.after.ref.update({
        logo: FieldValue.delete(),
        scoreCardColorDT: FieldValue.delete(),
        courseColorsDT: FieldValue.delete(),
        customAdActive: false,
        adTitle: FieldValue.delete(),
        adDescription: FieldValue.delete(),
        adLink: FieldValue.delete(),
        adImage: FieldValue.delete(),
        adClicks: FieldValue.delete(),
        leaderBoardActive: false,
    });

    // Delete all entries in the allTimeLeaderboard subcollection
    const leaderboardRef = db.collection("courses").doc(courseId).collection("allTimeLeaderboard");
    const snapshot = await leaderboardRef.get();

    if (snapshot.empty) return;

    // Batch delete in chunks of 500 (Firestore limit)
    for (let i = 0; i < snapshot.docs.length; i += 500) {
        const batch = db.batch();
        snapshot.docs.slice(i, i + 500).forEach((doc) => batch.delete(doc.ref));
        await batch.commit();
    }
});

// The in-app TTL is 5 minutes. Any live_games entry not updated in 1 hour is
// definitively stuck (crashed host, lost connection, etc.) and safe to delete.
export const cleanupStaleLiveGames = onSchedule("every 30 minutes", async () => {
    const rtdb = getDatabase();
    const oneHourAgoMs = Date.now() - 60 * 60 * 1000;

    const snapshot = await rtdb.ref("live_games")
        .orderByChild("lastUpdated")
        .endAt(oneHourAgoMs)
        .get();

    if (!snapshot.exists()) return;

    const deletions: Promise<void>[] = [];
    snapshot.forEach((child) => {
        deletions.push(child.ref.remove());
    });

    await Promise.all(deletions);
});

// Deletes email/password accounts that were never verified within 24 hours.
// Google and Apple users are always emailVerified so they are never matched.
// Deletion triggers cleanupDeletedAuthUser which wipes their Firestore data.
export const deleteUnverifiedAccounts = onSchedule("every 24 hours", async () => {
    const auth = getAuth();
    const cutoffMs = Date.now() - 24 * 60 * 60 * 1000;
    const uidsToDelete: string[] = [];

    let pageToken: string | undefined;
    do {
        const result = await auth.listUsers(1000, pageToken);

        for (const user of result.users) {
            if (user.emailVerified) continue;
            const isEmailProvider = user.providerData.some((p) => p.providerId === "password");
            if (!isEmailProvider) continue;
            const createdAt = new Date(user.metadata.creationTime).getTime();
            if (createdAt < cutoffMs) {
                uidsToDelete.push(user.uid);
            }
        }

        pageToken = result.pageToken;
    } while (pageToken);

    if (uidsToDelete.length === 0) return;

    // Delete one-by-one so onUserDeleted fires for each, triggering
    // cleanupDeletedAuthUser to wipe their Firestore doc automatically.
    await Promise.all(uidsToDelete.map((uid) => auth.deleteUser(uid)));
});

// When a Firebase Auth user is deleted, clean up their Firestore data.
// Apple users: keep a minimal stub (name, email, appleId) — Apple only provides
// these fields on first sign-in so they can't be recovered if deleted.
// Google / email users: delete the document entirely.
// Note: cleanupOrphanedGames fires automatically on the doc write/delete and
// removes any games no longer owned by any user.
export const cleanupDeletedAuthUser = functionsV1.auth.user().onDelete(async (user) => {
    const uid = user.uid;
    const userRef = db.collection("users").doc(uid);
    const userSnap = await userRef.get();

    if (!userSnap.exists) return;

    const data = userSnap.data()!;
    const accountType: string[] = data.accountType ?? [];

    if (accountType.includes("apple")) {
        // Overwrite with minimal stub — clears gameIDs/photoURL/etc.
        // This write will trigger cleanupOrphanedGames to delete their games.
        await userRef.set({
            googleId: uid,
            appleId: data.appleId ?? null,
            name: data.name ?? null,
            email: data.email ?? null,
            accountType: ["apple"],
            photoURL: null,
            gameIDs: [],
        });
    } else {
        // Google or email only — delete the full document.
        // cleanupOrphanedGames will detect the deletion and remove their games.
        await userRef.delete();
    }
});
