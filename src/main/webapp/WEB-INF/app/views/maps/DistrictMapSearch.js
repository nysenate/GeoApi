import React from 'react'
import { formatMemberName } from 'app/shared/formatters'

/**
 * The floating search panel for the District Maps page: district type select,
 * district name typeahead, member select (for member-backed types), and the
 * intersection menu. Uses the legacy stylesheet's classes so it matches the old
 * Angular page.
 */
export default function DistrictMapSearch({ districtTypes, type, onTypeChange, districtList,
                                            memberList, selectedDistrict, onSelectDistrict,
                                            showIntersectMenu, intersectType, onIntersectChange }) {
  const [ minimized, setMinimized ] = React.useState(false)
  const [ districtSearch, setDistrictSearch ] = React.useState('')
  const [ showDropdown, setShowDropdown ] = React.useState(false)
  const [ highlightIndex, setHighlightIndex ] = React.useState(0)

  // Reset the typeahead text when the type changes (the page clears the list).
  React.useEffect(() => {
    setDistrictSearch('')
  }, [ type ])

  const matchesSearch = (d) => {
    if (!districtSearch) {
      return true
    }
    const search = districtSearch.toLowerCase()
    const name = d.name.toLowerCase()
    return name.indexOf(search) === 0 || name.indexOf(' ' + search) !== -1
  }

  const selectDistrict = (d) => {
    setDistrictSearch(d.name)
    setShowDropdown(false)
    onSelectDistrict(d)
  }

  const handleKeydown = (event) => {
    const matches = districtList.filter(matchesSearch)
    if (event.keyCode === 40) {
      event.preventDefault()
      setHighlightIndex(Math.min(highlightIndex + 1, matches.length - 1))
    } else if (event.keyCode === 38) {
      event.preventDefault()
      setHighlightIndex(Math.max(highlightIndex - 1, 0))
    } else if (event.keyCode === 13) {
      if (matches.length && highlightIndex < matches.length) {
        selectDistrict(matches[highlightIndex])
      }
    }
  }

  // Delay hiding so clicks on dropdown items register first (legacy behavior).
  const hideDropdown = () => setTimeout(() => setShowDropdown(false), 200)

  const memberIndex = memberList.indexOf(selectedDistrict)

  return (
    <div id="districtMapViewSearch" className="search-container">
      <div id="districtMapFormMini">
        <div className="icon-list icon-teal"></div>
        {!minimized &&
          <React.Fragment>
            <label>Select which district(s) to display</label>
            <div onClick={() => setMinimized(true)}
                 className="collapse-search icon-arrow-up4 icon-hover-teal small-right-icon"></div>
          </React.Fragment>
        }
        {minimized &&
          <React.Fragment>
            <label onClick={() => setMinimized(false)} className="expand-search">Show search</label>
            <div onClick={() => setMinimized(false)}
                 className="expand-search icon-arrow-down4 icon-hover-teal small-right-icon"></div>
          </React.Fragment>
        }
        <br/>
        {!minimized &&
          <div className="search-container-content">
            <div className="section">
              <div style={{ float: 'left' }}>
                <label htmlFor="districtTypeMenu" className="menu-overhead">Type:</label>
                <select id="districtTypeMenu" className="menu" style={{ width: '115px' }}
                        value={type} onChange={(e) => onTypeChange(e.target.value)}>
                  <option value="" disabled hidden>Choose</option>
                  {districtTypes.map((t) =>
                    <option key={t.value} value={t.value}>{t.label}</option>
                  )}
                </select>
              </div>
              <div style={{ position: 'relative', float: 'left' }}>
                <label htmlFor="districtCodeMenu" className="menu-overhead">District name:
                  <a onClick={() => setDistrictSearch('')} className="clear-btn">Clear</a>
                </label>
                <input id="districtCodeMenu" type="text" className="menu" autoComplete="off"
                       value={districtSearch} placeholder="Type to filter..."
                       onChange={(e) => { setDistrictSearch(e.target.value); setShowDropdown(true); setHighlightIndex(0) }}
                       onFocus={() => setShowDropdown(true)}
                       onBlur={hideDropdown}
                       onKeyDown={handleKeydown}
                       style={{ width: '214px', height: '30px', fontSize: '14px' }}/>
                {showDropdown && districtList.length > 0 &&
                  <div className="typeahead-dropdown" onMouseDown={(e) => e.preventDefault()}>
                    {districtList.filter(matchesSearch).map((d, i) =>
                      <div key={`${d.district ?? 'all'}-${d.name}`}
                           className={`typeahead-item ${i === highlightIndex ? 'typeahead-item-default' : ''}`}
                           onClick={() => selectDistrict(d)}>{d.name}</div>
                    )}
                  </div>
                }
              </div>
            </div>
            {memberList.length > 0 &&
              <div style={{ padding: '5px' }}>
                <div style={{ float: 'left' }}>
                  <label htmlFor="districtMemberMenu" className="menu-overhead">Member</label>
                  <select id="districtMemberMenu" className="menu" style={{ width: '325px' }}
                          value={memberIndex >= 0 ? memberIndex : ''}
                          onChange={(e) => selectDistrict(memberList[Number(e.target.value)])}>
                    <option value="" disabled hidden></option>
                    {memberList.map((d, i) =>
                      <option key={d.district} value={i}>{formatMemberName(d.member.info, true)}</option>
                    )}
                  </select>
                </div>
              </div>
            }
            {showIntersectMenu &&
              <div style={{ marginTop: '10px', padding: '5px' }}>
                <label htmlFor="IntersectionMenu" className="menu-overhead">View intersection with:</label>
                <select id="IntersectionMenu" className="menu" style={{ width: '115px' }}
                        value={intersectType} onChange={(e) => onIntersectChange(e.target.value)}>
                  <option value="none">None</option>
                  {districtTypes.filter((t) => t.value !== type).map((t) =>
                    <option key={t.value} value={t.value}>{t.label}</option>
                  )}
                </select>
              </div>
            }
          </div>
        }
      </div>
    </div>
  )
}
