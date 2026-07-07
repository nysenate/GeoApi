import React from 'react'
import Header from 'app/shared/Header'
import JobUpload from 'app/views/job/JobUpload'
import JobStatus from 'app/views/job/JobStatus'
import JobHistory from 'app/views/job/JobHistory'

const TABS = { upload: 1, status: 2, history: 3 }

/**
 * Batch job console (replaces jobmain.jsp): upload new batch jobs, watch the
 * current jobs, and review the job history. The panes stay mounted and are
 * toggled with CSS (like the legacy ng-show menu) so the upload queue isn't
 * lost by switching tabs. Logout is a full page load so /globals reflects
 * the ended session.
 */
export default function JobMain() {
  const [ tab, setTab ] = React.useState(TABS.upload)

  const tabLink = (id, label) => (
    <li>
      <a className={tab === id ? 'active' : undefined} onClick={() => setTab(id)}>{label}</a>
    </li>
  )

  return (
    <div id="contentwrapper" style={{ height: 'auto', paddingBottom: '20px' }}>
      <Header>
        {tabLink(TABS.upload, 'New batch job')}
        {tabLink(TABS.status, 'Current jobs')}
        {tabLink(TABS.history, 'History')}
        <li><a href="/job/logout">Logout</a></li>
      </Header>

      <div id="contentcolumn"
           style={{ margin: 0, paddingTop: '20px', height: 'auto', backgroundColor: '#f5f5f5' }}>
        <JobUpload visible={tab === TABS.upload} onSubmitted={() => setTab(TABS.status)} />
        <JobStatus visible={tab === TABS.status} />
        <JobHistory visible={tab === TABS.history} />
      </div>
    </div>
  )
}
