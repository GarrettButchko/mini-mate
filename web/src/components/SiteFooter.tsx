import Link from "next/link";

interface FooterLink {
  href: string;
  label: string;
}

interface SiteFooterProps {
  links: FooterLink[];
  className?: string;
}

export default function SiteFooter({ links, className = "mt-auto" }: SiteFooterProps) {
  return (
    <footer
      className={`${className} py-8 px-6 text-center text-sm`}
      style={{ borderTop: "1px solid var(--glass-border)", color: "var(--text-muted)" }}
    >
      <div className="flex flex-wrap justify-center gap-6 mb-4">
        {links.map((link) =>
          link.href.startsWith("/") ? (
            <Link key={link.href} href={link.href} className="hover:opacity-70 transition-opacity">
              {link.label}
            </Link>
          ) : (
            <a key={link.href} href={link.href} className="hover:opacity-70 transition-opacity">
              {link.label}
            </a>
          )
        )}
      </div>
      <p>© {new Date().getFullYear()} Mini Mate. All rights reserved.</p>
    </footer>
  );
}
