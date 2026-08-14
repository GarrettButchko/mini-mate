"use client";
import { useEffect, useState } from "react";
import Link from "next/link";
import { AnimatePresence, motion } from "framer-motion";
import { MapPin, Plus, Lock, ArrowRight, CheckCircle, Sparkles } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { useAdminCourses } from "@/lib/useAdminCourses";
import { TIER_LABEL } from "@/lib/course";
import PageShell from "@/components/PageShell";
import SiteNav from "@/components/SiteNav";
import SiteFooter from "@/components/SiteFooter";
import { OverlayCard } from "@/components/OverlayCard";
import MiniMateLogo from "@/components/MiniMateLogo";
import { LogoLoadingView } from "@/components/profile/LogoLoadingView";

const PURPLE = "#AF52DE";
const BLUE = "#0088FF";
const RED = "#FF383C";
const GREEN = "#34C759";

const FAILED_LIMIT = 5;
const COOLDOWN_SECS = 30;

export default function ManageCoursesPage() {
  const { user, firestoreUser, loading: authLoading } = useAuth();
  const { courses, loading: coursesLoading } = useAdminCourses(
    user ? (firestoreUser?.adminCourses ?? []) : []
  );

  const [showSuccess, setShowSuccess] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [code, setCode] = useState("");
  const [busy, setBusy] = useState(false);
  const [banner, setBanner] = useState<{ text: string; kind: "error" | "cooldown" | "success" } | null>(null);
  const [failedAttempts, setFailedAttempts] = useState(0);
  const [cooldownEnd, setCooldownEnd] = useState<number | null>(null);
  const [secondsLeft, setSecondsLeft] = useState(0);

  useEffect(() => {
    if (!cooldownEnd) return;
    const id = setInterval(() => {
      const left = Math.max(0, Math.ceil((cooldownEnd - Date.now()) / 1000));
      setSecondsLeft(left);
      if (left === 0) {
        setCooldownEnd(null);
        setBanner(null);
        setFailedAttempts(0);
      }
    }, 1000);
    return () => clearInterval(id);
  }, [cooldownEnd]);

  const locked = cooldownEnd !== null && secondsLeft > 0;

  async function tryAdd() {
    if (!user || !code.trim()) return;
    setBusy(true);
    try {
      const res = await fetch("/api/add-course", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ password: code.trim(), uid: user.uid }),
      });
      const data = await res.json();

      if (res.ok) {
        setShowForm(false);
        setCode("");
        setFailedAttempts(0);
        setShowSuccess(true);
        return;
      }

      if (data.error === "already_added") {
        setShowForm(false);
        setCode("");
        setBanner({ text: "That course is already on your account.", kind: "error" });
        return;
      }

      setFailedAttempts((prev) => {
        const next = prev + 1;
        if (next >= FAILED_LIMIT) {
          setShowForm(false);
          setCode("");
          const end = Date.now() + COOLDOWN_SECS * 1000;
          setCooldownEnd(end);
          setSecondsLeft(COOLDOWN_SECS);
          setBanner({ text: "Too many attempts — please wait.", kind: "cooldown" });
        } else {
          setBanner({
            text: `Incorrect code. ${FAILED_LIMIT - next} attempt${FAILED_LIMIT - next === 1 ? "" : "s"} remaining.`,
            kind: "error",
          });
        }
        return next;
      });
    } catch {
      setBanner({ text: "Network error — please try again.", kind: "error" });
    } finally {
      setBusy(false);
    }
  }

  async function openSubscriptionPortal(customerId?: string | null) {
    if (!customerId) return;
    const res = await fetch("/api/portal", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ customerId }),
    });
    const data = await res.json();
    if (data.url) window.location.href = data.url;
  }

  return (
    <PageShell>
      <SiteNav logoColor={PURPLE}>
        <Link href="/for-courses" className="px-3 py-1.5 text-sm font-medium rounded-full hover:opacity-70 transition-opacity" style={{ color: "var(--text-sub)" }}>
          For Courses
        </Link>
      </SiteNav>

      <section className="max-w-2xl mx-auto w-full px-6 py-16 flex-1">

        {/* Header */}
        <div className="mb-10">
          <h1 className="text-3xl font-bold mb-2" style={{ color: "var(--text)" }}>Manage Courses</h1>
          <p className="text-sm" style={{ color: "var(--text-muted)" }}>
            Add and view the courses tied to your account.
          </p>
        </div>

        {/* Auth gate */}
        {authLoading ? null : !user ? (
          <div className="glass-card p-8 text-center">
            <p className="font-semibold mb-1" style={{ color: "var(--text)" }}>Sign in to manage your courses</p>
            <p className="text-sm mb-6" style={{ color: "var(--text-muted)" }}>Create an account or sign in to view and manage your course listings.</p>
            <Link
              href="/signin"
              onClick={() => sessionStorage.setItem("signinReturnTo", "/manage-courses")}
              className="inline-block rounded-full px-6 py-3 text-sm font-semibold text-white hover:opacity-90 hover:scale-105 active:scale-95 transition-all duration-200"
              style={{ background: PURPLE }}
            >
              Sign In
            </Link>
          </div>
        ) : (
          <div className="space-y-4">

            {/* Banner */}
            <AnimatePresence>
              {banner && (
                <motion.div
                  key="banner"
                  initial={{ opacity: 0, y: -6 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -6 }}
                  transition={{ duration: 0.2 }}
                  className="rounded-2xl px-5 py-4 flex items-start gap-3"
                  style={{
                    background: banner.kind === "success" ? `${GREEN}18` : banner.kind === "cooldown" ? `${BLUE}18` : `${RED}18`,
                  }}
                >
                  {banner.kind === "success" && <CheckCircle size={16} color={GREEN} style={{ flexShrink: 0, marginTop: 1 }} />}
                  {banner.kind === "cooldown" && <Lock size={16} color={BLUE} style={{ flexShrink: 0, marginTop: 1 }} />}
                  <div>
                    <p className="text-sm font-semibold" style={{ color: banner.kind === "success" ? GREEN : banner.kind === "cooldown" ? BLUE : RED }}>
                      {banner.text}
                    </p>
                    {banner.kind === "cooldown" && secondsLeft > 0 && (
                      <p className="text-xs mt-0.5" style={{ color: "var(--text-muted)" }}>Try again in {secondsLeft}s</p>
                    )}
                  </div>
                </motion.div>
              )}
            </AnimatePresence>

            {/* Course list */}
            {coursesLoading && courses.length === 0 ? (
              <div className="space-y-3">
                {[1, 2].map((i) => (
                  <div key={i} className="glass-card p-5 flex items-center gap-4">
                    {/* Icon */}
                    <div style={{ width: 44, height: 44, borderRadius: 12, background: "var(--surface-two)", flexShrink: 0 }} />
                    {/* Details */}
                    <div className="flex-1 min-w-0 space-y-2">
                      <div style={{ height: 14, width: "50%", borderRadius: 6, background: "var(--surface-two)" }} />
                      <div style={{ height: 12, width: "30%", borderRadius: 6, background: "var(--surface-two)" }} />
                    </div>
                    {/* Tier badge pill */}
                    <div style={{ width: 60, height: 22, borderRadius: 999, background: "var(--surface-two)", flexShrink: 0 }} />
                  </div>
                ))}
              </div>
            ) : courses.length > 0 ? (
              <div className="space-y-3">
                {courses.map((course) => {
                  const tierLabel = TIER_LABEL[course.tier] ?? "Listed";
                  return (
                    <div key={course.id} className="glass-card p-5 items-center gap-4">
                      <div className="flex flex-row gap-4 items-center w-full">
                        {/* Icon */}
                        <div style={{ width: 44, height: 44, borderRadius: 12, flexShrink: 0, background: `${PURPLE}18`, display: "flex", alignItems: "center", justifyContent: "center" }}>
                          <MapPin size={20} color={PURPLE} />
                        </div>

                        {/* Details */}
                        <div className="flex-1 min-w-0">
                          <p className="font-semibold text-sm truncate" style={{ color: "var(--text)" }}>{course.name}</p>
                          <p className="text-xs mt-0.5" style={{ color: "var(--text-muted)" }}>
                            {course.numHoles ?? 18} holes
                            {course.isClaimed ? " · Claimed" : " · Unclaimed"}
                          </p>
                        </div>

                        {/* Tier badge + subscription management */}
                        <span style={{ flexShrink: 0, fontSize: 11, fontWeight: 700, letterSpacing: "0.04em", padding: "4px 10px", borderRadius: 999, background: `${PURPLE}18`, color: PURPLE }}>
                          {tierLabel}
                        </span>




                      </div>

                      <div className="flex flex-row gap-3 mt-4">
                        {course.stripeCustomerId && (
                          <button
                            onClick={() => openSubscriptionPortal(course.stripeCustomerId)}
                            className="flex-1 rounded-2xl py-2.5 text-sm font-semibold flex items-center justify-center gap-2 transition-opacity hover:opacity-70"
                            style={{ background: "var(--surface-two)", color: PURPLE, cursor: "pointer", border: "none" }}
                          >
                            Manage subscription
                          </button>
                        )}
                        {course.tier < 3 && (
                          <Link
                            href="/pricing"
                            className="hero-gradient-courses flex-1 rounded-2xl py-2.5 text-sm font-semibold flex items-center justify-center gap-2 transition-opacity hover:opacity-90"
                            style={{ color: "#fff", textDecoration: "none" }}
                          >
                            <Sparkles size={14} fill="currentColor" />
                            Upgrade
                          </Link>
                        )}
                      </div>


                    </div>
                  );
                })}
              </div>
            ) : (
              <div className="glass-card p-8 text-center">
                <p className="font-semibold mb-1" style={{ color: "var(--text)" }}>No courses yet</p>
                <p className="text-sm" style={{ color: "var(--text-muted)" }}>Add your first course below.</p>
              </div>
            )}



            {/* Add button + get password link */}
            <div className="flex flex-row gap-3">
              <button
                onClick={() => { setShowForm(true); setBanner(null); setCode(""); }}
                disabled={locked}
                className="flex-1 rounded-2xl py-3.5 text-sm font-semibold flex items-center justify-center gap-2 transition-all hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed"
                style={{ background: locked ? "var(--surface-two)" : PURPLE, color: locked ? "var(--text-muted)" : "#fff" }}
              >
                {locked ? <Lock size={14} /> : <Plus size={14} />}
                {locked ? `Wait ${secondsLeft}s` : "Add a course"}
              </button>

              <Link
                href="/find-course"
                className="flex-1 rounded-2xl py-3.5 text-sm font-semibold flex items-center justify-center gap-2 transition-opacity hover:opacity-70"
                style={{ background: "var(--surface)", color: PURPLE, boxShadow: "var(--shadow)" }}
              >
                Get a code
                <ArrowRight size={15} style={{ color: PURPLE }} />
              </Link>
            </div>

            {/* Course password / loading / success overlays */}
            {/* Kept mounted so the shared OverlayCard enter/exit animations play;
                pointer-events is off so the empty wrapper never blocks the page. */}
            <div style={{ position: "fixed", inset: 0, zIndex: 50, pointerEvents: "none" }}>
              {/* Add course form */}
              <OverlayCard open={showForm && !busy} maxWidth={360} backdropRadius={0}>
                <p style={{ margin: "0 0 4px", fontSize: 13, fontWeight: 600, color: "var(--text)" }}>Enter your course code</p>
                <p style={{ margin: "0 0 12px", fontSize: 12, color: "var(--text-muted)" }}>We sent this code to the contact on file for your course.</p>
                <div style={{ display: "flex", alignItems: "center", gap: 10, padding: "0 12px", height: 46, borderRadius: 14, background: "var(--surface-two)", marginBottom: 12 }}>
                  <Lock size={14} style={{ color: "var(--text-muted)", flexShrink: 0 }} />
                  <input
                    autoFocus
                    type="text"
                    value={code}
                    onChange={(e) => setCode(e.target.value)}
                    onKeyDown={(e) => { if (e.key === "Enter" && code.trim() && !busy) tryAdd(); }}
                    placeholder="Enter password"
                    style={{ flex: 1, background: "transparent", border: "none", outline: "none", fontSize: 14, color: "var(--text)", fontFamily: "var(--font-geist-mono)", letterSpacing: "0.05em" }}
                  />
                </div>
                <div style={{ display: "flex", gap: 8 }}>
                  <button
                    onClick={() => { setShowForm(false); setCode(""); }}
                    style={{ flex: 1, height: 38, borderRadius: 9, border: "none", background: "var(--surface-two)", cursor: "pointer", fontSize: 13, fontWeight: 600, color: "var(--text-muted)" }}
                  >
                    Cancel
                  </button>
                  <button
                    onClick={tryAdd}
                    disabled={busy || !code.trim()}
                    style={{ flex: 1, height: 38, borderRadius: 9, border: "none", background: PURPLE, cursor: "pointer", fontSize: 13, fontWeight: 600, color: "#fff", opacity: busy || !code.trim() ? 0.5 : 1 }}
                  >
                    {busy ? "…" : "Add Course"}
                  </button>
                </div>
              </OverlayCard>

              {/* Loading popup while the course is being added */}
              <OverlayCard open={busy && showForm} maxWidth={360} backdropRadius={0}>
                <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 14, padding: "10px 0 6px" }}>
                  <LogoLoadingView size={25} color={PURPLE} />
                  <p style={{ margin: 0, fontSize: 12, color: "var(--text-muted)" }}>Adding…</p>
                </div>
              </OverlayCard>

              {/* Success popup after the course has been added */}
              <OverlayCard open={showSuccess} maxWidth={360} backdropRadius={0}>
                <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 12 }}>
                  <MiniMateLogo size={50} color={GREEN} />
                  <p style={{ margin: 0, fontSize: 13, fontWeight: 600, color: "var(--text)" }}>Course added!</p>
                  <button
                    onClick={() => setShowSuccess(false)}
                    style={{ width: "100%", height: 38, borderRadius: 9, border: "none", background: GREEN, cursor: "pointer", fontSize: 13, fontWeight: 600, color: "#fff" }}
                  >
                    Done
                  </button>
                </div>
              </OverlayCard>
            </div>

          </div>
        )}
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
