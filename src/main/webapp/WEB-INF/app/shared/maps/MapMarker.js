import React from 'react'
import { useMap } from 'app/shared/maps/GoogleMap'

/**
 * Places a marker on the enclosing <GoogleMap>. If center is true, the map is
 * centered on the marker (and zoomed to the given zoom level) when it is placed.
 * infoContent (optional) is a function returning the DOM node shown in an info
 * window when the marker is clicked. A string would be parsed as HTML, so build
 * nodes from untrusted data instead. Renders nothing; the marker is removed when
 * this unmounts.
 */
export default function MapMarker({ lat, lon, title, center = false, zoom, infoContent }) {
  const map = useMap()
  // Read through a ref so a new function each render doesn't re-create the marker.
  const infoContentRef = React.useRef(infoContent)
  infoContentRef.current = infoContent
  const hasInfo = !!infoContent

  React.useEffect(() => {
    const maps = window.google.maps
    const position = { lat: Number(lat), lng: Number(lon) }
    const marker = new maps.Marker({ map, position, title })
    let infoWindow = null
    if (hasInfo) {
      infoWindow = new maps.InfoWindow()
      marker.addListener('click', () => {
        infoWindow.setContent(infoContentRef.current())
        infoWindow.open(map, marker)
      })
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
  }, [ map, lat, lon, title, center, zoom, hasInfo ])

  return null
}
