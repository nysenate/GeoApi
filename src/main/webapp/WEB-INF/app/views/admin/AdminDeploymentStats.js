import React from 'react'
import { fetchDeploymentStats } from 'app/apis/adminApi'
import { formatDateMedium } from 'app/shared/formatters'

/**
 * "Deployment Stats" tab: when SAGE was last deployed and how many api
 * requests each deployment served.
 */
export default function AdminDeploymentStats() {
  const [ deployments, setDeployments ] = React.useState([])

  React.useEffect(() => {
    fetchDeploymentStats()
      .then((data) => setDeployments(data.deployments ?? []))
      .catch(() => console.log('Error retrieving deployment stats!'))
  }, [])

  // Deployments are returned ordered by deploy time ascending, so the latest
  // is last; the history table shows the newest first.
  const latest = deployments[deployments.length - 1]
  const history = [ ...deployments ].reverse()

  return (
    <div id="deployment-stats" className="highlight-section fixed">
      <ul className="horizontal">
        <li><label>Last Deployed | </label> {latest ? formatDateMedium(latest.deployTime) : ''}</li>
        <li><label>API Requests Since Deployment | </label>{latest ? latest.apiRequestsSince : ''}</li>
      </ul>

      <p className="blue-header">Deployment History</p>
      <table className="light-table" style={{ width: '650px', margin: 'auto', textAlign: 'left' }}>
        <thead>
        <tr>
          <th>Deployed</th>
          <th>API Request Count</th>
        </tr>
        </thead>
        <tbody>
        {history.map((deployment) => (
          <tr key={deployment.id}>
            <td>{formatDateMedium(deployment.deployTime)}</td>
            <td>{deployment.apiRequestsSince}</td>
          </tr>
        ))}
        </tbody>
      </table>
    </div>
  )
}
