export default function PageShell({ children }: { children: React.ReactNode }) {
  return (
    <div
      className="min-h-screen flex flex-col font-[family-name:var(--font-geist-sans)]"
      style={{ background: "var(--bg)", color: "var(--text)" }}
    >
      {children}
    </div>
  );
}
