import React from 'react'
import { fetchJobHistory, jobDownloadUrl } from 'app/apis/jobApi'
import { formatDateMedium } from 'app/shared/formatters'
import { conditionColor, conditionLabel, conditionSuccess, JOB_CONDITIONS } from 'app/views/job/jobUtils'

// Batch jobs have existed since 2013.
const EARLIEST_JOB_YEAR = 2013
const CURRENT_YEAR = new Date().getFullYear()
const YEAR_OPTIONS = []
for (let year = CURRENT_YEAR; year >= EARLIEST_JOB_YEAR; year--) {
  YEAR_OPTIONS.push(year)
}

/**
 * "History" pane: the batch jobs the user can see, filterable by request year
 * and status, with a download link for the ones that completed. Refreshed
 * each time the pane becomes visible or a filter changes.
 */
export default function JobHistory({ visible }) {
  const [ processes, setProcesses ] = React.useState([])
  const [ year, setYear ] = React.useState(String(CURRENT_YEAR))
  const [ condition, setCondition ] = React.useState('')

  React.useEffect(() => {
    if (visible) {
      fetchJobHistory(year, condition)
        .then((data) => {
          if (data.success) {
            setProcesses(data.statuses)
          }
        })
        .catch(() => console.log('Error retrieving job history.'))
    }
  }, [ visible, year, condition ])

  return (
    <div id="history-container" style={{ display: visible ? '' : 'none', width: '100%', height: '100%' }}>
      <div style={{ textAlign: 'center', padding: '20px', width: '95%', margin: 'auto' }}>
        <h3 style={{ color: '#333' }}>Batch Job History</h3>
        <div style={{ marginBottom: '15px' }}>
          <label>Year:&nbsp;
            <select value={year} onChange={(e) => setYear(e.target.value)}>
              <option value="">All</option>
              {YEAR_OPTIONS.map((yearOption) => (
                <option key={yearOption} value={yearOption}>{yearOption}</option>
              ))}
            </select>
          </label>
          <span>&nbsp;&nbsp;&nbsp;</span>
          <label>Status:&nbsp;
            <select value={condition} onChange={(e) => setCondition(e.target.value)}>
              <option value="">All</option>
              {JOB_CONDITIONS.map((conditionOption) => (
                <option key={conditionOption} value={conditionOption}>{conditionLabel(conditionOption)}</option>
              ))}
            </select>
          </label>
        </div>
        <div>
          <table className="job-table">
            <thead style={{ textAlign: 'left', borderBottom: '1px solid #999' }}>
            <tr>
              <th>Job Id</th>
              <th>File name</th>
              <th>Submitted by</th>
              <th>Records</th>
              <th>Started</th>
              <th>Completed</th>
              <th>Status</th>
              <th>Download Link</th>
            </tr>
            </thead>
            <tbody>
            {processes.length === 0 &&
              <tr>
                <td>No matching jobs found.</td>
              </tr>
            }
            {processes.map((status) => (
              <tr key={status.processId}>
                <td>{status.processId}</td>
                <td>{status.process.sourceFileName}</td>
                <td>{status.process.requestorEmail}</td>
                <td>{status.process.recordCount}</td>
                <td>{formatDateMedium(status.startTime)}</td>
                <td>{formatDateMedium(status.completeTime)}</td>
                <td style={{ color: conditionColor(status.condition) }}>{conditionLabel(status.condition)}</td>
                <td>
                  {conditionSuccess(status.condition) &&
                    <a style={{ padding: '2px' }} href={jobDownloadUrl(status.process.fileName)}>Download</a>
                  }
                </td>
              </tr>
            ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
