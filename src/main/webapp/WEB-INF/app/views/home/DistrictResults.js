import React from 'react'
import { formatAddressLines, formatMemberName } from 'app/shared/formatters'

const MATCH_LEVEL_TEXT = {
  HOUSE: 'Showing matching results for address',
  STREET: 'Showing matching results for street',
  CITY: 'Showing matching results for city',
  ZIP5: 'Showing matching results for zip code',
}

// These types render their district number in a dedicated labeled line, so the
// generic "Code: {district}" line is redundant (mirrors the legacy displayCode()).
const TYPES_WITHOUT_CODE_LINE = [ 'senate', 'assembly', 'congressional', 'zip' ]

/**
 * Displays the outcome of a district assign lookup with the legacy stylesheet's
 * info-container cards: the geocoded address, the senator for the matched senate
 * district, and the other matched districts. Clicking a district with a map shows
 * its boundary via onShowDistrict(type); clicking an office's locate icon marks
 * it on the map via onLocateOffice(office).
 */
export default function DistrictResults({ result, onShowDistrict, onLocateOffice }) {
  if (!result) {
    return null
  }
  if (!result.districtAssigned) {
    return (
      <div id="failed-district-result">
        <div className="info-container">
          <p className="member-name" style={{ color: 'orangered' }}>No District Lookup Result</p>
          <hr/>
          <span className="message">{result.description}</span>
        </div>
      </div>
    )
  }
  return (
    <React.Fragment>
      <GeocodeSummary result={result}/>
      {result.senateAssigned &&
        <SenatorSection senate={result.districts.senate} onShowDistrict={onShowDistrict}
                        onLocateOffice={onLocateOffice}/>
      }
      <DistrictList districts={result.districts} onShowDistrict={onShowDistrict}/>
    </React.Fragment>
  )
}

function GeocodeSummary({ result }) {
  const [ viewSuggestions, setViewSuggestions ] = React.useState(false)
  if (!result.geocoded) {
    return null
  }
  const { address, geocode, matchLevel, uspsValidated } = result
  return (
    <React.Fragment>
      {MATCH_LEVEL_TEXT[matchLevel] &&
        <div className="info-container title">
          <p>{MATCH_LEVEL_TEXT[matchLevel]}</p>
        </div>
      }
      <div style={{ borderBottom: '1px solid #ddd' }} className="info-container connected">
        <table style={{ width: '100%' }}>
          <tbody>
          <tr>
            <td>
              <div className="icon-location icon-teal"></div>
            </td>
            <td>
              <p style={{ fontSize: '16px', color: '#111', whiteSpace: 'pre-line' }}>
                {formatAddressLines(address).join('\n')}
              </p>
            </td>
            <td style={{ textAlign: 'right' }}>
              {uspsValidated && <small style={{ color: 'teal' }}>USPS</small>}
            </td>
          </tr>
          <tr>
            <td>
              <div className="icon-target icon-teal"></div>
            </td>
            <td>
              <p style={{ fontSize: '16px', color: 'teal' }}>
                ({Number(geocode.lat).toFixed(6)}, {Number(geocode.lon).toFixed(6)})
              </p>
            </td>
            <td style={{ textAlign: 'right' }}>
              <small style={{ color: 'teal' }}>{geocode.method?.replace('Dao', '')}</small>
              {geocode.cached && <p className="icon-database icon-teal" style={{ color: 'teal' }}></p>}
            </td>
          </tr>
          {geocode.openLocCode &&
            <tr>
              <td>
                <div className="icon-target icon-teal"></div>
              </td>
              <td>
                <p style={{ fontSize: '16px', color: 'teal' }}>({geocode.openLocCode})</p>
              </td>
              <td style={{ textAlign: 'right' }}>
                <small style={{ color: 'teal' }}>Open Location Code</small>
              </td>
            </tr>
          }
          </tbody>
        </table>
      </div>
      <div className="info-container connected-top slim">
        {!viewSuggestions &&
          <a style={{ fontSize: '13px' }} onClick={() => setViewSuggestions(true)}>
            Did you mean something else?
          </a>
        }
        {viewSuggestions &&
          <span style={{ color: '#333', fontSize: '13px' }}>
            If the returned location is not what you intended, try entering more information
            such as the city and zip code.
          </span>
        }
      </div>
    </React.Fragment>
  )
}

