"use client";
import { useEffect, useState, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { AnimatePresence, motion } from "framer-motion";
import { useAuth } from "@/contexts/AuthContext";
import { ProfileSignInOptions } from "@/components/profile/ProfileSignInOptions";
import { ProfileEmailForm } from "@/components/profile/ProfileEmailForm";
import MiniMateLogo from "@/components/MiniMateLogo";

function SignInContent() {
  const { user, firestoreUser, loading } = useAuth();
  const router = useRouter();
  const searchParams = useSearchParams();
  const [mode, setMode] = useState<"options" | "email">("options");

  useEffect(() => {
    if (loading) return;
    if (!user) return;
    const next = searchParams.get("next");
    if (next && next.startsWith("/")) { router.replace(next); return; }
    if ((firestoreUser?.adminCourses?.length ?? 0) > 0) {
      router.replace("/manage-courses");
    } else {
      router.replace("/find-course");
    }
  }, [user, firestoreUser, loading, router, searchParams]);

  if (loading) return <div style={{ flex: 1 }} />;

  return (
    <div style={{
      flex: 1,
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      justifyContent: "center",
      padding: "32px 16px",
      minHeight: "100dvh",
    }}>
      {/* Welcome header */}
      <div style={{ width: "100%", maxWidth: 400, marginBottom: 24, padding: "0 8px" }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
          <div>
            <p style={{ margin: "0 0 2px", fontSize: 18, fontWeight: 400, color: "rgba(255,255,255,0.75)", lineHeight: 1.3 }}>
              Welcome to,
            </p>
            <h1 style={{ margin: "0 0 6px", fontSize: 44, fontWeight: 700, color: "white", lineHeight: 1.05, letterSpacing: "-0.5px" }}>
              Mini Mate
            </h1>
          </div>
          <MiniMateLogo size={68} color="rgba(255,255,255,0.9)" />
        </div>
      </div>

      {/* Glass card */}
      <div style={{
        width: "100%",
        maxWidth: 400,
        background: "var(--glass-bg)",
        backdropFilter: "blur(40px)",
        WebkitBackdropFilter: "blur(40px)",
        borderRadius: 35,
        boxShadow: "0 8px 48px rgba(0,0,0,0.35)",
        overflow: "hidden",
      }}>
        <AnimatePresence mode="wait" initial={false}>
          {mode === "options" ? (
            <motion.div
              key="options"
              initial={{ opacity: 0, x: -24 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -24 }}
              transition={{ duration: 0.2, ease: "easeOut" }}
            >
              <ProfileSignInOptions onEmailClick={() => setMode("email")} onClose={() => {}} buttonRadius={19} />
            </motion.div>
          ) : (
            <motion.div
              key="email"
              initial={{ opacity: 0, x: 24 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 24 }}
              transition={{ duration: 0.2, ease: "easeOut" }}
            >
              <ProfileEmailForm onBack={() => setMode("options")} onClose={() => {}} buttonRadius={19} />
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}

export default function SignInPage() {
  return (
    <div
      className="min-h-screen flex flex-col font-[family-name:var(--font-geist-sans)]"
      style={{
        background: "linear-gradient(-45deg, #34C759, #34C759, #34C759, #0088FF, #0088FF, #0088FF, #AF52DE, #AF52DE, #AF52DE)",
        backgroundSize: "400% 400%",
        animation: "gradient-pan 20s ease infinite",
      }}
    >
      <Suspense fallback={<div style={{ flex: 1 }} />}>
        <SignInContent />
      </Suspense>
    </div>
  );
}
