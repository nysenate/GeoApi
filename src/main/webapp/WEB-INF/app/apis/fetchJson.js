/**
 * Fetches a JSON endpoint, throwing an Error if the response is not ok.
 */
export default async function fetchJson(url) {
  const response = await fetch(url)
  if (!response.ok) {
    throw new Error(`Request to ${url} failed with status ${response.status}`)
  }
  return response.json()
}
