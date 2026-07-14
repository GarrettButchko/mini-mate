"use client";
import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { AnimatePresence, motion } from "framer-motion";
import { Search, ArrowRight, Map } from "lucide-react";
import type { MapItemDTO } from "@/lib/foursquare";
import { distanceInMeters, metersToMiles, buildSubtitle } from "@/lib/SearchResultBusinessLogic";
import PageShell from "@/components/PageShell";
import SiteNav from "@/components/SiteNav";
import SiteFooter from "@/components/SiteFooter";

const TEST_COURSE: MapItemDTO = {
  placeID: "test-minimatehq",
  name: "Mini Mate HQ (Test)",
  address: { fullAddress: "412 Greenbriar Dr", shortAddress: "412 Greenbriar Dr" },
  coordinate: { latitude: 41.497, longitude: -82.004 },
  phoneNumber: "+14403207664",
  url: null,
  email: "garrettwm2005@gmail.com",
};

interface SearchState {
  loading: boolean;
  results: MapItemDTO[];
  error: string | null;
  userLat: number | null;
  userLng: number | null;
}

export default function CoursesPage() {
  const router = useRouter();
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
        setSearch({ loading: false, results: data.results ?? [], error: data.error ?? null, userLat: latitude, userLng: longitude });
      },
      () => setSearch((s) => ({ ...s, loading: false, error: "Location permission denied." }))
    );
  }

  const foursquareFiltered = query
    ? search.results.filter((r) => r.name.toLowerCase().includes(query.toLowerCase()))
    : search.results;
  const testVisible = !query || TEST_COURSE.name.toLowerCase().includes(query.toLowerCase());
  const displayed = [...(testVisible ? [TEST_COURSE] : []), ...foursquareFiltered];

  function distanceLabel(item: MapItemDTO): string | null {
    if (search.userLat == null || search.userLng == null) return null;
    const meters = distanceInMeters(search.userLat, search.userLng, item.coordinate.latitude, item.coordinate.longitude);
    return buildSubtitle(metersToMiles(meters), item.address.shortAddress);
  }

  function handleContinue() {
    if (!selected) return;
    sessionStorage.setItem(`course_${selected.placeID}`, JSON.stringify(selected));
    router.push(`/pricing?courseId=${encodeURIComponent(selected.placeID)}&courseName=${encodeURIComponent(selected.name)}`);
  }

  return (
    <PageShell>
      <SiteNav position="fixed" logoColor="#AF52DE" />

      {/* Gradient hero */}
      <section className="hero-gradient-courses text-white">
        <div className="max-w-3xl mx-auto px-6 pt-36 pb-16 text-center">
          <h1 className="text-5xl sm:text-6xl font-bold tracking-tight mb-4">
            Find your course
          </h1>
          <p className="text-lg leading-relaxed max-w-md mx-auto" style={{ color: "rgba(255,255,255,0.82)" }}>
            Search for your venue below. Once you find it, pick a plan and we&apos;ll send your setup code.
          </p>
        </div>
      </section>

      {/* Search section */}
      <section className="max-w-3xl mx-auto w-full px-6 py-10 pb-24">
        {/* Near-me button */}
        <button
          onClick={findNearMe}
          disabled={search.loading}
          className="w-full font-semibold text-white hover:opacity-90 hover:scale-105 active:scale-95 disabled:opacity-50 disabled:hover:scale-100 transition-all duration-200 mb-4 flex items-center justify-center gap-2"
          style={{ background: "#AF52DE", borderRadius: 9999, height: 50, fontSize: 15 }}
        >
          {search.loading ? "Searching nearby…" : (
            <>
              <Map size={16} />
              Search mini golf near me
            </>
          )}
        </button>

        {search.error && (
          <p className="text-sm mb-4" style={{ color: "#FF383C" }}>{search.error}</p>
        )}

        {/* Search + results card */}
        <div className="glass-card p-4">
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.25 }}
            className="relative"
            style={{ height: 330 }}
          >
            {/* Scrollable grouped list */}
            <div className="absolute inset-0 overflow-y-auto no-scrollbar" style={{ paddingTop: 58 }}>
              <motion.div
                style={{ borderRadius: 17, background: "var(--surface-two)", overflow: "hidden" }}
                initial="hidden"
                animate="show"
                variants={{ show: { transition: { staggerChildren: 0.04 } } }}
              >
                {displayed.map((item, i) => (
                  <motion.div
                    key={item.placeID}
                    variants={{
                      hidden: { opacity: 0, y: 6 },
                      show: { opacity: 1, y: 0, transition: { duration: 0.18, ease: "easeOut" } },
                    }}
                  >
                    {i > 0 && (
                      <div style={{ height: 1, background: "var(--glass-border)", marginLeft: 16 }} />
                    )}
                    <button
                      onClick={() => setSelected(item)}
                      className="w-full text-left px-4 py-3.5 transition-all hover:opacity-70"
                      style={
                        selected?.placeID === item.placeID
                          ? { background: "rgba(175,82,222,0.12)", display: "block" }
                          : { background: "transparent", display: "block" }
                      }
                    >
                      <p className="font-semibold text-sm" style={{ color: "var(--text)" }}>{item.name}</p>
                      <p className="text-xs mt-0.5" style={{ color: "var(--text-muted)" }}>
                        {distanceLabel(item) ?? item.address.shortAddress}
                      </p>
                    </button>
                  </motion.div>
                ))}
              </motion.div>
            </div>

            {/* Search bar */}
            <div
              className="absolute top-0 left-0 right-0 z-10 flex items-center gap-3 px-4"
              style={{
                height: 50,
                borderRadius: 17,
                background: "var(--surface-two)",
              }}
            >
              <Search size={16} style={{ color: "var(--text-muted)", flexShrink: 0 }} />
              <input
                type="text"
                placeholder="Search courses"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                className="flex-1 bg-transparent text-sm focus:outline-none"
                style={{ color: "var(--text)" }}
              />
            </div>
          </motion.div>
        </div>

        {/* Selected course confirm */}
        <AnimatePresence>
          {selected && (
            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 10 }}
              transition={{ duration: 0.25, ease: "easeOut" }}
              className="glass-card mt-4 p-5 flex items-center justify-between gap-4"
            >
              <div className="min-w-0">
                <p className="font-semibold text-sm truncate" style={{ color: "var(--text)" }}>{selected.name}</p>
                <p className="text-xs mt-0.5 truncate" style={{ color: "var(--text-muted)" }}>{selected.address.shortAddress}</p>
              </div>
              <button
                onClick={handleContinue}
                className="flex items-center gap-2 text-sm font-semibold text-white hover:opacity-90 hover:scale-105 active:scale-95 transition-all duration-200 shrink-0"
                style={{ background: "#AF52DE", borderRadius: 9999, height: 44, padding: "0 20px" }}
              >
                Continue
                <ArrowRight size={15} />
              </button>
            </motion.div>
          )}
        </AnimatePresence>
      </section>

      <SiteFooter links={[
        { href: "/", label: "For Players" },
        { href: "/for-courses", label: "For Courses" },
        { href: "/privacy", label: "Privacy Policy" },
        { href: "/terms", label: "Terms" },
        { href: "mailto:garrettwm2005@gmail.com", label: "Support" },
      ]} />
    </PageShell>
  );
}
