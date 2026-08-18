import { NextRequest, NextResponse } from "next/server";
import { db } from "@/lib/firebaseAdmin";
import { FieldValue } from "firebase-admin/firestore";
import { logger } from "@/lib/logger";

export async function POST(req: NextRequest) {
  const { password, uid } = await req.json();

  if (!password || !uid) {
    return NextResponse.json({ error: "missing_fields" }, { status: 400 });
  }

  try {
    const snap = await db.collection("courses").where("password", "==", password).limit(1).get();

    if (snap.empty) {
      return NextResponse.json({ error: "wrong_password" }, { status: 404 });
    }

    const courseDoc = snap.docs[0];
    const courseId = courseDoc.id;
    const adminIDs: string[] = courseDoc.data().adminIDs ?? [];

    if (adminIDs.includes(uid)) {
      return NextResponse.json({ error: "already_added" }, { status: 409 });
    }

    await db.runTransaction(async (tx) => {
      tx.update(courseDoc.ref, { adminIDs: FieldValue.arrayUnion(uid), isClaimed: true });
      tx.update(db.collection("users").doc(uid), { adminCourses: FieldValue.arrayUnion(courseId) });
    });

    return NextResponse.json({ courseId });
  } catch (err) {
    logger.error("add-course", "Failed to add course", err);
    return NextResponse.json({ error: "server_error" }, { status: 500 });
  }
}
