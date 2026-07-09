import Link from "next/link";
import { CheckCircle, Mail, MessageSquare, MapPin, Clock } from "lucide-react";
import PageShell from "@/components/PageShell";
import SiteNav from "@/components/SiteNav";
import SiteFooter from "@/components/SiteFooter";

const methodConfig = {
  email: { icon: Mail, heading: "Check your email", body: "We sent your course code to the email address on file with Foursquare. Open it and follow the instructions inside." },
  sms:   { icon: MessageSquare, heading: "Check your messages", body: "We texted your course code to the phone number on file with Foursquare." },
  mail:  { icon: MapPin, heading: "Watch your mailbox", body: "We're mailing your course code to the address on file with Foursquare. Expect it in 3–5 business days." },
  pending: { icon: Clock, heading: "We'll be in touch", body: "We weren't able to reach you automatically — we'll contact you via the information Foursquare has on file shortly." },
};

export default async function SuccessPage({ searchParams }: { searchParams: Promise<{ method?: string; course?: string }> }) {
  const { method: rawMethod, course } = await searchParams;
  const method = (rawMethod ?? "email") as keyof typeof methodConfig;
  const config = methodConfig[method] ?? methodConfig.email;
  const Icon = config.icon;

  return (
    <PageShell>
      <SiteNav logoColor="#AF52DE" wrapChildren={false}>
        <Link href="/pricing" className="rounded-full px-4 py-2.5 text-base text-white font-semibold hover:opacity-90 transition-all duration-200" style={{ background: "#AF52DE" }}>
          Course Pricing
        </Link>
      </SiteNav>

      <section className="min-h-screen flex items-center justify-center px-6 py-24">
        <div className="glass-card p-10 max-w-md w-full text-center animate-fade-in-up">
          <div className="flex justify-center mb-6">
            <div className="rounded-full p-4" style={{ background: "rgba(175,82,222,0.12)" }}>
              <CheckCircle size={36} style={{ color: "#AF52DE" }} />
            </div>
          </div>

          <h1 className="text-2xl font-bold mb-2" style={{ color: "var(--text)" }}>
            Code sent!
          </h1>
          {course && (
            <p className="text-sm mb-6" style={{ color: "var(--text-muted)" }}>
              {decodeURIComponent(course)}
            </p>
          )}

          <div className="rounded-[20px] p-5 mb-8 text-left" style={{ background: "var(--surface-two)" }}>
            <div className="flex items-start gap-3">
              <Icon size={20} style={{ color: "#AF52DE", flexShrink: 0, marginTop: 2 }} />
              <div>
                <p className="font-semibold text-sm mb-1" style={{ color: "var(--text)" }}>{config.heading}</p>
                <p className="text-sm leading-relaxed" style={{ color: "var(--text-muted)" }}>{config.body}</p>
              </div>
            </div>
          </div>

          <div className="rounded-[20px] p-5 mb-8 text-left" style={{ background: "var(--surface-two)" }}>
            <p className="font-semibold text-sm mb-2" style={{ color: "var(--text)" }}>Next steps</p>
            <ol className="space-y-2">
              {["Download Mini Mate Manager from the App Store", 'Tap "Add a Course"', "Enter the code we sent you", "Configure your course settings in the app"].map((step, i) => (
                <li key={i} className="flex items-center gap-3 text-sm" style={{ color: "var(--text-muted)" }}>
                  <span className="flex-shrink-0 w-5 h-5 rounded-full flex items-center justify-center text-xs font-bold text-white" style={{ background: "#AF52DE" }}>{i + 1}</span>
                  {step}
                </li>
              ))}
            </ol>
          </div>

          <div className="flex flex-col gap-3">
            <Link href="/manage" className="w-full rounded-full py-3 text-sm font-semibold text-center hover:opacity-90 hover:scale-105 active:scale-95 transition-all duration-200 text-white" style={{ background: "#AF52DE" }}>
              Manage my subscription
            </Link>
            <Link href="/for-courses" className="w-full rounded-full py-3 text-sm font-semibold text-center hover:opacity-70 transition-all duration-200" style={{ color: "var(--text-muted)" }}>
              Back to for course owners
            </Link>
          </div>
        </div>
      </section>

      <SiteFooter links={[
        { href: "/", label: "For Players" },
        { href: "/for-courses", label: "For Courses" },
        { href: "/pricing", label: "Pricing" },
        { href: "/privacy", label: "Privacy Policy" },
        { href: "/terms", label: "Terms" },
        { href: "mailto:garrettwm2005@gmail.com", label: "Support" },
      ]} />
    </PageShell>
  );
}
