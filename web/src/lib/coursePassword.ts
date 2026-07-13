import { db } from "./firebaseAdmin";

const UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
const LOWER = "abcdefghijklmnopqrstuvwxyz";
const DIGITS = "0123456789";
const SYMBOLS = "!@#$%^&*";
const ALL = UPPER + LOWER + DIGITS + SYMBOLS;

function cryptoRandInt(max: number): number {
  const buf = new Uint32Array(1);
  crypto.getRandomValues(buf);
  return buf[0] % max;
}

export function generateCoursePassword(length = 20): string {
  const pick = (set: string) => set[cryptoRandInt(set.length)];
  const chars = [pick(UPPER), pick(LOWER), pick(DIGITS), pick(SYMBOLS)];
  while (chars.length < length) chars.push(pick(ALL));
  // Fisher-Yates shuffle
  for (let i = chars.length - 1; i > 0; i--) {
    const j = cryptoRandInt(i + 1);
    [chars[i], chars[j]] = [chars[j], chars[i]];
  }
  return chars.join("");
}

export async function getOrCreateCoursePassword(
  placeId: string,
  courseName: string
): Promise<string> {
  const ref = db.collection("courses").doc(placeId);

  return db.runTransaction(async (tx) => {
    const snap = await tx.get(ref);
    if (snap.exists) {
      const data = snap.data();
      if (data?.password) return data.password as string;
    }
    const password = generateCoursePassword();
    tx.set(ref, { id: placeId, name: courseName, password, isClaimed: false, adminIDs: [] }, { merge: true });
    return password;
  });
}
