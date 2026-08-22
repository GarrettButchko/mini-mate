import { NextRequest, NextResponse } from "next/server";
import { stripe } from "@/lib/stripe";
import { logger } from "@/lib/logger";

type PortalFlow = "cancel" | "update";

function flowData(flow: PortalFlow | undefined, subscriptionId: string | undefined) {
  if (!flow || !subscriptionId) return undefined;
  if (flow === "cancel") {
    return {
      type: "subscription_cancel" as const,
      subscription_cancel: { subscription: subscriptionId },
    };
  }
  return {
    type: "subscription_update" as const,
    subscription_update: { subscription: subscriptionId },
  };
}

export async function POST(req: NextRequest) {
  const { email, customerId, subscriptionId, flow } = await req.json();

  // If we already have the Stripe customer (e.g. stored on a course doc), open
  // their billing portal directly instead of looking them up by email.
  if (customerId) {
    try {
      const session = await stripe.billingPortal.sessions.create({
        customer: customerId,
        return_url: `${process.env.NEXT_PUBLIC_BASE_URL}/manage-courses`,
        flow_data: flowData(flow, subscriptionId),
      });
      return NextResponse.json({ url: session.url });
    } catch (err) {
      // The referenced subscription may already be canceled or otherwise unusable
      // for the requested flow (e.g. a stale stripeSubscriptionId on the course doc).
      // Fall back to the plain billing portal rather than failing outright.
      if (flow) {
        try {
          const session = await stripe.billingPortal.sessions.create({
            customer: customerId,
            return_url: `${process.env.NEXT_PUBLIC_BASE_URL}/manage-courses`,
          });
          return NextResponse.json({ url: session.url });
        } catch (fallbackErr) {
          logger.error("portal", "Failed to create fallback portal session for customer", fallbackErr);
        }
      }
      logger.error("portal", "Failed to create portal session for customer", err);
      return NextResponse.json({ error: "Failed to open billing portal" }, { status: 500 });
    }
  }

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
    logger.error("portal", "Failed to create portal session", err);
    return NextResponse.json({ error: "Failed to open billing portal" }, { status: 500 });
  }
}
