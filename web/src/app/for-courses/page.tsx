import Link from "next/link";
import { MapPin, Users, BarChart2, Palette, Megaphone, Trophy } from "lucide-react";
import PageShell from "@/components/PageShell";
import SiteNav from "@/components/SiteNav";
import SiteFooter from "@/components/SiteFooter";
import FaqSection from "@/components/FaqSection";
import TestimonialGrid from "@/components/TestimonialGrid";
import FeatureGrid from "@/components/FeatureGrid";

const steps = [
  { num: "1", title: "List your course — free", desc: "Your course appears on the Mini Mate map. Players find you when searching for mini golf nearby." },
  { num: "2", title: "Players score at your venue", desc: "Guests open Mini Mate, select your course, and score every hole with real-time multiplayer." },
  { num: "3", title: "Choose a plan", desc: "Upgrade to Partner for branding and in-app ads, or Analytics for growth data and player emails." },
  { num: "4", title: "Grow your business", desc: "See round counts, retention trends, and downloadable player data — all in one dashboard." },
];

const highlights = [
  { icon: MapPin, title: "Verified map listing", desc: "Show up when players search for mini golf near them.", imageSrc: null as string | null, imageAlt: "Map listing screen" },
  { icon: Palette, title: "Custom branding", desc: "Your colors and logo on scorecards and your course profile.", imageSrc: null as string | null, imageAlt: "Custom branding screen" },
  { icon: Megaphone, title: "In-app advertising", desc: "Serve your own ads to players inside the app.", imageSrc: null as string | null, imageAlt: "In-app advertising screen" },
  { icon: Trophy, title: "Leaderboards", desc: "Run your own course leaderboard — players compete, you own the data.", imageSrc: null as string | null, imageAlt: "Leaderboard screen" },
  { icon: BarChart2, title: "Analytics dashboard", desc: "Retention, growth, and experience metrics in a beautiful interface.", imageSrc: null as string | null, imageAlt: "Analytics dashboard screen" },
  { icon: Users, title: "Player emails", desc: "Download a CSV of emails from players who've visited your course.", imageSrc: null as string | null, imageAlt: "Player emails screen" },
];

const testimonials = [
  { quote: "Our players love checking in through the app. It's become part of the experience at our course.", name: "Chris D.", detail: "Course owner" },
  { quote: "The analytics give us a real sense of who's playing and when — we've never had that before.", name: "Sam T.", detail: "Course manager" },
];

const faqs = [
  { q: "How does my course get listed?", a: "We verify your venue exists via Foursquare, then set up your profile in the Mini Mate app. Use the 'Find Your Course' tool to confirm we already have you in our system." },
  { q: "Do players need a special account to score at my course?", a: "No. Any Mini Mate user — free or Pro — can find your course and score a round at your venue." },
  { q: "Can I cancel my subscription?", a: "Yes, cancel anytime. Your access continues through the end of the current billing period with no penalties." },
  { q: "What's the difference between Partner and Analytics?", a: "Partner gives you full brand control and in-app advertising. Analytics adds a growth dashboard, player retention metrics, and a downloadable CSV of player emails from your course." },
  { q: "How long does setup take?", a: "Usually within 24 hours of subscribing. We verify your Foursquare listing and configure your account before going live." },
];

const heroGhostStyle = { background: "rgba(255,255,255,0.15)", border: "1px solid rgba(255,255,255,0.3)", color: "white" } as const;

export default function ForCoursesPage() {
  return (
    <PageShell>
      <SiteNav position="fixed" logoColor="#AF52DE">
        <Link href="/" className="px-3 py-1.5 text-sm font-medium rounded-full hover:opacity-70 transition-opacity" style={{ color: "var(--text-sub)" }}>
          For Players
        </Link>
        <Link href="/manage" className="px-3 py-1.5 text-sm font-medium rounded-full hover:opacity-70 transition-opacity" style={{ color: "var(--text-sub)" }}>
          Manage
        </Link>
        <Link href="/pricing" className="rounded-full px-4 py-1.5 text-sm text-white font-semibold hover:opacity-90 hover:scale-105 active:scale-95 transition-all duration-200" style={{ background: "#AF52DE" }}>
          View Pricing
        </Link>
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
              <Link href="/pricing" className="rounded-full px-6 py-3 text-sm font-semibold hover:opacity-90 hover:scale-105 active:scale-95 transition-all duration-200" style={{ background: "white", color: "#1F4073" }}>
                See Pricing
              </Link>
              <Link href="/courses" className="rounded-full px-6 py-3 text-sm font-semibold hover:opacity-90 hover:scale-105 active:scale-95 transition-all duration-200" style={heroGhostStyle}>
                Find Your Course
              </Link>
              <Link href="/manage" className="rounded-full px-6 py-3 text-sm font-semibold hover:opacity-90 hover:scale-105 active:scale-95 transition-all duration-200" style={heroGhostStyle}>
                Manage Subscription
              </Link>
            </div>
          </div>
          <div className="shrink-0 flex justify-center">
            <div className="rounded-[44px]" style={{ width: 260, height: 520, border: "2px solid rgba(255,255,255,0.25)", background: "rgba(0,0,0,0.2)", backdropFilter: "blur(20px)" }} />
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

      <FaqSection faqs={faqs} />

      {/* CTA */}
      <section className="py-20 px-6 text-center">
        <h2 className="text-3xl font-bold mb-4" style={{ color: "var(--text)" }}>Ready to get listed?</h2>
        <p className="mb-8 max-w-md mx-auto" style={{ color: "var(--text-muted)" }}>Start free. Upgrade when you need more. No lock-in.</p>
        <div className="flex flex-wrap gap-4 justify-center">
          <Link href="/pricing" className="rounded-full px-10 py-4 text-base font-semibold text-white hover:opacity-90 hover:scale-105 active:scale-95 transition-all duration-200" style={{ background: "#AF52DE" }}>
            View Plans
          </Link>
          <Link href="/courses" className="rounded-full px-10 py-4 text-base font-semibold hover:opacity-70 hover:scale-105 active:scale-95 transition-all duration-200" style={{ color: "var(--text-sub)", border: "1px solid var(--glass-border)" }}>
            Find Your Course
          </Link>
        </div>
      </section>

      <SiteFooter links={[
        { href: "/", label: "For Players" },
        { href: "/pricing", label: "Pricing" },
        { href: "/courses", label: "Find Your Course" },
        { href: "/privacy", label: "Privacy Policy" },
        { href: "/terms", label: "Terms" },
        { href: "mailto:garrettwm2005@gmail.com", label: "Support" },
      ]} />
    </PageShell>
  );
}
