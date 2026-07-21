import React from 'react'
import { cancelJobProcess, fetchActiveJobs, fetchRecentlyCompletedJobs, fetchRunningJobs, jobDownloadUrl }
  from 'app/apis/jobApi'
import { formatDateMedium, formatDateShort } from 'app/shared/formatters'
import { conditionLabel } from 'app/views/job/jobUtils'

// The legacy page polled the running job every 3s and the queues every 5-6s.
const RUNNING_POLL_MS = 3000
const QUEUE_POLL_MS = 6000

/**
 * "Current jobs" pane: the running job with its progress bar, the queued
 * jobs, and the recently completed jobs. Polls the /job/status endpoints
 * while the pane is visible.
 */
export default function JobStatus({ visible }) {
  const [ runningProcesses, setRunningProcesses ] = React.useState([])
  const [ activeProcesses, setActiveProcesses ] = React.useState([])
  const [ completedProcesses, setCompletedProcesses ] = React.useState([])
  const [ processorRunning, setProcessorRunning ] = React.useState(false)

  const getRunningProcesses = () => {
    fetchRunningJobs()
      .then((data) => {
        if (data.success) {
          setRunningProcesses(data.statuses)
        }
      })
      .catch(() => console.log('Failed to retrieve running job processes'))
  }

  const getActiveProcesses = () => {
    fetchActiveJobs()
      .then((data) => {
        if (data.success) {
          setActiveProcesses(data.statuses)
          setProcessorRunning(data.processorRunning)
        }
      })
      .catch(() => console.log('Error retrieving active processes.'))
  }

  const getCompletedProcesses = () => {
    fetchRecentlyCompletedJobs()
      .then((data) => {
        if (data.success) {
          setCompletedProcesses(data.statuses)
        }
      })
      .catch(() => console.log('Error retrieving completed processes.'))
  }

  React.useEffect(() => {
    if (!visible) {
      return undefined
    }
    getRunningProcesses()
    getActiveProcesses()
    getCompletedProcesses()
    const runningInterval = setInterval(getRunningProcesses, RUNNING_POLL_MS)
    const queueInterval = setInterval(() => {
      getActiveProcesses()
      getCompletedProcesses()
    }, QUEUE_POLL_MS)
    return () => {
      clearInterval(runningInterval)
      clearInterval(queueInterval)
    }
  }, [ visible ])

  const cancelJob = async (processId) => {
    try {
      const data = await cancelJobProcess(processId)
      window.alert(data.message)
    } catch (e) {
      window.alert('Failed to cancel job process!')
    }
    getActiveProcesses()
  }

  return (
    <div id="status-container" style={{ display: visible ? '' : 'none', width: '100%' }}>
      <div style={{ textAlign: 'center', padding: '20px', width: '95%', margin: 'auto' }}>
        {processorRunning
          ? <span style={{ color: '#639A00', fontWeight: 'bold' }}>Job processor is running.</span>
          : <span style={{ color: '#CC333F', fontWeight: 'bold' }}>Job processor is not running.</span>
        }
        {runningProcesses.length > 0 &&
          <h3 style={{ color: '#333' }}>Running Job</h3>
        }
        {runningProcesses.map((runningProcess) => (
          <div className="running-process-view" key={runningProcess.processId}>
            <table className="job-table">
              <tbody>
              <tr>
                <td>
                  <span style={{ color: 'teal', fontWeight: 'bold' }}>
                    Job {runningProcess.processId} - {runningProcess.process.sourceFileName}
                  </span>
                </td>
                <td><span></span></td>
                <td style={{ textAlign: 'right' }}>
                  <span>Started - {formatDateMedium(runningProcess.startTime)}</span>
                </td>
              </tr>
              <tr>
                <td colSpan="3">
                  <div id="current-job" style={{ width: '100%', margin: 'auto' }}>
                    <div id="progress-bar" style={{ margin: 'auto', height: '30px', border: '1px solid #aaa' }}>
                      <div style={{
                        width: `${getProgressPercent(runningProcess)}%`,
                        height: '100%',
                        background: '#7BA838',
                      }}>&nbsp;</div>
                    </div>
                  </div>
                </td>
              </tr>
              <tr>
                <td colSpan="3">
                  <span>Records Completed: {runningProcess.completedRecords} / {runningProcess.process.recordCount}</span>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        ))}

        {activeProcesses.length > 0 &&
          <div style={{ padding: '20px', width: '95%', margin: 'auto' }}>
            <h3 style={{ color: '#333' }}>Queued jobs</h3>
            <div>
              <table className="job-table">
                <thead style={{ textAlign: 'left', borderBottom: '1px solid #999' }}>
                <tr>
                  <th>Job Id</th>
                  <th style={{ width: '240px' }}>File name</th>
                  <th>Submitter</th>
                  <th>Records</th>
                  <th>Submitted On</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {activeProcesses.map((activeProcess) => (
                  <tr key={activeProcess.processId}>
                    <td>{activeProcess.processId}</td>
                    <td style={{ color: 'teal' }}>{activeProcess.process.sourceFileName}</td>
                    <td>{activeProcess.process.requestorEmail}</td>
                    <td>{activeProcess.process.recordCount}</td>
                    <td>{formatDateMedium(activeProcess.process.requestTime)}</td>
                    <td>{conditionLabel(activeProcess.condition)}</td>
                    <td>
                      <button className="cancel" onClick={() => cancelJob(activeProcess.processId)}>Cancel</button>
                    </td>
                  </tr>
                ))}
                </tbody>
              </table>
            </div>
          </div>
        }

        <div style={{ padding: '20px', width: '95%', margin: 'auto' }}>
          <h3 style={{ color: '#333' }}>Recently Completed Jobs</h3>
          <div>
            <table className="job-table">
              <thead style={{ textAlign: 'left', borderBottom: '1px solid #999' }}>
              <tr>
                <th>Job Id</th>
                <th>File name</th>
                <th>Submitted By</th>
                <th>Started On</th>
                <th>Completed On</th>
                <th>Download Link</th>
              </tr>
              </thead>
              <tbody>
              {completedProcesses.map((completedProcess) => (
                <tr key={completedProcess.processId}>
                  <td style={{ paddingTop: '5px' }}>{completedProcess.processId}</td>
                  <td style={{ color: 'teal' }}>{completedProcess.process.sourceFileName}</td>
                  <td>{completedProcess.process.requestorEmail}</td>
                  <td>{formatDateShort(completedProcess.startTime)}</td>
                  <td>{formatDateShort(completedProcess.completeTime)}</td>
                  <td>
                    <a style={{ background: '#477326', color: 'white', padding: '2px' }}
                       href={jobDownloadUrl(completedProcess.process.fileName)}>Download</a>
                  </td>
                </tr>
              ))}
              {completedProcesses.length === 0 &&
                <tr>
                  <td></td>
                  <td>Nothing recently completed.</td>
                  <td></td>
                  <td></td>
                </tr>
              }
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  )
}

function getProgressPercent(runningProcess) {
  const total = runningProcess.process.recordCount
  return total > 0 ? (runningProcess.completedRecords / total) * 100 : 0
}
