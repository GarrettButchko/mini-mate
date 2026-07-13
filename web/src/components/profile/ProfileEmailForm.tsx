"use client";
import { useState } from "react";
import { ArrowLeft, Mail, Lock } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { AnimatePresence, motion } from "framer-motion";

export function ProfileEmailForm({ onBack, onClose }: {
  onBack: () => void;
  onClose: () => void;
}) {
  const { signInWithEmail, createAccountWithEmail } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showSignUp, setShowSignUp] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (showSignUp && password !== confirmPassword) { setError("Passwords don't match."); return; }
    setBusy(true); setError(null);
    try {
      if (!showSignUp) await signInWithEmail(email, password);
      else await createAccountWithEmail(email, password);
      onClose();
    } catch (e: unknown) { setError((e as Error).message); }
    finally { setBusy(false); }
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

      <p style={{ margin: "0 0 2px", fontSize: 28, fontWeight: 700, color: "var(--text)", lineHeight: 1.15 }}>
        {showSignUp ? "Sign Up" : "Login"}
      </p>
      <p style={{ margin: "0 0 18px", fontSize: 14, color: "var(--text-muted)", fontWeight: 300 }}>
        Enter email and password
      </p>

      <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: 14 }}>
        <FormField label="Email" type="email" placeholder="example@example.com" value={email} onChange={setEmail} icon={Mail} required />
        <FormField label="Password" type="password" placeholder="••••••" value={password} onChange={setPassword} icon={Lock} required />

        <AnimatePresence>
          {showSignUp && (
            <motion.div
              key="confirm"
              initial={{ opacity: 0, x: 24 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 24 }}
              transition={{ duration: 0.18, ease: "easeOut" }}
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
            style={{ width: 150, height: 50, borderRadius: 12, border: "none", background: "#AF52DE", cursor: "pointer", fontSize: 16, fontWeight: 600, color: "#fff", opacity: busy ? 0.5 : 1 }}
          >
            {busy ? "…" : showSignUp ? "Sign Up" : "Login"}
          </button>
        </div>

        <button
          type="button"
          onClick={() => {
            if (showSignUp) { setShowSignUp(false); setConfirmPassword(""); setError(null); }
            else { setShowSignUp(true); setError(null); }
          }}
          style={{ background: "none", border: "none", cursor: "pointer", fontSize: 12, color: "var(--text-muted)", padding: "0 0 4px" }}
        >
          {showSignUp ? "Already have an account? Login" : "Don't have an account? Sign Up"}
        </button>
      </form>
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
