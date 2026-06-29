import Link from "next/link";
import { Target, Globe, BarChart2, Map, Smartphone, Building2, Download } from "lucide-react";
import { MainViewBusinessLogic } from "@/lib/MainViewBusinessLogic";
import PageShell from "@/components/PageShell";
import SiteNav from "@/components/SiteNav";
import SiteFooter from "@/components/SiteFooter";
import FaqSection from "@/components/FaqSection";
import TestimonialGrid from "@/components/TestimonialGrid";
import FeatureGrid from "@/components/FeatureGrid";

const logic = new MainViewBusinessLogic();

const features = [
  { icon: Target, title: "Instant Scorecards", desc: "Start a round in seconds. Track every hole with live par comparison.", imageSrc: "/screenshots/test.jpg", imageAlt: "Scorecard screen" },
  { icon: Globe, title: "Online Multiplayer", desc: logic.getInfoMessage(true), imageSrc: null as string | null, imageAlt: "Multiplayer lobby screen" },
  { icon: BarChart2, title: "Stats & Recap", desc: "See your best rounds, stroke averages, and course records.", imageSrc: null as string | null, imageAlt: "Stats and recap screen" },
  { icon: Map, title: "Course Discovery", desc: "Find verified mini golf venues near you powered by Foursquare.", imageSrc: null as string | null, imageAlt: "Course discovery screen" },
];

const steps = [
  { num: "1", title: "Download Mini Mate", desc: "Free on iOS and Android. No account required to start playing." },
  { num: "2", title: "Find or start a game", desc: "Search for a verified course near you, or start a local game anywhere." },
  { num: "3", title: "Play, track, and compete", desc: "Score every hole, follow friends in real-time, and review your stats after every round." },
];

const faqs = [
  { q: "Is Mini Mate really free?", a: "Yes — forever for casual play. You get up to 2 saved games, local scorecards, basic stats, and course discovery at no cost." },
  { q: "What does Pro include?", a: "Unlimited saved games, no Google-served ads (course ads still support venues), and early access to new features as they ship." },
  { q: "Is Pro a subscription?", a: "No. Pro is a one-time $2 purchase made inside the app through the App Store or Google Play. Pay once, keep it forever." },
  { q: "How does course discovery work?", a: "We use Foursquare to find and verify mini golf venues near you. Tap 'Find Near Me' on the course search screen and we'll show you what's around." },
  { q: "Does it work offline?", a: "Scorecards and local games work offline. Online multiplayer and course discovery require an internet connection." },
];

const testimonials = [
  { quote: "Finally a scorecard app that doesn't get in the way. Clean, fast, and the multiplayer is actually fun.", name: "Alex R.", detail: "Casual golfer" },
  { quote: "We use it every time we go out. Keeping score on paper feels ancient now.", name: "Jordan M.", detail: "Frequent player" },
];

