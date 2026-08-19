"use client";
import { AnimatePresence, motion } from "framer-motion";

export function OverlayCard({ open, children, maxWidth = 400, backdropRadius = 28, floating = false, viewport = false, onBackdropClick }: {
  open: boolean;
  children: React.ReactNode;
  maxWidth?: number;
  backdropRadius?: number;
  floating?: boolean;
  /** Covers the full browser viewport (position: fixed) instead of the nearest positioned ancestor. */
  viewport?: boolean;
  onBackdropClick?: () => void;
}) {
  return (
    <AnimatePresence>
      {open && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.2 }}
          style={{
            position: viewport ? "fixed" : "absolute", inset: 0,
            background: "rgba(0,0,0,0.5)",
            zIndex: viewport ? 1000 : 10,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            padding: 16,
            borderRadius: backdropRadius,
            pointerEvents: "auto",
          }}
          onClick={onBackdropClick}
        >
          {floating ? (
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 16 }}
              transition={{ duration: 0.22, ease: "easeOut" }}
              style={{ pointerEvents: "auto" }}
            >
              {children}
            </motion.div>
          ) : (
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 16 }}
              transition={{ duration: 0.22, ease: "easeOut" }}
              style={{
                width: "100%",              maxWidth,              borderRadius: 25,
                background: "var(--surface)",
                padding: 16,
                pointerEvents: "auto",
                boxShadow: "var(--shadow)",
              }}
            >
              {children}
            </motion.div>
          )}
        </motion.div>
      )}
    </AnimatePresence>
  );
}
