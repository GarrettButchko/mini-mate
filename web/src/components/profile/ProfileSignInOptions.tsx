"use client";
import { useState } from "react";
import { Mail } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";

export function ProfileSignInOptions({ onEmailClick, onClose, buttonRadius = 12 }: {
  onEmailClick: () => void;
  onClose: () => void;
  buttonRadius?: number;
}) {
  const { signInWithGoogle, signInWithApple } = useAuth();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleGoogle() {
    setBusy(true); setError(null);
    try { await signInWithGoogle(); onClose(); }
    catch (e: unknown) { setError((e as Error).message); }
    finally { setBusy(false); }
  }

  async function handleApple() {
    setBusy(true); setError(null);
    try { await signInWithApple(); onClose(); }
    catch (e: unknown) { setError((e as Error).message); }
    finally { setBusy(false); }
  }

  return (
    <div style={{ padding: 16, display: "flex", flexDirection: "column", gap: 12 }}>
      <SignInButton onClick={onEmailClick} disabled={busy} background="#AF52DE" radius={buttonRadius}>
        <Mail size={17} />
        Sign in with Email
      </SignInButton>

      <SignInButton onClick={handleGoogle} disabled={busy} background="#4285F4" radius={buttonRadius}>
        <span style={{ background: "#fff", borderRadius: "50%", width: 18, height: 18, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
          <GoogleIcon />
        </span>
        Sign in with Google
      </SignInButton>

      <SignInButton onClick={handleApple} disabled={busy} background="#000" radius={buttonRadius}>
        <AppleIcon />
        Sign in with Apple
      </SignInButton>

      {error && <p style={{ margin: 0, fontSize: 12, color: "#FF383C", textAlign: "center" }}>{error}</p>}
    </div>
  );
}

function SignInButton({ onClick, disabled, background, radius, children }: {
  onClick: () => void;
  disabled: boolean;
  background: string;
  radius: number;
  children: React.ReactNode;
}) {
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      style={{
        width: "100%", height: 50, borderRadius: radius, border: "none",
        background, cursor: "pointer",
        display: "flex", alignItems: "center", justifyContent: "center", gap: 8,
        fontSize: 15, fontWeight: 600, color: "#fff",
        opacity: disabled ? 0.6 : 1,
      }}
    >
      {children}
    </button>
  );
}

function AppleIcon() {
  return (
    <svg width="16" height="19" viewBox="0 0 24 24" fill="white" xmlns="http://www.w3.org/2000/svg">
      <path d="M12.152 6.896c-.948 0-2.415-1.078-3.96-1.04-2.04.027-3.91 1.183-4.961 3.014-2.117 3.675-.54 9.103 1.519 12.09 1.013 1.46 2.208 3.09 3.792 3.039 1.52-.065 2.09-.987 3.935-.987 1.831 0 2.35.987 3.96.948 1.637-.026 2.676-1.48 3.676-2.948 1.156-1.688 1.636-3.325 1.662-3.415-.039-.013-3.182-1.221-3.22-4.857-.026-3.04 2.48-4.494 2.597-4.559-1.429-2.09-3.623-2.324-4.39-2.376-2-.156-3.675 1.09-4.61 1.09zM15.53 3.83c.843-1.012 1.4-2.427 1.245-3.83-1.207.052-2.662.805-3.532 1.818-.78.896-1.454 2.338-1.273 3.714 1.338.104 2.715-.688 3.559-1.701z"/>
    </svg>
  );
}

function GoogleIcon() {
  return (
    <svg width="11" height="11" viewBox="0 0 18 18" xmlns="http://www.w3.org/2000/svg">
      <path d="M17.64 9.205c0-.638-.057-1.252-.164-1.84H9v3.48h4.844a4.14 4.14 0 0 1-1.796 2.717v2.258h2.908c1.702-1.567 2.684-3.875 2.684-6.615z" fill="#4285F4"/>
      <path d="M9 18c2.43 0 4.467-.806 5.956-2.18l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332A8.997 8.997 0 0 0 9 18z" fill="#34A853"/>
      <path d="M3.964 10.71A5.41 5.41 0 0 1 3.682 9c0-.593.102-1.17.282-1.71V4.958H.957A8.996 8.996 0 0 0 0 9c0 1.452.348 2.827.957 4.042l3.007-2.332z" fill="#FBBC05"/>
      <path d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0A8.997 8.997 0 0 0 .957 4.958L3.964 7.29C4.672 5.163 6.656 3.58 9 3.58z" fill="#EA4335"/>
    </svg>
  );
}
