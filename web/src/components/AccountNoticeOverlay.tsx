"use client";
import { useRouter } from "next/navigation";
import { useAuth, type AccountNotice } from "@/contexts/AuthContext";
import { OverlayCard } from "@/components/OverlayCard";

const GREEN = "#34C759";
const RED = "#FF383C";

const NOTICE_COPY: Record<AccountNotice, { title: string; body: string; color: string; icon: "logout" | "trash" }> = {
  signedOut: {
    title: "Signed Out",
    body: "You have been signed out of Mini Mate.",
    color: GREEN,
    icon: "logout",
  },
  accountDeleted: {
    title: "Account Deleted",
    body: "Your account has been permanently deleted.",
    color: RED,
    icon: "trash",
  },
  accountDeletedElsewhere: {
    title: "Account Deleted",
    body: "Your account was deleted on another device.",
    color: RED,
    icon: "trash",
  },
};

function NoticeIcon({ icon, color, size = 34 }: { icon: "logout" | "trash"; color: string; size?: number }) {
  const strokeProps = {
    fill: "none",
    stroke: color,
    strokeWidth: 2,
    strokeLinecap: "round" as const,
    strokeLinejoin: "round" as const,
  };
  return (
    <div style={{ width: size * 1.8, height: size * 1.8, borderRadius: "50%", background: `${color}22`, display: "flex", alignItems: "center", justifyContent: "center" }}>
      <svg width={size} height={size} viewBox="0 0 24 24" {...strokeProps}>
        {icon === "logout" ? (
          <>
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
            <polyline points="16 17 21 12 16 7" />
            <line x1="21" y1="12" x2="9" y2="12" />
          </>
        ) : (
          <>
            <polyline points="3 6 5 6 21 6" />
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
            <line x1="10" y1="11" x2="10" y2="17" />
            <line x1="14" y1="11" x2="14" y2="17" />
          </>
        )}
      </svg>
    </div>
  );
}

export function AccountNoticeOverlay() {
  const router = useRouter();
  const { notice, dismissNotice } = useAuth();
  const copy = notice ? NOTICE_COPY[notice] : null;

  function handleDismiss() {
    dismissNotice();
    router.push("/");
  }

  return (
    <OverlayCard open={!!copy} viewport backdropRadius={0} onBackdropClick={handleDismiss}>
      {copy && (
        <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 14, textAlign: "center" }}>
          <NoticeIcon icon={copy.icon} color={copy.color} />
          <p style={{ margin: 0, fontSize: 18, fontWeight: 700, color: "var(--text)" }}>{copy.title}</p>
          <p style={{ margin: 0, fontSize: 14, color: "var(--text-muted)" }}>{copy.body}</p>
          <button
            onClick={handleDismiss}
            style={{ width: "100%", height: 42, borderRadius: 10, border: "none", background: copy.color, cursor: "pointer", fontSize: 14, fontWeight: 600, color: "#fff" }}
          >
            OK
          </button>
        </div>
      )}
    </OverlayCard>
  );
}
