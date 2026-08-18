import { NextRequest, NextResponse } from "next/server";
import { stripe } from "@/lib/stripe";
import { logger } from "@/lib/logger";

const PRICE_IDS: Record<string, string> = {
  partner: process.env.NEXT_PUBLIC_STRIPE_PRO_PRICE_ID ?? "",
  analytics: process.env.NEXT_PUBLIC_STRIPE_PREMIUM_PRICE_ID ?? "",
};

function predictDeliveryMethod(contact: {
  email?: string | null;
  phoneNumber?: string | null;
  address?: { fullAddress?: string } | null;
}): string {
  if (contact.email) return "email";
  if (contact.phoneNumber) return "sms";
  if (contact.address?.fullAddress) return "mail";
  return "pending";
}

export async function POST(req: NextRequest) {
  const { placeId, courseName, tier, contact } = await req.json();

  if (!placeId || !courseName || !PRICE_IDS[tier]) {
    return NextResponse.json({ error: "Invalid request" }, { status: 400 });
  }

  const base = process.env.NEXT_PUBLIC_BASE_URL;
  const method = predictDeliveryMethod(contact);

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
      success_url: `${base}/find-course/success?course=${encodeURIComponent(courseName)}&method=${method}&session_id={CHECKOUT_SESSION_ID}`,
      cancel_url: `${base}/find-course`,
    });

    return NextResponse.json({ url: session.url });
  } catch (err) {
    logger.error("checkout", "Failed to create checkout session", err);
    return NextResponse.json({ error: "Failed to create checkout session" }, { status: 500 });
  }
}
