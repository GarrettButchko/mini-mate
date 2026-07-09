import ScreenshotFrame from "./ScreenshotFrame";

interface FeatureItem {
  icon: React.ElementType;
  title: string;
  desc: string;
  imageSrc: string | null;
  imageAlt: string;
}

interface FeatureGridProps {
  heading: string;
  features: FeatureItem[];
  accentColor?: string;
}

export default function FeatureGrid({ heading, features, accentColor = "#34C759" }: FeatureGridProps) {
  return (
    <section className="py-20 px-6">
      <div className="max-w-5xl mx-auto">
        <h2 className="text-center text-3xl font-bold mb-14" style={{ color: "var(--text)" }}>{heading}</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
          {features.map((f) => (
            <div key={f.title} className="glass-card p-8 flex flex-row items-center gap-6">
              <div className="flex-1">
                <div className="mb-4" style={{ color: accentColor }}><f.icon size={32} /></div>
                <h3 className="text-lg font-semibold mb-2" style={{ color: "var(--text)" }}>{f.title}</h3>
                <p className="text-sm leading-relaxed" style={{ color: "var(--text-muted)" }}>{f.desc}</p>
              </div>
              <ScreenshotFrame imageSrc={f.imageSrc} imageAlt={f.imageAlt} frameWidth={100} frameHeight={200}>
                <f.icon size={28} style={{ color: "var(--text-muted)", opacity: 0.2 }} />
              </ScreenshotFrame>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
