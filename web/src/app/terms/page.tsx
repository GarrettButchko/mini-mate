import Link from "next/link";
import MiniMateLogo from "@/components/MiniMateLogo";

export default function TermsPage() {
  return (
    <div className="min-h-screen flex flex-col font-[family-name:var(--font-geist-sans)]" style={{ background: "var(--bg)", color: "var(--text)" }}>
      <nav className="sticky top-0 z-50 flex items-center justify-between px-6 py-4">
        <Link href="/" className="glass-pill flex items-center gap-2 px-4 py-3.5 no-underline">
          <MiniMateLogo size={20} color="#34C759" />
          <span className="text-[20px] font-medium tracking-tight leading-none" style={{ color: "var(--text)" }}>Mini Mate</span>
        </Link>
      </nav>

      <main className="flex-1 max-w-2xl mx-auto px-6 py-16 w-full">
        <h1 className="text-3xl font-bold mb-2" style={{ color: "var(--text)" }}>Terms of Service</h1>
        <p className="text-sm mb-12" style={{ color: "var(--text-muted)" }}>Last updated June 29, 2026</p>

        <div className="space-y-10" style={{ color: "var(--text-sub)" }}>

          <section>
            <h2 className="text-lg font-semibold mb-3" style={{ color: "var(--text)" }}>Acceptance</h2>
            <p className="text-sm leading-relaxed">
              By downloading or using Mini Mate, you agree to these terms. If you do not agree, do not use the app or website.
            </p>
          </section>

          <section>
            <h2 className="text-lg font-semibold mb-3" style={{ color: "var(--text)" }}>The Service</h2>
            <p className="text-sm leading-relaxed">
              Mini Mate provides a mini golf scorecard app, course discovery, and optional analytics tools for course owners. We reserve the right to modify or discontinue any feature at any time.
            </p>
          </section>

          <section>
            <h2 className="text-lg font-semibold mb-3" style={{ color: "var(--text)" }}>Pro Upgrade (Players)</h2>
            <p className="text-sm leading-relaxed">
              The Pro upgrade is a one-time in-app purchase processed by Apple or Google. It removes Google-served ads and unlocks unlimited saved games. The purchase is subject to Apple App Store and Google Play Store terms. We do not offer refunds outside of those platforms' policies.
            </p>
          </section>

          <section>
            <h2 className="text-lg font-semibold mb-3" style={{ color: "var(--text)" }}>Course Owner Subscriptions</h2>
            <p className="text-sm leading-relaxed">
              Course owner plans (Listed, Partner, Analytics) are monthly subscriptions billed in USD. You may cancel at any time and your access continues through the end of the billing period. We reserve the right to change subscription pricing with 30 days' notice. Course owner features are delivered through the Mini Mate admin app.
            </p>
          </section>

          <section>
            <h2 className="text-lg font-semibold mb-3" style={{ color: "var(--text)" }}>Acceptable Use</h2>
            <ul className="text-sm leading-relaxed space-y-2">
              <li>— Do not use Mini Mate for any unlawful purpose</li>
              <li>— Do not attempt to reverse engineer, scrape, or abuse the API</li>
              <li>— Do not submit false or misleading course information</li>
              <li>— Do not use the platform to harass other users</li>
            </ul>
          </section>

          <section>
            <h2 className="text-lg font-semibold mb-3" style={{ color: "var(--text)" }}>Intellectual Property</h2>
            <p className="text-sm leading-relaxed">
              Mini Mate, its logo, and all associated content are owned by Garrett Butchko. You may not reproduce or redistribute any part of the app or website without written permission.
            </p>
          </section>

          <section>
            <h2 className="text-lg font-semibold mb-3" style={{ color: "var(--text)" }}>Disclaimer & Limitation of Liability</h2>
            <p className="text-sm leading-relaxed">
              Mini Mate is provided "as is" without warranties of any kind. We are not liable for any indirect, incidental, or consequential damages arising from your use of the service. Our total liability to you shall not exceed the amount you paid us in the 12 months prior to the claim.
            </p>
          </section>

          <section>
            <h2 className="text-lg font-semibold mb-3" style={{ color: "var(--text)" }}>Changes to These Terms</h2>
            <p className="text-sm leading-relaxed">
              We may update these terms from time to time. Continued use of Mini Mate after changes are posted constitutes acceptance of the new terms.
            </p>
          </section>

          <section>
            <h2 className="text-lg font-semibold mb-3" style={{ color: "var(--text)" }}>Contact</h2>
            <p className="text-sm leading-relaxed">
              Questions? Email{" "}
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
          <Link href="/privacy" className="hover:opacity-70 transition-opacity">Privacy Policy</Link>
        </div>
        <p>© {new Date().getFullYear()} Mini Mate. All rights reserved.</p>
      </footer>
    </div>
  );
}
