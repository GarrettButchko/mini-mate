"use client";
import { useState } from "react";
import Link from "next/link";
import { Mail, ArrowUpCircle } from "lucide-react";
import { FadeUp } from "@/components/FadeUp";
import PageShell from "@/components/PageShell";
import SiteNav from "@/components/SiteNav";
import SiteFooter from "@/components/SiteFooter";

type State = "idle" | "loading" | "free-tier" | "error";

export default function ManagePage() {
  const [email, setEmail] = useState("");
  const [state, setState] = useState<State>("idle");
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setState("loading");
    setError(null);

    const res = await fetch("/api/portal", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email }),
    });

    const data = await res.json();

    if (data.freeTier) {
      setState("free-tier");
      return;
    }

    if (!res.ok) {
      setState("error");
      setError(data.error ?? "Something went wrong.");
      return;
    }

    window.location.href = data.url;
  }

  return (
    <PageShell>
      <SiteNav logoColor="#AF52DE" />

      <section className="min-h-screen flex items-center justify-center px-6 py-24">
        <FadeUp className="glass-card p-10 max-w-md w-full">

          {state === "free-tier" ? (
            <>
              <div className="flex justify-center mb-6">
                <div className="rounded-full p-4" style={{ background: "rgba(175,82,222,0.12)" }}>
                  <ArrowUpCircle size={36} style={{ color: "#AF52DE" }} />
                </div>
              </div>
              <h1 className="text-2xl font-bold mb-2 text-center" style={{ color: "var(--text)" }}>
                You&apos;re on the free plan
              </h1>
              <p className="text-sm mb-8 text-center" style={{ color: "var(--text-muted)" }}>
                You&apos;re currently on the Listed (free) tier. View pricing below and find your course to upgrade.
              </p>
              <div className="space-y-3">
                <Link
                  href="/pricing"
                  className="w-full rounded-xl py-3.5 text-white font-semibold text-center block hover:opacity-90 hover:scale-105 active:scale-95 transition-all duration-200"
                  style={{ background: "#AF52DE" }}
                >
                  View plans & upgrade
                </Link>
                <button
                  onClick={() => { setState("idle"); setEmail(""); }}
                  className="w-full rounded-xl py-3 text-sm font-semibold text-center hover:opacity-70 transition-opacity"
                  style={{ color: "var(--text-muted)" }}
                >
                  Try a different email
                </button>
              </div>
            </>
          ) : (
            <>
              <h1 className="text-2xl font-bold mb-2" style={{ color: "var(--text)" }}>
                Manage your subscription
              </h1>
              <p className="text-sm mb-8" style={{ color: "var(--text-muted)" }}>
                Enter the email you used when subscribing and we&apos;ll take you to your billing portal.
              </p>

              <form onSubmit={handleSubmit} className="space-y-4">
                <div
                  className="flex items-center gap-3 px-4"
                  style={{ height: 50, borderRadius: 12, background: "var(--surface-two)" }}
                >
                  <Mail size={16} style={{ color: "var(--text-muted)", flexShrink: 0 }} />
                  <input
                    type="email"
                    placeholder="your@email.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    className="flex-1 bg-transparent text-sm focus:outline-none"
                    style={{ color: "var(--text)" }}
                  />
                </div>

                {state === "error" && error && (
                  <p className="text-sm px-1" style={{ color: "#FF383C" }}>{error}</p>
                )}

                <button
                  type="submit"
                  disabled={state === "loading" || !email}
                  className="w-full rounded-xl py-3.5 text-white font-semibold hover:opacity-90 hover:scale-105 active:scale-95 disabled:opacity-50 disabled:hover:scale-100 transition-all duration-200"
                  style={{ background: "#AF52DE" }}
                >
                  {state === "loading" ? "Looking up your account…" : "Go to billing portal"}
                </button>
              </form>

              <p className="text-xs mt-6 text-center" style={{ color: "var(--text-muted)" }}>
                Don&apos;t have a subscription yet?{" "}
                <Link href="/find-course" style={{ color: "#AF52DE" }}>Find your course</Link>
              </p>
            </>
          )}
        </FadeUp>
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
