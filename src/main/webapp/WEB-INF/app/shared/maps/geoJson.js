/**
 * Extends a google.maps.LatLngBounds with every position in a GeoJSON Polygon
 * or MultiPolygon geometry. GeoJSON positions are [lon, lat].
 */
export function extendGeoJsonBounds(bounds, geom) {
  if (!geom?.coordinates) {
    return bounds
  }
  // MultiPolygon nests as [polygon][ring][point]; Polygon as [ring][point].
  const polygons = geom.type === 'MultiPolygon' ? geom.coordinates : [ geom.coordinates ]
  polygons.forEach((rings) => rings.forEach((ring) =>
    ring.forEach(([ lng, lat ]) => bounds.extend({ lat, lng }))))
  return bounds
}
