import { onCall, HttpsError } from "firebase-functions/v2/https";
import { getFirestore, FieldValue } from "firebase-admin/firestore";
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
