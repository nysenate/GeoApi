import fetchJson from 'app/apis/fetchJson'

/**
 * Client for the admin console endpoints (/admin/login and /admin/api/...,
 * see AdminController, AdminApiController and UserApiController). These are
 * session-based: authentication comes from the admin's Shiro session.
 */

const BASE_API = '/admin/api'

/** Whether a GenericResponse (or other BaseResponse) reports success. */
export function isSuccess(response) {
  return response.status === 'SUCCESS'
}

/** Logs an admin in. Returns a GenericResponse: { status, message }. */
export function adminLogin(username, password) {
  return fetchJson('/admin/login', {
    method: 'POST',
    body: new URLSearchParams({ username, password }),
  })
}

/** Deployment history: { deployments: [{ id, deployTime, apiRequestsSince }] },
 *  ordered by deploy time ascending. */
export function fetchDeploymentStats() {
  return fetchJson(`${BASE_API}/deployment`)
}

/** Hourly api usage between the epoch-millis bounds:
 *  { rangeFrom, rangeTo, usageCounts: [{ time, count }] }. */
export function fetchApiUsage(from, to) {
  return fetchJson(`${BASE_API}/usage?from=${from}&to=${to}`)
}

/** Geocoder usage between the epoch-millis bounds:
 *  { geocoderUsage: { [geocoder]: requests }, totalRequests, totalGeocodes, totalCacheHits }. */
export function fetchGeocodeUsage(from, to) {
  return fetchJson(`${BASE_API}/geocodeUsage?from=${from}&to=${to}`)
}

/** Per-api-user request stats between the epoch-millis bounds, keyed by user id:
 *  { [id]: { apiUser, apiRequests, geoRequests, distRequests, requestsByMethod } }. */
export function fetchApiUserUsage(from, to) {
  return fetchJson(`${BASE_API}/apiUserUsage?from=${from}&to=${to}`)
}

/** The registered api users: [{ id, apiKey, name, description, admin }]. */
export function fetchCurrentApiUsers() {
  return fetchJson(`${BASE_API}/currentApiUsers`)
}

/** The registered job users: [{ id, firstname, lastname, email, active, admin }]. */
export function fetchCurrentJobUsers() {
  return fetchJson(`${BASE_API}/currentJobUsers`)
}

/** Creates an api user. Returns a GenericResponse. */
export function createApiUser(name, desc, admin) {
  return postAction('/createApiUser', { name, desc, admin })
}

/** Deletes an api user by id. Returns a GenericResponse. */
export function deleteApiUser(id) {
  return postAction('/deleteApiUser', { id })
}

/** Creates a job user. Returns a GenericResponse. */
export function createJobUser(email, password, firstname, lastname, admin) {
  return postAction('/createJobUser', { email, password, firstname, lastname, admin })
}

/** Deletes a job user by id. Returns a GenericResponse. */
export function deleteJobUser(id) {
  return postAction('/deleteJobUser', { id })
}

function postAction(path, params) {
  return fetchJson(`${BASE_API}${path}?${new URLSearchParams(params)}`, { method: 'POST' })
}
