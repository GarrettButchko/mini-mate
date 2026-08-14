"use client";
import { useEffect, useRef, useState } from "react";
import Image from "next/image";
import { useRouter } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";
import { OverlayCard } from "@/components/OverlayCard";
import MiniMateLogo from "@/components/MiniMateLogo";
import { LogoLoadingView } from "./LogoLoadingView";
import { Spinner } from "@/components/Spinner";

const PURPLE = "#AF52DE";
const BLUE   = "#0088FF";
const ORANGE = "#FF9500";
const RED    = "#FF383C";
const GREEN  = "#34C759";

export function ProfileView({ onClose }: { onClose: () => void }) {
  const router = useRouter();
  const { user, signOut, updateName, sendPasswordReset, deleteAccount, reauthenticateWithEmail, reauthenticateWithGoogle, reauthenticateWithApple, firestoreUser } = useAuth();

  const [editingName, setEditingName] = useState(false);
  const [newName, setNewName] = useState("");
  const [profileBusy, setProfileBusy] = useState(false);
  const [showNameSuccess, setShowNameSuccess] = useState(false);
  const [profileMsg, setProfileMsg] = useState<{ text: string; ok: boolean } | null>(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deletePassword, setDeletePassword] = useState("");
  const [deleteConfirmPassword, setDeleteConfirmPassword] = useState("");

  if (!user) return null;

  const courseCount = firestoreUser?.adminCourses?.length ?? 0;

  const displayName = firestoreUser?.name ?? user.displayName ?? null;

  const providerId = user.providerData[0]?.providerId;
  const accountType = providerId === "google.com" ? "google" : providerId === "apple.com" ? "apple" : "email";
  const email = user.email ?? "";

  const initials = displayName
    ? displayName.split(" ").map((n) => n[0]).join("").slice(0, 2).toUpperCase()
    : user.email?.[0]?.toUpperCase() ?? "?";

  const detailRows = [
    { label: "Name",    value: displayName ?? "—" },
    { label: "Email",   value: firestoreUser?.email ?? user.email ?? "—" },
    { label: "UID",     value: firestoreUser?.googleId ?? "—" },
    ...(courseCount > 0 ? [{ label: "Courses", value: String(courseCount) }] : []),
  ];

  async function handleDelete() {
    setProfileBusy(true);
    try {
      if (accountType === "email") {
        await reauthenticateWithEmail(email, deletePassword);
      } else if (accountType === "google") {
        await reauthenticateWithGoogle();
      } else {
        await reauthenticateWithApple();
      }
      await deleteAccount();
      onClose();
    } catch {
      setProfileMsg({ text: "Sign in again then retry.", ok: false });
      setShowDeleteConfirm(false);
    } finally {
      setProfileBusy(false);
    }
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>

      {/* ── Island 1: Profile ── */}
      <div style={{ borderRadius: 28, background: "var(--glass-bg)", backdropFilter: "blur(24px)", WebkitBackdropFilter: "blur(24px)", boxShadow: "var(--shadow)", padding: 14, position: "relative", overflow: "hidden" }}>

        {/* Avatar + name */}
        <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 14 }}>
          {user.photoURL ? (
            <Image src={user.photoURL} alt="" width={44} height={44} style={{ borderRadius: "50%", objectFit: "cover", flexShrink: 0 }} />
          ) : (
            <div style={{ width: 44, height: 44, borderRadius: "50%", background: `${PURPLE}22`, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
              <span style={{ fontSize: 16, fontWeight: 700, color: PURPLE }}>{initials}</span>
            </div>
          )}
          <div style={{ minWidth: 0, overflow: "hidden" }}>
            {displayName && (
              <OverflowMarquee style={{ fontSize: 14, fontWeight: 700, color: "var(--text)" }}>{displayName}</OverflowMarquee>
            )}
            <OverflowMarquee style={{ fontSize: 11, color: "var(--text-muted)" }}>{user.email ?? ""}</OverflowMarquee>
          </div>
        </div>

        {/* Detail rows */}
        <div style={{ borderRadius: 12, background: "var(--surface-two)", overflow: "hidden", marginBottom: 10 }}>
          {detailRows.map((row, i, arr) => (
            <div key={row.label}>
              {i > 0 && <div style={{ height: 1, background: "var(--glass-border)", marginLeft: 12 }} />}
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "9px 12px" }}>
                <span style={{ fontSize: 12, color: "var(--text-muted)", flexShrink: 0 }}>{row.label}</span>
                <OverflowMarquee style={{ fontSize: 12, fontWeight: 500, color: "var(--text)", maxWidth: 150, marginLeft: 12 }}>{row.value}</OverflowMarquee>
              </div>
            </div>
          ))}
        </div>

        {/* Actions */}
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          <div style={{ display: "flex", gap: 10 }}>
            <SmallButton label="Edit Name" background={BLUE} onClick={() => { setNewName(displayName ?? ""); setEditingName(true); setProfileMsg(null); }} />
            
          </div>
          {profileMsg && (
            <p style={{ margin: 0, fontSize: 11, color: profileMsg.ok ? GREEN : RED, textAlign: "center" }}>{profileMsg.text}</p>
          )}
          <div style={{ display: "flex", gap: 10 }}>
            <SmallButton label="Sign Out" background={ORANGE} onClick={() => { onClose(); setTimeout(signOut, 180); }} />
            <SmallButton label="Delete Account" background={RED} onClick={() => { setDeletePassword(""); setDeleteConfirmPassword(""); setProfileMsg(null); setShowDeleteConfirm(true); }} />
          </div>
          <div style={{ display: "flex", gap: 10 }}>
            <SmallButton label="Manage Courses" background={PURPLE} onClick={() => { onClose(); router.push("/manage-courses"); }} />
          </div>
        </div>

        

       

        {/* Edit Name overlay (hidden while saving so the loading popup takes over) */}
        <OverlayCard open={editingName && !profileBusy}>
          <p style={{ margin: "0 0 8px", fontSize: 12, color: "var(--text-muted)" }}>New display name</p>
          <div style={{ borderRadius: 8, background: "var(--surface-two)", padding: "10px 12px", marginBottom: 10 }}>
            <input
              autoFocus
              value={newName}
              onChange={(e) => setNewName(e.target.value)}
              placeholder={displayName ?? "Your name"}
              style={{ width: "100%", background: "transparent", border: "none", outline: "none", fontSize: 14, color: "var(--text)" }}
            />
          </div>
          <InlineActionRow
            onCancel={() => { setEditingName(false); setNewName(""); setProfileMsg(null); }}
            onConfirm={async () => {
              setProfileBusy(true);
              try { await updateName(newName.trim()); setEditingName(false); setNewName(""); setShowNameSuccess(true); }
              catch { setProfileMsg({ text: "Failed to update name.", ok: false }); }
              finally { setProfileBusy(false); }
            }}
            confirmLabel="Save"
            confirmBg={BLUE}
            disabled={profileBusy || !newName.trim()}
            busy={profileBusy}
          />
        </OverlayCard>

        {/* Loading popup while the name is being updated */}
        <OverlayCard open={profileBusy && editingName}>
          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 14, padding: "10px 0 6px" }}>
            <LogoLoadingView size={25} color={PURPLE} />
            <p style={{ margin: 0, fontSize: 12, color: "var(--text-muted)" }}>Updating…</p>
          </div>
        </OverlayCard>

        {/* Success popup after the name has been changed */}
        <OverlayCard open={showNameSuccess}>
          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 12 }}>
            <MiniMateLogo size={50} color={GREEN} />
            <p style={{ margin: 0, fontSize: 13, fontWeight: 600, color: "var(--text)" }}>Name updated!</p>
            <button
              onClick={() => setShowNameSuccess(false)}
              style={{ width: "100%", height: 38, borderRadius: 9, border: "none", background: GREEN, cursor: "pointer", fontSize: 13, fontWeight: 600, color: "#fff" }}
            >
              Done
            </button>
          </div>
        </OverlayCard>

        {/* Delete Account overlay */}
        <OverlayCard open={showDeleteConfirm}>
          <p style={{ margin: "0 0 8px", fontSize: 14, color: "var(--text)", fontWeight: 600, textAlign: "center" }}>Confirm Deletion</p>
          <p style={{ margin: "0 0 12px", fontSize: 12, color: "var(--text-muted)", textAlign: "center" }}>This will permanently delete your account.</p>

          {accountType === "email" && (
            <>
              <div style={{ borderRadius: 8, background: "var(--surface-two)", padding: "10px 12px", marginBottom: 8 }}>
                <input
                  type="password"
                  value={deletePassword}
                  onChange={(e) => setDeletePassword(e.target.value)}
                  placeholder="Password"
                  style={{ width: "100%", background: "transparent", border: "none", outline: "none", fontSize: 14, color: "var(--text)" }}
                />
              </div>
              <div style={{ borderRadius: 8, background: "var(--surface-two)", padding: "10px 12px", marginBottom: 12 }}>
                <input
                  type="password"
                  value={deleteConfirmPassword}
                  onChange={(e) => setDeleteConfirmPassword(e.target.value)}
                  placeholder="Confirm Password"
                  style={{ width: "100%", background: "transparent", border: "none", outline: "none", fontSize: 14, color: "var(--text)" }}
                />
              </div>
            </>
          )}

          <InlineActionRow
            onCancel={() => { setShowDeleteConfirm(false); setDeletePassword(""); setDeleteConfirmPassword(""); }}
            onConfirm={handleDelete}
            confirmLabel="Delete"
            confirmBg={RED}
            disabled={profileBusy || (accountType === "email" && (deletePassword === "" || deletePassword !== deleteConfirmPassword))}
            busy={profileBusy}
          />
        </OverlayCard>
      </div>

      

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
      gapRef.current = gap;
      loopWidthRef.current = tw + gap;
      setScrolling(tw > ww);
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
        offset += (delta * MARQUEE_SPEED) / 1000;
        if (offset >= loopWidthRef.current) { offset = 0; pausing = true; pauseRemaining = MARQUEE_PAUSE_MS; }
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

