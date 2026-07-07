import React from 'react'
import fetchJson from 'app/apis/fetchJson'


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
  const [ error, setError ] = React.useState()

  React.useEffect(() => {
    fetchJson('/globals')
      .then((res) => setGlobals(res))
      .catch(setError)
  }, [])

  // Rethrow during render so a failed globals fetch reaches the ErrorBoundary
  // instead of leaving the app permanently blank.
  if (error) {
    throw error
  }
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
