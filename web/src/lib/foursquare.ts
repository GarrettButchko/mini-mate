// Mirrors FoursquareSearchService.kt — same API, same field list
export interface CoordinateDTO {
  latitude: number;
  longitude: number;
}

export interface AddressDTO {
  fullAddress: string;
  shortAddress: string;
}

export interface MapItemDTO {
  placeID: string;
  name: string;
  email: string | null;
  phoneNumber: string | null;
  url: string | null;
  address: AddressDTO;
  coordinate: CoordinateDTO;
}

interface FsqPlace {
  fsq_place_id: string;
  name: string;
  latitude: number;
  longitude: number;
  location?: { formatted_address?: string; address?: string };
  distance?: number;
  email?: string;
  tel?: string;
  website?: string;
}

interface FsqSearchResponse {
  results: FsqPlace[];
}

export async function searchMiniGolf(
  center: CoordinateDTO,
  radiusInMeters = 50000,
  limit = 10
): Promise<MapItemDTO[]> {
  const apiKey = process.env.FOURSQUARE_API_KEY;
  if (!apiKey) throw new Error("FOURSQUARE_API_KEY not set");

  const params = new URLSearchParams({
    ll: `${center.latitude},${center.longitude}`,
    radius: String(radiusInMeters),
    query: "mini golf",
    sort: "DISTANCE",
    limit: String(limit),
    fields: "fsq_place_id,name,categories,latitude,longitude,location,distance,email,tel,website",
  });

  const res = await fetch(`https://places-api.foursquare.com/places/search?${params}`, {
    headers: {
      Authorization: `Bearer ${apiKey}`,
      "X-Places-Api-Version": "2025-06-17",
      Accept: "application/json",
    },
  });

  if (!res.ok) return [];

  const body: FsqSearchResponse = await res.json();

  return body.results.map((r) => ({
    placeID: r.fsq_place_id,
    name: r.name,
    email: r.email ?? null,
    phoneNumber: r.tel ?? null,
    url: r.website ?? null,
    address: {
      fullAddress: r.location?.formatted_address ?? "",
      shortAddress: r.location?.address ?? r.location?.formatted_address ?? "N/A",
    },
    coordinate: { latitude: r.latitude, longitude: r.longitude },
  }));
}
