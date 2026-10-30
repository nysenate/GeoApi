import React from 'react'
import { Navigate, useLocation, useParams } from 'react-router-dom'
import useGlobals from 'app/shared/useGlobals'
import JobLogin from 'app/views/job/JobLogin'
import JobMain from 'app/views/job/JobMain'

/**
 * Batch jobs page (/job and its tab URLs): job users get the batch job console
 * (from jobmain.jsp), everyone else is sent to /job/login (from joblogin.jsp),
 * which remembers the job URL they asked for. Login and logout are full page
 * loads, so /globals always reflects the current session.
 */
export default function JobPage() {
  const globals = useGlobals()
  const location = useLocation()
  const onLoginPage = useParams()['*'] === 'login'

  if (globals.isJobUser) {
    return <JobMain />
  }
  if (onLoginPage) {
    return <JobLogin redirectTo={location.state?.from} />
  }
  return <Navigate to="/job/login" replace state={{ from: location.pathname }} />
}
