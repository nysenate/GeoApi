import React from 'react'
import useGlobals from 'app/shared/useGlobals'
import JobLogin from 'app/views/job/JobLogin'
import JobMain from 'app/views/job/JobMain'

/**
 * Batch jobs page (/job, /job/home): job users get the batch job console
 * (from jobmain.jsp), everyone else gets the login page (from joblogin.jsp).
 * Login and logout are full page loads, so /globals always reflects the
 * current session.
 */
export default function JobPage() {
  const globals = useGlobals()

  return globals.isJobUser ? <JobMain /> : <JobLogin />
}
