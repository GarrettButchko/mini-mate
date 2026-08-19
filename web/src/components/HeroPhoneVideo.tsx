import { IPHONE_CORNER_RATIO, IPHONE_SCREEN_ASPECT } from "@/lib/iphoneFrame";

interface Props {
  src: string;
  width?: number;
}

export default function HeroPhoneVideo({ src, width = 260 }: Props) {
  const height = width / IPHONE_SCREEN_ASPECT;
  return (
    <div
      className="overflow-hidden shrink-0"
      style={{
        width,
        height,
        borderRadius: width * IPHONE_CORNER_RATIO,
        border: "2px solid rgba(255,255,255,0.25)",
        background: "rgba(0,0,0,0.2)",
        backdropFilter: "blur(20px)",
      }}
    >
      <video
        src={src}
        autoPlay
        loop
        muted
        playsInline
        className="w-full h-full object-cover"
      />
    </div>
  );
}
