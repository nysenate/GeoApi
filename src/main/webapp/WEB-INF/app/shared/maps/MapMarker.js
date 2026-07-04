import React from 'react'
import { useMap } from 'app/shared/maps/GoogleMap'

/**
 * Places a marker on the enclosing <GoogleMap>. If center is true, the map is
 * centered on the marker (and zoomed to the given zoom level) when it is placed.
 * infoContent (optional HTML string) is shown in an info window when the marker
 * is clicked. Renders nothing; the marker is removed when this unmounts.
 */
export default function MapMarker({ lat, lon, title, center = false, zoom, infoContent }) {
  const map = useMap()

  React.useEffect(() => {
    const maps = window.google.maps
    const position = { lat: Number(lat), lng: Number(lon) }
    const marker = new maps.Marker({ map, position, title })
    let infoWindow = null
    if (infoContent) {
      infoWindow = new maps.InfoWindow({ content: infoContent })
      marker.addListener('click', () => infoWindow.open(map, marker))
    }
    if (center) {
      map.setCenter(position)
      if (zoom != null) {
        map.setZoom(zoom)
      }
    }
    return () => {
      if (infoWindow) {
        infoWindow.close()
      }
      marker.setMap(null)
    }
  }, [ map, lat, lon, title, center, zoom, infoContent ])

  return null
}
