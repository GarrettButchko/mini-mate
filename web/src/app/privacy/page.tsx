import Link from "next/link";
import MiniMateLogo from "@/components/MiniMateLogo";

export default function PrivacyPage() {
  return (
    <div className="min-h-screen flex flex-col font-[family-name:var(--font-geist-sans)]" style={{ background: "var(--bg)", color: "var(--text)" }}>
      <nav className="sticky top-0 z-50 flex items-center justify-between px-6 py-4">
        <Link href="/" className="glass-pill flex items-center gap-2 px-4 py-3.5 no-underline">
          <MiniMateLogo size={20} color="#34C759" />
          <span className="text-[20px] font-medium tracking-tight leading-none" style={{ color: "var(--text)" }}>Mini Mate</span>
        </Link>
      </nav>

      <main className="flex-1 max-w-2xl mx-auto px-6 py-16 w-full">
        <h1 className="text-3xl font-bold mb-2" style={{ color: "var(--text)" }}>Privacy Policy</h1>
        <p className="text-sm mb-12" style={{ color: "var(--text-muted)" }}>Last updated June 29, 2026</p>

        <div className="space-y-10" style={{ color: "var(--text-sub)" }}>

          <section>
            <h2 className="text-lg font-semibold mb-3" style={{ color: "var(--text)" }}>Overview</h2>
            <p className="text-sm leading-relaxed">
              Mini Mate ("we," "our," or "us") is a mini golf scorecard app. This policy explains what information we collect when you use the Mini Mate mobile app or this website, how we use it, and your choices.
            </p>
          </section>

          <section>
            <h2 className="text-lg font-semibold mb-3" style={{ color: "var(--text)" }}>Information We Collect</h2>
            <ul className="text-sm leading-relaxed space-y-3">
              <li><span className="font-medium" style={{ color: "var(--text)" }}>Account information.</span> If you create an account for multiplayer, we collect your email address and a hashed password.</li>
              <li><span className="font-medium" style={{ color: "var(--text)" }}>Game data.</span> We store scorecard data (scores, par, hole count, timestamps) associated with your account so you can review past rounds.</li>
              <li><span className="font-medium" style={{ color: "var(--text)" }}>Location.</span> If you use Course Discovery, we request your device location to find nearby mini golf venues. We do not store your location on our servers.</li>
              <li><span className="font-medium" style={{ color: "var(--text)" }}>Course owner data.</span> If you subscribe as a course owner, we collect your name, email, course name, and payment information (processed by Stripe — we never see your full card number).</li>
              <li><span className="font-medium" style={{ color: "var(--text)" }}>Usage analytics.</span> We may collect anonymous crash reports and feature usage data to improve the app.</li>
            </ul>
          </section>

          <section>
            <h2 className="text-lg font-semibold mb-3" style={{ color: "var(--text)" }}>How We Use Your Information</h2>
            <ul className="text-sm leading-relaxed space-y-2">
              <li>— Provide and improve the Mini Mate app and website</li>
              <li>— Sync your scorecards and multiplayer sessions across devices</li>
              <li>— Process course owner subscriptions and send billing receipts</li>
              <li>— Respond to support requests</li>
              <li>— Comply with legal obligations</li>
            </ul>
          </section>

          <section>
            <h2 className="text-lg font-semibold mb-3" style={{ color: "var(--text)" }}>Third-Party Services</h2>
            <ul className="text-sm leading-relaxed space-y-3">
              <li><span className="font-medium" style={{ color: "var(--text)" }}>Foursquare.</span> Course Discovery uses the Foursquare Places API to find nearby venues. Your location is sent to Foursquare's servers for this query. See Foursquare's privacy policy for details.</li>
              <li><span className="font-medium" style={{ color: "var(--text)" }}>Apple & Google.</span> In-app purchases are processed through the App Store and Google Play. Their respective privacy policies govern that data.</li>
              <li><span className="font-medium" style={{ color: "var(--text)" }}>Stripe.</span> Course owner subscriptions are processed by Stripe. We do not store payment card details.</li>
            </ul>
          </section>

          <section>
            <h2 className="text-lg font-semibold mb-3" style={{ color: "var(--text)" }}>Data Retention</h2>
            <p className="text-sm leading-relaxed">
              We retain your account and game data for as long as your account is active. You can request deletion of your data at any time by contacting us.
            </p>
          </section>

          <section>
            <h2 className="text-lg font-semibold mb-3" style={{ color: "var(--text)" }}>Children</h2>
            <p className="text-sm leading-relaxed">
              Mini Mate is not directed to children under 13. We do not knowingly collect personal information from children under 13.
            </p>
          </section>

          <section>
            <h2 className="text-lg font-semibold mb-3" style={{ color: "var(--text)" }}>Contact</h2>
            <p className="text-sm leading-relaxed">
              Questions about this policy? Email us at{" "}
              <a href="mailto:garrettwm2005@gmail.com" className="hover:opacity-70 transition-opacity" style={{ color: "#34C759" }}>
                garrettwm2005@gmail.com
              </a>.
            </p>
          </section>

        </div>
      </main>

      <footer
        className="py-8 px-6 text-center text-sm"
        style={{ borderTop: "1px solid var(--glass-border)", color: "var(--text-muted)" }}
      >
        <div className="flex flex-wrap justify-center gap-6 mb-4">
          <Link href="/" className="hover:opacity-70 transition-opacity">Home</Link>
          <Link href="/terms" className="hover:opacity-70 transition-opacity">Terms</Link>
        </div>
        <p>© {new Date().getFullYear()} Mini Mate. All rights reserved.</p>
      </footer>
    </div>
  );
}
