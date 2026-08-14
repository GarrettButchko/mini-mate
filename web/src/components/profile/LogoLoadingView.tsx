"use client";
import { motion } from "framer-motion";
import MiniMateLogo from "@/components/MiniMateLogo";

// Mirrors the SwiftUI LogoLoadingView:
// grayscale + 0.5 opacity, rotating 0 -> 360deg, linear, 1.2s, infinite.
export function LogoLoadingView({ size = 25, color = "#AF52DE" }: {
  size?: number;
  color?: string;
}) {
  return (
    <motion.div
      animate={{ rotate: 360 }}
      transition={{ duration: 1.2, ease: "linear", repeat: Infinity }}
      style={{ width: size, height: size, filter: "grayscale(1)", opacity: 0.5 }}
    >
      <MiniMateLogo size={size} color={color} />
    </motion.div>
  );
}
