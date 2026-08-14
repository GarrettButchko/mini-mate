// Mirrors Course.kt in shared/src/commonMain/.../dataModels/courseModels/Course.kt
export interface CourseDoc {
  id: string;
  placeID?: string | null;
  name: string;
  password?: string;
  logo?: string | null;
  scoreCardColorDT?: string | null;
  numHoles?: number;
  tier: number;
  isClaimed?: boolean;
  leaderBoardActive?: boolean;
  adminIDs?: string[];
  stripeCustomerId?: string | null;
  stripeSubscriptionId?: string | null;
}

export const TIER_LABEL: Record<number, string> = {
  1: "Listed",
  2: "Partner",
  3: "Analytics",
};
