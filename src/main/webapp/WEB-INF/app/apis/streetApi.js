import fetchJson from 'app/apis/fetchJson'

const BASE_API = '/api/v2'

/**
 * Looks up the Board of Elections street ranges for a zip5 code via
 * /api/v2/street/lookup. Returns the StreetResponse JSON, with the ranges in `streets`.
 */
export function fetchStreets(zip5) {
  return fetchJson(`${BASE_API}/street/lookup?zip5=${encodeURIComponent(zip5)}`)
}
