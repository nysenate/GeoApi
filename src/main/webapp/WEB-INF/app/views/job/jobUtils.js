/** Display labels for JobProcessStatus.Condition values (the legacy conditionFilter). */
const CONDITION_LABELS = {
  WAITING_FOR_CRON: 'Waiting',
  RUNNING: 'Processing',
  COMPLETED: 'Completed',
  COMPLETED_WITH_ERRORS: 'Completed with some errors',
  SKIPPED: 'Skipped',
  FAILED: 'Failed',
  CANCELLED: 'Cancelled',
  INACTIVE: 'Currently inactive',
}

export function conditionLabel(condition) {
  return CONDITION_LABELS[condition] ?? condition
}

/** Whether a job's condition means it produced a downloadable result file. */
export function conditionSuccess(condition) {
  return condition === 'COMPLETED' || condition === 'COMPLETED_WITH_ERRORS'
}

/** The condition colors from the legacy job history page. */
export function conditionColor(condition) {
  switch (condition) {
    case 'RUNNING':
    case 'COMPLETED': return '#639A00'
    case 'SKIPPED': return 'orangered'
    case 'COMPLETED_WITH_ERRORS':
    case 'FAILED':
    case 'CANCELLED': return 'red'
    default: return '#333'
  }
}

export function yesNo(value) {
  return value ? 'Yes' : 'No'
}
