"use client";
import { useEffect, useRef, useState } from "react";
import Image from "next/image";
import { useAuth } from "@/contexts/AuthContext";
import { OverlayCard } from "@/components/OverlayCard";

const PURPLE = "#AF52DE";
const BLUE   = "#0088FF";
const ORANGE = "#FF9500";
const RED    = "#FF383C";

export function ProfileView({ onClose }: { onClose: () => void }) {
  const { user, signOut, updateName, sendPasswordReset, deleteAccount, firestoreUser } = useAuth();
  const [editingName, setEditingName] = useState(false);
  const [newName, setNewName] = useState("");
  const [profileBusy, setProfileBusy] = useState(false);
  const [profileMsg, setProfileMsg] = useState<string | null>(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  if (!user) return null;

  const initials = user.displayName
    ? user.displayName.split(" ").map((n) => n[0]).join("").slice(0, 2).toUpperCase()
    : user.email?.[0]?.toUpperCase() ?? "?";

  return (
    <div style={{ padding: 16, position: "relative" }}>
      {/* Avatar + name header */}
      <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 18 }}>
        {user.photoURL ? (
          <Image src={user.photoURL} alt="" width={48} height={48} style={{ borderRadius: "50%", objectFit: "cover", flexShrink: 0 }} />
        ) : (
          <div style={{ width: 48, height: 48, borderRadius: "50%", background: "rgba(175,82,222,0.15)", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
            <span style={{ fontSize: 17, fontWeight: 700, color: PURPLE }}>{initials}</span>
          </div>
        )}
        <div style={{ minWidth: 0, overflow: "hidden" }}>
          {user.displayName && (
            <OverflowMarquee style={{ fontSize: 15, fontWeight: 700, color: "var(--text)" }}>{user.displayName}</OverflowMarquee>
          )}
          <OverflowMarquee style={{ fontSize: 12, color: "var(--text-muted)" }}>{user.email ?? ""}</OverflowMarquee>
        </div>
      </div>

      {/* Section: User Details */}
      <SectionLabel>User Details</SectionLabel>
      <div style={{ borderRadius: 12, background: "var(--surface-two)", overflow: "hidden", marginBottom: 18 }}>
        {[
          { label: "Name", value: user.displayName ?? "—" },
          { label: "Email", value: firestoreUser?.email ?? user.email ?? "—" },
          { label: "UID", value: firestoreUser?.googleId ?? "—" },
        ].map((row, i, arr) => (
          <div
            key={row.label}
            style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "11px 14px", borderBottom: i < arr.length - 1 ? "1px solid var(--glass-border, rgba(255,255,255,0.08))" : undefined }}
          >
            <span style={{ fontSize: 13, color: "var(--text-muted)" }}>{row.label}</span>
            <OverflowMarquee style={{ fontSize: 13, fontWeight: 500, color: "var(--text)", maxWidth: 160 }}>{row.value}</OverflowMarquee>
          </div>
        ))}
      </div>

      {/* Section: Account Management */}
      <SectionLabel>Account Management</SectionLabel>
      <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
        <AccountButton label="Edit Name" background={BLUE} onClick={() => { setNewName(user.displayName ?? ""); setEditingName(true); setProfileMsg(null); }} />

        {user.providerData[0]?.providerId === "password" && (
          <AccountButton
            label="Password Reset"
            background={BLUE}
            disabled={profileBusy}
            onClick={async () => {
              setProfileBusy(true);
              try { await sendPasswordReset(); setProfileMsg("Reset email sent!"); setTimeout(() => setProfileMsg(null), 3000); }
              catch { setProfileMsg("Could not send reset email."); }
              finally { setProfileBusy(false); }
            }}
          />
        )}

        {profileMsg && (
          <p style={{ margin: 0, fontSize: 12, color: profileMsg.includes("!") ? "#34C759" : RED, textAlign: "center" }}>{profileMsg}</p>
        )}

        <AccountButton label="Sign Out" background={ORANGE} onClick={() => { onClose(); setTimeout(signOut, 180); }} />
        <AccountButton label="Delete Account" background={RED} onClick={() => setShowDeleteConfirm(true)} />
      </div>

      {/* Edit Name overlay */}
      <OverlayCard open={editingName}>
        <p style={{ margin: "0 0 8px", fontSize: 12, color: "var(--text-muted)" }}>New display name</p>
        <div style={{ borderRadius: 10, background: "var(--surface-two)", padding: "10px 12px", marginBottom: 10 }}>
          <input
            autoFocus
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
            placeholder={user.displayName ?? "Your name"}
            style={{ width: "100%", background: "transparent", border: "none", outline: "none", fontSize: 14, color: "var(--text)" }}
          />
        </div>
        <InlineActionRow
          onCancel={() => { setEditingName(false); setNewName(""); setProfileMsg(null); }}
          onConfirm={async () => {
            setProfileBusy(true);
            try { await updateName(newName.trim()); setEditingName(false); setNewName(""); setProfileMsg("Name updated!"); setTimeout(() => setProfileMsg(null), 2500); }
            catch { setProfileMsg("Failed to update name."); }
            finally { setProfileBusy(false); }
          }}
          confirmLabel="Save"
          confirmBg={BLUE}
          disabled={profileBusy || !newName.trim()}
        />
      </OverlayCard>

      {/* Delete Account overlay */}
      <OverlayCard open={showDeleteConfirm}>
        <p style={{ margin: "0 0 10px", fontSize: 13, color: "var(--text)", fontWeight: 500, textAlign: "center" }}>Delete your account? This cannot be undone.</p>
        <InlineActionRow
          onCancel={() => setShowDeleteConfirm(false)}
          onConfirm={async () => {
            setProfileBusy(true);
            try { await deleteAccount(); onClose(); }
            catch { setProfileMsg("Sign in again then retry."); setShowDeleteConfirm(false); }
            finally { setProfileBusy(false); }
          }}
          confirmLabel="Delete"
          confirmBg={RED}
          disabled={profileBusy}
        />
      </OverlayCard>
    </div>
  );
}

