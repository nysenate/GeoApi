const BASE_API = '/api/v2'

/**
 * Looks up the Board of Elections street ranges for a zip5 code via
 * /api/v2/street/lookup. Returns the StreetResponse JSON, with the ranges in `streets`.
 */
export async function fetchStreets(zip5) {
  const url = `${BASE_API}/street/lookup?zip5=${encodeURIComponent(zip5)}`
  const response = await fetch(url)
  if (!response.ok) {
    throw new Error(`Request to ${url} failed with status ${response.status}`)
  }
  return response.json()
}
