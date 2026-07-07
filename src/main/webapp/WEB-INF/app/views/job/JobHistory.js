import React from 'react'
import { fetchAllJobs, jobDownloadUrl } from 'app/apis/jobApi'
import { conditionColor, conditionLabel, conditionSuccess, formatDateMedium }
  from 'app/views/job/jobUtils'

/**
 * "History" pane: every batch job the user can see, with its final condition
 * and a download link for the ones that completed. Refreshed each time the
 * pane becomes visible.
 */
export default function JobHistory({ visible }) {
  const [ allProcesses, setAllProcesses ] = React.useState([])

  React.useEffect(() => {
    if (visible) {
      fetchAllJobs()
        .then((data) => {
          if (data.success) {
            setAllProcesses(data.statuses)
          }
        })
        .catch(() => console.log('Error retrieving job history.'))
    }
  }, [ visible ])

  return (
    <div id="history-container" style={{ display: visible ? '' : 'none', width: '100%', height: '100%' }}>
      <div style={{ textAlign: 'center', padding: '20px', width: '95%', margin: 'auto' }}>
        <h3 style={{ color: '#333' }}>Batch Job History</h3>
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
            {allProcesses.length === 0 &&
              <tr>
                <td>No files have been processed.</td>
              </tr>
            }
            {allProcesses.map((status) => (
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
