import Link from "next/link";
import { Mail, MessageSquare, MapPin, Clock } from "lucide-react";
import PageShell from "@/components/PageShell";
import SiteNav from "@/components/SiteNav";
import SiteFooter from "@/components/SiteFooter";
import { FadeUp } from "@/components/FadeUp";

const methodConfig = {
  email:   { icon: Mail,          heading: "Check your email",    body: "We sent your course code to the email address on file with Foursquare." },
  sms:     { icon: MessageSquare, heading: "Check your messages", body: "We texted your course code to the phone number on file with Foursquare." },
  mail:    { icon: MapPin,        heading: "Watch your mailbox",  body: "We're mailing your course code to the address on file. Expect it in 3–5 business days." },
  pending: { icon: Clock,         heading: "We'll be in touch",   body: "We weren't able to reach you automatically — we'll contact you shortly." },
};

const STEPS = [
  "Sign in to Mini Mate",
  "Open Manage Courses",
  'Tap "Add a course" and enter your code',
  "Your course goes live on the map",
];

export default async function SuccessPage({ searchParams }: { searchParams: Promise<{ method?: string; course?: string }> }) {
  const { method: rawMethod, course } = await searchParams;
  const method = (rawMethod ?? "email") as keyof typeof methodConfig;
  const config = methodConfig[method] ?? methodConfig.email;
  const Icon = config.icon;
  const courseName = course ? decodeURIComponent(course) : null;

  return (
    <PageShell>
      <SiteNav logoColor="#AF52DE" />

      <section className="flex-1 flex items-center justify-center px-5 py-16">
        <FadeUp className="w-full" style={{ maxWidth: 440 }}>

          {/* Check badge */}
          <div style={{ display: "flex", justifyContent: "center", marginBottom: 24 }}>
            <div style={{
              width: 80,
              height: 80,
              borderRadius: "50%",
              background: "rgba(175,82,222,0.12)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
            }}>
              {/* Checkmark SVG */}
              <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
                <circle cx="20" cy="20" r="20" fill="rgba(175,82,222,0.15)" />
                <path d="M11 21l6.5 6.5L29 13" stroke="#AF52DE" strokeWidth="2.8" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            </div>
          </div>

          {/* Title */}
          <h1 style={{ margin: "0 0 6px", fontSize: 30, fontWeight: 700, color: "var(--text)", textAlign: "center", letterSpacing: "-0.3px" }}>
            You&rsquo;re all set
          </h1>
          {courseName && (
            <p style={{ margin: "0 0 28px", fontSize: 15, color: "var(--text-muted)", textAlign: "center" }}>
              {courseName}
            </p>
          )}
          {!courseName && <div style={{ marginBottom: 28 }} />}

          {/* Cards */}
          <div style={{ display: "flex", flexDirection: "column", gap: 10, marginBottom: 20 }}>

            {/* Delivery method */}
            <div className="glass-card" style={{ padding: 20, display: "flex", alignItems: "flex-start", gap: 14 }}>
              <div style={{
                width: 40,
                height: 40,
                borderRadius: 12,
                background: "rgba(175,82,222,0.10)",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                flexShrink: 0,
              }}>
                <Icon size={19} style={{ color: "#AF52DE" }} />
              </div>
              <div>
                <p style={{ margin: "0 0 3px", fontSize: 14, fontWeight: 600, color: "var(--text)" }}>{config.heading}</p>
                <p style={{ margin: 0, fontSize: 13, lineHeight: 1.55, color: "var(--text-muted)" }}>{config.body}</p>
              </div>
            </div>

            {/* Next steps */}
            <div className="glass-card" style={{ padding: 20 }}>
              <p style={{ margin: "0 0 14px", fontSize: 12, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.06em", color: "var(--text-muted)" }}>
                Next steps
              </p>
              <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
                {STEPS.map((step, i) => (
                  <div key={i} style={{ display: "flex", alignItems: "center", gap: 12 }}>
                    <span style={{
                      flexShrink: 0,
                      width: 24,
                      height: 24,
                      borderRadius: "50%",
                      background: "rgba(175,82,222,0.12)",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      fontSize: 11,
                      fontWeight: 700,
                      color: "#AF52DE",
                    }}>
                      {i + 1}
                    </span>
                    <span style={{ fontSize: 13, color: "var(--text-sub)" }}>{step}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Actions */}
          <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            <Link
              href="/manage-courses"
              className="hover:opacity-90 hover:scale-[1.02] active:scale-[0.98] transition-all duration-200"
              style={{
                display: "block",
                textAlign: "center",
                background: "#AF52DE",
                color: "white",
                borderRadius: 9999,
                height: 50,
                lineHeight: "50px",
                fontSize: 15,
                fontWeight: 600,
                textDecoration: "none",
              }}
            >
              Go to Manage Courses
            </Link>
            <Link
              href="/for-courses"
              className="hover:opacity-70 transition-opacity"
              style={{
                display: "block",
                textAlign: "center",
                color: "var(--text-muted)",
                height: 44,
                lineHeight: "44px",
                fontSize: 13,
                fontWeight: 500,
                textDecoration: "none",
              }}
            >
              Back to Mini Mate for Courses
            </Link>
          </div>

        </FadeUp>
      </section>

      <SiteFooter links={[
        { href: "/", label: "For Players" },
        { href: "/for-courses", label: "For Courses" },
        { href: "/privacy", label: "Privacy Policy" },
        { href: "/terms", label: "Terms" },
      ]} />
    </PageShell>
  );
}
