"use client";
import { useEffect, useRef, useState } from "react";
import Image from "next/image";
import { useRouter } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";
import { ProfileView } from "./ProfileView";
import { AnimatePresence, motion } from "framer-motion";

export default function ProfileButton() {
  const { user, loading } = useAuth();
  const router = useRouter();
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  function close() { setOpen(false); }
  function toggle() { if (open) { close(); } else { setOpen(true); } }

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
      {user ? (
        <button
          onClick={toggle}
          aria-label="Profile"
          style={{
            width: 48, height: 48, borderRadius: "50%", overflow: "hidden",
            border: "none", padding: 0, cursor: "pointer",
            background: "var(--surface)", display: "flex",
            alignItems: "center", justifyContent: "center", flexShrink: 0,
            boxShadow: "var(--shadow)",
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
          onClick={() => router.push("/signin")}
          className="glass-pill flex items-center px-4 py-3.5"
          style={{ fontSize: 14, fontWeight: 500, color: "#AF52DE", cursor: "pointer", border: "none", whiteSpace: "nowrap" }}
        >
          Sign In
        </button>
      )}

      <AnimatePresence>
        {open && user && (
          <motion.div
            initial={{ opacity: 0, y: -8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -8 }}
            transition={{ duration: 0.18, ease: "easeOut" }}
            style={{ position: "absolute", top: "calc(100% + 10px)", right: 0, width: 320, zIndex: 100 }}
          >
            <ProfileView onClose={close} />
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
