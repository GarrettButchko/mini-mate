"use client";
import { useState } from "react";

interface FaqItem {
  q: string;
  a: string;
}

export default function FaqSection({ faqs }: { faqs: FaqItem[] }) {
  const [openIndex, setOpenIndex] = useState<number | null>(null);

  return (
    <section className="py-20 px-6">
      <div className="max-w-2xl mx-auto">
        <h2 className="text-center text-3xl font-bold mb-14" style={{ color: "var(--text)" }}>
          Frequently asked questions
        </h2>
        <div className="space-y-4">
          {faqs.map((item, i) => {
            const isOpen = openIndex === i;
            return (
              <div key={item.q} className="glass-card">
                <button
                  onClick={() => setOpenIndex(isOpen ? null : i)}
                  className="w-full flex items-center justify-between px-6 py-5 text-left font-medium text-sm cursor-pointer"
                  style={{ color: "var(--text)" }}
                >
                  {item.q}
                  <span
                    className="ml-4 shrink-0 text-xl leading-none select-none"
                    style={{
                      color: "var(--text-muted)",
                      display: "inline-block",
                      transition: "transform 0.25s ease",
                      transform: isOpen ? "rotate(45deg)" : "rotate(0deg)",
                    }}
                  >
                    +
                  </span>
                </button>
                <div
                  style={{
                    display: "grid",
                    gridTemplateRows: isOpen ? "1fr" : "0fr",
                    transition: "grid-template-rows 0.3s ease",
                  }}
                >
                  <div style={{ overflow: "hidden" }}>
                    <p className="px-6 pb-5 text-sm leading-relaxed" style={{ color: "var(--text-muted)" }}>
                      {item.a}
                    </p>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}