export default function Home() {
  return (
    <PageShell>
      <SiteNav position="fixed" logoLinked={false}>
        <Link href="/for-courses" className="px-3 py-1.5 text-sm font-medium rounded-full hover:opacity-70 transition-opacity" style={{ color: "var(--text-sub)" }}>
          For Courses
        </Link>
      </SiteNav>

      {/* Hero */}
      <section className="hero-gradient-user text-white">
        <div className="max-w-6xl mx-auto px-6 pt-36 pb-24 flex flex-col lg:flex-row lg:items-center gap-16">
          <div className="flex-1 text-center lg:text-left">
            <span className="inline-flex items-center gap-1.5 rounded-full px-4 py-1.5 text-sm font-semibold mb-6" style={{ background: "rgba(255,255,255,0.2)", backdropFilter: "blur(8px)" }}>
              <Smartphone size={13} />
              Now on iOS & Android
            </span>
            <h1 className="text-5xl sm:text-6xl font-bold tracking-tight leading-tight mb-5">
              The smartest mini golf scorecard, ever.
            </h1>
            <p className="text-lg leading-relaxed mb-10 max-w-lg mx-auto lg:mx-0" style={{ color: "rgba(255,255,255,0.85)" }}>
              Real-time multiplayer, verified course data, and beautiful stats — free for everyone.
            </p>
            <div className="flex flex-wrap gap-3 justify-center lg:justify-start mb-8">
              <a href="#app-store" className="flex items-center gap-2.5 rounded-full px-6 py-3 text-sm font-semibold hover:opacity-90 transition-opacity" style={{ background: "white", color: "#1C1C1E" }}>
                <AppleIcon />
                App Store
              </a>
              <a href="#google-play" className="flex items-center gap-2.5 rounded-full px-6 py-3 text-sm font-semibold hover:opacity-90 transition-opacity" style={{ background: "rgba(255,255,255,0.15)", border: "1px solid rgba(255,255,255,0.3)", color: "white" }}>
                <GooglePlayIcon />
                Google Play
              </a>
            </div>
            <Link href="/for-courses" className="inline-flex items-center gap-1 text-sm hover:opacity-80 transition-opacity" style={{ color: "rgba(255,255,255,0.65)" }}>
              Are you a course owner? →
            </Link>
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
          <div className="relative grid grid-cols-1 sm:grid-cols-3 gap-8">
            <div className="hidden sm:block absolute top-6 left-[16.67%] right-[16.67%]" style={{ height: "2px", borderTop: "2px dashed var(--glass-border)" }} />
            {steps.map((s) => (
              <div key={s.num} className="text-center">
                <div className="relative z-10 inline-flex items-center justify-center w-12 h-12 rounded-full text-lg font-bold text-white mb-4" style={{ background: "#34C759" }}>
                  {s.num}
                </div>
                <h3 className="font-semibold mb-2" style={{ color: "var(--text)" }}>{s.title}</h3>
                <p className="text-sm leading-relaxed" style={{ color: "var(--text-muted)" }}>{s.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <FeatureGrid heading="Everything you need on the course" features={features} />

      <TestimonialGrid heading="What players say" testimonials={testimonials}>
        <div className="mt-10 flex flex-wrap justify-center gap-4">
          <a href="#app-store" className="flex items-center gap-2.5 rounded-full px-6 py-3 text-sm font-semibold hover:opacity-90 transition-opacity" style={{ background: "var(--surface)", border: "1px solid var(--glass-border)", color: "var(--text)" }}>
            <AppleIcon />
            Download on App Store
          </a>
          <a href="#google-play" className="flex items-center gap-2.5 rounded-full px-6 py-3 text-sm font-semibold hover:opacity-90 transition-opacity" style={{ background: "var(--surface)", border: "1px solid var(--glass-border)", color: "var(--text)" }}>
            <GooglePlayIcon />
            Get it on Google Play
          </a>
        </div>
      </TestimonialGrid>

      <FaqSection faqs={faqs} />

      {/* Fork */}
      <section className="py-20 px-6">
        <div className="max-w-3xl mx-auto">
          <h2 className="text-center text-2xl font-bold mb-10" style={{ color: "var(--text)" }}>What brings you here?</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
            <div className="glass-card p-8 text-center flex flex-col items-center">
              <div className="mb-4" style={{ color: "#34C759" }}><Download size={32} /></div>
              <h3 className="text-lg font-semibold mb-2" style={{ color: "var(--text)" }}>I want to track my golf</h3>
              <p className="text-sm leading-relaxed mb-6" style={{ color: "var(--text-muted)" }}>Download the app, play for free, and upgrade to Pro for $2.</p>
              <a href="#app-store" className="rounded-full px-6 py-3 text-sm font-semibold text-white hover:opacity-90 transition-opacity" style={{ background: "#34C759" }}>
                Download the App
              </a>
            </div>
            <div className="glass-card p-8 text-center flex flex-col items-center">
              <div className="mb-4" style={{ color: "#AF52DE" }}><Building2 size={32} /></div>
              <h3 className="text-lg font-semibold mb-2" style={{ color: "var(--text)" }}>I own a mini golf course</h3>
              <p className="text-sm leading-relaxed mb-6" style={{ color: "var(--text-muted)" }}>Get listed, build your brand in the app, and track your players.</p>
              <Link href="/for-courses" className="rounded-full px-6 py-3 text-sm font-semibold text-white hover:opacity-90 transition-opacity" style={{ background: "#AF52DE" }}>
                Learn More
              </Link>
            </div>
          </div>
        </div>
      </section>

      <SiteFooter links={[
        { href: "/pricing", label: "Pricing" },
        { href: "/for-courses", label: "For Courses" },
        { href: "/privacy", label: "Privacy Policy" },
        { href: "/terms", label: "Terms" },
        { href: "mailto:garrettwm2005@gmail.com", label: "Support" },
      ]} />
    </PageShell>
  );
}

function AppleIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
      <path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.8-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M13 3.5c.73-.83 1.94-1.46 2.94-1.5.13 1.17-.34 2.35-1.04 3.19-.69.85-1.83 1.51-2.95 1.42-.15-1.15.41-2.35 1.05-3.11z"/>
    </svg>
  );
}

function GooglePlayIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
      <path d="M8 5v14l11-7z"/>
    </svg>
  );
}
