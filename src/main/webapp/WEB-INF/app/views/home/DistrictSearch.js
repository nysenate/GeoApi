import React from 'react'
import { fetchDistrictSourceOptions, fetchGeocoderOptions } from 'app/apis/districtApi'
import useAddressAutocomplete from 'app/shared/maps/useAddressAutocomplete'

/**
 * The floating address search panel for district lookup, with optional overrides
 * for the district data source, geocoder, and USPS validation. Uses the legacy
 * stylesheet's search-container classes to match the old Angular page.
 */
export default function DistrictSearch({ onSearch }) {
  const addressInputRef = React.useRef()
  const [ addr, setAddr ] = React.useState('')
  const [ minimized, setMinimized ] = React.useState(false)
  const [ showOptions, setShowOptions ] = React.useState(false)
  const [ showHelp, setShowHelp ] = React.useState(false)
  const [ geocoders, setGeocoders ] = React.useState([])
  const [ districtSources, setDistrictSources ] = React.useState([])
  const [ geocoder, setGeocoder ] = React.useState('')
  const [ districtSource, setDistrictSource ] = React.useState('')
  const [ uspsValidate, setUspsValidate ] = React.useState('true')

  React.useEffect(() => {
    fetchGeocoderOptions().then(setGeocoders).catch(() => setGeocoders([]))
    fetchDistrictSourceOptions().then(setDistrictSources).catch(() => setDistrictSources([]))
  }, [])

  useAddressAutocomplete(addressInputRef, setAddr)

  const onSubmit = (e) => {
    e.preventDefault()
    if (addr.trim().length < 5) {
      window.alert('Your address search should be at least 5 characters long. ' +
        'Please try to be as specific as possible.')
      return
    }
    onSearch({ addr: addr.trim(), districtSource, geocoder, uspsValidate })
  }

  return (
    <div id="districtInfoSearch" className="search-container">
      <form id="districtsFormMini" onSubmit={onSubmit}>
        <div className="icon-directions icon-teal"></div>
        {!minimized &&
          <React.Fragment>
            <label htmlFor="addressInput">Enter an address for district lookup</label>
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
            <div style={{ marginTop: '5px' }}>
              <input id="addressInput" type="text" value={addr} ref={addressInputRef}
                     onChange={(e) => setAddr(e.target.value)}
                     placeholder="e.g. 200 State St, Albany NY 12210"/>
              <button className="submit mini">
                <div className="icon-search icon-white-no-hover"></div>
                <span></span>
              </button>
            </div>

            <div className="options-link-container" onClick={() => setShowOptions(!showOptions)}
                 style={{ paddingLeft: '5px', borderRight: '1px solid #ddd' }}>
              <a className="options-link">{showOptions ? 'Hide options' : 'Options'}</a>
            </div>
            <div className="options-link-container" onClick={() => setShowHelp(!showHelp)}
                 style={{ paddingLeft: '10px' }}>
              <a className="options-link">{showHelp ? 'Hide help' : 'Help'}</a>
            </div>

            <br/>
            {showOptions &&
              <div id="districtInfoOptions" style={{ marginTop: '10px' }}>
                <table className="options-table">
                  <tbody>
                  <tr>
                    <td><label htmlFor="dataSourceMenu">District data source</label></td>
                    <td style={{ width: '180px' }}>
                      <OptionSelect id="dataSourceMenu" value={districtSource}
                                    onChange={setDistrictSource} options={districtSources}/>
                    </td>
                  </tr>
                  <tr>
                    <td><label htmlFor="geocoderMenu">Geocoder</label></td>
                    <td>
                      <OptionSelect id="geocoderMenu" value={geocoder}
                                    onChange={setGeocoder} options={geocoders}/>
                    </td>
                  </tr>
                  <tr>
                    <td><label htmlFor="uspsValidateMenu">USPS Validate</label></td>
                    <td>
                      <select id="uspsValidateMenu" style={{ width: '100%' }} value={uspsValidate}
                              onChange={(e) => setUspsValidate(e.target.value)}>
                        <option value="false">No</option>
                        <option value="true">Yes</option>
                      </select>
                    </td>
                  </tr>
                  </tbody>
                </table>
              </div>
            }
            {showHelp &&
              <div id="districtInfoHelp">
                <p>District lookup can be used to retrieve district information for a specific street address.
                  <br/><br/>
                </p>
                <p>A <strong>building match</strong> displays districts for a specific address and is likely
                  with an input of the following style: </p>
                <a onClick={() => setAddr('200 State St, Albany NY 12203')}>200 State St, Albany NY 12203</a>
                <br/>
                <a onClick={() => setAddr('1222 East 96th St, Brooklyn, NY 11236')}>1222 East 96th St, Brooklyn, NY
                  11236</a>
                <br/><br/>
              </div>
            }
          </div>
        }
      </form>
    </div>
  )
}

function OptionSelect({ id, value, onChange, options }) {
  return (
    <select id={id} style={{ width: '100%' }} value={value} onChange={(e) => onChange(e.target.value)}>
      <option value="">Default</option>
      {options.map((option) =>
        <option key={option.enumName} value={option.enumName}>{option.displayName}</option>
      )}
    </select>
  )
}
