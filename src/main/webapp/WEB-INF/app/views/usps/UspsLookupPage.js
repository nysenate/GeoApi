import React from 'react'
import Header from 'app/shared/Header'
import useGlobals from 'app/shared/useGlobals'

/**
 * USPS Lookup page (/usps): embeds the USPS AMS lookup tool in an iframe,
 * same as the legacy tab. The AMS URL comes from the /globals config.
 */
export default function UspsLookupPage() {
  const globals = useGlobals()

  return (
    <div id="contentwrapper">
      <Header />

      <div id="contentcolumn">
        <iframe id="uspsIframe" title="USPS Lookup" src={globals.amsUrl}></iframe>
      </div>
    </div>
  )
}
