import Link from "next/link";
import { MainViewBusinessLogic } from "@/logic/MainViewBusinessLogic";

const logic = new MainViewBusinessLogic();

const features = [
  {
    icon: "⛳",
    title: "Instant Scorecards",
    desc: "Start a round in seconds. Track every hole with live par comparison.",
  },
  {
    icon: "🌐",
    title: "Online Multiplayer",
    desc: logic.getInfoMessage(true),
  },
  {
    icon: "📊",
    title: "Stats & Recap",
    desc: "See your best rounds, stroke averages, and course records.",
  },
  {
    icon: "🗺️",
    title: "Course Discovery",
    desc: "Find verified mini golf venues near you powered by Foursquare.",
  },
];

export default function Home() {
  return (
    <div className="min-h-screen bg-white dark:bg-zinc-950 font-[family-name:var(--font-geist-sans)]">
      {/* Nav */}
      <nav className="flex items-center justify-between px-8 py-5 border-b border-zinc-100 dark:border-zinc-800">
        <span className="text-xl font-bold tracking-tight text-green-600">⛳ Mini-Mate</span>
        <div className="flex items-center gap-6 text-sm font-medium text-zinc-600 dark:text-zinc-400">
          <Link href="/pricing" className="hover:text-zinc-900 dark:hover:text-white transition-colors">
            Pricing
          </Link>
          <Link href="/courses" className="hover:text-zinc-900 dark:hover:text-white transition-colors">
            For Courses
          </Link>
          <Link
            href="/pricing"
            className="rounded-full bg-green-600 px-4 py-2 text-white hover:bg-green-700 transition-colors"
          >
            Get Started
          </Link>
        </div>
      </nav>

      {/* Hero */}
      <section className="flex flex-col items-center text-center px-6 pt-24 pb-20">
        <span className="inline-block rounded-full bg-green-50 dark:bg-green-900/30 px-4 py-1.5 text-sm font-semibold text-green-700 dark:text-green-400 mb-6">
          Now on iOS & Android
        </span>
        <h1 className="max-w-2xl text-5xl sm:text-6xl font-bold tracking-tight text-zinc-900 dark:text-white leading-tight">
          The smartest mini golf scorecard, ever.
        </h1>
        <p className="mt-6 max-w-xl text-lg text-zinc-600 dark:text-zinc-400 leading-relaxed">
          {logic.getInfoMessage(false)} Built for players who want real-time multiplayer, verified
          course data, and beautiful stats — all in one app.
        </p>
        <div className="mt-10 flex flex-wrap gap-4 justify-center">
          <Link
            href="/pricing"
            className="rounded-full bg-green-600 px-8 py-3.5 text-base font-semibold text-white hover:bg-green-700 transition-colors shadow-sm"
          >
            View Plans
          </Link>
          <Link
            href="/courses"
            className="rounded-full border border-zinc-200 dark:border-zinc-700 px-8 py-3.5 text-base font-semibold text-zinc-700 dark:text-zinc-300 hover:bg-zinc-50 dark:hover:bg-zinc-800 transition-colors"
          >
            List Your Course →
          </Link>
        </div>
      </section>

      {/* Features */}
      <section className="bg-zinc-50 dark:bg-zinc-900 py-20 px-6">
        <div className="max-w-5xl mx-auto">
          <h2 className="text-center text-3xl font-bold text-zinc-900 dark:text-white mb-14">
            Everything you need on the course
          </h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-8">
            {features.map((f) => (
              <div
                key={f.title}
                className="rounded-2xl bg-white dark:bg-zinc-800 p-8 shadow-sm border border-zinc-100 dark:border-zinc-700"
              >
                <div className="text-4xl mb-4">{f.icon}</div>
                <h3 className="text-lg font-semibold text-zinc-900 dark:text-white mb-2">{f.title}</h3>
                <p className="text-zinc-500 dark:text-zinc-400 text-sm leading-relaxed">{f.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="py-24 px-6 text-center">
        <h2 className="text-3xl font-bold text-zinc-900 dark:text-white mb-4">
          Ready to play smarter?
        </h2>
        <p className="text-zinc-500 dark:text-zinc-400 mb-8 max-w-md mx-auto">
          Free forever for casual players. Upgrade to Pro for unlimited games and online multiplayer.
        </p>
        <Link
          href="/pricing"
          className="inline-block rounded-full bg-green-600 px-10 py-4 text-lg font-semibold text-white hover:bg-green-700 transition-colors"
        >
          See Pricing
        </Link>
      </section>

      {/* Footer */}
      <footer className="border-t border-zinc-100 dark:border-zinc-800 py-8 px-6 text-center text-sm text-zinc-400">
        <div className="flex flex-wrap justify-center gap-6 mb-4">
          <Link href="/pricing" className="hover:text-zinc-700 dark:hover:text-zinc-200 transition-colors">Pricing</Link>
          <Link href="/courses" className="hover:text-zinc-700 dark:hover:text-zinc-200 transition-colors">For Courses</Link>
        </div>
        <p>© {new Date().getFullYear()} Mini-Mate. All rights reserved.</p>
      </footer>
    </div>
  );
}
