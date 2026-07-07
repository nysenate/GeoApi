import React from 'react'
import useGlobals from 'app/shared/useGlobals'
import AdminLogin from 'app/views/admin/AdminLogin'
import AdminMain from 'app/views/admin/AdminMain'

/**
 * Admin console page (/admin, /admin/home): admins get the console (from
 * adminmain.jsp), everyone else gets the login page (from adminlogin.jsp).
 * Login and logout are full page loads, so /globals always reflects the
 * current session.
 */
export default function AdminPage() {
  const globals = useGlobals()

  return globals.isAdmin ? <AdminMain /> : <AdminLogin />
}
