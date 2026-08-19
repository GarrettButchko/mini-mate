import Link from "next/link";
import { MapPin, Users, BarChart2, Palette, Megaphone, Trophy } from "lucide-react";
import PageShell from "@/components/PageShell";
import SiteNav from "@/components/SiteNav";
import SiteFooter from "@/components/SiteFooter";
import FaqSection from "@/components/FaqSection";
import TestimonialGrid from "@/components/TestimonialGrid";
import FeatureGrid from "@/components/FeatureGrid";
import HeroPhoneVideo from "@/components/HeroPhoneVideo";
import GetStartedButton from "@/components/GetStartedButton";
import { tiers, type Tier, type TierFeature } from "@/lib/tiers";

const steps = [
  { num: "1", title: "Find your course", desc: "Search for your venue on our map. We verify your listing exists on Foursquare before anything else." },
  { num: "2", title: "Pick a plan", desc: "Start free with a basic map listing, or go Partner and Analytics for branding, ads, and player data." },
  { num: "3", title: "Get your code", desc: "We send a setup code to the contact on file for your course — email, phone, website, or mail." },
  { num: "4", title: "Grow your business", desc: "Activate your listing in the app, then watch round counts, retention, and player data roll in." },
];

const highlights = [
  { icon: MapPin, title: "Verified map listing", desc: "Show up when players search for mini golf near them.", imageSrc: "/screenshots/map-listing.png", imageAlt: "Map listing screen" },
  { icon: Palette, title: "Custom branding", desc: "Your colors and logo on scorecards and your course profile.", imageSrc: "/screenshots/custom-branding.png", imageAlt: "Custom branding screen" },
  { icon: Megaphone, title: "In-app advertising", desc: "Serve your own ads to players inside the app.", imageSrc: "/screenshots/app-advertising.png", imageAlt: "In-app advertising screen" },
  { icon: Trophy, title: "Leaderboards", desc: "Run your own course leaderboard — players compete, you own the data.", imageSrc: "/screenshots/leaderboard.png", imageAlt: "Leaderboard screen" },
  { icon: BarChart2, title: "Analytics dashboard", desc: "Retention, growth, and experience metrics in a beautiful interface.", imageSrc: "/screenshots/analytics-dashboard.png", imageAlt: "Analytics dashboard screen" },
  { icon: Users, title: "Player emails", desc: "Download a CSV of emails from players who've visited your course.", imageSrc: "/screenshots/player-emails.png", imageAlt: "Player emails screen" },
];

const testimonials = [
  { quote: "Our players love checking in through the app. It's become part of the experience at our course.", name: "Chris D.", detail: "Course owner" },
  { quote: "The analytics give us a real sense of who's playing and when — we've never had that before.", name: "Sam T.", detail: "Course manager" },
];

const faqs = [
  { q: "How does the signup flow work?", a: "Search for your course, pick a plan, and we send a setup code to the contact on file with Foursquare — email, phone, or mail. Enter that code in Mini Mate to activate your listing." },
  { q: "Do players need a special account to score at my course?", a: "No. Any Mini Mate user — free or Pro — can find your course and score a round at your venue." },
  { q: "Can I cancel my subscription?", a: "Yes, cancel anytime. Your access continues through the end of the current billing period with no penalties." },
  { q: "What's the difference between Partner and Analytics?", a: "Partner gives you full brand control and in-app advertising. Analytics adds a growth dashboard, player retention metrics, and a downloadable CSV of player emails from your course." },
  { q: "How long does setup take?", a: "Usually within 24 hours. We send your setup code as soon as you confirm, then configure your account before going live." },
];


