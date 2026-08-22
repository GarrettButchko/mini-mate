import { NextRequest, NextResponse } from "next/server";
import type Stripe from "stripe";
import { stripe } from "@/lib/stripe";
import { getOrCreateCoursePassword } from "@/lib/coursePassword";
import { deliverCode } from "@/lib/sendCode";
import { db } from "@/lib/firebaseAdmin";
import { logger } from "@/lib/logger";

export const dynamic = "force-dynamic";

const TIER_BY_PRICE_ID: Record<string, number> = {
  [process.env.NEXT_PUBLIC_STRIPE_PRO_PRICE_ID ?? ""]: 2,
  [process.env.NEXT_PUBLIC_STRIPE_PREMIUM_PRICE_ID ?? ""]: 3,
};

const ACTIVE_STATUSES = new Set<Stripe.Subscription.Status>(["active", "trialing", "past_due"]);

async function coursesForSubscription(subscriptionId: string) {
  const snap = await db.collection("courses").where("stripeSubscriptionId", "==", subscriptionId).get();
  return snap.docs;
}

async function syncSubscription(subscription: Stripe.Subscription) {
  const docs = await coursesForSubscription(subscription.id);
  if (!docs.length) return;

  if (!ACTIVE_STATUSES.has(subscription.status)) {
    await Promise.all(docs.map((doc) => doc.ref.set({ tier: 1, stripeSubscriptionId: null }, { merge: true })));
    return;
  }

  const priceId = subscription.items.data[0]?.price.id;
  const tier = priceId ? TIER_BY_PRICE_ID[priceId] : undefined;
  if (!tier) return;

  await Promise.all(docs.map((doc) => doc.ref.set({ tier }, { merge: true })));
}

export async function POST(req: NextRequest) {
  const body = await req.text();
  const sig = req.headers.get("stripe-signature") ?? "";

  let event: Stripe.Event;
  try {
    event = stripe.webhooks.constructEvent(body, sig, process.env.STRIPE_WEBHOOK_SECRET!);
  } catch {
    return NextResponse.json({ error: "Invalid signature" }, { status: 400 });
  }

  if (event.type === "checkout.session.completed") {
    const session = event.data.object as Stripe.Checkout.Session;
    const meta = session.metadata ?? {};
    const { placeId, courseName, email, phoneNumber, fullAddress, shortAddress, latitude, longitude } = meta;

    if (!placeId || !courseName) {
      return NextResponse.json({ error: "Missing metadata" }, { status: 400 });
    }

    const code = await getOrCreateCoursePassword(placeId, courseName);

    const contact = {
      email: email || null,
      phoneNumber: phoneNumber || null,
      url: null,
      address: { fullAddress: fullAddress ?? "", shortAddress: shortAddress ?? "" },
      coordinate: { latitude: parseFloat(latitude ?? "0"), longitude: parseFloat(longitude ?? "0") },
    };

    const method = await deliverCode(contact, courseName, code);

    const tierNumber = meta.tier === "analytics" ? 3 : 2;

    await db.collection("courses").doc(placeId).set(
      {
        tier: tierNumber,
        stripeCustomerId: session.customer,
        stripeSubscriptionId: session.subscription,
        deliveryMethod: method,
      },
      { merge: true }
    );
  }

  if (event.type === "customer.subscription.updated" || event.type === "customer.subscription.deleted") {
    try {
      await syncSubscription(event.data.object as Stripe.Subscription);
    } catch (err) {
      logger.error("webhook", "Failed to sync subscription", err);
    }
  }

  return NextResponse.json({ received: true });
}