function SenatorSection({ senate, onShowDistrict, onLocateOffice }) {
  const [ showOffices, setShowOffices ] = React.useState(false)
  const info = senate.member?.info
  const offices = (senate.member?.offices ?? []).filter((office) => office?.name)

  return (
    <React.Fragment>
      <div className="info-container title">
        <p className="member-name success-color">New York State Senator</p>
      </div>
      <div className="info-container clickable connected senator" title="Show Senate District Map"
           onClick={() => senate.map && onShowDistrict('senate')}>
        {info?.imageUrl &&
          <div className="senator-pic-holder">
            <a target="_blank" rel="noreferrer" href={info.url} onClick={(e) => e.stopPropagation()}>
              <img src={info.imageUrl} alt={formatMemberName(info)} className="senator-pic"/>
            </a>
          </div>
        }
        <div style={{ marginTop: '10px', overflow: 'hidden' }}>
          <table className="senator-info-district-result">
            <tbody>
            <tr>
              <td>
                <p className="senator member-name">
                  <a target="_blank" rel="noreferrer" href={info?.url}
                     onClick={(e) => e.stopPropagation()}>{formatMemberName(info)}</a>
                </p>
                <p className="senate district">Senate District {senate.district}</p>
              </td>
              <td className="right-icon-placeholder">
                {senate.map &&
                  <a title="Show Map">
                    <div className="icon-map"></div>
                  </a>
                }
              </td>
            </tr>
            </tbody>
          </table>
          <br/>
        </div>
      </div>

      {info?.email &&
        <div className="info-container connected slim" style={{ borderBottom: '1px solid #ddd' }}>
          <div className="icon-mail icon-teal" style={{ marginRight: '5px' }}></div>
          <span style={{ fontSize: '15px' }}>{info.email}</span>
        </div>
      }

      {offices.length > 0 &&
        <div className="info-container connected slim">
          <table style={{ width: '100%' }}>
            <tbody>
            <tr>
              <td>
                <a onClick={() => setShowOffices(!showOffices)}>Senator Office Locations</a>
              </td>
              <td className="right-icon-placeholder">
                <a onClick={() => setShowOffices(!showOffices)}>
                  <div className={`${showOffices ? 'icon-arrow-up4' : 'icon-arrow-down4'} icon-hover-teal`}></div>
                </a>
              </td>
            </tr>
            </tbody>
          </table>
          {showOffices && offices.map((office) =>
            <div key={office.name}
                 style={{ padding: '5px', borderTop: '1px solid #ddd', fontSize: '14px' }}>
              <table style={{ width: '100%' }}>
                <tbody>
                <tr>
                  <td><p style={{ fontSize: '16px', color: 'teal' }}>{office.name}</p></td>
                  <td className="right-icon-placeholder">
                    {office.point &&
                      <a title="Locate office" onClick={() => onLocateOffice(office)}>
                        <div className="icon-location icon-hover-teal"></div>
                      </a>
                    }
                  </td>
                </tr>
                </tbody>
              </table>
              <p>{office.address?.addr1}</p>
              <p>{office.address?.addr2}</p>
              <p>{office.address?.city}, {office.address?.state} {office.address?.zip5}</p>
              <p>Phone {office.phone}</p>
            </div>
          )}
        </div>
      }
    </React.Fragment>
  )
}

function DistrictList({ districts, onShowDistrict }) {
  const entries = Object.entries(districts)
    .filter(([ type, district ]) => district && district.district && type !== 'senate')
  if (entries.length === 0) {
    return null
  }
  return (
    <div id="success-district-results">
      <div className="info-container title connected-bottom">
        <p className="member-name success-color">Matched New York State Districts</p>
      </div>
      {entries.map(([ type, district ]) =>
        <div key={type}
             className={`info-container connected ${district.map ? 'clickable' : ''}`}
             title={district.map ? `Show ${district.displayName} Map` : undefined}
             onClick={() => district.map && onShowDistrict(type)}>
          <table style={{ width: '100%' }}>
            <tbody>
            <tr>
              <td>
                {district.member &&
                  <p className="member-name">
                    <a target="_blank" rel="noreferrer" href={district.member.info?.url}
                       onClick={(e) => e.stopPropagation()}>{formatMemberName(district.member.info)}</a>
                  </p>
                }
                {district.name && !district.member &&
                  <p className="district-name">{district.name}</p>
                }
                {district.name && district.member &&
                  <p className="district">{district.name}</p>
                }
                {!TYPES_WITHOUT_CODE_LINE.includes(type) &&
                  <p className="district">{district.displayName} Code: {district.district}</p>
                }
              </td>
              {district.map &&
                <td className="right-icon-placeholder">
                  <a title="Show Map">
                    <div className="icon-map"></div>
                  </a>
                </td>
              }
            </tr>
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
