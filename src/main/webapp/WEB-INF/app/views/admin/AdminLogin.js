import React from 'react'
import Header from 'app/shared/Header'
import { adminLogin, isSuccess } from 'app/apis/adminApi'

/**
 * Admin login page (replaces adminlogin.jsp). A successful login is followed
 * by a full page load of /admin/home so /globals reflects the new admin
 * session; failures show the error message under the form.
 */
export default function AdminLogin() {
  const [ username, setUsername ] = React.useState('')
  const [ password, setPassword ] = React.useState('')
  const [ errorMessage, setErrorMessage ] = React.useState('')

  const login = async (event) => {
    event.preventDefault()
    try {
      const response = await adminLogin(username, password)
      if (isSuccess(response)) {
        window.location.assign('/admin/home')
      } else {
        setErrorMessage(response.message)
      }
    } catch (e) {
      setErrorMessage('Failed to process login request.')
    }
  }

  return (
    <div id="contentwrapper">
      <Header>
        <li><a className="active">Login</a></li>
      </Header>

      <div id="contentcolumn">
        <h1 style={{ textAlign: 'center', color: '#222', fontWeight: 400 }}>SAGE Admin Interface</h1>
        <div style={{ width: '500px', margin: 'auto' }}>
          <form onSubmit={login}>
            <ol className="input-container" style={{ width: '280px', margin: 'auto', padding: '20px' }}>
              <li>
                <label htmlFor="username">Username</label>
                <input id="username" name="username" type="text"
                       value={username} onChange={(e) => setUsername(e.target.value)}/>
              </li>
              <li>
                <label htmlFor="password">Password</label>
                <input id="password" name="password" type="password"
                       value={password} onChange={(e) => setPassword(e.target.value)}/>
              </li>
              <li>
                <br/>
              </li>
              <li>
                <button type="submit" className="submit">
                  <div className="icon-user icon-white-no-hover"></div>
                  <span>Login</span>
                </button>
              </li>
            </ol>
          </form>
          <div id="login-error">{errorMessage}</div>
        </div>
      </div>
    </div>
  )
}
