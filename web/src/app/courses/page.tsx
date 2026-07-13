"use client";
import React, { useState } from "react";
import Link from "next/link";
import { AnimatePresence, motion } from "framer-motion";
import { MapPin, Users, Smartphone, Map, Mail, Phone, Globe, Send, Search } from "lucide-react";
import type { MapItemDTO } from "@/lib/foursquare";
import { resolveContactRoute, getAllContactRoutes } from "@/lib/contactRouter";
import { SearchResultBusinessLogic } from "@/lib/SearchResultBusinessLogic";
import PageShell from "@/components/PageShell";
import SiteNav from "@/components/SiteNav";
import SiteFooter from "@/components/SiteFooter";

const searchLogic = new SearchResultBusinessLogic();

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

  const foursquareFiltered = query
    ? search.results.filter((r) => r.name.toLowerCase().includes(query.toLowerCase()))
    : search.results;
  const testVisible = !query || TEST_COURSE.name.toLowerCase().includes(query.toLowerCase());
  const displayed = [...(testVisible ? [TEST_COURSE] : []), ...foursquareFiltered];

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
    <PageShell>
      <SiteNav logoColor="#AF52DE" wrapChildren={false}>
        <Link
          href="/pricing"
          className="rounded-full px-4 py-2.5 text-base text-white font-semibold hover:opacity-90 hover:scale-105 active:scale-95 transition-all duration-200"
          style={{ background: "#AF52DE" }}
        >
          Course Pricing
        </Link>
      </SiteNav>

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
      <section className="py-14 px-6 mb-12">
        <div className="max-w-4xl mx-auto grid grid-cols-1 sm:grid-cols-3 gap-8 text-center">
          {[
            { icon: MapPin, title: "Verified Listing", desc: "We confirm your business on the map before contacting you." },
            { icon: Users, title: "Player Discovery", desc: "Thousands of players search for courses near them every day." },
            { icon: Smartphone, title: "Digital Scorecards", desc: "Players score their rounds at your course inside the app." },
          ].map((b) => (
            <div key={b.title}>
              <div className="flex justify-center mb-3" style={{ color: "#AF52DE" }}>
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
            className="w-full rounded-xl py-3.5 text-white font-semibold hover:opacity-90 hover:scale-105 active:scale-95 disabled:opacity-50 disabled:hover:scale-100 transition-all duration-200 mb-4"
            style={{ background: "#AF52DE" }}
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

          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.25 }}
            className="relative mt-4"
            style={{ height: 340 }}
          >
              {/* List scrolls behind the search bar */}
              <div className="absolute inset-0 -mx-1 px-1 overflow-y-auto no-scrollbar">
                <motion.ul
                  className="space-y-2 pb-2"
                  style={{ paddingTop: 62 }}
                  initial="hidden"
                  animate="show"
                  variants={{ show: { transition: { staggerChildren: 0.04 } } }}
                >
                  {displayed.map((item) => (
                    <motion.li
                      key={item.placeID}
                      variants={{
                        hidden: { opacity: 0, y: 8 },
                        show: { opacity: 1, y: 0, transition: { duration: 0.2, ease: "easeOut" } },
                      }}
                    >
                      <button
                        onClick={() => setSelected(item)}
                        className="w-full text-left rounded-xl px-4 py-3 transition-all hover:opacity-80"
                        style={
                          selected?.placeID === item.placeID
                            ? { background: "rgba(175,82,222,0.12)", outline: "2px solid #AF52DE", outlineOffset: 0 }
                            : { background: "var(--surface-two)" }
                        }
                      >
                        <p className="font-semibold text-sm" style={{ color: "var(--text)" }}>{item.name}</p>
                        <p className="text-xs mt-0.5" style={{ color: "var(--text-muted)" }}>
                          {distanceLabel(item) ?? item.address.shortAddress}
                        </p>
                      </button>
                    </motion.li>
                  ))}
                </motion.ul>
              </div>

              {/* Glass search bar — floats on top */}
              <div
                className="absolute top-0 left-0 right-0 z-10 flex items-center gap-3 px-4"
                style={{
                  height: 50,
                  borderRadius: 25,
                  background: "var(--glass-bg)",
                  backdropFilter: "blur(20px)",
                  WebkitBackdropFilter: "blur(20px)",
                  boxShadow: "var(--shadow)",
                }}
              >
                <Search size={16} style={{ color: "var(--text-muted)", flexShrink: 0 }} />
                <input
                  type="text"
                  placeholder="Search"
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  className="flex-1 bg-transparent text-sm focus:outline-none"
                  style={{ color: "var(--text)" }}
                />
              </div>
          </motion.div>
        </div>

        <AnimatePresence>
          {selected && (
            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 10 }}
              transition={{ duration: 0.25, ease: "easeOut" }}
            >
              <ContactCard course={selected} />
            </motion.div>
          )}
        </AnimatePresence>
      </section>

      <SiteFooter links={[
        { href: "/", label: "For Players" },
        { href: "/for-courses", label: "For Courses" },
        { href: "/pricing", label: "Pricing" },
        { href: "/privacy", label: "Privacy Policy" },
        { href: "/terms", label: "Terms" },
        { href: "mailto:garrettwm2005@gmail.com", label: "Support" },
      ]} />
    </PageShell>
  );
}

