import React from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import './app.css'
import useGlobals, { GlobalsProvider } from 'app/shared/useGlobals'
import ErrorBoundary from 'app/views/ErrorBoundary'
import Home from 'app/views/home/Home'
import DistrictMapsPage from 'app/views/maps/DistrictMapsPage'
import UspsLookupPage from 'app/views/usps/UspsLookupPage'
import StreetFinderPage from 'app/views/street/StreetFinderPage'
import RevGeoPage from 'app/views/revgeo/RevGeoPage'

function App() {
  return (
    <ErrorBoundary>
      <GlobalsProvider>
        <RequireGlobals>
          <BrowserRouter>
            <AppRoutes />
          </BrowserRouter>
        </RequireGlobals>
      </GlobalsProvider>
    </ErrorBoundary>
  )
}

/**
 * Routes served by the React app. As legacy JSP pages are rebuilt in React, add their
 * routes here (the matching server-side mappings live in ReactAppCtrl).
 */
function AppRoutes() {
  return (
    <Routes>
      <Route path="/maps" element={<DistrictMapsPage />} />
      <Route path="/usps" element={<UspsLookupPage />} />
      <Route path="/street" element={<StreetFinderPage />} />
      <Route path="/revgeo" element={<RevGeoPage />} />
      <Route path="*" element={<Home />} />
    </Routes>
  )
}

/**
 * Prevents the rendering of our application until globals have been loaded.
 */
function RequireGlobals({ children }) {
  const globals = useGlobals()

  if (!globals) {
    return null
  }
  return (
    <React.Fragment>
      {children}
    </React.Fragment>
  )
}

createRoot(document.getElementById('app')).render(<App />)
