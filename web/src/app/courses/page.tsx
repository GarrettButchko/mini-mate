"use client";
import { useState } from "react";
import Link from "next/link";
import type { MapItemDTO } from "@/lib/foursquare";
import { resolveContactRoute, getAllContactRoutes } from "@/lib/contactRouter";
import { SearchResultBusinessLogic } from "@/logic/SearchResultBusinessLogic";

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
    <div className="min-h-screen bg-white dark:bg-zinc-950 font-[family-name:var(--font-geist-sans)]">
      <nav className="flex items-center justify-between px-8 py-5 border-b border-zinc-100 dark:border-zinc-800">
        <Link href="/" className="text-xl font-bold tracking-tight text-green-600">⛳ Mini-Mate</Link>
        <Link href="/pricing" className="rounded-full bg-green-600 px-4 py-2 text-sm text-white font-medium hover:bg-green-700 transition-colors">
          Player Pricing
        </Link>
      </nav>

      {/* Hero */}
      <section className="pt-20 pb-12 px-6 text-center">
        <h1 className="text-4xl font-bold text-zinc-900 dark:text-white mb-4">
          List your mini golf course on Mini-Mate
        </h1>
        <p className="max-w-xl mx-auto text-zinc-500 dark:text-zinc-400 text-lg leading-relaxed">
          Get discovered by players near you. We verify your venue exists on the map before reaching
          out — so you know we&apos;re the real deal.
        </p>
      </section>

      {/* Benefits */}
      <section className="bg-zinc-50 dark:bg-zinc-900 py-14 px-6 mb-12">
        <div className="max-w-4xl mx-auto grid grid-cols-1 sm:grid-cols-3 gap-8 text-center">
          {[
            { icon: "📍", title: "Verified Listing", desc: "We confirm your business on the map before contacting you." },
            { icon: "👥", title: "Player Discovery", desc: "Thousands of players search for courses near them every day." },
            { icon: "📲", title: "Digital Scorecards", desc: "Players score their rounds at your course inside the app." },
          ].map((b) => (
            <div key={b.title}>
              <div className="text-3xl mb-3">{b.icon}</div>
              <h3 className="font-semibold text-zinc-900 dark:text-white mb-1">{b.title}</h3>
              <p className="text-sm text-zinc-500 dark:text-zinc-400">{b.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Course Finder */}
      <section className="max-w-3xl mx-auto px-6 pb-24">
        <div className="rounded-2xl border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-800 p-8">
          <h2 className="text-2xl font-bold text-zinc-900 dark:text-white mb-2">Find your course</h2>
          <p className="text-zinc-500 dark:text-zinc-400 text-sm mb-6">
            We&apos;ll search Foursquare to verify your business is on the map, then show you the best way
            to reach you — email first, then phone, website, or mail as a last resort.
          </p>

          <button
            onClick={findNearMe}
            disabled={search.loading}
            className="w-full rounded-xl bg-green-600 py-3 text-white font-semibold hover:bg-green-700 disabled:opacity-60 transition-colors mb-4"
          >
            {search.loading ? "Searching nearby…" : "🗺️ Search mini golf courses near me"}
          </button>

          {search.error && (
            <p className="text-sm text-red-500 mb-4">{search.error}</p>
          )}

          {search.results.length > 0 && (
            <>
              <input
                type="text"
                placeholder="Filter by name…"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                className="w-full rounded-xl border border-zinc-200 dark:border-zinc-600 bg-zinc-50 dark:bg-zinc-700 px-4 py-2.5 text-sm text-zinc-900 dark:text-white mb-4 focus:outline-none focus:ring-2 focus:ring-green-500"
              />
              <ul className="space-y-3 max-h-80 overflow-y-auto pr-1">
                {displayed.map((item) => (
                  <li key={item.placeID}>
                    <button
                      onClick={() => setSelected(item)}
                      className={`w-full text-left rounded-xl border px-4 py-3 transition-colors ${
                        selected?.placeID === item.placeID
                          ? "border-green-500 bg-green-50 dark:bg-green-900/20"
                          : "border-zinc-100 dark:border-zinc-700 hover:bg-zinc-50 dark:hover:bg-zinc-700"
                      }`}
                    >
                      <p className="font-medium text-zinc-900 dark:text-white text-sm">{item.name}</p>
                      <p className="text-xs text-zinc-400 mt-0.5">{distanceLabel(item) ?? item.address.shortAddress}</p>
                    </button>
                  </li>
                ))}
              </ul>
            </>
          )}
        </div>

        {/* Selected course contact card */}
        {selected && <ContactCard course={selected} />}
      </section>
    </div>
  );
}

function ContactCard({ course }: { course: MapItemDTO }) {
  const contact = {
    email: null, // Foursquare doesn't return email — collected after verification
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
    <div className="mt-6 rounded-2xl border border-zinc-200 dark:border-zinc-700 bg-white dark:bg-zinc-800 p-8">
      <h3 className="text-lg font-bold text-zinc-900 dark:text-white mb-1">{course.name}</h3>
      <p className="text-sm text-zinc-400 mb-6">{course.address.fullAddress}</p>

      {primary ? (
        <>
          <p className="text-sm text-zinc-500 dark:text-zinc-400 mb-3">
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
            className="flex items-center gap-3 rounded-xl bg-green-600 px-5 py-3 text-white font-semibold text-sm hover:bg-green-700 transition-colors mb-4"
          >
            <span>{methodIcons[primary.method]}</span>
            {primary.label}
          </a>

          {all.length > 1 && (
            <div>
              <p className="text-xs text-zinc-400 mb-2">All available contacts:</p>
              <div className="flex flex-wrap gap-2">
                {all.map((c) => (
                  <span
                    key={c.method}
                    className="rounded-full border border-zinc-200 dark:border-zinc-600 px-3 py-1 text-xs text-zinc-500 dark:text-zinc-300"
                  >
                    {methodIcons[c.method]} {c.value}
                  </span>
                ))}
              </div>
            </div>
          )}
        </>
      ) : (
        <p className="text-sm text-zinc-400">
          No direct contact info found. We&apos;ll reach out by mail:{" "}
          <span className="font-medium text-zinc-700 dark:text-zinc-200">{course.address.fullAddress}</span>
        </p>
      )}

      <div className="mt-6 pt-6 border-t border-zinc-100 dark:border-zinc-700">
        <p className="text-xs text-zinc-400">
          Foursquare ID: <code className="font-mono text-zinc-500">{course.placeID}</code>
        </p>
      </div>
    </div>
  );
}
