import React from 'react'
import Header from 'app/shared/Header'
import AdminApiUsage from 'app/views/admin/AdminApiUsage'
import AdminApiUserStats from 'app/views/admin/AdminApiUserStats'
import AdminGeocodeUsage from 'app/views/admin/AdminGeocodeUsage'
import AdminUserConsole from 'app/views/admin/AdminUserConsole'
import AdminDeploymentStats from 'app/views/admin/AdminDeploymentStats'

const TABS = [
  { id: 'api-usage', label: 'Api Usage', icon: 'icon-graph' },
  { id: 'api-user-stats', label: 'Api User Stats', icon: 'icon-users' },
  { id: 'geocode-usage', label: 'Geocode Usage', icon: 'icon-compass' },
  { id: 'user-console', label: 'User Console', icon: 'icon-user-add' },
  { id: 'deployment-stats', label: 'Deployment Stats', icon: 'icon-statistics' },
]

/**
 * Admin console (replaces adminmain.jsp): api usage stats, geocoder usage,
 * user management, and deployment history. The stats tabs share the date
 * range picked on the Api Usage tab (like the legacy DashboardController
 * scope); each tab fetches its data when opened or when the range changes.
 * Logout is a full page load so /globals reflects the ended session.
 */
export default function AdminMain() {
  const [ activeTab, setActiveTab ] = React.useState('api-usage')
  // The dates in the picker inputs (yyyy-MM-dd), defaulting to the past week.
  const [ dateRange, setDateRange ] = React.useState(() => ({
    from: toIsoDate(daysAgo(7)),
    to: toIsoDate(new Date()),
  }))
  // The applied query bounds in epoch millis, widened to cover the full first
  // and last day. Only updated when the picker's Update button is pressed.
  const [ queryRange, setQueryRange ] = React.useState(() => toQueryRange({
    from: toIsoDate(daysAgo(7)),
    to: toIsoDate(new Date()),
  }))

  const pickerProps = {
    dateRange,
    maxDate: toIsoDate(new Date()),
    onChange: setDateRange,
    onUpdate: () => setQueryRange(toQueryRange(dateRange)),
  }

  return (
    <div id="contentwrapper" style={{ height: 'auto', paddingBottom: '20px' }}>
      <Header>
        {TABS.map((tab) => (
          <li key={tab.id}>
            <a className={activeTab === tab.id ? 'active' : undefined}
               onClick={() => setActiveTab(tab.id)}>
              <div className={tab.icon}></div>&nbsp;{tab.label}
            </a>
          </li>
        ))}
        <li><a href="/admindocs/html/index.html" target="_blank" rel="noreferrer">Admin Docs</a></li>
        <li><a href="/admin/logout">Logout</a></li>
      </Header>

      <div id="contentcolumn"
           style={{ margin: 0, paddingTop: '20px', height: 'auto', textAlign: 'center' }}>
        {activeTab === 'api-usage' && <AdminApiUsage queryRange={queryRange} picker={pickerProps} />}
        {activeTab === 'api-user-stats' && <AdminApiUserStats queryRange={queryRange} />}
        {activeTab === 'geocode-usage' && <AdminGeocodeUsage queryRange={queryRange} />}
        {activeTab === 'user-console' && <AdminUserConsole />}
        {activeTab === 'deployment-stats' && <AdminDeploymentStats />}
      </div>
    </div>
  )
}

/**
 * The from/to date range picker shared by the stats tabs (replaces
 * datepicker.tag). Edits are applied when Update is pressed.
 */
export function DateRangePicker({ dateRange, maxDate, onChange, onUpdate }) {
  return (
    <div>
      <span>The time frame to view stats is between &nbsp;</span>
      <input type="date" max={maxDate} value={dateRange.from}
             onChange={(e) => onChange({ ...dateRange, from: e.target.value })}/>
      <span>&nbsp; and &nbsp;</span>
      <input type="date" max={maxDate} value={dateRange.to}
             onChange={(e) => onChange({ ...dateRange, to: e.target.value })}/>
      <button className="submit" style={{ width: 'auto', padding: '5px 10px' }} onClick={onUpdate}>
        <span>Update</span>
      </button>
    </div>
  )
}

function daysAgo(days) {
  const date = new Date()
  date.setDate(date.getDate() - days)
  return date
}

/** Formats a Date as a local yyyy-MM-dd string (as <input type="date"> expects). */
function toIsoDate(date) {
  return [
    date.getFullYear(),
    String(date.getMonth() + 1).padStart(2, '0'),
    String(date.getDate()).padStart(2, '0'),
  ].join('-')
}

/** The epoch-millis bounds for a picked range: local midnight of the first day
 *  to the last millisecond of the last day. */
function toQueryRange({ from, to }) {
  return {
    from: parseLocalDate(from).getTime(),
    to: parseLocalDate(to).getTime() + (24 * 3600000 - 1),
  }
}

/** Parses yyyy-MM-dd as local time (new Date(string) would parse it as UTC). */
function parseLocalDate(isoDate) {
  const [ year, month, day ] = isoDate.split('-').map(Number)
  return new Date(year, month - 1, day)
}
