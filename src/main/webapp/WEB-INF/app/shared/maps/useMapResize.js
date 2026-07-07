import React from 'react'

/**
 * Triggers a Google Maps resize whenever the results pane reserves/releases its
 * column, so the map re-frames correctly (the legacy pages did this with jQuery).
 */
export default function useMapResize(mapRef, paneOpen) {
  React.useEffect(() => {
    if (mapRef.current) {
      window.google.maps.event.trigger(mapRef.current, 'resize')
    }
  }, [ mapRef, paneOpen ])
}
