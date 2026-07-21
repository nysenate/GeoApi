/** Display labels for JobProcessStatus.Condition values (the legacy conditionFilter). */
const CONDITION_LABELS = {
  WAITING_FOR_CRON: 'Waiting',
  RUNNING: 'Processing',
  COMPLETED: 'Completed',
  SKIPPED: 'Skipped',
  FAILED: 'Failed',
  CANCELLED: 'Cancelled',
}

export function conditionLabel(condition) {
  return CONDITION_LABELS[condition] ?? condition
}

/** Every JobProcessStatus.Condition value, for building filter dropdowns. */
export const JOB_CONDITIONS = Object.keys(CONDITION_LABELS)

/** Whether a job's condition means it produced a downloadable result file. */
export function conditionSuccess(condition) {
  return condition === 'COMPLETED'
}

/** The condition colors from the legacy job history page. */
export function conditionColor(condition) {
  switch (condition) {
    case 'RUNNING':
    case 'COMPLETED': return '#639A00'
    case 'SKIPPED': return 'orangered'
    case 'FAILED':
    case 'CANCELLED': return 'red'
    default: return '#333'
  }
}

export function yesNo(value) {
  return value ? 'Yes' : 'No'
}
