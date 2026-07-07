import React from 'react'
import GoogleMap, { DEFAULT_CENTER, DEFAULT_ZOOM } from 'app/shared/maps/GoogleMap'
import MapMarker from 'app/shared/maps/MapMarker'
import DistrictPolygon from 'app/shared/maps/DistrictPolygon'
import MapLines from 'app/shared/maps/MapLines'
import Header from 'app/shared/Header'
import ResultsPane from 'app/shared/ResultsPane'
import UiBlocker from 'app/shared/UiBlocker'
import useMapResize from 'app/shared/maps/useMapResize'
import { fetchDistrictMaps, fetchIntersect, fetchMapTypes } from 'app/apis/mapApi'
import { formatMemberName, getMapName, POLY_COLORS } from 'app/shared/formatters'
import DistrictMapSearch from 'app/views/maps/DistrictMapSearch'
import MemberResults from 'app/views/maps/MemberResults'
import IntersectResults from 'app/views/maps/IntersectResults'

const ALL_DISTRICTS = { district: null, name: 'All districts' }

/**
 * District Maps page (/maps): browse the boundaries of any district type, view a
 * single district with its member, or intersect a district with another district
 * type to see coverage percentages.
 */
export default function DistrictMapsPage() {
  const [ districtTypes, setDistrictTypes ] = React.useState([])
  const [ type, setType ] = React.useState('')
  const [ districtList, setDistrictList ] = React.useState([])
  const [ memberList, setMemberList ] = React.useState([])
  const [ selectedDistrict, setSelectedDistrict ] = React.useState(null)
  const [ intersectType, setIntersectType ] = React.useState('none')
  const [ loading, setLoading ] = React.useState(false)
  const [ loadingMessage, setLoadingMessage ] = React.useState('')
  // What is drawn on the map: {kind: 'multi'|'single'|'intersect', ...}
  const [ display, setDisplay ] = React.useState(null)
  // Index of the clicked district polygon in the 'multi' view.
  const [ highlighted, setHighlighted ] = React.useState(-1)
  // {kind: 'member'|'intersect'} content for the results pane.
  const [ paneContent, setPaneContent ] = React.useState(null)
  const [ paneOpen, setPaneOpen ] = React.useState(false)
  const [ officeMarkers, setOfficeMarkers ] = React.useState([])
  const mapRef = React.useRef()

  React.useEffect(() => {
    fetchMapTypes()
      .then((types) => setDistrictTypes(types.map((t) => ({ value: t.enumName.toLowerCase(), label: t.displayName }))))
      .catch(() => setDistrictTypes([]))
  }, [])

  useMapResize(mapRef, paneOpen)

  const closePane = () => {
    setPaneContent(null)
    setPaneOpen(false)
  }

  const onTypeChange = (newType) => {
    setType(newType)
    setSelectedDistrict(null)
    setIntersectType('none')
    setMemberList([])
    setDistrictList([])
    fetchDistrictMaps(newType, { meta: true })
      .then((data) => {
        const members = data.districts.filter((d) => d.member != null)
          .sort((a, b) => formatMemberName(a.member.info, true)
            .localeCompare(formatMemberName(b.member.info, true)))
        setMemberList(members)
        setDistrictList([ ALL_DISTRICTS, ...data.districts ])
      })
      .catch(() => window.alert('Failed to retrieve the list of districts.'))
  }

  const showMember = (district) => {
    setPaneContent({ kind: 'member', member: district.member, districtName: district.name })
    setOfficeMarkers((district.member.offices ?? [])
      .filter((office) => office?.name && office.point)
      .map((office) => ({ office, focus: false })))
    setPaneOpen(true)
  }

  const lookup = async (district, newIntersectType) => {
    setLoadingMessage(`Loading ${type.replace('_', '/')} maps...`)
    setLoading(true)
    setHighlighted(-1)
    setOfficeMarkers([])
    try {
      // With no intersection requested (or "All districts" selected), just show the maps.
      if (newIntersectType === 'none' || newIntersectType === type || district.district == null) {
        const data = await fetchDistrictMaps(type, { district: district.district })
        if (data.statusCode !== 0) {
          closePane()
          setDisplay(null)
          window.alert('Failed to retrieve district maps.')
          return
        }
        if (data.districts) {
          setDisplay({ kind: 'multi', districts: data.districts })
          closePane()
          frameMultiView(data.districts)
        } else if (data.map) {
          setDisplay({ kind: 'single', district: data })
          if (data.member != null) {
            showMember(data)
          } else {
            closePane()
          }
        }
      } else {
        const data = await fetchIntersect(type, district.district, newIntersectType)
        if (data.statusCode != null && data.statusCode !== 0) {
          window.alert('You must select the type and district / member first. ' +
            'Same source and Intersection type is not supported')
          return
        }
        setDisplay({ kind: 'intersect', data, fullOverlap: null })
        setPaneContent({ kind: 'intersect', data })
        setPaneOpen(true)
      }
    } catch (e) {
      closePane()
      setDisplay(null)
      window.alert('Failed to retrieve district maps.')
    } finally {
      setLoading(false)
    }
  }

  /** Frames the all-districts view: NYC-only types fit the boroughs, others frame NY. */
  const frameMultiView = (districts) => {
    const map = mapRef.current
    if (!map) {
      return
    }
    if (districts[0] && districts[0].type === 'CITY_COUNCIL') {
      const bounds = new window.google.maps.LatLngBounds()
      districts.forEach((d) => d.map?.geom?.forEach((points) =>
        points.forEach(([ lat, lng ]) => bounds.extend({ lat, lng }))))
      map.fitBounds(bounds)
    } else {
      map.setCenter(DEFAULT_CENTER)
      map.setZoom(DEFAULT_ZOOM)
    }
  }

  const onSelectDistrict = (district) => {
    setSelectedDistrict(district)
    lookup(district, intersectType)
  }

  const onIntersectChange = (newIntersectType) => {
    setIntersectType(newIntersectType)
    if (selectedDistrict) {
      lookup(selectedDistrict, newIntersectType)
    }
  }

  const onLocateOffice = (office) => {
    setOfficeMarkers([ { office, focus: true } ])
  }

  const showIntersectMenu = selectedDistrict != null && selectedDistrict.district != null

  return (
    <div id="contentwrapper">
      <Header />

      <div id="contentcolumn" className={paneOpen ? 'sidebar' : undefined}>
        <GoogleMap className="map-canvas" onMapLoad={(map) => { mapRef.current = map }}>
          <MapDisplay display={display} highlighted={highlighted}
                      onDistrictClick={(i, d) => { setHighlighted(i); showMember(d) }}/>
          {officeMarkers.map(({ office, focus }) =>
            <MapMarker key={office.name} lat={office.point.lat} lon={office.point.lon}
                       title={`${office.name} - ${office.address?.addr1}`}
                       center={focus}
                       infoContent={focus ? undefined : officeInfoContent(office)}/>
          )}
        </GoogleMap>
        <DistrictMapSearch districtTypes={districtTypes} type={type} onTypeChange={onTypeChange}
                           districtList={districtList} memberList={memberList}
                           selectedDistrict={selectedDistrict} onSelectDistrict={onSelectDistrict}
                           showIntersectMenu={showIntersectMenu} intersectType={intersectType}
                           onIntersectChange={onIntersectChange}/>
      </div>

      <ResultsPane open={paneOpen} onToggle={setPaneOpen} showTab={paneContent != null}>
        {paneContent?.kind === 'member' &&
          <MemberResults member={paneContent.member} districtName={paneContent.districtName}
                         onLocateOffice={onLocateOffice}/>
        }
        {paneContent?.kind === 'intersect' &&
          <IntersectResults data={paneContent.data}
                            onShowOverlap={(i, overlap) => setDisplay({
                              ...display,
                              fullOverlap: {
                                geom: overlap.fullMap?.geom,
                                name: overlap.name,
                                color: POLY_COLORS[i % POLY_COLORS.length],
                              },
                            })}
                            onShowCoverage={() => setDisplay({ ...display, fullOverlap: null })}/>
        }
      </ResultsPane>

      {loading && <UiBlocker message={loadingMessage}/>}

      {/* Tooltip shown when hovering district polygons (positioned by DistrictPolygon). */}
      <div id="mapTooltip"></div>
    </div>
  )
}

