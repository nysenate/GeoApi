import fetchJson from 'app/apis/fetchJson'

const BASE_API = '/api/v2'

/** Lists the district types backed by shapefiles, as [{ enumName, displayName }]. */
export function fetchMapTypes() {
  return fetchJson(`${BASE_API}/map/options`)
}

/**
 * Fetches district maps of the given type via /api/v2/map/{type}.
 * With meta=true, returns name/member metadata for every district without polygons.
 * With a district code, returns that district's map; otherwise all districts of the type.
 */
export function fetchDistrictMaps(type, { district, meta } = {}) {
  const params = new URLSearchParams({ showMembers: true })
  if (meta) {
    params.set('meta', true)
  } else if (district) {
    params.set('district', district)
  }
  return fetchJson(`${BASE_API}/map/${type}?${params}`)
}

/** Finds the districts of intersectType that overlap the given source district. */
export function fetchIntersect(sourceType, sourceId, intersectType) {
  const params = new URLSearchParams({ sourceType, sourceId, intersectType })
  return fetchJson(`${BASE_API}/district/intersect?${params}`)
}
