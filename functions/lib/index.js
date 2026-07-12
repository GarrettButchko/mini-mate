"use strict";
var __rest = (this && this.__rest) || function (s, e) {
    var t = {};
    for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p) && e.indexOf(p) < 0)
        t[p] = s[p];
    if (s != null && typeof Object.getOwnPropertySymbols === "function")
        for (var i = 0, p = Object.getOwnPropertySymbols(s); i < p.length; i++) {
            if (e.indexOf(p[i]) < 0 && Object.prototype.propertyIsEnumerable.call(s, p[i]))
                t[p[i]] = s[p[i]];
        }
    return t;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.cleanupDeletedAuthUser = exports.onUserCreated = exports.deleteUnverifiedAccounts = exports.cleanupStaleLiveGames = exports.clearTierDowngradeData = exports.cleanupOrphanedGames = exports.syncUserLastUpdated = exports.removeAdminFromCourse = void 0;
const https_1 = require("firebase-functions/v2/https");
const firestore_1 = require("firebase-functions/v2/firestore");
const functionsV1 = require("firebase-functions/v1");
const scheduler_1 = require("firebase-functions/v2/scheduler");
const firestore_2 = require("firebase-admin/firestore");
const database_1 = require("firebase-admin/database");
const auth_1 = require("firebase-admin/auth");
const app_1 = require("firebase-admin/app");
(0, app_1.initializeApp)();
const db = (0, firestore_2.getFirestore)(); // v2
exports.removeAdminFromCourse = (0, https_1.onCall)(async (request) => {
    var _c, _d;
    if (!request.auth) {
        throw new https_1.HttpsError("unauthenticated", "Must be signed in.");
    }
    const { userId, courseId } = request.data;
    if (!userId || !courseId) {
        throw new https_1.HttpsError("invalid-argument", "userId and courseId are required.");
    }
    const courseRef = db.collection("courses").doc(courseId);
    const courseSnap = await courseRef.get();
    if (!courseSnap.exists) {
        throw new https_1.HttpsError("not-found", "Course not found.");
    }
    const callerUid = request.auth.uid;
    const adminIDs = (_d = (_c = courseSnap.data()) === null || _c === void 0 ? void 0 : _c.adminIDs) !== null && _d !== void 0 ? _d : [];
    if (!adminIDs.includes(callerUid)) {
        throw new https_1.HttpsError("permission-denied", "You are not an admin of this course.");
    }
    const userRef = db.collection("users").doc(userId);
    const batch = db.batch();
    batch.update(courseRef, {
        adminIDs: firestore_2.FieldValue.arrayRemove(userId),
    });
    batch.update(userRef, {
        adminCourses: firestore_2.FieldValue.arrayRemove(courseId),
        lastUpdated: firestore_2.FieldValue.serverTimestamp(),
    });
    await batch.commit();
    return { success: true };
});
exports.syncUserLastUpdated = (0, firestore_1.onDocumentWritten)("users/{userId}", async (event) => {
    var _c, _d, _e, _f;
    const before = (_d = (_c = event.data) === null || _c === void 0 ? void 0 : _c.before) === null || _d === void 0 ? void 0 : _d.data();
    const after = (_f = (_e = event.data) === null || _e === void 0 ? void 0 : _e.after) === null || _f === void 0 ? void 0 : _f.data();
    // Skip creates and deletes — only care about updates
    if (!before || !after)
        return;
    // Strip lastUpdated from both snapshots and compare everything else
    const { lastUpdated: _a } = before, beforeRest = __rest(before, ["lastUpdated"]);
    const { lastUpdated: _b } = after, afterRest = __rest(after, ["lastUpdated"]);
    // If nothing other than lastUpdated changed, bail out (avoids infinite loop)
    if (JSON.stringify(beforeRest) === JSON.stringify(afterRest))
        return;
    await event.data.after.ref.update({
        lastUpdated: firestore_2.FieldValue.serverTimestamp(),
    });
});
exports.cleanupOrphanedGames = (0, firestore_1.onDocumentWritten)("users/{userId}", async (event) => {
    var _c, _d, _e, _f, _g, _h, _j, _k;
    const before = (_f = (_e = (_d = (_c = event.data) === null || _c === void 0 ? void 0 : _c.before) === null || _d === void 0 ? void 0 : _d.data()) === null || _e === void 0 ? void 0 : _e.gameIDs) !== null && _f !== void 0 ? _f : [];
    const after = (_k = (_j = (_h = (_g = event.data) === null || _g === void 0 ? void 0 : _g.after) === null || _h === void 0 ? void 0 : _h.data()) === null || _j === void 0 ? void 0 : _j.gameIDs) !== null && _k !== void 0 ? _k : [];
    const removedIds = before.filter((id) => !after.includes(id));
    if (removedIds.length === 0)
        return;
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
    if (hasDeletes)
        await batch.commit();
});
exports.clearTierDowngradeData = (0, firestore_1.onDocumentWritten)("courses/{courseId}", async (event) => {
    var _c, _d, _e, _f, _g, _h;
    const before = (_d = (_c = event.data) === null || _c === void 0 ? void 0 : _c.before) === null || _d === void 0 ? void 0 : _d.data();
    const after = (_f = (_e = event.data) === null || _e === void 0 ? void 0 : _e.after) === null || _f === void 0 ? void 0 : _f.data();
    if (!before || !after)
        return;
    const tierBefore = (_g = before.tier) !== null && _g !== void 0 ? _g : 1;
    const tierAfter = (_h = after.tier) !== null && _h !== void 0 ? _h : 1;
    // Only act when tier is newly 1 and was previously 2 or 3
    if (tierAfter !== 1 || tierBefore <= 1)
        return;
    const courseId = event.params.courseId;
    // Clear tier 2/3 fields and disable leaderboard on the course doc
    await event.data.after.ref.update({
        logo: firestore_2.FieldValue.delete(),
        scoreCardColorDT: firestore_2.FieldValue.delete(),
        courseColorsDT: firestore_2.FieldValue.delete(),
        customAdActive: false,
        adTitle: firestore_2.FieldValue.delete(),
        adDescription: firestore_2.FieldValue.delete(),
        adLink: firestore_2.FieldValue.delete(),
        adImage: firestore_2.FieldValue.delete(),
        adClicks: firestore_2.FieldValue.delete(),
        leaderBoardActive: false,
    });
    // Delete all entries in the allTimeLeaderboard subcollection
    const leaderboardRef = db.collection("courses").doc(courseId).collection("allTimeLeaderboard");
    const snapshot = await leaderboardRef.get();
    if (snapshot.empty)
        return;
    // Batch delete in chunks of 500 (Firestore limit)
    for (let i = 0; i < snapshot.docs.length; i += 500) {
        const batch = db.batch();
        snapshot.docs.slice(i, i + 500).forEach((doc) => batch.delete(doc.ref));
        await batch.commit();
    }
});
// The in-app TTL is 5 minutes. Any live_games entry not updated in 1 hour is
// definitively stuck (crashed host, lost connection, etc.) and safe to delete.
exports.cleanupStaleLiveGames = (0, scheduler_1.onSchedule)("every 30 minutes", async () => {
    const rtdb = (0, database_1.getDatabase)();
    const oneHourAgoMs = Date.now() - 60 * 60 * 1000;
    const snapshot = await rtdb.ref("live_games")
        .orderByChild("lastUpdated")
        .endAt(oneHourAgoMs)
        .get();
    if (!snapshot.exists())
        return;
    const deletions = [];
    snapshot.forEach((child) => {
        deletions.push(child.ref.remove());
    });
    await Promise.all(deletions);
});
// Deletes email/password accounts that were never verified within 24 hours.
// Google and Apple users are always emailVerified so they are never matched.
// Deletion triggers cleanupDeletedAuthUser which wipes their Firestore data.
exports.deleteUnverifiedAccounts = (0, scheduler_1.onSchedule)("every 24 hours", async () => {
    const auth = (0, auth_1.getAuth)();
    const cutoffMs = Date.now() - 24 * 60 * 60 * 1000;
    const uidsToDelete = [];
    let pageToken;
    do {
        const result = await auth.listUsers(1000, pageToken);
        for (const user of result.users) {
            if (user.emailVerified)
                continue;
            const isEmailProvider = user.providerData.some((p) => p.providerId === "password");
            if (!isEmailProvider)
                continue;
            const createdAt = new Date(user.metadata.creationTime).getTime();
            if (createdAt < cutoffMs) {
                uidsToDelete.push(user.uid);
            }
        }
        pageToken = result.pageToken;
    } while (pageToken);
    if (uidsToDelete.length === 0)
        return;
    // Delete one-by-one so onUserDeleted fires for each, triggering
    // cleanupDeletedAuthUser to wipe their Firestore doc automatically.
    await Promise.all(uidsToDelete.map((uid) => auth.deleteUser(uid)));
});
// When a new Firebase Auth user is created (any platform), create their Firestore doc.
// Mirrors the UserRepository.createUser() logic in Kotlin — same field names as UserModel.
exports.onUserCreated = functionsV1.auth.user().onCreate(async (user) => {
    var _c, _d, _e;
    const userRef = db.collection("users").doc(user.uid);
    // Don't overwrite an existing doc (e.g. Apple stub left from a previous deletion)
    const existing = await userRef.get();
    if (existing.exists)
        return;
    const providerType = (() => {
        var _c;
        const id = (_c = user.providerData[0]) === null || _c === void 0 ? void 0 : _c.providerId;
        if (id === "google.com")
            return "google";
        if (id === "apple.com")
            return "apple";
        if (id === "password")
            return "email";
        return id !== null && id !== void 0 ? id : null;
    })();
    await userRef.set({
        googleId: user.uid,
        appleId: null,
        name: (_c = user.displayName) !== null && _c !== void 0 ? _c : `User#${user.uid.slice(0, 5)}`,
        email: (_d = user.email) !== null && _d !== void 0 ? _d : null,
        photoURL: (_e = user.photoURL) !== null && _e !== void 0 ? _e : null,
        isPro: false,
        gameIDs: [],
        adminCourses: [],
        accountType: providerType ? [providerType] : [],
        ballColorDT: null,
        lastUpdated: firestore_2.FieldValue.serverTimestamp(),
    });
});
// When a Firebase Auth user is deleted, clean up their Firestore data.
// Apple users: keep a minimal stub (name, email, appleId) — Apple only provides
// these fields on first sign-in so they can't be recovered if deleted.
// Google / email users: delete the document entirely.
// Note: cleanupOrphanedGames fires automatically on the doc write/delete and
// removes any games no longer owned by any user.
exports.cleanupDeletedAuthUser = functionsV1.auth.user().onDelete(async (user) => {
    var _c, _d, _e, _f;
    const uid = user.uid;
    const userRef = db.collection("users").doc(uid);
    const userSnap = await userRef.get();
    if (!userSnap.exists)
        return;
    const data = userSnap.data();
    const accountType = (_c = data.accountType) !== null && _c !== void 0 ? _c : [];
    if (accountType.includes("apple")) {
        // Overwrite with minimal stub — clears gameIDs/photoURL/etc.
        // This write will trigger cleanupOrphanedGames to delete their games.
        await userRef.set({
            googleId: uid,
            appleId: (_d = data.appleId) !== null && _d !== void 0 ? _d : null,
            name: (_e = data.name) !== null && _e !== void 0 ? _e : null,
            email: (_f = data.email) !== null && _f !== void 0 ? _f : null,
            accountType: ["apple"],
            photoURL: null,
            gameIDs: [],
        });
    }
    else {
        // Google or email only — delete the full document.
        // cleanupOrphanedGames will detect the deletion and remove their games.
        await userRef.delete();
    }
});
//# sourceMappingURL=index.js.map