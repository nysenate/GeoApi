import React from 'react'
import GoogleMap from 'app/shared/maps/GoogleMap'
import MapMarker from 'app/shared/maps/MapMarker'
import Header from 'app/shared/Header'
import ResultsPane from 'app/shared/ResultsPane'
import { reverseGeocode } from 'app/apis/geoApi'
import { formatAddressLines } from 'app/shared/formatters'
import RevGeoSearch from 'app/views/revgeo/RevGeoSearch'

/**
 * Reverse Geocode page (/revgeo): finds the closest address to a lat/lon
 * coordinate, marking it on the map and showing it in the results pane.
 */
export default function RevGeoPage() {
  const [ result, setResult ] = React.useState()
  const [ paneOpen, setPaneOpen ] = React.useState(false)
  const mapRef = React.useRef()

  // The legacy page triggered a map resize whenever the results pane
  // reserved/released its column, so the map re-frames correctly.
  React.useEffect(() => {
    if (mapRef.current) {
      window.google.maps.event.trigger(mapRef.current, 'resize')
    }
  }, [ paneOpen ])

  const onSearch = async (point) => {
    try {
      const data = await reverseGeocode(point)
      setResult(data)
      setPaneOpen(true)
    } catch (e) {
      window.alert('Failed to reverse geocode. The application did not return a response.')
    }
  }

  const revGeocoded = result?.statusCode === 0

  return (
    <div id="contentwrapper">
      <Header />

      <div id="contentcolumn" className={paneOpen ? 'sidebar' : undefined}>
        <GoogleMap className="map-canvas" onMapLoad={(map) => { mapRef.current = map }}>
          {revGeocoded &&
            <MapMarker lat={result.geocode.lat} lon={result.geocode.lon}
                       title={formatAddressLines(result.address).join(', ')} center />
          }
        </GoogleMap>
        <RevGeoSearch onSearch={onSearch} />
      </div>

      <ResultsPane open={paneOpen} onToggle={setPaneOpen} showTab={!!result}>
        <RevGeoResults result={result} />
      </ResultsPane>
    </div>
  )
}

/** The reverse geocoded address, or the failure message, as the legacy page showed them. */
function RevGeoResults({ result }) {
  if (!result) {
    return null
  }
  if (result.statusCode !== 0) {
    return (
      <div id="rev-geo-results">
        <div id="failed-geocode-result">
          <div className="info-container">
            <p className="member-name" style={{ color: 'orangered' }}>No Reverse Geocode Result</p>
            <span>{result.description}</span>
          </div>
        </div>
      </div>
    )
  }
  return (
    <div id="rev-geo-results">
      <div className="info-container">
        <p style={{ color: 'teal' }}>Reverse Geocoded Address</p>
      </div>
      <div className="info-container">
        <table style={{ width: '100%' }}>
          <tbody>
          <tr>
            <td>
              <div className="icon-location icon-teal"></div>
            </td>
            <td>
              <p style={{ fontSize: '16px', color: '#111', whiteSpace: 'pre-line' }}>
                {formatAddressLines(result.address).join('\n')}
              </p>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  )
}
