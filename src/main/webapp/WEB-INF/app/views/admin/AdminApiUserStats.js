import React from 'react'
import { fetchApiUserUsage } from 'app/apis/adminApi'

/**
 * "Api User Stats" tab: request totals per api user over the picked date
 * range, plus a breakdown of requests per service method.
 */
export default function AdminApiUserStats({ queryRange }) {
  const [ apiUserStats, setApiUserStats ] = React.useState({})

  React.useEffect(() => {
    fetchApiUserUsage(queryRange.from, queryRange.to)
      .then(setApiUserStats)
      .catch(() => console.log('Error retrieving api user stats!'))
  }, [ queryRange ])

  const statEntries = Object.entries(apiUserStats)

  return (
    <div id="api-user-stats" className="highlight-section fixed">
      <p className="blue-header">Api User Request Stats</p>
      <hr/>
      <table className="light-table">
        <tbody>
        <tr>
          <th>Api User Id</th>
          <th>Api User Name</th>
          <th>Api Requests</th>
          <th>Geocode Requests</th>
          <th>District Assign Requests</th>
        </tr>
        {statEntries.map(([ id, stat ]) => (
          <tr key={id}>
            <td>{id}</td>
            <td>{stat.apiUser.name}</td>
            <td>{stat.apiRequests}</td>
            <td>{stat.geoRequests}</td>
            <td>{stat.distRequests}</td>
          </tr>
        ))}
        </tbody>
      </table>

      <br/>
      <p className="blue-header">Requests per method</p>
      <hr/>
      <table className="light-table">
        <tbody>
        <tr>
          <th style={{ width: '100px' }}>Api User Id</th>
          <th style={{ width: '300px' }}>Api User Name</th>
          <th style={{ width: '175px' }}>Service</th>
          <th style={{ width: '175px' }}>Method</th>
          <th style={{ width: '100px' }}>Requests</th>
        </tr>
        {statEntries.flatMap(([ id, stat ]) =>
          Object.entries(stat.requestsByMethod ?? {}).flatMap(([ service, methods ]) =>
            Object.entries(methods).map(([ method, requests ]) => (
              <tr key={`${id}-${service}-${method}`}>
                <td style={{ width: '100px' }}>{id}</td>
                <td style={{ width: '300px' }}>{stat.apiUser.name}</td>
                <td style={{ width: '175px' }}>{service}</td>
                <td style={{ width: '175px' }}>{method}</td>
                <td style={{ width: '100px' }}>{requests}</td>
              </tr>
            ))))}
        </tbody>
      </table>
    </div>
  )
}
