"use client";
import Link from "next/link";
import { MainViewBusinessLogic } from "@/logic/MainViewBusinessLogic";

const logic = new MainViewBusinessLogic();

const tiers = [
  {
    name: "Free",
    price: "$0",
    period: "forever",
    description: "Perfect for casual rounds with friends.",
    features: [
      `Up to 2 saved games`,
      "Local scorecard",
      "Basic stats",
      "Course discovery",
    ],
    cta: "Download Free",
    href: "#download",
    highlight: false,
    priceId: null,
  },
  {
    name: "Pro",
    price: "$2.99",
    period: "/ month",
    description: "Unlimited games and real-time online multiplayer.",
    features: [
      "Unlimited saved games",
      "Online multiplayer — host & join",
      "Full stats & recap",
      "Course discovery",
      "No ads",
    ],
    cta: "Start Pro",
    href: "#checkout-pro",
    highlight: true,
    priceId: process.env.NEXT_PUBLIC_STRIPE_PRO_PRICE_ID ?? "price_pro",
  },
  {
    name: "Premium",
    price: "$6.99",
    period: "/ month",
    description: "Everything in Pro, plus advanced analytics and priority support.",
    features: [
      "Everything in Pro",
      "Advanced round analytics",
      "Leaderboards",
      "Priority support",
      "Early access to new features",
    ],
    cta: "Start Premium",
    href: "#checkout-premium",
    highlight: false,
    priceId: process.env.NEXT_PUBLIC_STRIPE_PREMIUM_PRICE_ID ?? "price_premium",
  },
];

export default function PricingPage() {
  // Uses MainViewBusinessLogic.isPlayDisabled to explain the free limit inline
  const freeGameLimit = 2;
  const proStopperMessage = logic.shouldShowProStopper(false, freeGameLimit)
    ? `Free accounts are limited to ${freeGameLimit} games. Upgrade to play more.`
    : null;

  return (
    <div className="min-h-screen bg-white dark:bg-zinc-950 font-[family-name:var(--font-geist-sans)]">
      <nav className="flex items-center justify-between px-8 py-5 border-b border-zinc-100 dark:border-zinc-800">
        <Link href="/" className="text-xl font-bold tracking-tight text-green-600">⛳ Mini-Mate</Link>
        <Link href="/courses" className="text-sm font-medium text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-white transition-colors">
          For Courses →
        </Link>
      </nav>

      <section className="pt-20 pb-10 px-6 text-center">
        <h1 className="text-4xl font-bold text-zinc-900 dark:text-white mb-4">Simple, honest pricing</h1>
        <p className="text-zinc-500 dark:text-zinc-400 max-w-md mx-auto">
          Start free. Upgrade when you&apos;re ready to play more.
        </p>
        {proStopperMessage && (
          <div className="mt-6 inline-block rounded-full bg-amber-50 dark:bg-amber-900/30 px-5 py-2 text-sm font-medium text-amber-700 dark:text-amber-400">
            {proStopperMessage}
          </div>
        )}
      </section>

      <section className="pb-24 px-6">
        <div className="max-w-5xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-8 items-stretch">
          {tiers.map((tier) => (
            <div
              key={tier.name}
              className={`relative rounded-2xl p-8 flex flex-col border ${
                tier.highlight
                  ? "bg-green-600 border-green-600 text-white shadow-xl scale-105"
                  : "bg-white dark:bg-zinc-800 border-zinc-200 dark:border-zinc-700 text-zinc-900 dark:text-white shadow-sm"
              }`}
            >
              {tier.highlight && (
                <span className="absolute -top-3.5 left-1/2 -translate-x-1/2 rounded-full bg-amber-400 text-amber-900 text-xs font-bold px-4 py-1">
                  MOST POPULAR
                </span>
              )}
              <div className="mb-6">
                <h2 className={`text-xl font-bold mb-1 ${tier.highlight ? "text-white" : ""}`}>{tier.name}</h2>
                <div className="flex items-baseline gap-1">
                  <span className={`text-4xl font-extrabold ${tier.highlight ? "text-white" : "text-zinc-900 dark:text-white"}`}>
                    {tier.price}
                  </span>
                  <span className={`text-sm ${tier.highlight ? "text-green-100" : "text-zinc-400"}`}>
                    {tier.period}
                  </span>
                </div>
                <p className={`mt-2 text-sm ${tier.highlight ? "text-green-100" : "text-zinc-500 dark:text-zinc-400"}`}>
                  {tier.description}
                </p>
              </div>

              <ul className="flex-1 space-y-3 mb-8">
                {tier.features.map((f) => (
                  <li key={f} className="flex items-start gap-2 text-sm">
                    <span className={tier.highlight ? "text-green-200" : "text-green-500"}>✓</span>
                    <span className={tier.highlight ? "text-green-50" : "text-zinc-600 dark:text-zinc-300"}>{f}</span>
                  </li>
                ))}
              </ul>

              <a
                href={tier.href}
                className={`w-full rounded-full py-3 text-center text-sm font-semibold transition-colors ${
                  tier.highlight
                    ? "bg-white text-green-700 hover:bg-green-50"
                    : "bg-green-600 text-white hover:bg-green-700"
                }`}
              >
                {tier.cta}
              </a>
            </div>
          ))}
        </div>
      </section>

      <footer className="border-t border-zinc-100 dark:border-zinc-800 py-8 px-6 text-center text-sm text-zinc-400">
        <p>© {new Date().getFullYear()} Mini-Mate. Pricing is in USD and billed monthly.</p>
      </footer>
    </div>
  );
}
