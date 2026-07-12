"use client";
import { useEffect, useRef, useState } from "react";
import Image from "next/image";
import { useAuth } from "@/contexts/AuthContext";
import { ProfileSignInOptions } from "./ProfileSignInOptions";
import { ProfileEmailForm } from "./ProfileEmailForm";
import { ProfileView } from "./ProfileView";

export default function ProfileButton() {
  const { user, loading } = useAuth();
  const [open, setOpen] = useState(false);
  const [closing, setClosing] = useState(false);
  const [mode, setMode] = useState<"options" | "email">("options");
  const [emailExiting, setEmailExiting] = useState(false);
  const [returning, setReturning] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  function close() {
    setClosing(true);
    setTimeout(() => {
      setOpen(false);
      setClosing(false);
      setMode("options");
      setReturning(false);
    }, 140);
  }

  function toggle() {
    if (open) { close(); } else { setOpen(true); setMode("options"); setReturning(false); }
  }

  function goToEmail() { setReturning(false); setMode("email"); }

  function goBack() {
    setEmailExiting(true);
    setTimeout(() => {
      setEmailExiting(false);
      setReturning(true);
      setMode("options");
    }, 200);
  }

  useEffect(() => {
    function handler(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) close();
    }
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  if (loading) return <div style={{ width: 48, height: 48 }} />;

  const initials = user?.displayName
    ? user.displayName.split(" ").map((n) => n[0]).join("").slice(0, 2).toUpperCase()
    : user?.email?.[0]?.toUpperCase() ?? "?";

  return (
    <div ref={ref} style={{ position: "relative" }}>
      {/* Trigger */}
      {user ? (
        <button
          onClick={toggle}
          aria-label="Profile"
          style={{
            width: 48, height: 48, borderRadius: "50%", overflow: "hidden",
            border: "none", padding: 0, cursor: "pointer",
            background: "var(--surface)", display: "flex",
            alignItems: "center", justifyContent: "center", flexShrink: 0,
          }}
        >
          {user.photoURL ? (
            <Image src={user.photoURL} alt={user.displayName ?? "Profile"} width={48} height={48} style={{ borderRadius: "50%", objectFit: "cover" }} />
          ) : (
            <span style={{ fontSize: 14, fontWeight: 600, color: "#AF52DE" }}>{initials}</span>
          )}
        </button>
      ) : (
        <button
          onClick={toggle}
          className="glass-pill"
          style={{
            display: "flex", alignItems: "center", padding: "14px 16px",
            fontSize: 15, fontWeight: 500, color: "#AF52DE",
            cursor: "pointer", border: "none", whiteSpace: "nowrap",
          }}
        >
          Sign In
        </button>
      )}

      {/* Dropdown */}
      {open && (
        <div
          style={{
            position: "absolute",
            top: "calc(100% + 10px)",
            right: 0,
            width: 300,
            borderRadius: 28,
            background: "var(--glass-bg)",
            backdropFilter: "blur(24px)",
            WebkitBackdropFilter: "blur(24px)",
            boxShadow: "var(--shadow)",
            zIndex: 100,
            overflow: "hidden",
            animation: closing ? "fadeScaleOut 0.14s ease forwards" : "fadeScaleIn 0.15s ease",
          }}
        >
          {user ? (
            <ProfileView onClose={close} />
          ) : mode === "options" ? (
            <ProfileSignInOptions onEmailClick={goToEmail} returning={returning} onClose={close} />
          ) : (
            <ProfileEmailForm onBack={goBack} onClose={close} emailExiting={emailExiting} />
          )}
        </div>
      )}

      <style>{`
        @keyframes fadeScaleIn {
          from { opacity: 0; transform: scale(0.95) translateY(-4px); }
          to   { opacity: 1; transform: scale(1) translateY(0); }
        }
        @keyframes fadeScaleOut {
          from { opacity: 1; transform: scale(1) translateY(0); }
          to   { opacity: 0; transform: scale(0.95) translateY(-4px); }
        }
        @keyframes slideFromRight {
          from { opacity: 0; transform: translateX(24px); }
          to   { opacity: 1; transform: translateX(0); }
        }
        @keyframes slideToRight {
          from { opacity: 1; transform: translateX(0); }
          to   { opacity: 0; transform: translateX(24px); }
        }
        @keyframes slideFromLeft {
          from { opacity: 0; transform: translateX(-24px); }
          to   { opacity: 1; transform: translateX(0); }
        }
      `}</style>
    </div>
  );
}
