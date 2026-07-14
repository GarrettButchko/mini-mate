"use client";
import React, { useEffect, useState, Suspense } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Mail, Phone, Globe, Send, MapPin, ArrowLeft } from "lucide-react";
import type { MapItemDTO } from "@/lib/foursquare";
import { resolveContactRoute, getAllContactRoutes } from "@/lib/contactRouter";
import PageShell from "@/components/PageShell";
import SiteNav from "@/components/SiteNav";
import SiteFooter from "@/components/SiteFooter";

type TierId = "listed" | "partner" | "analytics";

const TIER_LABELS: Record<TierId, string> = {
  listed: "Listed — Free",
  partner: "Partner — $29/mo",
  analytics: "Analytics — $69/mo",
};

const methodIcons: Record<string, React.ReactElement> = {
  email: <Mail size={16} />,
  phone: <Phone size={16} />,
  website: <Globe size={16} />,
  mail: <Send size={16} />,
};

function VerifyContent() {
  const searchParams = useSearchParams();
  const courseId = searchParams.get("courseId");
  const courseName = searchParams.get("courseName");
  const tier = searchParams.get("tier") as TierId | null;

  const [course, setCourse] = useState<MapItemDTO | null>(null);
  const [mounted, setMounted] = useState(false);
  const [claiming, setClaiming] = useState(false);
  const [claimError, setClaimError] = useState<string | null>(null);

  useEffect(() => {
    setMounted(true);
    if (!courseId) return;
    const stored = sessionStorage.getItem(`course_${courseId}`);
    if (stored) {
      try { setCourse(JSON.parse(stored)); } catch {}
    }
  }, [courseId]);

  if (!mounted) return null;

  if (!courseId || !courseName || !tier || !TIER_LABELS[tier]) {
    return (
      <div className="flex-1 flex items-center justify-center px-6 py-24 text-center">
        <div>
          <p className="text-lg font-semibold mb-4" style={{ color: "var(--text)" }}>Something went wrong</p>
          <Link href="/find-course" className="text-sm font-semibold" style={{ color: "#AF52DE" }}>Start over</Link>
        </div>
      </div>
    );
  }

  if (!course) {
    return (
      <div className="flex-1 flex items-center justify-center px-6 py-24 text-center">
        <div>
          <p className="text-sm mb-4" style={{ color: "var(--text-muted)" }}>Session expired. Please select your course again.</p>
          <Link href="/find-course" className="text-sm font-semibold" style={{ color: "#AF52DE" }}>Find your course</Link>
        </div>
      </div>
    );
  }

  const contact = {
    email: course.email,
    phone: course.phoneNumber,
    website: course.url,
    address: course.address.fullAddress,
  };
  const primary = resolveContactRoute(contact);
  const all = getAllContactRoutes(contact);
  const decodedName = decodeURIComponent(courseName);

  async function handleConfirm() {
    setClaiming(true);
    setClaimError(null);

    const body = {
      placeId: course!.placeID,
      courseName: course!.name,
      contact: {
        email: course!.email,
        phoneNumber: course!.phoneNumber,
        url: course!.url,
        address: course!.address,
        coordinate: course!.coordinate,
      },
    };

    if (tier === "listed") {
      const res = await fetch("/api/claim-course", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
      const data = await res.json();
      setClaiming(false);
      if (!res.ok) { setClaimError(data.error ?? "Something went wrong."); return; }
      window.location.href = `/find-course/success?method=${data.method ?? "pending"}&course=${encodeURIComponent(course!.name)}`;
    } else {
      const res = await fetch("/api/checkout", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...body, tier }),
      });
      const data = await res.json();
      setClaiming(false);
      if (!res.ok) { setClaimError(data.error ?? "Something went wrong."); return; }
      window.location.href = data.url;
    }
  }

  return (
    <section className="max-w-xl mx-auto w-full px-6 py-20">
      {/* Back button */}
      <Link
        href={`/pricing?courseId=${encodeURIComponent(courseId)}&courseName=${encodeURIComponent(courseName)}`}
        className="inline-flex items-center gap-2 mb-5 hover:opacity-70 active:scale-95 transition-all duration-200"
        style={{
          background: "var(--glass-bg)",
          backdropFilter: "blur(20px)",
          WebkitBackdropFilter: "blur(20px)",
          boxShadow: "var(--shadow)",
          borderRadius: 9999,
          height: 40,
          padding: "0 16px 0 12px",
          fontSize: 14,
          fontWeight: 600,
          color: "var(--text-sub)",
          textDecoration: "none",
        }}
      >
        <ArrowLeft size={15} style={{ color: "var(--text-muted)" }} />
        Back to plans
      </Link>

      {/* Course + tier summary */}
      <div
        className="glass-card p-4 mb-5 flex items-center gap-3"
      >
        <div
          style={{ width: 40, height: 40, borderRadius: 12, background: "rgba(175,82,222,0.12)", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}
        >
          <MapPin size={18} style={{ color: "#AF52DE" }} />
        </div>
        <div className="flex-1 min-w-0">
          <p className="font-bold text-sm truncate" style={{ color: "var(--text)" }}>{decodedName}</p>
          <p className="text-xs mt-0.5" style={{ color: "#AF52DE" }}>{TIER_LABELS[tier]}</p>
        </div>
      </div>

      {/* Main card */}
      <div className="glass-card p-8">
        <h1 className="text-2xl font-bold mb-1" style={{ color: "var(--text)" }}>Almost there</h1>
        <p className="text-sm mb-8" style={{ color: "var(--text-muted)" }}>
          We&apos;ll send a setup code to the contact on file for your course. Enter it in Mini Mate to activate your listing.
        </p>

        {/* Delivery contact */}
        <div className="mb-8">
          <p className="text-xs font-semibold uppercase tracking-wide mb-3" style={{ color: "var(--text-muted)" }}>
            {primary ? "Code will be sent to" : "No digital contact found — code will be mailed to"}
          </p>
          {primary ? (
            <>
              <div className="flex items-center gap-3 px-4 py-3.5" style={{ background: "var(--surface-two)", borderRadius: 17 }}>
                <span style={{ color: "#AF52DE" }}>{methodIcons[primary.method]}</span>
                <span className="text-sm font-medium" style={{ color: "var(--text)" }}>{primary.value}</span>
              </div>
              {all.length > 1 && (
                <div className="mt-3">
                  <p className="text-xs mb-2" style={{ color: "var(--text-muted)" }}>All available contacts:</p>
                  <div className="flex flex-wrap gap-2">
                    {all.map((c) => (
                      <span
                        key={c.method}
                        className="flex items-center gap-1.5 px-3 py-1 text-xs"
                        style={{ background: "var(--surface-two)", color: "var(--text-muted)", borderRadius: 9999 }}
                      >
                        <span style={{ color: "#AF52DE" }}>{methodIcons[c.method]}</span>
                        {c.value}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </>
          ) : (
            <p className="text-sm font-medium" style={{ color: "var(--text)" }}>{course.address.fullAddress}</p>
          )}
        </div>

        {claimError && (
          <p className="text-sm mb-4 px-1" style={{ color: "#FF383C" }}>{claimError}</p>
        )}

        <button
          onClick={handleConfirm}
          disabled={claiming}
          className="w-full text-white font-semibold hover:opacity-90 hover:scale-105 active:scale-95 disabled:opacity-50 disabled:hover:scale-100 transition-all duration-200"
          style={{ background: "#AF52DE", borderRadius: 9999, height: 50, fontSize: 15 }}
        >
          {claiming
            ? "Setting things up…"
            : tier === "listed"
            ? "Claim free listing"
            : `Subscribe to ${tier.charAt(0).toUpperCase() + tier.slice(1)}`}
        </button>
      </div>
    </section>
  );
}

export default function VerifyPage() {
  return (
    <PageShell>
      <SiteNav logoColor="#AF52DE" />
      <Suspense fallback={<div style={{ flex: 1 }} />}>
        <VerifyContent />
      </Suspense>
      <SiteFooter links={[
        { href: "/", label: "For Players" },
        { href: "/for-courses", label: "For Courses" },
        { href: "/privacy", label: "Privacy Policy" },
        { href: "/terms", label: "Terms" },
      ]} />
    </PageShell>
  );
}
