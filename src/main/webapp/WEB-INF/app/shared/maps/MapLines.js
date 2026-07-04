import React from 'react'
import { useMap } from 'app/shared/maps/GoogleMap'

/**
 * Draws a dashed boundary line on the enclosing <GoogleMap>, used for the
 * reference district in intersection views (matches the legacy setLines style).
 * geom is the same nested [lat, lon] format as DistrictPolygon.
 */
export default function MapLines({ geom, fitBounds = false }) {
  const map = useMap()

  React.useEffect(() => {
    if (!geom || geom.length === 0) {
      return undefined
    }
    const maps = window.google.maps
    const lineSymbol = {
      path: 'M 0,-0.5 0,0.5',
      strokeWeight: 3,
      strokeOpacity: 1,
      scale: 1,
    }
    const bounds = new maps.LatLngBounds()
    const lines = geom.map((points) => {
      const path = points.map(([ lat, lng ]) => ({ lat, lng }))
      path.forEach((point) => bounds.extend(point))
      return new maps.Polyline({
        map,
        path,
        strokeColor: '#333',
        strokeOpacity: 0,
        icons: [ { icon: lineSymbol, offset: '100%', repeat: '8px' } ],
      })
    })

    if (fitBounds) {
      map.fitBounds(bounds)
    }
    return () => lines.forEach((line) => line.setMap(null))
  }, [ map, geom, fitBounds ])

  return null
}
