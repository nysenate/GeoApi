import React from 'react'
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom'
import Header from 'app/shared/Header'
import JobUpload from 'app/views/job/JobUpload'
import JobStatus from 'app/views/job/JobStatus'
import JobHistory from 'app/views/job/JobHistory'

// The ids avoid "upload" and "status", which are already /job API paths.
const TABS = [
  { id: 'new', label: 'New batch job' },
  { id: 'current', label: 'Current jobs' },
  { id: 'history', label: 'History' },
]

/**
 * Batch job console (replaces jobmain.jsp): upload new batch jobs, watch the
 * current jobs, and review the job history. Each tab has its own URL,
 * /job/<tab id>; any other /job URL redirects to the first tab. The panes stay
 * mounted and are toggled with CSS (like the legacy ng-show menu) so the upload
 * queue isn't lost by switching tabs. Logout is a full page load so /globals
 * reflects the ended session.
 */
export default function JobMain() {
  const activeTab = useParams()['*']
  const navigate = useNavigate()

  if (!TABS.some((tab) => tab.id === activeTab)) {
    return <Navigate to={`/job/${TABS[0].id}`} replace />
  }

  return (
    <div id="contentwrapper" style={{ height: 'auto', paddingBottom: '20px' }}>
      <Header>
        {TABS.map((tab) => (
          <li key={tab.id}>
            <Link className={activeTab === tab.id ? 'active' : undefined} to={`/job/${tab.id}`}>
              {tab.label}
            </Link>
          </li>
        ))}
        <li><a href="/job/logout">Logout</a></li>
      </Header>

      <div id="contentcolumn"
           style={{ margin: 0, paddingTop: '20px', height: 'auto', backgroundColor: '#f5f5f5' }}>
        <JobUpload visible={activeTab === 'new'} onSubmitted={() => navigate('/job/current')} />
        <JobStatus visible={activeTab === 'current'} />
        <JobHistory visible={activeTab === 'history'} />
      </div>
    </div>
  )
}
