import React from 'react'

/**
 * The floating coordinate search panel for reverse geocoding, using the legacy
 * stylesheet's search-container classes to match the old Angular page.
 */
export default function RevGeoSearch({ onSearch }) {
  const [ coord, setCoord ] = React.useState('')
  const [ minimized, setMinimized ] = React.useState(false)

  const onSubmit = (e) => {
    e.preventDefault()
    const [ lat = '', lon = '' ] = coord.trim().split(/[,\s]+/)
    onSearch({ lat, lon })
  }

  return (
    <div id="reverseGeocodeSearch" className="search-container small">
      <form id="revGeoForm" onSubmit={onSubmit}>
        <div className="icon-target icon-teal"></div>
        {!minimized &&
          <React.Fragment>
            <label>Enter geo-coordinate</label>
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
          <div className="section search-container-content">
            <div style={{ float: 'left' }}>
              <label htmlFor="revGeoCoordInput" className="menu-overhead">Latitude, Longitude</label>
              <input id="revGeoCoordInput" type="text" style={{ width: '170px', marginRight: '5px' }}
                     placeholder="42.6521, -73.7572"
                     value={coord} onChange={(e) => setCoord(e.target.value)}/>
            </div>
            <div style={{ float: 'left' }}>
              <label className="menu-overhead">&nbsp;</label>
              <button type="submit" className="submit mini">
                <div className="icon-search icon-white-no-hover"></div>
              </button>
            </div>
          </div>
        }
      </form>
    </div>
  )
}
