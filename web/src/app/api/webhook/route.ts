import { NextRequest, NextResponse } from "next/server";
import type Stripe from "stripe";
import { stripe } from "@/lib/stripe";
import { getOrCreateCoursePassword } from "@/lib/coursePassword";
import { deliverCode } from "@/lib/sendCode";
import { db } from "@/lib/firebaseAdmin";

export const dynamic = "force-dynamic";

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

  return NextResponse.json({ received: true });
}
