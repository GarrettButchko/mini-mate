import { NextRequest, NextResponse } from "next/server";
import { getOrCreateCoursePassword } from "@/lib/coursePassword";
import { deliverCode } from "@/lib/sendCode";

export async function POST(req: NextRequest) {
  const { placeId, courseName, contact } = await req.json();

  if (!placeId || !courseName || !contact) {
    return NextResponse.json({ error: "Missing required fields" }, { status: 400 });
  }

  try {
    const code = await getOrCreateCoursePassword(placeId, courseName);
    const method = await deliverCode(contact, courseName, code);
    return NextResponse.json({ success: true, method });
  } catch (err) {
    console.error("claim-course error", err);
    return NextResponse.json({ error: "Failed to claim course" }, { status: 500 });
  }
}
