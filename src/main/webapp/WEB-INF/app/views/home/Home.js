import React from 'react'
import GoogleMap from 'app/shared/maps/GoogleMap'
import MapMarker from 'app/shared/maps/MapMarker'
import DistrictPolygon from 'app/shared/maps/DistrictPolygon'
import Header from 'app/shared/Header'
import ResultsPane from 'app/shared/ResultsPane'
import UiBlocker from 'app/shared/UiBlocker'
import useMapResize from 'app/shared/maps/useMapResize'
import { assignDistricts } from 'app/apis/districtApi'
import { formatAddressLines, getMapName } from 'app/shared/formatters'
import DistrictSearch from 'app/views/home/DistrictSearch'
import DistrictResults from 'app/views/home/DistrictResults'

/**
 * Main SAGE page: district lookup by address, with the matched district drawn
 * on the map. Laid out with the legacy stylesheet's classes so it looks the same
 * as the old Angular page. The district maps, street finder, and reverse geocode
 * tools will be added as they are converted.
 */
export default function Home() {
  const [ loading, setLoading ] = React.useState(false)
  const [ loadingMessage, setLoadingMessage ] = React.useState('')
  const [ result, setResult ] = React.useState()
  // Which district type's boundary is drawn. Starts on senate for a new result;
  // clicking a district in the results panel focuses (fits the map to) its boundary.
  const [ shownDistrict, setShownDistrict ] = React.useState({ type: 'senate', focus: false })
  // A senator office being located on the map; replaces the address marker while set.
  const [ officeMarker, setOfficeMarker ] = React.useState()
  const [ paneOpen, setPaneOpen ] = React.useState(false)
  const mapRef = React.useRef()

  const onSearch = async (params) => {
    setLoading(true)
    setLoadingMessage(`Looking up districts for ${params.addr}`)
    setResult(undefined)
    setOfficeMarker(undefined)
    setShownDistrict({ type: 'senate', focus: false })
    try {
      setResult(await assignDistricts(params))
      setPaneOpen(true)
    } catch (e) {
      window.alert('Failed to lookup districts. The application did not return a response.')
    } finally {
      setLoading(false)
    }
  }

  useMapResize(mapRef, paneOpen)

  const onLocateOffice = (office) => {
    setOfficeMarker({
      lat: office.point.lat,
      lon: office.point.lon,
      title: `${office.name} - ${office.address.addr1}`,
    })
  }

  const shownDistrictView = result?.districts?.[shownDistrict.type]
  const shownGeom = shownDistrictView?.map
  const shownName = getMapName(shownDistrictView)

  return (
    <div id="contentwrapper">
      <Header />

      <div id="contentcolumn" className={paneOpen ? 'sidebar' : undefined}>
        <GoogleMap className="map-canvas" onMapLoad={(map) => { mapRef.current = map }}>
          {shownGeom &&
            <DistrictPolygon geom={shownGeom} name={shownName} fitBounds={shownDistrict.focus} />
          }
          {officeMarker &&
            <MapMarker lat={officeMarker.lat} lon={officeMarker.lon}
                       title={officeMarker.title} center />
          }
          {!officeMarker && result?.geocoded &&
            <MapMarker lat={result.geocode.lat} lon={result.geocode.lon}
                       title={formatAddressLines(result.address).join(', ')}
                       center={!shownDistrict.focus} zoom={15} />
          }
        </GoogleMap>
        <DistrictSearch onSearch={onSearch} />
      </div>

      <ResultsPane open={paneOpen} onToggle={setPaneOpen} showTab={!!result}>
        <DistrictResults result={result}
                         onShowDistrict={(type) => setShownDistrict({ type, focus: true })}
                         onLocateOffice={onLocateOffice} />
      </ResultsPane>

      {loading && <UiBlocker message={loadingMessage} />}

      {/* Tooltip shown when hovering district polygons (positioned by DistrictPolygon). */}
      <div id="mapTooltip"></div>
    </div>
  )
}
