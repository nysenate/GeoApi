import React from 'react'
import Header from 'app/shared/Header'
import { jobLogin } from 'app/apis/jobApi'

/**
 * Batch services login page (replaces joblogin.jsp). A successful login is
 * followed by a full page load of /job/home so /globals reflects the new
 * job user session; failures show the error message under the form.
 */
export default function JobLogin() {
  const [ email, setEmail ] = React.useState('')
  const [ password, setPassword ] = React.useState('')
  const [ errorMessage, setErrorMessage ] = React.useState('')

  const login = async (event) => {
    event.preventDefault()
    try {
      const response = await jobLogin(email, password)
      if (response.success) {
        window.location.assign('/job/home')
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
        <h1 style={{ textAlign: 'center', color: '#222', fontWeight: 400 }}>SAGE Batch Services</h1>
        <div style={{ margin: 'auto', width: '720px', textAlign: 'center' }}>
          <p>SAGE provides batch geocoding and district assignment services to registered users.</p>
        </div>
        <div style={{ width: '500px', margin: 'auto' }}>
          <form id="uploadForm" onSubmit={login}>
            <ol className="input-container" style={{ width: '280px', margin: 'auto', padding: '20px' }}>
              <li>
                <label htmlFor="email">Email</label>
                <input id="email" name="email" type="text" placeholder="example@email.com"
                       value={email} onChange={(e) => setEmail(e.target.value)}/>
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
