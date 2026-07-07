import React from 'react'
import { fetchGeocodeUsage } from 'app/apis/adminApi'

/**
 * "Geocode Usage" tab: geocoder request totals and the share each geocoder
 * handled over the picked date range.
 */
export default function AdminGeocodeUsage({ queryRange }) {
  const [ stats, setStats ] = React.useState(null)

  React.useEffect(() => {
    fetchGeocodeUsage(queryRange.from, queryRange.to)
      .then(setStats)
      .catch(() => console.log('Failed to retrieve geocode usage response!'))
  }, [ queryRange ])

  if (!stats) {
    return null
  }
  const { totalGeocodes = 0, totalCacheHits = 0, totalRequests = 0 } = stats
  const usageEntries = Object.entries(stats.geocoderUsage ?? {})
    .sort(([ , a ], [ , b ]) => b - a)
  const cacheHitRate = totalGeocodes > 0
    ? ((totalCacheHits / totalGeocodes) * 100).toFixed(2)
    : '0.00'

  return (
    <div id="geocode-usage" className="highlight-section fixed">
      <p className="blue-header">Geocoder Usage</p>
      <hr/>
      <ul className="horizontal">
        <li><label>Total Geocodes: </label>{totalGeocodes}</li>
        <li><label>Cache Hits: </label>{totalCacheHits}</li>
        <li><label>Cache Hit Rate: </label>{cacheHitRate}%</li>
      </ul>
      <hr/>
      <table className="light-table" style={{ width: '650px', margin: 'auto', textAlign: 'left' }}>
        <thead>
        <tr>
          <th>Geocoder</th>
          <th>Requests</th>
          <th colSpan="2" style={{ width: '300px' }}>Percentage of requests</th>
        </tr>
        </thead>
        <tbody>
        {usageEntries.map(([ geocoder, requests ]) => (
          <tr key={geocoder}>
            <td>{geocoder}</td>
            <td>{requests}</td>
            <td style={{ width: '50px' }}>
              {totalRequests > 0 ? ((requests / totalRequests) * 100).toFixed(1) : '0.0'}%
            </td>
            <td style={{ width: '250px', background: '#f5f5f5' }}>
              <div style={{
                background: '#CC333F',
                width: totalRequests > 0 ? `${(requests / totalRequests) * 100}%` : 0,
              }}>&nbsp;</div>
            </td>
          </tr>
        ))}
        </tbody>
      </table>
    </div>
  )
}
