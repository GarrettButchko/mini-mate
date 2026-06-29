interface Testimonial {
  quote: string;
  name: string;
  detail: string;
}

interface TestimonialGridProps {
  heading: string;
  testimonials: Testimonial[];
  children?: React.ReactNode;
}

export default function TestimonialGrid({ heading, testimonials, children }: TestimonialGridProps) {
  return (
    <section className="py-20 px-6">
      <div className="max-w-4xl mx-auto">
        <h2 className="text-center text-3xl font-bold mb-14" style={{ color: "var(--text)" }}>{heading}</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
          {testimonials.map((t) => (
            <div key={t.name} className="glass-card p-8">
              <p className="text-sm leading-relaxed mb-6" style={{ color: "var(--text-sub)" }}>
                &ldquo;{t.quote}&rdquo;
              </p>
              <div>
                <p className="text-sm font-semibold" style={{ color: "var(--text)" }}>{t.name}</p>
                <p className="text-xs" style={{ color: "var(--text-muted)" }}>{t.detail}</p>
              </div>
            </div>
          ))}
        </div>
        {children}
      </div>
    </section>
  );
}
