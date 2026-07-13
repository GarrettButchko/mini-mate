import { NextRequest, NextResponse } from "next/server";
import { stripe } from "@/lib/stripe";

export async function POST(req: NextRequest) {
  const { email } = await req.json();

  if (!email) {
    return NextResponse.json({ error: "Email is required" }, { status: 400 });
  }

  try {
    const customers = await stripe.customers.list({ email, limit: 1 });
    if (!customers.data.length) {
      return NextResponse.json({ freeTier: true });
    }

    const session = await stripe.billingPortal.sessions.create({
      customer: customers.data[0].id,
      return_url: `${process.env.NEXT_PUBLIC_BASE_URL}/manage`,
    });

    return NextResponse.json({ url: session.url });
  } catch (err) {
    console.error("portal error", err);
    return NextResponse.json({ error: "Failed to open billing portal" }, { status: 500 });
  }
}
