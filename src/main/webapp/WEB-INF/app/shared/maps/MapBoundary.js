import React from 'react'
import { useMap } from 'app/shared/maps/GoogleMap'
import { extendGeoJsonBounds } from 'app/shared/maps/geoJson'

/**
 * Draws a dashed outline of a GeoJSON Polygon or MultiPolygon geometry on the
 * enclosing <GoogleMap>, used for the reference district in intersection views
 * (matches the legacy mapService.setBoundary style).
 */
export default function MapBoundary({ geom, fitBounds = false }) {
  const map = useMap()

  React.useEffect(() => {
    if (!geom?.coordinates?.length) {
      return undefined
    }
    const maps = window.google.maps
    const dashSymbol = {
      path: 'M 0,-0.5 0,0.5',
      strokeWeight: 3,
      strokeOpacity: 1,
      scale: 1,
    }
    const polygons = geom.type === 'MultiPolygon' ? geom.coordinates : [ geom.coordinates ]
    const lines = []
    polygons.forEach((rings) => rings.forEach((ring) => {
      // GeoJSON positions are [lon, lat].
      const path = ring.map(([ lng, lat ]) => ({ lat, lng }))
      lines.push(new maps.Polyline({
        map,
        path,
        strokeColor: '#333',
        strokeOpacity: 0,
        zIndex: 1000,
        icons: [ { icon: dashSymbol, offset: '100%', repeat: '8px' } ],
      }))
    }))

    if (fitBounds) {
      const bounds = extendGeoJsonBounds(new maps.LatLngBounds(), geom)
      if (!bounds.isEmpty()) {
        map.fitBounds(bounds)
      }
    }
    return () => lines.forEach((line) => line.setMap(null))
  }, [ map, geom, fitBounds ])

  return null
}
