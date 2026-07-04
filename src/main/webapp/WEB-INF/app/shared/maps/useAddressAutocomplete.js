import React from 'react'
import useGlobals from 'app/shared/useGlobals'
import loadGoogleMaps from 'app/shared/maps/googleMapsLoader'

// Rough outline of NY State, used to bias predictions toward NY (from the legacy mapService).
const NY_BOUNDS = [
  [ 40.488737, -74.264832 ], [ 40.955011, -71.762695 ], [ 41.294317, -71.932983 ],
  [ 40.955011, -73.641357 ], [ 41.100052, -73.721008 ], [ 41.215854, -73.487549 ],
  [ 41.298444, -73.550720 ], [ 42.085994, -73.504028 ], [ 42.747012, -73.267822 ],
  [ 43.612217, -73.289795 ], [ 45.003651, -73.300781 ], [ 45.011419, -74.959717 ],
  [ 43.612217, -77.189941 ], [ 43.269206, -79.112549 ], [ 42.843751, -78.936768 ],
  [ 42.536892, -79.782715 ], [ 42.000325, -79.749756 ], [ 41.983994, -75.366211 ],
  [ 41.327326, -74.783936 ], [ 40.996484, -73.907776 ], [ 40.653555, -74.058838 ],
  [ 40.640009, -74.200287 ],
]

/**
 * Attaches Google Places autocomplete to the given text input, biased toward NY
 * addresses. When the user picks a prediction, the widget writes directly into
 * the input's DOM value, so onSelect(value) is called to sync React state.
 */
export default function useAddressAutocomplete(inputRef, onSelect) {
  const globals = useGlobals()
  // Track the latest callback so the widget doesn't need re-creating when it changes.
  const onSelectRef = React.useRef(onSelect)
  onSelectRef.current = onSelect

  React.useEffect(() => {
    let autocomplete = null
    let canceled = false
    loadGoogleMaps(globals.googleMapsUrl)
      .then((maps) => {
        if (canceled || !inputRef.current) {
          return
        }
        const bounds = new maps.LatLngBounds()
        NY_BOUNDS.forEach(([ lat, lng ]) => bounds.extend({ lat, lng }))
        autocomplete = new maps.places.Autocomplete(inputRef.current, {
          bounds,
          types: [ 'geocode' ],
          componentRestrictions: { country: 'us' },
        })
        autocomplete.addListener('place_changed', () => {
          onSelectRef.current?.(inputRef.current.value)
        })
      })
    return () => {
      canceled = true
      if (autocomplete) {
        window.google.maps.event.clearInstanceListeners(autocomplete)
      }
    }
  }, [ globals.googleMapsUrl, inputRef ])
}