const TIERS = [
  { id: "listed", label: "Listed", price: "Free" },
  { id: "partner", label: "Partner", price: "$29/mo" },
  { id: "analytics", label: "Analytics", price: "$69/mo" },
] as const;
type TierId = (typeof TIERS)[number]["id"];

function ContactCard({ course }: { course: MapItemDTO }) {
  const [selectedTier, setSelectedTier] = useState<TierId>("listed");
  const [claiming, setClaiming] = useState(false);
  const [claimError, setClaimError] = useState<string | null>(null);

  const contact = {
    email: course.email,
    phone: course.phoneNumber,
    website: course.url,
    address: course.address.fullAddress,
  };
  const primary = resolveContactRoute(contact);
  const all = getAllContactRoutes(contact);
  const methodIcons: Record<string, React.ReactElement> = {
    email: <Mail size={16} />,
    phone: <Phone size={16} />,
    website: <Globe size={16} />,
    mail: <Send size={16} />,
  };

  async function handleClaim() {
    setClaiming(true);
    setClaimError(null);

    const body = {
      placeId: course.placeID,
      courseName: course.name,
      contact: {
        email: course.email,
        phoneNumber: course.phoneNumber,
        url: course.url,
        address: course.address,
        coordinate: course.coordinate,
      },
    };

    if (selectedTier === "listed") {
      const res = await fetch("/api/claim-course", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
      const data = await res.json();
      setClaiming(false);
      if (!res.ok) { setClaimError(data.error ?? "Something went wrong."); return; }
      window.location.href = `/courses/success?method=${data.method ?? "pending"}&course=${encodeURIComponent(course.name)}`;
    } else {
      const res = await fetch("/api/checkout", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...body, tier: selectedTier }),
      });
      const data = await res.json();
      setClaiming(false);
      if (!res.ok) { setClaimError(data.error ?? "Something went wrong."); return; }
      window.location.href = data.url;
    }
  }

  return (
    <div className="glass-card mt-6 p-8">
      <h3 className="text-lg font-bold mb-1" style={{ color: "var(--text)" }}>{course.name}</h3>
      <p className="text-sm mb-6" style={{ color: "var(--text-muted)" }}>{course.address.fullAddress}</p>

      {/* Delivery contact */}
      <div className="mb-6">
        <p className="text-sm mb-3" style={{ color: "var(--text-muted)" }}>
          {primary ? "Your course code will be sent to:" : "No digital contact found — your code will be mailed to:"}
        </p>
        {primary ? (
          <>
            <div
              className="flex items-center gap-3 rounded-full px-4 py-3"
              style={{ background: "var(--surface-two)" }}
            >
              <span style={{ color: "#AF52DE" }}>{methodIcons[primary.method]}</span>
              <span className="text-sm font-medium" style={{ color: "var(--text)" }}>{primary.value}</span>
            </div>
            {all.length > 1 && (
              <div className="mt-3">
                <p className="text-xs mb-2" style={{ color: "var(--text-muted)" }}>All available contacts:</p>
                <div className="flex flex-wrap gap-2">
                  {all.map((c) => (
                    <span
                      key={c.method}
                      className="flex items-center gap-1.5 rounded-full px-3 py-1 text-xs"
                      style={{ background: "var(--surface-two)", color: "var(--text-muted)" }}
                    >
                      <span style={{ color: "#AF52DE" }}>{methodIcons[c.method]}</span>
                      {c.value}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </>
        ) : (
          <p className="text-sm font-medium" style={{ color: "var(--text)" }}>{course.address.fullAddress}</p>
        )}
      </div>

      {/* Tier picker */}
      <div className="mb-6">
        <p className="text-sm font-semibold mb-3" style={{ color: "var(--text)" }}>Choose your plan</p>
        <div className="flex gap-2">
          {TIERS.map((t) => (
            <button
              key={t.id}
              onClick={() => setSelectedTier(t.id)}
              className="flex-1 rounded-xl px-3 py-3 text-center transition-all duration-200"
              style={
                selectedTier === t.id
                  ? { background: "rgba(175,82,222,0.12)", outline: "2px solid #AF52DE", outlineOffset: 0 }
                  : { background: "var(--surface-two)" }
              }
            >
              <p className="text-xs font-bold mb-0.5" style={{ color: selectedTier === t.id ? "#AF52DE" : "var(--text)" }}>{t.label}</p>
              <p className="text-xs" style={{ color: "var(--text-muted)" }}>{t.price}</p>
            </button>
          ))}
        </div>
      </div>

      {claimError && (
        <p className="text-sm mb-3 px-1" style={{ color: "#FF383C" }}>{claimError}</p>
      )}

      <button
        onClick={handleClaim}
        disabled={claiming}
        className="w-full rounded-xl py-3.5 text-white font-semibold hover:opacity-90 hover:scale-105 active:scale-95 disabled:opacity-50 disabled:hover:scale-100 transition-all duration-200"
        style={{ background: "#AF52DE" }}
      >
        {claiming
          ? "Setting things up…"
          : selectedTier === "listed"
          ? "Claim free listing"
          : `Subscribe to ${selectedTier.charAt(0).toUpperCase() + selectedTier.slice(1)}`}
      </button>
    </div>
  );
}
