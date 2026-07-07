import React from 'react'
import Header from 'app/shared/Header'

/**
 * Batch jobs page (/job, /job/home): skeleton shown while the legacy job
 * pages (joblogin.jsp, jobmain.jsp) are rebuilt in React.
 */
export default function JobPage() {
  return (
    <div id="contentwrapper">
      <Header />

      <div id="contentcolumn">
        <div className="m-8 text-center">
          <h1 className="text-2xl font-semibold">Batch Jobs</h1>
          <p className="mt-4">This page is being rebuilt in React and is not available yet.</p>
        </div>
      </div>
    </div>
  )
}
