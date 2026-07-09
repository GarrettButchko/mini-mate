"use client";
import { useState } from "react";
import Link from "next/link";
import { Mail } from "lucide-react";
import PageShell from "@/components/PageShell";
import SiteNav from "@/components/SiteNav";

export default function ManagePage() {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);

    const res = await fetch("/api/portal", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email }),
    });

    const data = await res.json();
    setLoading(false);

    if (!res.ok) {
      setError(data.error ?? "Something went wrong.");
      return;
    }

    window.location.href = data.url;
  }

  return (
    <PageShell>
      <SiteNav logoColor="#AF52DE" wrapChildren={false}>
        <Link href="/pricing" className="rounded-full px-4 py-2.5 text-base text-white font-semibold hover:opacity-90 transition-all duration-200" style={{ background: "#AF52DE" }}>
          Course Pricing
        </Link>
      </SiteNav>

      <section className="min-h-screen flex items-center justify-center px-6 py-24">
        <div className="glass-card p-10 max-w-md w-full animate-fade-in-up">
          <h1 className="text-2xl font-bold mb-2" style={{ color: "var(--text)" }}>
            Manage your subscription
          </h1>
          <p className="text-sm mb-8" style={{ color: "var(--text-muted)" }}>
            Enter the email you used when subscribing and we&apos;ll take you to your billing portal.
          </p>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div
              className="flex items-center gap-3 px-4"
              style={{ height: 50, borderRadius: 25, background: "var(--surface-two)" }}
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

            {error && (
              <p className="text-sm px-1" style={{ color: "#FF383C" }}>{error}</p>
            )}

            <button
              type="submit"
              disabled={loading || !email}
              className="w-full rounded-full py-3.5 text-white font-semibold hover:opacity-90 hover:scale-105 active:scale-95 disabled:opacity-50 disabled:hover:scale-100 transition-all duration-200"
              style={{ background: "#AF52DE" }}
            >
              {loading ? "Looking up your account…" : "Go to billing portal"}
            </button>
          </form>

          <p className="text-xs mt-6 text-center" style={{ color: "var(--text-muted)" }}>
            Don&apos;t have a subscription yet?{" "}
            <Link href="/courses" style={{ color: "#AF52DE" }}>Find your course</Link>
          </p>
        </div>
      </section>
    </PageShell>
  );
}
