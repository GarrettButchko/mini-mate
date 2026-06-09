"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.removeAdminFromCourse = void 0;
const https_1 = require("firebase-functions/v2/https");
const firestore_1 = require("firebase-admin/firestore");
const app_1 = require("firebase-admin/app");
(0, app_1.initializeApp)();
const db = (0, firestore_1.getFirestore)();
exports.removeAdminFromCourse = (0, https_1.onCall)(async (request) => {
    var _a, _b;
    if (!request.auth) {
        throw new https_1.HttpsError("unauthenticated", "Must be signed in.");
    }
    const { email, courseId } = request.data;
    if (!email || !courseId) {
        throw new https_1.HttpsError("invalid-argument", "email and courseId are required.");
    }
    const courseRef = db.collection("courses").doc(courseId);
    const courseSnap = await courseRef.get();
    if (!courseSnap.exists) {
        throw new https_1.HttpsError("not-found", "Course not found.");
    }
    const callerEmail = request.auth.token.email;
    const adminIDs = (_b = (_a = courseSnap.data()) === null || _a === void 0 ? void 0 : _a.adminIDs) !== null && _b !== void 0 ? _b : [];
    if (!callerEmail || !adminIDs.includes(callerEmail)) {
        throw new https_1.HttpsError("permission-denied", "You are not an admin of this course.");
    }
    if (adminIDs.length <= 1) {
        throw new https_1.HttpsError("failed-precondition", "Cannot remove the last admin.");
    }
    // Find the user by email and update their adminCourses
    const usersSnap = await db.collection("users")
        .where("email", "==", email)
        .limit(1)
        .get();
    const batch = db.batch();
    batch.update(courseRef, {
        adminIDs: firestore_1.FieldValue.arrayRemove(email),
        isClaimed: adminIDs.filter((id) => id !== email).length > 0,
    });
    if (!usersSnap.empty) {
        batch.update(usersSnap.docs[0].ref, {
            adminCourses: firestore_1.FieldValue.arrayRemove(courseId),
            lastUpdated: firestore_1.FieldValue.serverTimestamp(),
        });
    }
    await batch.commit();
    return { success: true };
});
//# sourceMappingURL=index.js.map