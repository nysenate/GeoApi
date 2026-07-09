import React from 'react'
import { useMap } from 'app/shared/maps/GoogleMap'

/**
 * Draws a district boundary on the enclosing <GoogleMap>.
 *
 * geom is the raw GeoJSON geometry the API returns (a MultiPolygon, with
 * positions as [lon, lat]). It is drawn with a google.maps.Data layer, which
 * renders holes in the geometry correctly, unlike google.maps.Polygon. If
 * fitBounds is true, the map viewport is adjusted to frame the district.
 * Hovering highlights the district and shows its name in the #mapTooltip
 * element (matching the legacy mapService behavior). onClick (optional) makes
 * the district clickable; highlight fills it with the legacy selection yellow.
 * Renders nothing; the layer is removed when this unmounts.
 *
 * The geometry is only re-added when it changes; style changes (highlight,
 * color, fillOpacity) restyle the existing layer, which matters in the
 * all-districts view where a highlight toggle would otherwise recreate every
 * district's geometry.
 */
export default function DistrictPolygon({ geom, name, color = 'teal', fillOpacity = 0.3,
                                           fitBounds = false, onClick, highlight = false }) {
  const map = useMap()
  const layerRef = React.useRef()
  const onClickRef = React.useRef(onClick)
  onClickRef.current = onClick
  const hasClick = !!onClick

  // The listeners read the current style and name through refs, so prop changes
  // take effect without re-adding the geometry.
  const styleRef = React.useRef()
  styleRef.current = {
    strokeColor: color,
    strokeOpacity: 1,
    strokeWeight: 1.5,
    fillColor: highlight ? '#ffcc00' : color,
    fillOpacity: highlight ? 0.6 : fillOpacity,
  }
  const nameRef = React.useRef(name)
  nameRef.current = name

  React.useEffect(() => {
    if (!geom?.coordinates?.length) {
      return undefined
    }
    const maps = window.google.maps
    const tooltipEl = document.getElementById('mapTooltip')
    const layer = new maps.Data({ map, style: { ...styleRef.current } })
    layer.addGeoJson({ type: 'Feature', geometry: geom, properties: {} })
    if (hasClick) {
      layer.addListener('click', () => onClickRef.current?.())
    }
    // Hover is a temporary style override on top of the layer's base style.
    layer.addListener('mouseover', (e) => {
      layer.overrideStyle(e.feature, { fillOpacity: styleRef.current.fillOpacity - 0.2 })
    })
    layer.addListener('mousemove', (e) => {
      if (tooltipEl && nameRef.current && e.domEvent) {
        tooltipEl.style.display = 'block'
        tooltipEl.style.top = `${e.domEvent.clientY + 20}px`
        tooltipEl.style.left = `${e.domEvent.clientX}px`
        tooltipEl.textContent = nameRef.current
      }
    })
    layer.addListener('mouseout', (e) => {
      layer.revertStyle(e.feature)
      if (tooltipEl) {
        tooltipEl.style.display = 'none'
      }
    })
    layerRef.current = layer
    return () => {
      layerRef.current = null
      layer.setMap(null)
      if (tooltipEl) {
        tooltipEl.style.display = 'none'
      }
    }
  }, [ map, geom, hasClick ])

  React.useEffect(() => {
    layerRef.current?.setStyle({ ...styleRef.current })
  }, [ color, highlight, fillOpacity ])

  React.useEffect(() => {
    if (!fitBounds || !layerRef.current) {
      return
    }
    const bounds = new window.google.maps.LatLngBounds()
    layerRef.current.forEach((feature) =>
      feature.getGeometry().forEachLatLng((latLng) => bounds.extend(latLng)))
    if (!bounds.isEmpty()) {
      map.fitBounds(bounds)
    }
  }, [ map, geom, fitBounds ])

  return null
}
