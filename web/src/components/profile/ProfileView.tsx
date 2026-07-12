"use client";
import { useState } from "react";
import Image from "next/image";
import { useAuth } from "@/contexts/AuthContext";

const PURPLE = "#AF52DE";
const BLUE   = "#007AFF";
const ORANGE = "#FF9500";
const RED    = "#FF3B30";

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

  const providerLabel = (() => {
    const id = user.providerData[0]?.providerId;
    if (id === "google.com") return "Google";
    if (id === "apple.com") return "Apple";
    if (id === "password") return "Email";
    return id ?? "—";
  })();

  return (
    <div style={{ padding: 16, maxHeight: 520, overflowY: "auto" }}>
      {/* Avatar + name header */}
      <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 18 }}>
        {user.photoURL ? (
          <Image src={user.photoURL} alt="" width={48} height={48} style={{ borderRadius: "50%", objectFit: "cover", flexShrink: 0 }} />
        ) : (
          <div style={{ width: 48, height: 48, borderRadius: "50%", background: "rgba(175,82,222,0.15)", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
            <span style={{ fontSize: 17, fontWeight: 700, color: PURPLE }}>{initials}</span>
          </div>
        )}
        <div style={{ minWidth: 0 }}>
          {user.displayName && (
            <p style={{ margin: 0, fontSize: 15, fontWeight: 700, color: "var(--text)", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{user.displayName}</p>
          )}
          <p style={{ margin: 0, fontSize: 12, color: "var(--text-muted)", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{user.email}</p>
        </div>
      </div>

      {/* Section: User Details */}
      <SectionLabel>User Details</SectionLabel>
      <div style={{ borderRadius: 12, background: "var(--surface-two)", overflow: "hidden", marginBottom: 18 }}>
        {[
          { label: "Name", value: user.displayName ?? "—" },
          { label: "Email", value: firestoreUser?.email ?? "—" },
          { label: "Google ID", value: firestoreUser?.googleId ?? "—" },
          { label: "Provider", value: providerLabel },
        ].map((row, i, arr) => (
          <div
            key={row.label}
            style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "11px 14px", borderBottom: i < arr.length - 1 ? "1px solid var(--glass-border, rgba(255,255,255,0.08))" : undefined }}
          >
            <span style={{ fontSize: 13, color: "var(--text-muted)" }}>{row.label}</span>
            <span style={{ fontSize: 13, fontWeight: 500, color: "var(--text)", maxWidth: 160, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{row.value}</span>
          </div>
        ))}
      </div>

      {/* Section: Account Management */}
      <SectionLabel>Account Management</SectionLabel>
      <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>

        {editingName ? (
          <div style={{ borderRadius: 12, background: "var(--surface-two)", padding: "12px 14px" }}>
            <p style={{ margin: "0 0 8px", fontSize: 12, color: "var(--text-muted)" }}>New display name</p>
            <input
              autoFocus
              value={newName}
              onChange={(e) => setNewName(e.target.value)}
              placeholder={user.displayName ?? "Your name"}
              style={{ width: "100%", background: "transparent", border: "none", outline: "none", fontSize: 14, color: "var(--text)", marginBottom: 10 }}
            />
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
          </div>
        ) : (
          <AccountButton label="Edit Name" background={BLUE} onClick={() => { setNewName(user.displayName ?? ""); setEditingName(true); setProfileMsg(null); }} />
        )}

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

        <AccountButton label="Sign Out" background={ORANGE} onClick={async () => { await signOut(); onClose(); }} />

        {showDeleteConfirm ? (
          <div style={{ borderRadius: 12, background: "var(--surface-two)", padding: "12px 14px", textAlign: "center" }}>
            <p style={{ margin: "0 0 10px", fontSize: 13, color: "var(--text)", fontWeight: 500 }}>Delete your account? This cannot be undone.</p>
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
          </div>
        ) : (
          <AccountButton label="Delete Account" background={RED} onClick={() => setShowDeleteConfirm(true)} />
        )}
      </div>
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
        style={{ flex: 1, height: 38, borderRadius: 8, border: "none", background: "var(--glass-bg)", cursor: "pointer", fontSize: 13, fontWeight: 600, color: "var(--text-muted)" }}
      >Cancel</button>
      <button
        disabled={disabled}
        onClick={onConfirm}
        style={{ flex: 1, height: 38, borderRadius: 8, border: "none", background: confirmBg, cursor: "pointer", fontSize: 13, fontWeight: 600, color: "#fff", opacity: disabled ? 0.6 : 1 }}
      >{disabled ? "…" : confirmLabel}</button>
    </div>
  );
}
