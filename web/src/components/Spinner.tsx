"use client";
import { motion } from "framer-motion";

export function Spinner({ size = 16, color = "#fff", trackColor = "rgba(255,255,255,0.3)", thickness = 2 }: {
  size?: number;
  color?: string;
  trackColor?: string;
  thickness?: number;
}) {
  return (
    <motion.span
      animate={{ rotate: 360 }}
      transition={{ duration: 0.8, ease: "linear", repeat: Infinity }}
      style={{
        width: size,
        height: size,
        borderRadius: "50%",
        border: `${thickness}px solid ${trackColor}`,
        borderTopColor: color,
        display: "inline-block",
        boxSizing: "border-box",
      }}
    />
  );
}
