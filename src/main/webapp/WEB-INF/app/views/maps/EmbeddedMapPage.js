import React from 'react'
import { useParams, useSearchParams } from 'react-router-dom'
import GoogleMap from 'app/shared/maps/GoogleMap'
import MapMarker from 'app/shared/maps/MapMarker'
import DistrictPolygon from 'app/shared/maps/DistrictPolygon'
import officeInfoContent from 'app/shared/maps/officeInfoContent'
import UiBlocker from 'app/shared/UiBlocker'
import { fetchDistrictMaps } from 'app/apis/mapApi'
import { formatMemberName, getMapName } from 'app/shared/formatters'

/**
 * Embedded map page (/map/{districtType}/{districtCode}), from maps.jsp: a map of
 * one district, or all districts of a type, without any surrounding UI, for
 * iframing on external sites. In the all-districts view, clicking a senate
 * district opens an info panel with its senator and marks the senator's offices.
 * County embeds with ?doh=true show Department of Health info on county clicks
 * instead. Served publicly (no whitelist) by EmbeddedMapController.
 */
export default function EmbeddedMapPage() {
  const { districtType, districtCode } = useParams()
  const [ searchParams ] = useSearchParams()
  const isDoh = districtType?.toLowerCase() === 'county' && searchParams.get('doh') === 'true'
  // Only districts of this type are clickable and show the info panel.
  const clickableType = isDoh ? 'county' : 'senate'

  const [ loading, setLoading ] = React.useState(false)
  // What is drawn on the map: {kind: 'multi'|'single', ...}
  const [ display, setDisplay ] = React.useState(null)
  // Index of the clicked district in the 'multi' view, and its info panel data.
  const [ selected, setSelected ] = React.useState(-1)
  const [ info, setInfo ] = React.useState(null)
  const [ officeMarkers, setOfficeMarkers ] = React.useState([])

  React.useEffect(() => {
    if (!districtType) {
      return
    }
    setLoading(true)
    fetchDistrictMaps(districtType, { district: districtCode })
      .then((data) => {
        if (data.statusCode !== 0) {
          return
        }
        if (data.districts) {
          setDisplay({ kind: 'multi', districts: data.districts })
        } else if (data.map) {
          setDisplay({ kind: 'single', district: data })
          if (data.type?.toLowerCase() === clickableType) {
            setOfficeMarkers(officesOf(data))
          }
        }
      })
      // Embeds fail quietly (as the legacy page did); the empty base map remains.
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [ districtType, districtCode, clickableType ])

  const onDistrictClick = (i, district) => {
    setSelected(i)
    setInfo(district)
    setOfficeMarkers(officesOf(district))
  }

  /** County DoH embeds label districts by county name only. */
  const tooltipName = (district) => {
    const name = getMapName(district)
    return isDoh ? name.split('-')[0].trim() : name
  }

  return (
    <div id="contentwrapper">
      <div id="mapcontentcolumn">
        {info && <InfoPanel info={info} isDoh={isDoh}/>}
        <GoogleMap className="map-canvas">
          {display?.kind === 'multi' && display.districts.map((d, i) =>
            d.map &&
              <DistrictPolygon key={`${d.type}-${d.district}`} geom={d.map} name={tooltipName(d)}
                               highlight={i === selected}
                               onClick={d.type?.toLowerCase() === clickableType
                                 ? () => onDistrictClick(i, d) : undefined}/>
          )}
          {display?.kind === 'single' && display.district.map &&
            <DistrictPolygon geom={display.district.map} name={tooltipName(display.district)}
                             fitBounds/>
          }
          {officeMarkers.map((office) =>
            <MapMarker key={office.name} lat={office.point.lat} lon={office.point.lon}
                       title={`${office.name} - ${office.address?.addr1}`}
                       infoContent={officeInfoContent(office)}/>
          )}
        </GoogleMap>
      </div>

      {loading && <UiBlocker message="Loading maps.."/>}

      {/* Tooltip shown when hovering district polygons (positioned by DistrictPolygon). */}
      <div id="mapTooltip"></div>
    </div>
  )
}

function officesOf(district) {
  return (district.member?.offices ?? []).filter((office) => office?.name && office.point)
}

/** The collapsible senator (or DoH) details card overlaying the map, from maps.jsp. */
function InfoPanel({ info, isDoh }) {
  const [ open, setOpen ] = React.useState(true)
  const member = info.member?.info

  return (
    <div className="info-container"
         style={{ width: '280px', padding: '3px 10px', position: 'absolute', left: '40px', zIndex: 10000 }}>
      <table style={{ width: '100%' }}>
        <tbody>
        <tr>
          <td>
            <a onClick={() => setOpen(!open)}>{isDoh ? 'DoH Information' : 'Senator Information'}</a>
          </td>
          <td className="right-icon-placeholder">
            <a onClick={() => setOpen(!open)}>
              <div className={open ? 'icon-arrow-up4' : 'icon-arrow-down4'}></div>
            </a>
          </td>
        </tr>
        </tbody>
      </table>
      {open &&
        <div id="senator-view" style={{ paddingTop: '10px', borderTop: '1px solid #ddd' }}>
          {!isDoh &&
            <React.Fragment>
              {member?.imageUrl &&
                <div className="mini-senator-pic-holder">
                  <a href={member.url} target="_top">
                    <img src={member.imageUrl} alt={formatMemberName(member)} className="senator-pic"/>
                  </a>
                </div>
              }
              <div>
                <p className="senator member-name">
                  <a target="_blank" rel="noreferrer" href={member?.url}>{formatMemberName(member)}</a>
                </p>
                <p className="senate district">Senate District {info.district}</p>
              </div>
            </React.Fragment>
          }
          {isDoh &&
            <React.Fragment>
              <p className="senator district">
                <a target="_blank" rel="noreferrer" href={info.link}>Department of Health</a>
              </p>
              <p className="senate member-name">{info.name}</p>
            </React.Fragment>
          }
        </div>
      }
    </div>
  )
}
