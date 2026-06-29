"use client";
import { useState, useEffect } from "react";
import Image from "next/image";

interface Props {
  imageSrc: string | null;
  imageAlt: string;
  frameWidth?: number;
  frameHeight?: number;
  children?: React.ReactNode;
}

export default function ScreenshotFrame({ imageSrc, imageAlt, frameWidth = 100, frameHeight = 200, children }: Props) {
  const [mounted, setMounted] = useState(false);
  const [visible, setVisible] = useState(false);

  const openLightbox = () => {
    if (!imageSrc) return;
    setMounted(true);
    requestAnimationFrame(() => requestAnimationFrame(() => setVisible(true)));
  };

  const closeLightbox = () => {
    setVisible(false);
    setTimeout(() => setMounted(false), 300);
  };

  useEffect(() => {
    if (!mounted) return;
    const onKey = (e: KeyboardEvent) => { if (e.key === "Escape") closeLightbox(); };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [mounted]);

  return (
    <>
      <div
        onClick={openLightbox}
        style={{ cursor: imageSrc ? "zoom-in" : "default" }}
        className="shrink-0"
      >
        <div
          className="rounded-[28px] overflow-hidden flex items-center justify-center transition-opacity hover:opacity-80"
          style={{ width: frameWidth, height: frameHeight, border: "2px solid var(--glass-border)", background: "var(--surface)" }}
        >
          {imageSrc ? (
            <Image src={imageSrc} alt={imageAlt} width={frameWidth} height={frameHeight} className="w-full h-full object-cover" />
          ) : (
            children
          )}
        </div>
      </div>

      {mounted && imageSrc && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-6"
          style={{
            background: visible ? "rgba(0,0,0,0.96)" : "rgba(0,0,0,0)",
            backdropFilter: "blur(16px)",
            WebkitBackdropFilter: "blur(16px)",
            transition: "background 0.25s ease",
          }}
          onClick={closeLightbox}
        >
          <Image
            src={imageSrc}
            alt={imageAlt}
            width={400}
            height={800}
            className="rounded-[44px] shadow-2xl"
            style={{
              height: "min(82vh, 720px)",
              width: "auto",
              transform: visible ? "scale(1)" : "scale(0.8)",
              opacity: visible ? 1 : 0,
              transition: "transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.25s ease",
            }}
            onClick={(e) => e.stopPropagation()}
          />
        </div>
      )}
    </>
  );
}
