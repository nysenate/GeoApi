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
 *
 * The polygons are only rebuilt when the geometry itself changes; style changes
 * (highlight, color, fillOpacity) and fitBounds are applied to the existing
 * polygons, which matters in the all-districts view where a highlight toggle
 * would otherwise recreate every district's geometry.
 */
export default function DistrictPolygon({ geom, name, color = 'teal', fillOpacity = 0.3,
                                           fitBounds = false, onClick, highlight = false }) {
  const map = useMap()
  const polygonsRef = React.useRef([])
  const onClickRef = React.useRef(onClick)
  onClickRef.current = onClick
  const hasClick = !!onClick

  // The listeners read the current style and name through refs, so prop changes
  // take effect without re-creating the polygons.
  const baseOpacity = highlight ? 0.6 : fillOpacity
  const styleRef = React.useRef()
  styleRef.current = {
    strokeColor: color,
    fillColor: highlight ? '#ffcc00' : color,
    fillOpacity: baseOpacity,
  }
  const nameRef = React.useRef(name)
  nameRef.current = name

  React.useEffect(() => {
    if (!geom || geom.length === 0) {
      return undefined
    }
    const maps = window.google.maps
    const tooltipEl = document.getElementById('mapTooltip')
    const polygons = geom.map((points) => {
      const polygon = new maps.Polygon({
        map,
        paths: points.map(([ lat, lng ]) => ({ lat, lng })),
        strokeOpacity: 1,
        strokeWeight: 1.5,
        clickable: true,
        ...styleRef.current,
      })
      if (hasClick) {
        polygon.addListener('click', () => onClickRef.current?.())
      }
      polygon.addListener('mouseover', () => {
        polygon.setOptions({ fillOpacity: styleRef.current.fillOpacity - 0.2 })
      })
      polygon.addListener('mousemove', (e) => {
        if (tooltipEl && nameRef.current && e.domEvent) {
          tooltipEl.style.display = 'block'
          tooltipEl.style.top = `${e.domEvent.clientY + 20}px`
          tooltipEl.style.left = `${e.domEvent.clientX}px`
          tooltipEl.textContent = nameRef.current
        }
      })
      polygon.addListener('mouseout', () => {
        polygon.setOptions({ fillOpacity: styleRef.current.fillOpacity })
        if (tooltipEl) {
          tooltipEl.style.display = 'none'
        }
      })
      return polygon
    })
    polygonsRef.current = polygons
    return () => {
      polygonsRef.current = []
      polygons.forEach((polygon) => polygon.setMap(null))
      if (tooltipEl) {
        tooltipEl.style.display = 'none'
      }
    }
  }, [ map, geom, hasClick ])

  React.useEffect(() => {
    polygonsRef.current.forEach((polygon) => polygon.setOptions(styleRef.current))
  }, [ color, highlight, fillOpacity ])

  React.useEffect(() => {
    if (!fitBounds || !geom || geom.length === 0) {
      return
    }
    const bounds = new window.google.maps.LatLngBounds()
    geom.forEach((points) => points.forEach(([ lat, lng ]) => bounds.extend({ lat, lng })))
    map.fitBounds(bounds)
  }, [ map, geom, fitBounds ])

  return null
}
