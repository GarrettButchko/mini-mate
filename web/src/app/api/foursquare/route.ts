import { NextRequest, NextResponse } from "next/server";
import { searchMiniGolf } from "@/lib/foursquare";

export async function GET(req: NextRequest) {
  const { searchParams } = new URL(req.url);
  const lat = parseFloat(searchParams.get("lat") ?? "");
  const lng = parseFloat(searchParams.get("lng") ?? "");
  const radius = parseInt(searchParams.get("radius") ?? "50000");
  const limit = parseInt(searchParams.get("limit") ?? "10");

  if (isNaN(lat) || isNaN(lng)) {
    return NextResponse.json({ error: "lat and lng required" }, { status: 400 });
  }

  try {
    const results = await searchMiniGolf({ latitude: lat, longitude: lng }, radius, limit);
    return NextResponse.json({ results });
  } catch {
    return NextResponse.json({ error: "Search failed" }, { status: 500 });
  }
}
