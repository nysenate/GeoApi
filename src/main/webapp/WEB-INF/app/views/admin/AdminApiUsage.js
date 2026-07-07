import React from 'react'
import { fetchApiUsage } from 'app/apis/adminApi'
import { DateRangePicker } from 'app/views/admin/AdminMain'
import ApiUsageChart from 'app/views/admin/ApiUsageChart'

/**
 * "Api Usage" tab: chart of hourly api request counts over the picked date
 * range (replaces the legacy Highcharts graph).
 */
export default function AdminApiUsage({ queryRange, picker }) {
  const [ usage, setUsage ] = React.useState(null)

  React.useEffect(() => {
    fetchApiUsage(queryRange.from, queryRange.to)
      .then(setUsage)
      .catch(() => console.log('Error retrieving api usage stats!'))
  }, [ queryRange ])

  return (
    <div id="api-usage" className="highlight-section fixed">
      <p className="blue-header">Api Hourly Usage</p>
      <DateRangePicker {...picker} />
      <hr/>
      <div id="api-usage-stats">
        {usage &&
          <ApiUsageChart rangeFrom={usage.rangeFrom} rangeTo={usage.rangeTo}
                         usageCounts={usage.usageCounts ?? []} />
        }
      </div>
    </div>
  )
}