/** Renders the map overlays for the current display mode. */
function MapDisplay({ display, highlighted, onDistrictClick }) {
  if (!display) {
    return null
  }
  if (display.kind === 'multi') {
    return display.districts.map((d, i) =>
      d.map?.geom &&
        <DistrictPolygon key={`${d.type}-${d.district}`} geom={d.map.geom} name={getMapName(d)}
                         highlight={i === highlighted}
                         onClick={d.member != null ? () => onDistrictClick(i, d) : undefined}/>
    )
  }
  if (display.kind === 'single') {
    const d = display.district
    return d.map?.geom &&
      <DistrictPolygon geom={d.map.geom} name={getMapName(d)} fitBounds/>
  }
  if (display.kind === 'intersect') {
    const { data, fullOverlap } = display
    return (
      <React.Fragment>
        {fullOverlap == null && data.overlaps.map((overlap, i) =>
          overlap.map?.geom &&
            <DistrictPolygon key={overlap.district} geom={overlap.map.geom}
                             name={`${overlap.name} Coverage`}
                             color={POLY_COLORS[i % POLY_COLORS.length]} fillOpacity={0.5}/>
        )}
        {fullOverlap != null && fullOverlap.geom &&
          <DistrictPolygon geom={fullOverlap.geom} name={fullOverlap.name} color={fullOverlap.color}/>
        }
        {data.referenceMap?.geom &&
          <MapLines geom={data.referenceMap.geom} fitBounds/>
        }
      </React.Fragment>
    )
  }
  return null
}

/** Builds the office marker popup, matching the legacy setOfficeMarkers content. */
function officeInfoContent(office) {
  return "<div style='width:160px;'>" +
    `<p style='color:teal;font-size:18px;'>${office.name}</p>` +
    `<p>${office.address?.addr1 ?? ''}</p>` +
    `<p>${office.address?.city ?? ''}, NY ${office.address?.zip5 ?? ''}</p>` +
    `<p>Phone ${office.phone ?? ''}</p>` +
    '</div>'
}
