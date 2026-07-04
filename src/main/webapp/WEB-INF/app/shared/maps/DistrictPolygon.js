import React from 'react'
import { useMap } from 'app/shared/maps/GoogleMap'

/**
 * Draws a district boundary on the enclosing <GoogleMap>.
 *
 * geom is the PolygonMapView format returned by the API with showMaps=true:
 * a list of polygons, each a list of [lat, lon] points. If fitBounds is true,
 * the map viewport is adjusted to frame the district. Hovering highlights the
 * polygon and shows the district name in the #mapTooltip element (matching the
 * legacy mapService behavior). onClick (optional) makes the polygon clickable;
 * highlight fills it with the legacy selection yellow. Renders nothing; the
 * polygons are removed when this unmounts.
 */
export default function DistrictPolygon({ geom, name, color = 'teal', fillOpacity = 0.3,
                                           fitBounds = false, onClick, highlight = false }) {
  const map = useMap()
  const onClickRef = React.useRef(onClick)
  onClickRef.current = onClick
  const hasClick = !!onClick

  React.useEffect(() => {
    if (!geom || geom.length === 0) {
      return undefined
    }
    const maps = window.google.maps
    const tooltipEl = document.getElementById('mapTooltip')
    const fillColor = highlight ? '#ffcc00' : color
    const baseOpacity = highlight ? 0.6 : fillOpacity
    const polygons = geom.map((points) => {
      const polygon = new maps.Polygon({
        map,
        paths: points.map(([ lat, lng ]) => ({ lat, lng })),
        strokeColor: color,
        strokeOpacity: 1,
        strokeWeight: 1.5,
        fillColor: fillColor,
        fillOpacity: baseOpacity,
        clickable: true,
      })
      if (hasClick) {
        polygon.addListener('click', () => onClickRef.current?.())
      }
      polygon.addListener('mouseover', () => {
        polygon.setOptions({ fillOpacity: baseOpacity - 0.2 })
      })
      polygon.addListener('mousemove', (e) => {
        if (tooltipEl && name && e.domEvent) {
          tooltipEl.style.display = 'block'
          tooltipEl.style.top = `${e.domEvent.clientY + 20}px`
          tooltipEl.style.left = `${e.domEvent.clientX}px`
          tooltipEl.textContent = name
        }
      })
      polygon.addListener('mouseout', () => {
        polygon.setOptions({ fillOpacity: baseOpacity })
        if (tooltipEl) {
          tooltipEl.style.display = 'none'
        }
      })
      return polygon
    })

    if (fitBounds) {
      const bounds = new maps.LatLngBounds()
      geom.forEach((points) => points.forEach(([ lat, lng ]) => bounds.extend({ lat, lng })))
      map.fitBounds(bounds)
    }
    return () => {
      polygons.forEach((polygon) => polygon.setMap(null))
      if (tooltipEl) {
        tooltipEl.style.display = 'none'
      }
    }
  }, [ map, geom, name, color, fillOpacity, fitBounds, hasClick, highlight ])

  return null
}
