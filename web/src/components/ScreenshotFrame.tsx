"use client";
import { useState, useEffect } from "react";
import Image from "next/image";
import { OverlayCard } from "./OverlayCard";

interface Props {
  imageSrc: string | null;
  imageAlt: string;
  frameWidth?: number;
  frameHeight?: number;
  children?: React.ReactNode;
}

export default function ScreenshotFrame({ imageSrc, imageAlt, frameWidth = 100, frameHeight = 200, children }: Props) {
  const [open, setOpen] = useState(false);

  const openLightbox = () => {
    if (!imageSrc) return;
    setOpen(true);
  };

  const closeLightbox = () => setOpen(false);

  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => { if (e.key === "Escape") closeLightbox(); };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open]);

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

      {/* Lightbox — floating image over the shared overlay's backdrop/animation,
          with the original image styling preserved. */}
      <div style={{ position: "fixed", inset: 0, zIndex: 50, pointerEvents: "none" }}>
        <OverlayCard open={open} floating onBackdropClick={closeLightbox} backdropRadius={0}>
          {imageSrc && (
            <Image
              src={imageSrc}
              alt={imageAlt}
              width={400}
              height={800}
              className="rounded-[44px] shadow-2xl"
              style={{ width: "auto", height: "min(82vh, 720px)", pointerEvents: "auto" }}
              onClick={(e) => e.stopPropagation()}
            />
          )}
        </OverlayCard>
      </div>
    </>
  );
}
