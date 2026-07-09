import { NextRequest, NextResponse } from "next/server";
import Stripe from "stripe";

const stripe = new Stripe(process.env.STRIPE_SECRET_KEY!);

export async function POST(req: NextRequest) {
  const { email } = await req.json();

  if (!email) {
    return NextResponse.json({ error: "Email is required" }, { status: 400 });
  }

  const customers = await stripe.customers.list({ email, limit: 1 });
  if (!customers.data.length) {
    return NextResponse.json({ freeTier: true });
  }

  const session = await stripe.billingPortal.sessions.create({
    customer: customers.data[0].id,
    return_url: `${process.env.NEXT_PUBLIC_BASE_URL}/manage`,
  });

  return NextResponse.json({ url: session.url });
}
