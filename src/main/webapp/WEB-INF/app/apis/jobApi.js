import fetchJson from 'app/apis/fetchJson'

/**
 * Client for the batch job endpoints under /job (see JobController,
 * JobStatusController and JobDownloadController). Unlike /api/v2 these are
 * session-based: the user logs in via /job/login and the uploaded files are
 * tracked server-side in the session until submitted.
 */

/** Logs a job user in. Returns a JobActionResponse: { success, message }. */
export function jobLogin(email, password) {
  return fetchJson('/job/login', {
    method: 'POST',
    body: new URLSearchParams({ email, password }),
  })
}

/** Submits the session's uploaded files for processing. Returns { success, message }. */
export function submitJobRequest() {
  return fetchJson('/job/submit', { method: 'POST' })
}

/** Removes an uploaded file from the session's job request. Returns { success, message }. */
export function removeJobFile(fileName) {
  return fetchJson(`/job/remove?fileName=${encodeURIComponent(fileName)}`, { method: 'POST' })
}

/** Cancels a queued job process. Returns { success, message }. */
export function cancelJobProcess(processId) {
  return fetchJson(`/job/cancel?id=${encodeURIComponent(processId)}`, { method: 'POST' })
}

/** The status endpoints return a JobStatusResponse:
 *  { success, statuses, processorRunning, message }. */
export function fetchRunningJobs() {
  return fetchJson('/job/status/running')
}

export function fetchActiveJobs() {
  return fetchJson('/job/status/active')
}

/** Fetches jobs that completed successfully within the past day. */
export function fetchRecentlyCompletedJobs() {
  return fetchJson('/job/status/recentlyCompleted')
}

/**
 * Fetches the job history, optionally filtered by the year the jobs were
 * requested in and/or their status condition (e.g. 'COMPLETED'). Pass a
 * falsy year/condition to include all of them.
 */
export function fetchJobHistory(year, condition) {
  const params = new URLSearchParams()
  if (year) {
    params.set('year', year)
  }
  if (condition) {
    params.set('condition', condition)
  }
  const query = params.toString()
  return fetchJson(`/job/status/history${query ? `?${query}` : ''}`)
}

/** URL to download a completed job's result file. */
export function jobDownloadUrl(fileName) {
  return `/job/download/${encodeURIComponent(fileName)}`
}

/**
 * Uploads a job file, reporting upload progress in [0, 1] through onProgress
 * (XMLHttpRequest is used because fetch cannot report upload progress).
 * Resolves with the server's JSON: { success, jobProcess } on success,
 * { error } or { message } when the file is rejected.
 */
export function uploadJobFile(file, onProgress) {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest()
    xhr.open('POST', `/job/upload?qqfile=${encodeURIComponent(file.name)}`)
    xhr.setRequestHeader('Content-Type', 'application/octet-stream')
    xhr.upload.onprogress = (event) => {
      if (event.lengthComputable && onProgress) {
        onProgress(event.loaded / event.total)
      }
    }
    xhr.onload = () => {
      try {
        resolve(JSON.parse(xhr.responseText))
      } catch (e) {
        reject(new Error('Server did not respond to upload request.'))
      }
    }
    xhr.onerror = () => reject(new Error('Failed to upload file.'))
    xhr.send(file)
  })
}
