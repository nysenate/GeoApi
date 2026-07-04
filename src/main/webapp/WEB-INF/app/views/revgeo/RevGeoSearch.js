import React from 'react'

/**
 * The floating coordinate search panel for reverse geocoding, using the legacy
 * stylesheet's search-container classes to match the old Angular page.
 */
export default function RevGeoSearch({ onSearch }) {
  const [ lat, setLat ] = React.useState('')
  const [ lon, setLon ] = React.useState('')
  const [ minimized, setMinimized ] = React.useState(false)

  const onSubmit = (e) => {
    e.preventDefault()
    onSearch({ lat: lat.trim(), lon: lon.trim() })
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
              <label htmlFor="revGeoLatInput" className="menu-overhead">Latitude</label>
              <input id="revGeoLatInput" type="text" style={{ width: '80px', marginRight: '5px' }}
                     value={lat} onChange={(e) => setLat(e.target.value)}/>
            </div>
            <div style={{ float: 'left' }}>
              <label htmlFor="revGeoLonInput" className="menu-overhead">Longitude</label>
              <input id="revGeoLonInput" type="text" style={{ width: '80px', marginRight: '5px' }}
                     value={lon} onChange={(e) => setLon(e.target.value)}/>
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
