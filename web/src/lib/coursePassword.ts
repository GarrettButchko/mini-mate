import { db } from "./firebaseAdmin";

const UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
const LOWER = "abcdefghijklmnopqrstuvwxyz";
const DIGITS = "0123456789";
const SYMBOLS = "!@#$%^&*";
const ALL = UPPER + LOWER + DIGITS + SYMBOLS;

export function generateCoursePassword(length = 20): string {
  const pick = (set: string) => set[Math.floor(Math.random() * set.length)];
  const chars = [pick(UPPER), pick(LOWER), pick(DIGITS), pick(SYMBOLS)];
  while (chars.length < length) chars.push(pick(ALL));
  return chars.sort(() => Math.random() - 0.5).join("");
}

export async function getOrCreateCoursePassword(
  placeId: string,
  courseName: string
): Promise<string> {
  const ref = db.collection("courses").doc(placeId);
  const snap = await ref.get();

  if (snap.exists) {
    const data = snap.data();
    if (data?.password) return data.password as string;
  }

  const password = generateCoursePassword();
  await ref.set(
    { id: placeId, name: courseName, password, isClaimed: false, adminIDs: [] },
    { merge: true }
  );
  return password;
}
