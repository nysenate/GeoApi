import fetchJson from 'app/apis/fetchJson'

const BASE_API = '/api/v2'

/**
 * Reverse geocodes a coordinate via /api/v2/geo/revgeocode. Returns the
 * RevGeocodeResponse JSON, with the matched address and its geocode.
 */
export function reverseGeocode({ lat, lon }) {
  return fetchJson(`${BASE_API}/geo/revgeocode?${new URLSearchParams({ lat, lon })}`)
}
