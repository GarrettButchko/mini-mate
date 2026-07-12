import Link from "next/link";
import {
  MapPin,
  Sliders,
  Link2,
  Palette,
  Megaphone,
  Image,
  Trophy,
  Zap,
  Gift,
  Nfc,
  TrendingUp,
  RefreshCw,
  Star,
  Mail,
  Download,
  BarChart2,
  Building2,
  Layers,
} from "lucide-react";
import PageShell from "@/components/PageShell";
import SiteNav from "@/components/SiteNav";
import SiteFooter from "@/components/SiteFooter";

type TierFeature = { icon: React.ElementType; text: string; soon?: boolean };

const tiers: {
  name: string;
  price: string;
  period: string;
  description: string;
  tierIcon: React.ElementType;
  inherits?: string;
  features: TierFeature[];
  cta: string;
  href: string;
  highlight: boolean;
}[] = [
  {
    name: "Listed",
    price: "Free",
    period: "forever",
    description: "Get discovered. Put your course on the Mini Mate map.",
    tierIcon: MapPin,
    features: [
      { icon: MapPin, text: "Course listed on the Mini Mate map" },
      { icon: Sliders, text: "Custom par info for every hole" },
      { icon: Link2, text: "Social links on your course profile" },
    ],
    cta: "Subscribe",
    href: "/courses",
    highlight: false,
  },
  {
    name: "Partner",
    price: "$29",
    period: "/ month",
    description: "Full brand presence across the Mini Mate app and real-world marketing.",
    tierIcon: Building2,
    inherits: "Everything in Listed, plus:",
    features: [
      { icon: Palette, text: "Custom scorecard colors & accent" },
      { icon: Megaphone, text: "Your ad in scorecards & app pages" },
      { icon: Image, text: "Your logo in the app home & scorecard" },
      { icon: Trophy, text: "Managed course leaderboard" },
      { icon: Zap, text: "Tournament mode access", soon: true },
      { icon: Gift, text: "Mini Mate pitch tent & marketing kit" },
      { icon: Nfc, text: "NFC-enabled tent tag (Mini Mate branded)" },
    ],
    cta: "Subscribe",
    href: "/courses",
    highlight: true,
  },
  {
    name: "Analytics",
    price: "$69",
    period: "/ month",
    description: "Everything in Partner plus deep player insights and data exports.",
    tierIcon: BarChart2,
    inherits: "Everything in Partner, plus:",
    features: [
      { icon: TrendingUp, text: "Growth analytics dashboard" },
      { icon: RefreshCw, text: "Player retention tracking" },
      { icon: Star, text: "Experience score metrics" },
      { icon: Mail, text: "Player email list from your course" },
      { icon: Download, text: "CSV export — all data, any time" },
      { icon: Layers, text: "Beautiful analytics interface" },
    ],
    cta: "Subscribe",
    href: "/courses",
    highlight: false,
  },
];

