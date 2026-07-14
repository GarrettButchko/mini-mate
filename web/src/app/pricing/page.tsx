import Link from "next/link";
import { Building2, MapPin, ChevronRight } from "lucide-react";
import PageShell from "@/components/PageShell";
import SiteNav from "@/components/SiteNav";
import SiteFooter from "@/components/SiteFooter";
import { tiers, type Tier } from "@/lib/tiers";

export default async function PricingPage({
  searchParams,
}: {
  searchParams: Promise<{ courseId?: string; courseName?: string }>;
}) {
  const { courseId, courseName: rawCourseName } = await searchParams;
  const courseName = rawCourseName ? decodeURIComponent(rawCourseName) : undefined;
  const hasCourse = Boolean(courseId && courseName);

  function tierHref(tier: Tier): string {
    if (hasCourse) {
      return `/find-course/verify?courseId=${encodeURIComponent(courseId!)}&courseName=${encodeURIComponent(courseName!)}&tier=${tier.name.toLowerCase()}`;
    }
    return "/find-course";
  }

  return (
    <PageShell>
      <SiteNav
        position="fixed"
        logoColor="#AF52DE"
        context={hasCourse ? (
          <Link href="/find-course" className="flex items-center gap-2 hover:opacity-70 transition-opacity no-underline">
            <MapPin size={13} style={{ color: "#AF52DE", flexShrink: 0 }} />
            <span className="text-sm font-medium" style={{ color: "var(--text)" }}>{courseName}</span>
            <ChevronRight size={12} style={{ color: "var(--text-muted)" }} />
          </Link>
        ) : undefined}
      >
        <Link href="/" className="px-3 py-1.5 text-sm font-medium rounded-full hover:opacity-70 transition-opacity" style={{ color: "var(--text-sub)" }}>
          For Players
        </Link>
      </SiteNav>

      {/* Gradient hero */}
      <section className="hero-gradient-courses text-white">
        <div className="max-w-5xl mx-auto px-6 pt-36 pb-16 text-center">
          <span
            className="inline-flex items-center gap-1.5 rounded-full px-4 py-1.5 text-sm font-semibold mb-6"
            style={{ background: "rgba(255,255,255,0.2)", backdropFilter: "blur(8px)" }}
          >
            <Building2 size={13} />
            For Course Owners
          </span>
          <h1 className="text-5xl sm:text-6xl font-bold mb-4 tracking-tight">
            {hasCourse ? "Pick your plan" : "Grow your course"}
          </h1>
          <p className="max-w-lg mx-auto text-lg leading-relaxed" style={{ color: "rgba(255,255,255,0.85)" }}>
            {hasCourse
              ? "Choose the tier that fits where your course is headed. You can upgrade anytime."
              : "From getting on the map to owning your brand in the app — pick the tier that fits."}
          </p>
        </div>
      </section>

      {/* Tiers */}
      <section className="py-14 px-6 pb-28">
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

              <div className="mb-6">
                <div
                  className="inline-flex items-center justify-center w-11 h-11 rounded-[14px] mb-4"
                  style={tier.highlight ? { background: "rgba(255,255,255,0.2)" } : { background: "rgba(175,82,222,0.12)" }}
                >
                  <tier.tierIcon size={22} style={{ color: tier.highlight ? "white" : "#AF52DE" }} />
                </div>
                <h2 className="text-xl font-bold mb-1" style={{ color: tier.highlight ? "white" : "var(--text)" }}>
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
                      <span className="mt-0.5 shrink-0" style={{ color: tier.highlight ? "rgba(255,255,255,0.9)" : "#AF52DE" }}>
                        <f.icon size={14} />
                      </span>
                      <span className="text-sm leading-snug flex items-center gap-2" style={{ color: tier.highlight ? "rgba(255,255,255,0.85)" : "var(--text-sub)" }}>
                        {f.text}
                        {f.soon && (
                          <span
                            className="text-[10px] font-semibold rounded-full px-2 py-0.5 shrink-0"
                            style={tier.highlight ? { background: "rgba(255,255,255,0.2)", color: "white" } : { background: "rgba(255,149,0,0.15)", color: "#FF9500" }}
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
                href={tierHref(tier)}
                className="w-full py-3.5 text-center text-sm font-semibold transition-all duration-200 hover:opacity-90 hover:scale-105 active:scale-95"
                style={
                  tier.highlight
                    ? { background: "white", color: "#AF52DE", borderRadius: 9999, display: "block" }
                    : { background: "#AF52DE", color: "white", borderRadius: 9999, display: "block" }
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