function SmallButton({ label, background, onClick, disabled = false }: {
  label: string;
  background: string;
  onClick: () => void;
  disabled?: boolean;
}) {
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      style={{ flex: 1, height: 36, borderRadius: 14, border: "none", background, cursor: "pointer", fontSize: 12, fontWeight: 600, color: "#fff", opacity: disabled ? 0.6 : 1 }}
    >
      {label}
    </button>
  );
}

function InlineActionRow({ onCancel, onConfirm, confirmLabel, confirmBg, disabled, busy = false }: {
  onCancel: () => void;
  onConfirm: () => void;
  confirmLabel: string;
  confirmBg: string;
  disabled: boolean;
  busy?: boolean;
}) {
  return (
    <div style={{ display: "flex", gap: 8 }}>
      <button onClick={onCancel} style={{ flex: 1, height: 38, borderRadius: 9, border: "none", background: "var(--surface-two)", cursor: "pointer", fontSize: 13, fontWeight: 600, color: "var(--text-muted)" }}>
        Cancel
      </button>
      <button disabled={disabled} onClick={onConfirm} style={{ flex: 1, height: 38, borderRadius: 9, border: "none", background: confirmBg, cursor: "pointer", fontSize: 13, fontWeight: 600, color: "#fff", opacity: disabled ? 0.6 : 1, display: "flex", alignItems: "center", justifyContent: "center" }}>
        {busy ? <Spinner size={14} /> : confirmLabel}
      </button>
    </div>
  );
}