function TierCard({ tier }: { tier: Tier }) {
  return (
    <div
      className={`relative flex flex-col p-6${tier.highlight ? " pricing-highlight-card" : ""}`}
      style={tier.highlight ? undefined : {
        background: "var(--glass-bg)",
        backdropFilter: "blur(20px)",
        WebkitBackdropFilter: "blur(20px)",
        borderRadius: "25px",
        boxShadow: "var(--shadow)",
      }}
    >
      {tier.highlight && (
        <span className="absolute -top-3.5 left-1/2 -translate-x-1/2 rounded-full text-xs font-bold px-4 py-1 whitespace-nowrap" style={{ background: "#FFD60A", color: "#1C1C1E" }}>
          MOST POPULAR
        </span>
      )}

      <div className="inline-flex items-center justify-center w-10 h-10 rounded-[12px] mb-3" style={tier.highlight ? { background: "rgba(255,255,255,0.2)" } : { background: "rgba(175,82,222,0.12)" }}>
        <tier.tierIcon size={20} style={{ color: tier.highlight ? "white" : "#AF52DE" }} />
      </div>

      <h3 className="text-lg font-bold mb-0.5" style={{ color: tier.highlight ? "white" : "var(--text)" }}>{tier.name}</h3>
      <div className="flex items-baseline gap-1 mb-4">
        <span className="text-3xl font-extrabold" style={{ color: tier.highlight ? "white" : "var(--text)" }}>{tier.price}</span>
        <span className="text-sm" style={{ color: tier.highlight ? "rgba(255,255,255,0.65)" : "var(--text-muted)" }}>{tier.period}</span>
      </div>

      {tier.inherits && (
        <p className="text-xs font-semibold uppercase tracking-wide mb-2" style={{ color: tier.highlight ? "rgba(255,255,255,0.55)" : "var(--text-muted)" }}>
          {tier.inherits}
        </p>
      )}

      <ul className="space-y-2.5">
        {tier.features.map((f: TierFeature) => (
          <li key={f.text} className="flex items-start gap-2">
            <span className="mt-0.5 shrink-0" style={{ color: tier.highlight ? "rgba(255,255,255,0.9)" : "#AF52DE" }}>
              <f.icon size={13} />
            </span>
            <span className="text-sm leading-snug flex items-center gap-1.5" style={{ color: tier.highlight ? "rgba(255,255,255,0.85)" : "var(--text-sub)" }}>
              {f.text}
              {f.soon && (
                <span className="text-[10px] font-semibold rounded-full px-2 py-0.5 shrink-0" style={tier.highlight ? { background: "rgba(255,255,255,0.2)", color: "white" } : { background: "rgba(255,149,0,0.15)", color: "#FF9500" }}>
                  Soon
                </span>
              )}
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default function ForCoursesPage() {
  return (
    <PageShell>
      <SiteNav position="fixed" logoColor="#AF52DE">
        <Link href="/" className="px-3 py-1.5 text-sm font-medium rounded-full hover:opacity-70 transition-opacity" style={{ color: "var(--text-sub)" }}>
          For Players
        </Link>
        <GetStartedButton
          label="Get Started"
          className="rounded-full px-4 py-1.5 text-sm text-white font-semibold hover:opacity-90 hover:scale-105 active:scale-95 transition-all duration-200"
          style={{ background: "#AF52DE", border: "none", cursor: "pointer" }}
        />
      </SiteNav>

      {/* Hero */}
      <section className="hero-gradient-courses text-white">
        <div className="max-w-6xl mx-auto px-6 pt-36 pb-24 flex flex-col lg:flex-row lg:items-center gap-16">
          <div className="flex-1 text-center lg:text-left">
            <span className="inline-flex items-center gap-1.5 rounded-full px-4 py-1.5 text-sm font-semibold mb-6" style={{ background: "rgba(255,255,255,0.2)", backdropFilter: "blur(8px)" }}>
              For Course Owners
            </span>
            <h1 className="text-5xl sm:text-6xl font-bold tracking-tight leading-tight mb-5">
              Grow your mini golf course with Mini Mate
            </h1>
            <p className="text-lg leading-relaxed mb-10 max-w-lg mx-auto lg:mx-0" style={{ color: "rgba(255,255,255,0.85)" }}>
              Get discovered by players near you, run in-app ads, and track your business — all from the Mini Mate admin app.
            </p>
            <div className="flex flex-wrap gap-3 justify-center lg:justify-start">
              <GetStartedButton
                label="Get Started"
                className="rounded-full px-6 py-3 text-sm font-semibold hover:opacity-90 hover:scale-105 active:scale-95 transition-all duration-200"
                style={{ background: "white", color: "#1F4073", border: "none", cursor: "pointer" }}
              />
            </div>
          </div>
          <div className="shrink-0 flex justify-center">
            <HeroPhoneVideo src="/videos/admin-main.mp4" />
          </div>
        </div>
      </section>

      {/* How it works */}
      <section className="py-20 px-6">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-center text-3xl font-bold mb-14" style={{ color: "var(--text)" }}>How it works</h2>
          <div className="relative grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
            <div className="hidden lg:block absolute top-6 left-[12.5%] right-[12.5%]" style={{ height: "2px", borderTop: "2px dashed var(--glass-border)" }} />
            {steps.map((s) => (
              <div key={s.num} className="text-center">
                <div className="relative z-10 inline-flex items-center justify-center w-12 h-12 rounded-full text-lg font-bold text-white mb-4" style={{ background: "#AF52DE" }}>
                  {s.num}
                </div>
                <h3 className="font-semibold mb-2 text-sm" style={{ color: "var(--text)" }}>{s.title}</h3>
                <p className="text-sm leading-relaxed" style={{ color: "var(--text-muted)" }}>{s.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <FeatureGrid heading="Everything in the admin app" features={highlights} accentColor="#AF52DE" />

      <TestimonialGrid heading="What course owners say" testimonials={testimonials} />

      {/* Pricing infographic */}
      <section id="pricing" className="py-20 px-6">
        <div className="max-w-5xl mx-auto p-6">
          <h2 className="text-center text-3xl font-bold mb-3" style={{ color: "var(--text)" }}>Simple, transparent pricing</h2>
          <p className="text-center mb-14" style={{ color: "var(--text-muted)" }}>Start free. Upgrade when you need more.</p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 items-stretch">
            {tiers.map((tier) => <TierCard key={tier.name} tier={tier} />)}
          </div>
          <div className="mt-10 flex justify-center">
            <GetStartedButton
              label="Get Started"
              className="rounded-full px-10 py-4 text-base font-semibold text-white hover:opacity-90 hover:scale-105 active:scale-95 transition-all duration-200"
              style={{ background: "#AF52DE", border: "none", cursor: "pointer" }}
            />
          </div>
        </div>
      </section>

      <FaqSection faqs={faqs} />

      {/* CTA */}
      <section className="py-20 px-6 text-center">
        <h2 className="text-3xl font-bold mb-4" style={{ color: "var(--text)" }}>Ready to grow your course?</h2>
        <p className="mb-8 max-w-md mx-auto" style={{ color: "var(--text-muted)" }}>Find your course, pick a plan, and we handle the rest. Start free — no lock-in.</p>
        <div className="flex flex-wrap gap-4 justify-center">
          <GetStartedButton
            label="Get Started"
            className="rounded-full px-10 py-4 text-base font-semibold text-white hover:opacity-90 hover:scale-105 active:scale-95 transition-all duration-200"
            style={{ background: "#AF52DE", border: "none", cursor: "pointer" }}
          />
        </div>
      </section>

      <SiteFooter links={[
        { href: "/", label: "For Players" },
        { href: "/pricing", label: "Pricing" },
        { href: "/find-course", label: "Find Your Course" },
        { href: "/privacy", label: "Privacy Policy" },
        { href: "/terms", label: "Terms" },
        { href: "mailto:garrettwm2005@gmail.com", label: "Support" },
      ]} />
    </PageShell>
  );
}
