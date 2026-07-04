import React from 'react'
import GoogleMap from 'app/shared/maps/GoogleMap'
import Header from 'app/shared/Header'
import UiBlocker from 'app/shared/UiBlocker'
import { fetchStreets } from 'app/apis/streetApi'
import StreetTable from 'app/views/street/StreetTable'

/**
 * Street Finder page (/street): looks up the Board of Elections street ranges for a
 * zip5 code and lists them with their district codes. The panel overlays the map,
 * using the legacy stylesheet's ids so it looks the same as the old tab.
 */
export default function StreetFinderPage() {
  const [ zip5, setZip5 ] = React.useState('')
  const [ streets, setStreets ] = React.useState([])
  const [ searched, setSearched ] = React.useState(false)
  const [ filter, setFilter ] = React.useState('')
  const [ loading, setLoading ] = React.useState(false)

  const lookup = async (event) => {
    event.preventDefault()
    setLoading(true)
    try {
      const data = await fetchStreets(zip5)
      setStreets(data.streets ?? [])
      setSearched(true)
    } catch (e) {
      window.alert('Failed to retrieve street information.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div id="contentwrapper">
      <Header />

      <div id="contentcolumn">
        <GoogleMap className="map-canvas" />
        <div id="streetView">
          <div id="streetViewFrame">
            <div id="streetLookupSearch">
              <h3 className="section-title street-finder">Board of Elections Street Finder</h3>
              <hr className="section-title-hr"/>
              <div style={{ padding: '10px' }}>
                <p>SAGE provides a database of NYS street ranges that are associated with
                  legislative district codes.</p>
                <p>You can begin your search by entering the Zip 5 code to obtain a listing
                  of street records.</p><br/>
                <form id="streetLookupForm" onSubmit={lookup}>
                  <label htmlFor="zip5-input">Zip 5</label>
                  <div style={{ marginTop: '2px' }}>
                    <input id="zip5-input" type="text" style={{ width: '175px' }} maxLength={5}
                           placeholder="e.g. 12210" value={zip5}
                           onChange={(e) => setZip5(e.target.value)}/>
                    <button type="submit" className="submit mini">
                      <div className="icon-search icon-white-no-hover"></div>
                      <span></span>
                    </button>
                  </div>
                </form>
                {searched &&
                  <div id="streetSearchFilter">
                    <label htmlFor="street-search">Filter by street</label>
                    <div style={{ marginTop: '2px' }}>
                      <input id="street-search" type="text" style={{ width: '175px' }}
                             value={filter} onChange={(e) => setFilter(e.target.value)}/>
                    </div>
                  </div>
                }
                <div style={{ clear: 'both' }}></div>
                <div id="streetViewTableContainer">
                  <StreetTable streets={streets} filter={filter} />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {loading && <UiBlocker message="Loading street information." />}
    </div>
  )
}
