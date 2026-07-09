import Link from "next/link";
import MiniMateLogo from "./MiniMateLogo";

interface SiteNavProps {
  position?: "fixed" | "sticky";
  logoLinked?: boolean;
  logoColor?: string;
  wrapChildren?: boolean;
  children?: React.ReactNode;
}

export default function SiteNav({ position = "sticky", logoLinked = true, logoColor = "#34C759", wrapChildren = true, children }: SiteNavProps) {
  const positionClass = position === "fixed" ? "fixed inset-x-0 top-0" : "sticky top-0";

  const logoContent = (
    <>
      <MiniMateLogo size={20} color={logoColor} />
      <span className="text-[20px] font-medium tracking-tight leading-none" style={{ color: "var(--text)" }}>
        Mini Mate
      </span>
    </>
  );

  return (
    <nav className={`${positionClass} z-50 flex items-center justify-between px-6 py-4`}>
      {logoLinked ? (
        <Link href="/" className="glass-pill flex items-center gap-2 px-4 py-3.5 no-underline">
          {logoContent}
        </Link>
      ) : (
        <div className="glass-pill flex items-center gap-2 px-4 py-3.5">
          {logoContent}
        </div>
      )}
      {children && (
        wrapChildren ? (
          <div className="glass-pill flex items-center gap-1 px-2 py-2">
            {children}
          </div>
        ) : (
          <div className="flex items-center gap-1">
            {children}
          </div>
        )
      )}
    </nav>
  );
}
