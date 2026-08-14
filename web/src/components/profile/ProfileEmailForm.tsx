"use client";
import { useEffect, useState } from "react";
import { ArrowLeft, Mail, Lock, MailCheck, Send } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { AnimatePresence, motion } from "framer-motion";
import { Spinner } from "@/components/Spinner";

const spring = { type: "spring" as const, stiffness: 247, damping: 24 };

export function ProfileEmailForm({ onBack, onClose, buttonRadius = 12 }: {
  onBack: () => void;
  onClose: () => void;
  buttonRadius?: number;
}) {
  const {
    signInWithEmail,
    createAccountWithEmail,
    sendVerificationEmail,
    refreshEmailVerified,
    signOutQuietly,
  } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showSignUp, setShowSignUp] = useState(false);
  const [showVerify, setShowVerify] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (!showVerify) return;
    let cancelled = false;
    const id = setInterval(async () => {
      const verified = await refreshEmailVerified();
      if (cancelled) return;
      if (verified) {
        await signOutQuietly();
        if (cancelled) return;
        resetToLogin();
      }
    }, 3000);
    return () => {
      cancelled = true;
      clearInterval(id);
    };
  }, [showVerify, refreshEmailVerified, signOutQuietly]);

  function resetToLogin() {
    setShowVerify(false);
    setShowSignUp(false);
    setEmail("");
    setPassword("");
    setConfirmPassword("");
    setError(null);
    setNotice(null);
  }

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (showSignUp && password !== confirmPassword) { setError("Passwords don't match."); return; }
    setBusy(true); setError(null); setNotice(null);
    try {
      if (!showSignUp) {
        await signInWithEmail(email, password);
        const verified = await refreshEmailVerified();
        if (!verified) {
          await sendVerificationEmail();
          setShowVerify(true);
        } else {
          onClose();
        }
      } else {
        await createAccountWithEmail(email, password);
        await sendVerificationEmail();
        setShowVerify(true);
      }
    } catch (e: unknown) { setError((e as Error).message); }
    finally { setBusy(false); }
  }

  async function doneVerifying() {
    setBusy(true); setError(null); setNotice(null);
    const verified = await refreshEmailVerified();
    if (verified) {
      await signOutQuietly();
      resetToLogin();
    } else {
      setError("Not verified yet — check your inbox and try again.");
    }
    setBusy(false);
  }

  async function resendEmail() {
    setError(null);
    try {
      await sendVerificationEmail();
      setNotice("Verification email sent.");
    } catch (e: unknown) { setError((e as Error).message); }
  }

  return (
    <div style={{ padding: 16 }}>
      <button
        onClick={onBack}
        style={{ width: 36, height: 36, borderRadius: "50%", background: "var(--glass-bg)", backdropFilter: "blur(12px)", WebkitBackdropFilter: "blur(12px)", boxShadow: "var(--shadow)", border: "none", cursor: "pointer", display: "flex", alignItems: "center", justifyContent: "center", marginBottom: 14 }}
        aria-label="Back"
      >
        <ArrowLeft size={16} color="#AF52DE" />
      </button>

      <AnimatePresence mode="wait" initial={false}>
        {showVerify ? (
          <motion.div
            key="verify"
            initial={{ opacity: 0, y: -24 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -24 }}
            transition={spring}
            style={{ display: "flex", flexDirection: "column", gap: 14, alignItems: "center", textAlign: "center" }}
          >
            <MailCheck size={44} color="#AF52DE" />
            <p style={{ margin: 0, fontSize: 22, fontWeight: 600, color: "var(--text)" }}>Please Verify Your Email</p>
            <p style={{ margin: 0, fontSize: 14, fontWeight: 300, color: "var(--text-muted)" }}>
              We sent a verification link to {email || "your email"}. Verify it, then come back here to continue.
            </p>

            {error && <p style={{ margin: 0, fontSize: 12, color: "#FF383C" }}>{error}</p>}
            {notice && <p style={{ margin: 0, fontSize: 12, color: "#34C759" }}>{notice}</p>}

            <button
              type="button"
              onClick={doneVerifying}
              disabled={busy}
              style={{ width: 150, height: 50, borderRadius: buttonRadius, border: "none", background: "#AF52DE", cursor: "pointer", fontSize: 16, fontWeight: 600, color: "#fff", opacity: busy ? 0.5 : 1, display: "flex", alignItems: "center", justifyContent: "center" }}
            >
              {busy ? <Spinner size={16} /> : "Done Verifying"}
            </button>

            <button
              type="button"
              onClick={resendEmail}
              style={{ background: "none", border: "none", cursor: "pointer", fontSize: 12, fontWeight: 600, color: "#AF52DE", display: "flex", alignItems: "center", gap: 4 }}
            >
              <Send size={12} /> Resend Email
            </button>
          </motion.div>
        ) : (
          <motion.div
            key="form"
            initial={{ opacity: 0, y: -24 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -24 }}
            transition={spring}
          >
            <p style={{ margin: "0 0 2px", fontSize: 28, fontWeight: 700, color: "var(--text)", lineHeight: 1.15 }}>
              {showSignUp ? "Sign Up" : "Login"}
            </p>
            <p style={{ margin: "0 0 18px", fontSize: 14, color: "var(--text-muted)", fontWeight: 300 }}>
              {"Enter email and password"}
            </p>
            <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: 14 }}>
            <FormField label="Email" type="email" placeholder="example@example.com" value={email} onChange={setEmail} icon={Mail} required />
            <FormField label="Password" type="password" placeholder="••••••" value={password} onChange={setPassword} icon={Lock} required />

            <AnimatePresence>
              {showSignUp && (
                <motion.div
                  key="confirm"
                  initial={{ opacity: 0, y: -24 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -24 }}
                  transition={spring}
                >
                  <FormField label="Confirm Password" type="password" placeholder="••••••" value={confirmPassword} onChange={setConfirmPassword} icon={Lock} required={showSignUp} />
                </motion.div>
              )}
            </AnimatePresence>

            {error && <p style={{ margin: 0, fontSize: 12, color: "#FF383C" }}>{error}</p>}

            <div style={{ display: "flex", justifyContent: "center", paddingTop: 2 }}>
              <button
                type="submit"
                disabled={busy}
                style={{ width: 150, height: 50, borderRadius: buttonRadius, border: "none", background: "#AF52DE", cursor: "pointer", fontSize: 16, fontWeight: 600, color: "#fff", opacity: busy ? 0.5 : 1, display: "flex", alignItems: "center", justifyContent: "center" }}
              >
                {busy ? <Spinner size={16} /> : showSignUp ? "Sign Up" : "Login"}
              </button>
            </div>

            <button
              type="button"
              onClick={() => {
                if (showSignUp) { setShowSignUp(false); setConfirmPassword(""); setError(null); setNotice(null); }
                else { setShowSignUp(true); setError(null); setNotice(null); }
              }}
              style={{ background: "none", border: "none", cursor: "pointer", fontSize: 12, color: "var(--text-muted)", padding: "0 0 4px" }}
            >
              {showSignUp ? (
                <>
                  <span>Already have an account? </span>
                  <span style={{ color: "#AF52DE", fontWeight: 600 }}>Login</span>
                </>
              ) : (
                <>
                  <span>{"Don't have an account? "}</span>
                  <span style={{ color: "#AF52DE", fontWeight: 600 }}>Sign Up</span>
                </>
              )}
            </button>
            </form>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}

function FormField({ label, type, placeholder, value, onChange, icon: Icon, required = false }: {
  label: string;
  type: string;
  placeholder: string;
  value: string;
  onChange: (v: string) => void;
  icon: React.ElementType;
  required?: boolean;
}) {
  return (
    <div>
      <p style={{ margin: "0 0 6px", fontSize: 13, color: "var(--text-muted)" }}>{label}</p>
      <div style={{ display: "flex", alignItems: "center", gap: 10, padding: "12px 14px", borderRadius: 12, background: "var(--glass-bg)", boxShadow: "var(--shadow)", backdropFilter: "blur(12px)", WebkitBackdropFilter: "blur(12px)" }}>
        <Icon size={15} color="var(--text-muted)" style={{ flexShrink: 0 }} />
        <input
          type={type} placeholder={placeholder} value={value} required={required}
          onChange={(e) => onChange(e.target.value)}
          style={{ flex: 1, background: "transparent", border: "none", outline: "none", fontSize: 14, color: "var(--text)" }}
        />
      </div>
    </div>
  );
}
