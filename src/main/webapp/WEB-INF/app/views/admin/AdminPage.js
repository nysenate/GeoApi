import React from 'react'
import Header from 'app/shared/Header'

/**
 * Admin console page (/admin, /admin/home): skeleton shown while the legacy
 * admin pages (adminlogin.jsp, adminmain.jsp) are rebuilt in React.
 */
export default function AdminPage() {
  return (
    <div id="contentwrapper">
      <Header />

      <div id="contentcolumn">
        <div className="m-8 text-center">
          <h1 className="text-2xl font-semibold">Admin Console</h1>
          <p className="mt-4">This page is being rebuilt in React and is not available yet.</p>
        </div>
      </div>
    </div>
  )
}