const MARQUEE_SPEED = 30;
const MARQUEE_PAUSE_MS = 3000;

function OverflowMarquee({ children, style }: { children: string; style?: React.CSSProperties }) {
  const wrapperRef = useRef<HTMLDivElement>(null);
  const textRef = useRef<HTMLSpanElement>(null);
  const innerRef = useRef<HTMLDivElement>(null);
  const loopWidthRef = useRef(0);
  const gapRef = useRef(32);
  const [scrolling, setScrolling] = useState(false);

  useEffect(() => {
    const wrapper = wrapperRef.current;
    const text = textRef.current;
    if (!wrapper || !text) return;
    const check = () => {
      const tw = text.scrollWidth;
      const gap = text.offsetHeight * 2;
      const ww = wrapper.clientWidth;
      const overflows = tw > ww;
      gapRef.current = gap;
      loopWidthRef.current = tw + gap;
      setScrolling(overflows);
    };
    let raf = requestAnimationFrame(() => { raf = requestAnimationFrame(check); });
    const ro = new ResizeObserver(check);
    ro.observe(wrapper);
    return () => { cancelAnimationFrame(raf); ro.disconnect(); };
  }, [children]);

  useEffect(() => {
    const inner = innerRef.current;
    if (!scrolling || !inner) return;

    let rafId: number;
    let offset = 0;
    let lastTime: number | null = null;
    let pauseRemaining = MARQUEE_PAUSE_MS;
    let pausing = true;

    const animate = (time: number) => {
      const delta = lastTime !== null ? time - lastTime : 0;
      lastTime = time;

      if (pausing) {
        pauseRemaining -= delta;
        if (pauseRemaining <= 0) pausing = false;
      } else {
        offset += delta * MARQUEE_SPEED / 1000;
        if (offset >= loopWidthRef.current) {
          offset = 0;
          pausing = true;
          pauseRemaining = MARQUEE_PAUSE_MS;
        }
        inner.style.transform = `translateX(-${offset}px)`;
      }

      rafId = requestAnimationFrame(animate);
    };

    rafId = requestAnimationFrame(animate);
    return () => cancelAnimationFrame(rafId);
  }, [scrolling]);

  return (
    <div ref={wrapperRef} style={{ overflow: "hidden", position: "relative", width: "100%", ...style }}>
      <span ref={textRef} style={{ whiteSpace: "nowrap", display: "block", visibility: scrolling ? "hidden" : "visible" }}>
        {children}
      </span>
      {scrolling && (
        <div ref={innerRef} style={{ position: "absolute", top: 0, left: 0, display: "inline-flex" }}>
          <span style={{ whiteSpace: "nowrap", paddingRight: gapRef.current }}>{children}</span>
          <span style={{ whiteSpace: "nowrap" }} aria-hidden>{children}</span>
        </div>
      )}
    </div>
  );
}

function SectionLabel({ children }: { children: React.ReactNode }) {
  return (
    <p style={{ margin: "0 0 6px 2px", fontSize: 13, fontWeight: 700, letterSpacing: "0.06em", color: "var(--text-muted)" }}>
      {children}
    </p>
  );
}

function AccountButton({ label, background, onClick, disabled = false }: {
  label: string;
  background: string;
  onClick: () => void;
  disabled?: boolean;
}) {
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      style={{ width: "100%", height: 50, borderRadius: 12, border: "none", background, cursor: "pointer", fontSize: 14, fontWeight: 600, color: "#fff", opacity: disabled ? 0.6 : 1 }}
    >
      {label}
    </button>
  );
}

function InlineActionRow({ onCancel, onConfirm, confirmLabel, confirmBg, disabled }: {
  onCancel: () => void;
  onConfirm: () => void;
  confirmLabel: string;
  confirmBg: string;
  disabled: boolean;
}) {
  return (
    <div style={{ display: "flex", gap: 8 }}>
      <button
        onClick={onCancel}
        style={{ flex: 1, height: 38, borderRadius: 8, border: "none", background: "var(--surface-two)", cursor: "pointer", fontSize: 13, fontWeight: 600, color: "var(--text-muted)" }}
      >Cancel</button>
      <button
        disabled={disabled}
        onClick={onConfirm}
        style={{ flex: 1, height: 38, borderRadius: 8, border: "none", background: confirmBg, cursor: "pointer", fontSize: 13, fontWeight: 600, color: "#fff", opacity: disabled ? 0.6 : 1 }}
      >{disabled ? "…" : confirmLabel}</button>
    </div>
  );
}
