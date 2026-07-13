import { NextRequest, NextResponse } from "next/server";
import { stripe } from "@/lib/stripe";

const PRICE_IDS: Record<string, string> = {
  partner: process.env.STRIPE_PRO_PRICE_ID ?? "",
  analytics: process.env.STRIPE_PREMIUM_PRICE_ID ?? "",
};

export async function POST(req: NextRequest) {
  const { placeId, courseName, tier, contact } = await req.json();

  if (!placeId || !courseName || !PRICE_IDS[tier]) {
    return NextResponse.json({ error: "Invalid request" }, { status: 400 });
  }

  const base = process.env.NEXT_PUBLIC_BASE_URL;

  try {
    const session = await stripe.checkout.sessions.create({
      mode: "subscription",
      line_items: [{ price: PRICE_IDS[tier], quantity: 1 }],
      ...(contact.email ? { customer_email: contact.email } : {}),
      metadata: {
        placeId,
        courseName,
        tier,
        email: contact.email ?? "",
        phoneNumber: contact.phoneNumber ?? "",
        url: contact.url ?? "",
        fullAddress: contact.address?.fullAddress ?? "",
        shortAddress: contact.address?.shortAddress ?? "",
        latitude: String(contact.coordinate?.latitude ?? ""),
        longitude: String(contact.coordinate?.longitude ?? ""),
      },
      success_url: `${base}/courses/success?session_id={CHECKOUT_SESSION_ID}`,
      cancel_url: `${base}/courses`,
    });

    return NextResponse.json({ url: session.url });
  } catch (err) {
    console.error("checkout error", err);
    return NextResponse.json({ error: "Failed to create checkout session" }, { status: 500 });
  }
}
