const BASE_API = '/api/v2'

/**
 * Assigns NY districts to the given address via /api/v2/district/assign.
 * Returns the DistrictResponse JSON, including district maps (showMaps=true).
 */
export async function assignDistricts({ addr, districtSource, geocoder, uspsValidate }) {
  const params = new URLSearchParams({
    addr: addr.replace(/#/g, ''), // Pound marks mess up the query string
    uspsValidate: uspsValidate,
    showMaps: true,
  })
  if (districtSource) {
    params.set('districtSource', districtSource)
  }
  if (geocoder) {
    params.set('geocoder', geocoder)
  }
  return fetchJson(`${BASE_API}/district/assign?${params}`)
}

/** Lists the available geocoders, as [{ enumName, displayName }]. */
export function fetchGeocoderOptions() {
  return fetchJson(`${BASE_API}/geo/options`)
}

/** Lists the available district data sources, as [{ enumName, displayName }]. */
export function fetchDistrictSourceOptions() {
  return fetchJson(`${BASE_API}/district/options`)
}

async function fetchJson(url) {
  const response = await fetch(url)
  if (!response.ok) {
    throw new Error(`Request to ${url} failed with status ${response.status}`)
  }
  return response.json()
}
