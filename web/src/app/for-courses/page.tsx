import Link from "next/link";
import ScreenshotFrame from "@/components/ScreenshotFrame";
import { MapPin, Users, BarChart2, Palette, Megaphone, Trophy } from "lucide-react";
import MiniMateLogo from "@/components/MiniMateLogo";

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

export default function ForCoursesPage() {
  return (
    <div className="min-h-screen flex flex-col font-[family-name:var(--font-geist-sans)]" style={{ background: "var(--bg)", color: "var(--text)" }}>
      <nav className="fixed inset-x-0 top-0 z-50 flex items-center justify-between px-6 py-4">
        <Link href="/" className="glass-pill flex items-center gap-2 px-4 py-3.5 no-underline">
          <MiniMateLogo size={20} color="#34C759" />
          <span className="text-[20px] font-medium tracking-tight leading-none" style={{ color: "var(--text)" }}>Mini Mate</span>
        </Link>
        <div className="glass-pill flex items-center gap-1 px-2 py-2">
          <Link href="/" className="px-3 py-1.5 text-sm font-medium rounded-full hover:opacity-70 transition-opacity" style={{ color: "var(--text-sub)" }}>
            For Players
          </Link>
          <Link href="/pricing" className="rounded-full px-4 py-1.5 text-sm text-white font-semibold hover:opacity-90 transition-opacity" style={{ background: "#AF52DE" }}>
            View Pricing
          </Link>
        </div>
      </nav>

      {/* Hero */}
      <section className="text-white" style={{ background: "linear-gradient(135deg, #0A84FF 0%, #AF52DE 100%)" }}>
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
              <Link href="/pricing" className="rounded-full px-6 py-3 text-sm font-semibold hover:opacity-90 transition-opacity" style={{ background: "white", color: "#1F4073" }}>
                See Pricing
              </Link>
              <Link href="/courses" className="rounded-full px-6 py-3 text-sm font-semibold hover:opacity-90 transition-opacity" style={{ background: "rgba(255,255,255,0.15)", border: "1px solid rgba(255,255,255,0.3)", color: "white" }}>
                Find Your Course
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

      {/* Features */}
      <section className="py-20 px-6" style={{ background: "var(--surface-sub)" }}>
        <div className="max-w-5xl mx-auto">
          <h2 className="text-center text-3xl font-bold mb-14" style={{ color: "var(--text)" }}>Everything in the admin app</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
            {highlights.map((h) => (
              <div key={h.title} className="glass-card p-8 flex flex-row items-center gap-6">
                <div className="flex-1">
                  <div className="mb-4" style={{ color: "#AF52DE" }}><h.icon size={32} /></div>
                  <h3 className="text-lg font-semibold mb-2" style={{ color: "var(--text)" }}>{h.title}</h3>
                  <p className="text-sm leading-relaxed" style={{ color: "var(--text-muted)" }}>{h.desc}</p>
                </div>
                <ScreenshotFrame imageSrc={h.imageSrc} imageAlt={h.imageAlt} frameWidth={100} frameHeight={200}>
                  <h.icon size={28} style={{ color: "var(--text-muted)", opacity: 0.2 }} />
                </ScreenshotFrame>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Social proof */}
      <section className="py-20 px-6">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-center text-3xl font-bold mb-14" style={{ color: "var(--text)" }}>What course owners say</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
            {testimonials.map((t) => (
              <div key={t.name} className="glass-card p-8">
                <p className="text-sm leading-relaxed mb-6" style={{ color: "var(--text-sub)" }}>&ldquo;{t.quote}&rdquo;</p>
                <div>
                  <p className="text-sm font-semibold" style={{ color: "var(--text)" }}>{t.name}</p>
                  <p className="text-xs" style={{ color: "var(--text-muted)" }}>{t.detail}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* FAQ */}
      <section className="py-20 px-6" style={{ background: "var(--surface-sub)" }}>
        <div className="max-w-2xl mx-auto">
          <h2 className="text-center text-3xl font-bold mb-14" style={{ color: "var(--text)" }}>Frequently asked questions</h2>
          <div className="space-y-4">
            {faqs.map((item) => (
              <details key={item.q} className="glass-card group">
                <summary className="flex items-center justify-between px-6 py-5 cursor-pointer list-none font-medium text-sm" style={{ color: "var(--text)" }}>
                  {item.q}
                  <span className="ml-4 shrink-0 text-lg leading-none" style={{ color: "var(--text-muted)" }}>+</span>
                </summary>
                <p className="px-6 pb-5 text-sm leading-relaxed" style={{ color: "var(--text-muted)" }}>{item.a}</p>
              </details>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="py-20 px-6 text-center">
        <h2 className="text-3xl font-bold mb-4" style={{ color: "var(--text)" }}>Ready to get listed?</h2>
        <p className="mb-8 max-w-md mx-auto" style={{ color: "var(--text-muted)" }}>Start free. Upgrade when you need more. No lock-in.</p>
        <div className="flex flex-wrap gap-4 justify-center">
          <Link href="/pricing" className="rounded-full px-10 py-4 text-base font-semibold text-white hover:opacity-90 transition-opacity" style={{ background: "#AF52DE" }}>
            View Plans
          </Link>
          <Link href="/courses" className="rounded-full px-10 py-4 text-base font-semibold hover:opacity-70 transition-opacity" style={{ color: "var(--text-sub)", border: "1px solid var(--glass-border)" }}>
            Find Your Course
          </Link>
        </div>
      </section>

      <footer className="mt-auto py-8 px-6 text-center text-sm" style={{ borderTop: "1px solid var(--glass-border)", color: "var(--text-muted)" }}>
        <div className="flex flex-wrap justify-center gap-6 mb-4">
          <Link href="/" className="hover:opacity-70 transition-opacity">For Players</Link>
          <Link href="/pricing" className="hover:opacity-70 transition-opacity">Pricing</Link>
          <Link href="/courses" className="hover:opacity-70 transition-opacity">Find Your Course</Link>
          <Link href="/privacy" className="hover:opacity-70 transition-opacity">Privacy Policy</Link>
          <Link href="/terms" className="hover:opacity-70 transition-opacity">Terms</Link>
          <a href="mailto:garrettwm2005@gmail.com" className="hover:opacity-70 transition-opacity">Support</a>
        </div>
        <p>© {new Date().getFullYear()} Mini Mate. All rights reserved.</p>
      </footer>
    </div>
  );
}
