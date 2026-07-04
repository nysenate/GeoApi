import React from 'react'


/**
 * Creates a global object to store constants and config params needed by the front end.
 *
 * These global config values can be accessed in any component like "const globals = useGlobals()".
 * Available fields (see ReactAppCtrl.GlobalsView): amsUrl, googleMapsUrl, isWhitelisted,
 * isAdmin, isJobUser.
 */
const GlobalsContext = React.createContext()

function useProvideGlobals() {
  const [ globals, setGlobals ] = React.useState()

  React.useEffect(() => {
    fetchGlobals()
      .then((res) => setGlobals(res))
  }, [])

  return globals
}

export function GlobalsProvider({ children }) {
  const globals = useProvideGlobals()

  return (
    <GlobalsContext.Provider value={globals}>
      {children}
    </GlobalsContext.Provider>
  )
}

export default function useGlobals() {
  return React.useContext(GlobalsContext)
}

async function fetchGlobals() {
  const response = await fetch('/globals')
  if (!response.ok) {
    throw new Error(`Failed to fetch globals: ${response.status}`)
  }
  return response.json()
}
