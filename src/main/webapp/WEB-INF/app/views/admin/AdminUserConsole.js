import React from 'react'
import {
  createApiUser, createJobUser, deleteApiUser, deleteJobUser,
  fetchCurrentApiUsers, fetchCurrentJobUsers, isSuccess,
} from 'app/apis/adminApi'

/**
 * "User Console" tab: lists the registered api and batch job users, with
 * forms to create new ones and links to delete them.
 */
export default function AdminUserConsole() {
  const [ apiUsers, setApiUsers ] = React.useState([])
  const [ jobUsers, setJobUsers ] = React.useState([])

  const loadApiUsers = () => {
    fetchCurrentApiUsers()
      .then(setApiUsers)
      .catch(() => console.log('Failed to retrieve list of current Api users!'))
  }

  const loadJobUsers = () => {
    fetchCurrentJobUsers()
      .then(setJobUsers)
      .catch(() => console.log('Failed to retrieve list of current Job users!'))
  }

  React.useEffect(() => {
    loadApiUsers()
    loadJobUsers()
  }, [])

  return (
    <div id="user-console" className="highlight-section fixed">
      <h3 className="slim">User Console</h3>
      <ApiUserSection apiUsers={apiUsers} onChanged={loadApiUsers} />
      <hr/>
      <JobUserSection jobUsers={jobUsers} onChanged={loadJobUsers} />
    </div>
  )
}

function ApiUserSection({ apiUsers, onChanged }) {
  const [ name, setName ] = React.useState('')
  const [ desc, setDesc ] = React.useState('')
  const [ admin, setAdmin ] = React.useState(false)

  const create = async (event) => {
    event.preventDefault()
    if (name === '') {
      window.alert('A name is required!')
      return
    }
    try {
      const response = await createApiUser(name, desc, admin)
      window.alert(response.message)
      if (isSuccess(response)) {
        setName('')
        setDesc('')
        setAdmin(false)
        onChanged()
      }
    } catch (e) {
      window.alert('Failed to add Api User!')
    }
  }

  const remove = async (id) => {
    if (!window.confirm('Are you sure you want to delete this user?')) {
      return
    }
    try {
      const response = await deleteApiUser(id)
      window.alert(response.message)
      if (isSuccess(response)) {
        onChanged()
      }
    } catch (e) {
      window.alert('Failed to delete Api User!')
    }
  }

  return (
    <div>
      <p className="title">Registered API Users</p>
      <table className="admin-table" style={{ marginTop: '10px' }}>
        <tbody>
        <tr>
          <th>ID</th>
          <th>Api Key</th>
          <th>Name</th>
          <th>Description</th>
          <th>Is Admin</th>
          <th>Actions</th>
        </tr>
        {apiUsers.map((apiUser) => (
          <tr key={apiUser.id}>
            <td>{apiUser.id}</td>
            <td>{apiUser.apiKey}</td>
            <td>{apiUser.name}</td>
            <td>{apiUser.description}</td>
            <td>{String(apiUser.admin)}</td>
            <td>
              <a style={{ color: '#CC333F', fontSize: '13px' }}
                 onClick={() => remove(apiUser.id)}>Delete</a>
            </td>
          </tr>
        ))}
        </tbody>
      </table>
      <br/>
      <p className="title" style={{ color: '#639A00' }}>Create new API User</p>
      <div className="create-entity">
        <form onSubmit={create}>
          <label htmlFor="new_apiUserName">Name</label>
          <input id="new_apiUserName" name="name" type="text"
                 value={name} onChange={(e) => setName(e.target.value)}/>
          <label htmlFor="new_apiUserDesc">Description</label>
          <input id="new_apiUserDesc" name="desc" type="text"
                 value={desc} onChange={(e) => setDesc(e.target.value)}/>
          <label htmlFor="new_apiUserAdmin">Is admin</label>
          <input id="new_apiUserAdmin" name="admin" type="checkbox"
                 checked={admin} onChange={(e) => setAdmin(e.target.checked)}/>
          <button type="submit" style={{ width: '80px' }} className="submit">Create</button>
        </form>
      </div>
    </div>
  )
}

function JobUserSection({ jobUsers, onChanged }) {
  const [ firstName, setFirstName ] = React.useState('')
  const [ lastName, setLastName ] = React.useState('')
  const [ email, setEmail ] = React.useState('')
  const [ password, setPassword ] = React.useState('')
  const [ admin, setAdmin ] = React.useState(false)

  const create = async (event) => {
    event.preventDefault()
    if (email === '' || password === '') {
      window.alert('Email and password must be specified!')
      return
    }
    try {
      const response = await createJobUser(email, password, firstName, lastName, admin)
      window.alert(response.message)
      if (isSuccess(response)) {
        setFirstName('')
        setLastName('')
        setEmail('')
        setPassword('')
        setAdmin(false)
        onChanged()
      }
    } catch (e) {
      window.alert('Failed to create Job User!')
    }
  }

  const remove = async (id) => {
    if (!window.confirm('Are you sure you want to delete this user?')) {
      return
    }
    try {
      const response = await deleteJobUser(id)
      window.alert(response.message)
      if (isSuccess(response)) {
        onChanged()
      }
    } catch (e) {
      window.alert('Failed to delete Job User!')
    }
  }

  return (
    <div>
      <p className="title">Registered Job Users</p>
      <table className="admin-table" style={{ marginTop: '10px' }}>
        <tbody>
        <tr>
          <th>ID</th>
          <th>First Name</th>
          <th>Last Name</th>
          <th>Email</th>
          <th>Active</th>
          <th>Admin</th>
          <th>Actions</th>
        </tr>
        {jobUsers.map((jobUser) => (
          <tr key={jobUser.id}>
            <td>{jobUser.id}</td>
            <td>{jobUser.firstname}</td>
            <td>{jobUser.lastname}</td>
            <td>{jobUser.email}</td>
            <td>{String(jobUser.active)}</td>
            <td>{String(jobUser.admin)}</td>
            <td>
              <a style={{ color: '#CC333F', fontSize: '13px' }}
                 onClick={() => remove(jobUser.id)}>Delete</a>
            </td>
          </tr>
        ))}
        </tbody>
      </table>
      <br/>
      <p className="title" style={{ color: '#639A00' }}>Create new Job User</p>
      <div className="create-entity">
        <form onSubmit={create}>
          <label htmlFor="new_jobFirstName">First Name</label>
          <input id="new_jobFirstName" name="firstname" style={{ width: '120px' }} type="text"
                 value={firstName} onChange={(e) => setFirstName(e.target.value)}/>
          <label htmlFor="new_jobLastName">Last Name</label>
          <input id="new_jobLastName" name="lastname" style={{ width: '120px' }} type="text"
                 value={lastName} onChange={(e) => setLastName(e.target.value)}/>
          <label htmlFor="new_jobEmail">Email</label>
          <input id="new_jobEmail" name="email" type="text"
                 value={email} onChange={(e) => setEmail(e.target.value)}/><br/>
          <br/>
          <label htmlFor="new_jobPassword">Password</label>
          <input id="new_jobPassword" name="password" style={{ width: '126px' }} type="password"
                 value={password} onChange={(e) => setPassword(e.target.value)}/>
          <label htmlFor="new_jobAdmin">Admin</label>
          <input id="new_jobAdmin" type="checkbox"
                 checked={admin} onChange={(e) => setAdmin(e.target.checked)}/>
          <button type="submit" style={{ width: '80px' }} className="submit">Create</button>
        </form>
      </div>
    </div>
  )
}
