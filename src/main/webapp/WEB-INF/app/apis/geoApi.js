const BASE_API = '/api/v2'

/**
 * Reverse geocodes a coordinate via /api/v2/geo/revgeocode. Returns the
 * RevGeocodeResponse JSON, with the matched address and its geocode.
 */
export async function reverseGeocode({ lat, lon }) {
  const url = `${BASE_API}/geo/revgeocode?${new URLSearchParams({ lat, lon })}`
  const response = await fetch(url)
  if (!response.ok) {
    throw new Error(`Request to ${url} failed with status ${response.status}`)
  }
  return response.json()
}
