import React from 'react'
import { Navigate, useLocation, useParams } from 'react-router-dom'
import useGlobals from 'app/shared/useGlobals'
import AdminLogin from 'app/views/admin/AdminLogin'
import AdminMain from 'app/views/admin/AdminMain'

/**
 * Admin console page (/admin and its tab URLs): admins get the console (from
 * adminmain.jsp), everyone else is sent to /admin/login (from adminlogin.jsp),
 * which remembers the admin URL they asked for. Login and logout are full page
 * loads, so /globals always reflects the current session.
 */
export default function AdminPage() {
  const globals = useGlobals()
  const location = useLocation()
  const onLoginPage = useParams()['*'] === 'login'

  if (globals.isAdmin) {
    return <AdminMain />
  }
  if (onLoginPage) {
    return <AdminLogin redirectTo={location.state?.from} />
  }
  return <Navigate to="/admin/login" replace state={{ from: location.pathname }} />
}
