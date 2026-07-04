import React from 'react'
import useGlobals from 'app/shared/useGlobals'
import loadGoogleMaps from 'app/shared/maps/googleMapsLoader'

const MapContext = React.createContext()

/**
 * Returns the google.maps.Map instance from the nearest <GoogleMap>. For use by
 * child components that draw on the map (markers, district polygons, etc.).
 */
export function useMap() {
  return React.useContext(MapContext)
}

// Centers the map nicely over NY, matching the view the legacy mapService used.
const DEFAULT_CENTER = { lat: 42.440510, lng: -76.495460 }
const DEFAULT_ZOOM = 7

const MAP_STYLES = [
  {
    featureType: 'transit',
    stylers: [ { visibility: 'off' } ],
  },
]

/**
 * Renders a Google map sized by the given className. Children do not render into
 * the map div; they receive the map instance through useMap() once it is ready.
 * onMapLoad (optional) is called with the map instance once it is created.
 */
export default function GoogleMap({ className, children, onMapLoad }) {
  const globals = useGlobals()
  const mapDivRef = React.useRef()
  const [ map, setMap ] = React.useState()
  const onMapLoadRef = React.useRef(onMapLoad)
  onMapLoadRef.current = onMapLoad

  React.useEffect(() => {
    let canceled = false
    loadGoogleMaps(globals.googleMapsUrl)
      .then((maps) => {
        if (canceled) {
          return
        }
        const newMap = new maps.Map(mapDivRef.current, {
          center: DEFAULT_CENTER,
          zoom: DEFAULT_ZOOM,
          mapTypeId: 'roadmap',
          mapTypeControl: false,
          streetViewControl: false,
          zoomControl: true,
          zoomControlOptions: {
            position: maps.ControlPosition.LEFT_TOP,
          },
          styles: MAP_STYLES,
        })
        setMap(newMap)
        onMapLoadRef.current?.(newMap)
      })
    return () => {
      canceled = true
    }
  }, [ globals.googleMapsUrl ])

  return (
    <React.Fragment>
      <div ref={mapDivRef} className={className} />
      {map &&
        <MapContext.Provider value={map}>
          {children}
        </MapContext.Provider>
      }
    </React.Fragment>
  )
}