export default function PricingPage() {
  return (
    <PageShell>
      <SiteNav logoColor="#AF52DE">
        <Link href="/" className="px-3 py-1.5 text-sm font-medium rounded-full hover:opacity-70 transition-opacity" style={{ color: "var(--text-sub)" }}>
          For Players
        </Link>
      </SiteNav>

      {/* Header */}
      <section className="pt-20 pb-12 px-6 text-center">
        <span
          className="inline-flex items-center gap-1.5 rounded-full px-4 py-1.5 text-sm font-semibold mb-6"
          style={{ background: "rgba(175,82,222,0.12)", color: "#AF52DE" }}
        >
          <Building2 size={13} />
          For Course Owners
        </span>
        <h1 className="text-4xl sm:text-5xl font-bold mb-4 tracking-tight" style={{ color: "var(--text)" }}>
          Grow your course with Mini Mate
        </h1>
        <p className="max-w-lg mx-auto text-lg leading-relaxed" style={{ color: "var(--text-muted)" }}>
          From getting on the map to owning your brand in the app — pick the tier that fits where your course is headed.
        </p>
      </section>

      {/* Tiers */}
      <section className="pb-28 px-6">
        <div className="max-w-5xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-6 items-stretch">
          {tiers.map((tier) => (
            <div
              key={tier.name}
              className={`relative flex flex-col p-8${tier.highlight ? " pricing-highlight-card" : ""}`}
              style={
                tier.highlight
                  ? undefined
                  : {
                      background: "var(--glass-bg)",
                      backdropFilter: "blur(20px)",
                      WebkitBackdropFilter: "blur(20px)",
                      borderRadius: "25px",
                      boxShadow: "var(--shadow)",
                    }
              }
            >
              {tier.highlight && (
                <span
                  className="absolute -top-3.5 left-1/2 -translate-x-1/2 rounded-full text-xs font-bold px-4 py-1 whitespace-nowrap"
                  style={{ background: "#FFD60A", color: "#1C1C1E" }}
                >
                  MOST POPULAR
                </span>
              )}

              {/* Tier header */}
              <div className="mb-6">
                <div
                  className="inline-flex items-center justify-center w-11 h-11 rounded-[14px] mb-4"
                  style={
                    tier.highlight
                      ? { background: "rgba(255,255,255,0.2)" }
                      : { background: "rgba(175,82,222,0.12)" }
                  }
                >
                  <tier.tierIcon size={22} style={{ color: tier.highlight ? "white" : "#AF52DE" }} />
                </div>
                <h2
                  className="text-xl font-bold mb-1"
                  style={{ color: tier.highlight ? "white" : "var(--text)" }}
                >
                  {tier.name}
                </h2>
                <div className="flex items-baseline gap-1 mb-2">
                  <span className="text-4xl font-extrabold" style={{ color: tier.highlight ? "white" : "var(--text)" }}>
                    {tier.price}
                  </span>
                  <span className="text-sm" style={{ color: tier.highlight ? "rgba(255,255,255,0.65)" : "var(--text-muted)" }}>
                    {tier.period}
                  </span>
                </div>
                <p className="text-sm leading-relaxed" style={{ color: tier.highlight ? "rgba(255,255,255,0.8)" : "var(--text-muted)" }}>
                  {tier.description}
                </p>
              </div>

              {/* Features */}
              <div className="flex-1 mb-8">
                {tier.inherits && (
                  <p
                    className="text-xs font-semibold uppercase tracking-wide mb-3"
                    style={{ color: tier.highlight ? "rgba(255,255,255,0.55)" : "var(--text-muted)" }}
                  >
                    {tier.inherits}
                  </p>
                )}
                <ul className="space-y-3">
                  {tier.features.map((f) => (
                    <li key={f.text} className="flex items-start gap-2.5">
                      <span
                        className="mt-0.5 shrink-0"
                        style={{ color: tier.highlight ? "rgba(255,255,255,0.9)" : "#AF52DE" }}
                      >
                        <f.icon size={14} />
                      </span>
                      <span className="text-sm leading-snug flex items-center gap-2" style={{ color: tier.highlight ? "rgba(255,255,255,0.85)" : "var(--text-sub)" }}>
                        {f.text}
                        {f.soon && (
                          <span
                            className="text-[10px] font-semibold rounded-full px-2 py-0.5 shrink-0"
                            style={
                              tier.highlight
                                ? { background: "rgba(255,255,255,0.2)", color: "white" }
                                : { background: "rgba(255,149,0,0.15)", color: "#FF9500" }
                            }
                          >
                            Coming Soon
                          </span>
                        )}
                      </span>
                    </li>
                  ))}
                </ul>
              </div>

              <Link
                href={tier.href}
                className="w-full rounded-xl py-3.5 text-center text-sm font-semibold transition-all duration-200 hover:opacity-90 hover:scale-105 active:scale-95"
                style={
                  tier.highlight
                    ? { background: "white", color: "#AF52DE", border: "1px solid var(--glass-border)" }
                    : { background: "#AF52DE", color: "white" }
                }
              >
                {tier.cta}
              </Link>
            </div>
          ))}
        </div>
      </section>

      <SiteFooter links={[
        { href: "/", label: "For Players" },
        { href: "/privacy", label: "Privacy Policy" },
        { href: "/terms", label: "Terms" },
      ]} />
    </PageShell>
  );
}
