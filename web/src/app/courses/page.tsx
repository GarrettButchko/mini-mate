"use client";
import { useState } from "react";
import Link from "next/link";
import { MapPin, Users, Smartphone, Map } from "lucide-react";
import MiniMateLogo from "@/components/MiniMateLogo";
import type { MapItemDTO } from "@/lib/foursquare";
import { resolveContactRoute, getAllContactRoutes } from "@/lib/contactRouter";
import { SearchResultBusinessLogic } from "@/lib/SearchResultBusinessLogic";

const searchLogic = new SearchResultBusinessLogic();

interface SearchState {
  loading: boolean;
  results: MapItemDTO[];
  error: string | null;
  userLat: number | null;
  userLng: number | null;
}

export default function CoursesPage() {
  const [search, setSearch] = useState<SearchState>({
    loading: false,
    results: [],
    error: null,
    userLat: null,
    userLng: null,
  });
  const [selected, setSelected] = useState<MapItemDTO | null>(null);
  const [query, setQuery] = useState("");

  async function findNearMe() {
    setSearch((s) => ({ ...s, loading: true, error: null }));
    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        const { latitude, longitude } = pos.coords;
        const res = await fetch(`/api/foursquare?lat=${latitude}&lng=${longitude}&limit=12`);
        const data = await res.json();
        setSearch({
          loading: false,
          results: data.results ?? [],
          error: data.error ?? null,
          userLat: latitude,
          userLng: longitude,
        });
      },
      () => setSearch((s) => ({ ...s, loading: false, error: "Location permission denied." }))
    );
  }

  const displayed = query
    ? search.results.filter((r) => r.name.toLowerCase().includes(query.toLowerCase()))
    : search.results;

  function distanceLabel(item: MapItemDTO): string | null {
    if (search.userLat == null || search.userLng == null) return null;
    const meters = searchLogic.distanceInMeters(
      search.userLat,
      search.userLng,
      item.coordinate.latitude,
      item.coordinate.longitude
    );
    return searchLogic.buildSubtitle(searchLogic.metersToMiles(meters), item.address.shortAddress);
  }

  return (
    <div className="min-h-screen flex flex-col font-[family-name:var(--font-geist-sans)]" style={{ background: "var(--bg)", color: "var(--text)" }}>
      {/* Nav */}
      <nav className="sticky top-0 z-50 flex items-center justify-between px-6 py-4">
        <Link href="/" className="glass-pill flex items-center gap-2 px-4 py-3.5 no-underline">
          <MiniMateLogo size={20} color="#34C759" />
          <span className="text-[20px] font-medium tracking-tight leading-none" style={{ color: "var(--text)" }}>Mini Mate</span>
        </Link>
        <div className="glass-pill flex items-center px-2 py-2">
          <Link
            href="/pricing"
            className="rounded-full px-4 py-1.5 text-sm text-white font-semibold hover:opacity-90 transition-opacity"
            style={{ background: "#34C759" }}
          >
            Course Pricing
          </Link>
        </div>
      </nav>

      {/* Hero */}
      <section className="pt-20 pb-12 px-6 text-center">
        <h1 className="text-4xl font-bold mb-4" style={{ color: "var(--text)" }}>
          List your mini golf course on Mini Mate
        </h1>
        <p className="max-w-xl mx-auto text-lg leading-relaxed" style={{ color: "var(--text-muted)" }}>
          Get discovered by players near you. We verify your venue exists on the map before reaching
          out — so you know we&apos;re the real deal.
        </p>
      </section>

      {/* Benefits */}
      <section className="py-14 px-6 mb-12" style={{ background: "var(--surface-sub)" }}>
        <div className="max-w-4xl mx-auto grid grid-cols-1 sm:grid-cols-3 gap-8 text-center">
          {[
            { icon: MapPin, title: "Verified Listing", desc: "We confirm your business on the map before contacting you." },
            { icon: Users, title: "Player Discovery", desc: "Thousands of players search for courses near them every day." },
            { icon: Smartphone, title: "Digital Scorecards", desc: "Players score their rounds at your course inside the app." },
          ].map((b) => (
            <div key={b.title}>
              <div className="flex justify-center mb-3" style={{ color: "#34C759" }}>
                <b.icon size={28} />
              </div>
              <h3 className="font-semibold mb-1" style={{ color: "var(--text)" }}>{b.title}</h3>
              <p className="text-sm" style={{ color: "var(--text-muted)" }}>{b.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Course Finder */}
      <section className="max-w-3xl mx-auto px-6 pb-24">
        <div className="glass-card p-8">
          <h2 className="text-2xl font-bold mb-2" style={{ color: "var(--text)" }}>
            Find your course
          </h2>
          <p className="text-sm mb-6" style={{ color: "var(--text-muted)" }}>
            We&apos;ll search Foursquare to verify your business is on the map, then show you the best way
            to reach you — email first, then phone, website, or mail as a last resort.
          </p>

          <button
            onClick={findNearMe}
            disabled={search.loading}
            className="w-full rounded-full py-3.5 text-white font-semibold hover:opacity-90 disabled:opacity-50 transition-opacity mb-4"
            style={{ background: "#34C759" }}
          >
            {search.loading ? "Searching nearby…" : (
              <span className="flex items-center justify-center gap-2">
                <Map size={16} />
                Search mini golf courses near me
              </span>
            )}
          </button>

          {search.error && (
            <p className="text-sm mb-4" style={{ color: "#FF383C" }}>{search.error}</p>
          )}

          {search.results.length > 0 && (
            <>
              <input
                type="text"
                placeholder="Filter by name…"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                className="w-full rounded-[25px] px-5 py-3 text-sm mb-4 focus:outline-none"
                style={{
                  background: "var(--surface-input)",
                  color: "var(--text)",
                  border: "1px solid var(--glass-border)",
                }}
              />
              <ul className="space-y-2 max-h-80 overflow-y-auto pr-1">
                {displayed.map((item) => (
                  <li key={item.placeID}>
                    <button
                      onClick={() => setSelected(item)}
                      className="w-full text-left rounded-[17px] px-4 py-3 transition-all hover:opacity-90"
                      style={
                        selected?.placeID === item.placeID
                          ? { background: "rgba(52,199,89,0.15)", border: "1px solid #34C759" }
                          : { background: "var(--surface-input)", border: "1px solid var(--glass-border)" }
                      }
                    >
                      <p className="font-medium text-sm" style={{ color: "var(--text)" }}>{item.name}</p>
                      <p className="text-xs mt-0.5" style={{ color: "var(--text-muted)" }}>
                        {distanceLabel(item) ?? item.address.shortAddress}
                      </p>
                    </button>
                  </li>
                ))}
              </ul>
            </>
          )}
        </div>

        {selected && <ContactCard course={selected} />}
      </section>
    </div>
  );
}

function ContactCard({ course }: { course: MapItemDTO }) {
  const contact = {
    email: null,
    phone: course.phoneNumber,
    website: course.url,
    address: course.address.fullAddress,
  };
  const primary = resolveContactRoute(contact);
  const all = getAllContactRoutes(contact);
  const methodIcons: Record<string, string> = {
    email: "✉️",
    phone: "📞",
    website: "🌐",
    mail: "📬",
  };

  return (
    <div className="glass-card mt-6 p-8">
      <h3 className="text-lg font-bold mb-1" style={{ color: "var(--text)" }}>{course.name}</h3>
      <p className="text-sm mb-6" style={{ color: "var(--text-muted)" }}>{course.address.fullAddress}</p>

      {primary ? (
        <>
          <p className="text-sm mb-3" style={{ color: "var(--text-muted)" }}>
            Best contact method for this business:
          </p>
          <a
            href={
              primary.method === "email"
                ? `mailto:${primary.value}`
                : primary.method === "phone"
                ? `tel:${primary.value}`
                : primary.method === "website"
                ? primary.value
                : "#"
            }
            target={primary.method === "website" ? "_blank" : undefined}
            rel="noopener noreferrer"
            className="flex items-center gap-3 rounded-full px-6 py-3.5 text-white font-semibold text-sm hover:opacity-90 transition-opacity mb-4"
            style={{ background: "#34C759" }}
          >
            <span>{methodIcons[primary.method]}</span>
            {primary.label}
          </a>

          {all.length > 1 && (
            <div>
              <p className="text-xs mb-2" style={{ color: "var(--text-muted)" }}>All available contacts:</p>
              <div className="flex flex-wrap gap-2">
                {all.map((c) => (
                  <span
                    key={c.method}
                    className="rounded-full px-3 py-1 text-xs"
                    style={{ background: "var(--surface-input)", color: "var(--text-sub)", border: "1px solid var(--glass-border)" }}
                  >
                    {methodIcons[c.method]} {c.value}
                  </span>
                ))}
              </div>
            </div>
          )}
        </>
      ) : (
        <p className="text-sm" style={{ color: "var(--text-muted)" }}>
          No direct contact info found. We&apos;ll reach out by mail:{" "}
          <span className="font-medium" style={{ color: "var(--text)" }}>{course.address.fullAddress}</span>
        </p>
      )}

      <div className="mt-6 pt-6" style={{ borderTop: "1px solid var(--glass-border)" }}>
        <p className="text-xs" style={{ color: "var(--text-muted)" }}>
          Foursquare ID: <code className="font-mono" style={{ color: "var(--text-sub)" }}>{course.placeID}</code>
        </p>
      </div>
    </div>
  );
}
