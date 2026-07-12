"use client";
import { useState } from "react";
import { Mail } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";

export function ProfileSignInOptions({ onEmailClick, returning, onClose }: {
  onEmailClick: () => void;
  returning: boolean;
  onClose: () => void;
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
    <div style={{ padding: 16, display: "flex", flexDirection: "column", gap: 12, animation: returning ? "slideFromLeft 0.22s ease" : undefined }}>
      <SignInButton onClick={onEmailClick} disabled={busy} background="#AF52DE">
        <Mail size={17} />
        Sign in with Email
      </SignInButton>

      <SignInButton onClick={handleGoogle} disabled={busy} background="#4285F4">
        <span style={{ background: "#fff", borderRadius: "50%", width: 22, height: 22, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
          <GoogleIcon />
        </span>
        Sign in with Google
      </SignInButton>

      <SignInButton onClick={handleApple} disabled={busy} background="#000">
        <AppleIcon />
        Sign in with Apple
      </SignInButton>

      {error && <p style={{ margin: 0, fontSize: 12, color: "#FF383C", textAlign: "center" }}>{error}</p>}
    </div>
  );
}

function SignInButton({ onClick, disabled, background, children }: {
  onClick: () => void;
  disabled: boolean;
  background: string;
  children: React.ReactNode;
}) {
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      style={{
        width: "100%", height: 50, borderRadius: 12, border: "none",
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
    <svg width="15" height="18" viewBox="0 0 15 18" fill="white" xmlns="http://www.w3.org/2000/svg">
      <path d="M12.273 9.551c-.02-2.107 1.722-3.12 1.8-3.172-0.981-1.434-2.508-1.63-3.05-1.651-1.3-.133-2.543.772-3.2.772-.664 0-1.685-.756-2.772-.735-1.42.02-2.733.833-3.463 2.112C-.014 9.472.894 13.27 2.676 15.337c.882 1.006 1.926 2.135 3.297 2.087 1.33-.053 1.83-.853 3.436-.853 1.6 0 2.058.853 3.451.822 1.428-.022 2.33-1.023 3.198-2.036.997-1.17 1.41-2.306 1.43-2.365-.03-.014-2.754-1.055-2.773-4.14l-.442-.301zM10.089 3.13c.726-.88 1.216-2.103 1.083-3.323-1.045.042-2.316.697-3.067 1.573-.669.77-1.261 2.002-1.104 3.18 1.165.09 2.361-.591 3.088-1.43z"/>
    </svg>
  );
}

function GoogleIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 18 18" xmlns="http://www.w3.org/2000/svg">
      <path d="M17.64 9.205c0-.638-.057-1.252-.164-1.84H9v3.48h4.844a4.14 4.14 0 0 1-1.796 2.717v2.258h2.908c1.702-1.567 2.684-3.875 2.684-6.615z" fill="#4285F4"/>
      <path d="M9 18c2.43 0 4.467-.806 5.956-2.18l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332A8.997 8.997 0 0 0 9 18z" fill="#34A853"/>
      <path d="M3.964 10.71A5.41 5.41 0 0 1 3.682 9c0-.593.102-1.17.282-1.71V4.958H.957A8.996 8.996 0 0 0 0 9c0 1.452.348 2.827.957 4.042l3.007-2.332z" fill="#FBBC05"/>
      <path d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0A8.997 8.997 0 0 0 .957 4.958L3.964 7.29C4.672 5.163 6.656 3.58 9 3.58z" fill="#EA4335"/>
    </svg>
  );
}
