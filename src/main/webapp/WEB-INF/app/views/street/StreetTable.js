import React from 'react'
import './dataTables.css'

/**
 * Client-side replacement for the jQuery DataTable the legacy page used: sortable
 * columns, a street filter, a page length menu, and full-numbers pagination. Keeps
 * the DataTables ids/classes so the bundled dataTables.css and the main.css
 * overrides still apply.
 */
const COLUMNS = [
  { key: 'bldgLoNum', label: 'From Bldg' },
  { key: 'bldgHiNum', label: 'To Bldg' },
  { key: 'street', label: 'Street', width: '170px' },
  { key: 'parity', label: 'E/O' },
  { key: 'location', label: 'Location' },
  { key: 'zip5', label: 'Zip' },
  { key: 'senate', label: 'Senate' },
  { key: 'congressional', label: 'Congress' },
  { key: 'assembly', label: 'Assembly' },
  { key: 'county', label: 'County' },
  { key: 'town', label: 'Town' },
  { key: 'election', label: 'Election' },
]

const PAGE_SIZES = [ 10, 20, 50, 100 ]

// Number of numbered page buttons shown, matching the legacy full_numbers pager.
const PAGE_BUTTONS = 5

export default function StreetTable({ streets, filter }) {
  // Default sort by street ascending, as the legacy table did.
  const [ sort, setSort ] = React.useState({ col: 2, dir: 'asc' })
  const [ pageSize, setPageSize ] = React.useState(20)
  const [ page, setPage ] = React.useState(0)

  React.useEffect(() => {
    setPage(0)
  }, [ streets, filter, pageSize ])

  const filtered = React.useMemo(() => {
    const term = filter.trim().toLowerCase()
    return term
      ? streets.filter((s) => (s.street ?? '').toLowerCase().includes(term))
      : streets
  }, [ streets, filter ])

  const sorted = React.useMemo(() => {
    const key = COLUMNS[sort.col].key
    const sign = sort.dir === 'asc' ? 1 : -1
    return [ ...filtered ].sort((a, b) => sign * compare(a[key], b[key]))
  }, [ filtered, sort ])

  const pageCount = Math.max(1, Math.ceil(sorted.length / pageSize))
  const curPage = Math.min(page, pageCount - 1)
  const start = curPage * pageSize
  const rows = sorted.slice(start, start + pageSize)

  const onSortColumn = (col) => {
    setSort({ col, dir: sort.col === col && sort.dir === 'asc' ? 'desc' : 'asc' })
  }

  return (
    <div id="street-view-table_wrapper" className="dataTables_wrapper">
      <div id="street-view-table_length" className="dataTables_length">
        <label>
          {'Show '}
          <select value={pageSize} onChange={(e) => setPageSize(Number(e.target.value))}>
            {PAGE_SIZES.map((size) => <option key={size} value={size}>{size}</option>)}
          </select>
          {' entries'}
        </label>
      </div>
      <table id="street-view-table" className="dataTable">
        <thead>
          <tr>
            {COLUMNS.map((column, i) =>
              <th key={column.key} style={column.width ? { width: column.width } : undefined}
                  className={sort.col === i ? `sorting_${sort.dir}` : 'sorting'}
                  onClick={() => onSortColumn(i)}>
                {column.label}
              </th>
            )}
          </tr>
        </thead>
        <tbody>
          {rows.map((street, rowIndex) =>
            <tr key={start + rowIndex} className={rowIndex % 2 === 0 ? 'odd' : 'even'}>
              {COLUMNS.map((column, i) =>
                <td key={column.key} className={sort.col === i ? 'sorting_1' : undefined}>
                  {street[column.key]}
                </td>
              )}
            </tr>
          )}
          {rows.length === 0 &&
            <tr className="odd">
              <td colSpan={COLUMNS.length} className="dataTables_empty">
                No data available in table
              </td>
            </tr>
          }
        </tbody>
      </table>
      <div id="street-view-table_info" className="dataTables_info">
        Showing {sorted.length === 0 ? 0 : start + 1} to {Math.min(start + pageSize, sorted.length)}
        {' '}of {sorted.length} entries
        {filtered.length !== streets.length && ` (filtered from ${streets.length} total entries)`}
      </div>
      <Paginate curPage={curPage} pageCount={pageCount} onPage={setPage} />
    </div>
  )
}

/** Full-numbers pager: First/Previous, a window of page numbers, Next/Last. */
function Paginate({ curPage, pageCount, onPage }) {
  let windowStart = Math.min(Math.max(0, curPage - Math.floor(PAGE_BUTTONS / 2)),
    Math.max(0, pageCount - PAGE_BUTTONS))
  const pages = []
  for (let p = windowStart; p < Math.min(windowStart + PAGE_BUTTONS, pageCount); p++) {
    pages.push(p)
  }

  const button = (label, page, disabled, className) =>
    <a className={`${className} ${disabled ? 'paginate_button_disabled' : ''}`}
       onClick={disabled ? undefined : () => onPage(page)}>
      {label}
    </a>

  return (
    <div id="street-view-table_paginate" className="dataTables_paginate paging_full_numbers">
      {button('First', 0, curPage === 0, 'first paginate_button')}
      {button('Previous', curPage - 1, curPage === 0, 'previous paginate_button')}
      <span>
        {pages.map((p) =>
          <a key={p} className={p === curPage ? 'paginate_active' : 'paginate_button'}
             onClick={() => onPage(p)}>
            {p + 1}
          </a>
        )}
      </span>
      {button('Next', curPage + 1, curPage >= pageCount - 1, 'next paginate_button')}
      {button('Last', pageCount - 1, curPage >= pageCount - 1, 'last paginate_button')}
    </div>
  )
}

/** Sorts numerically when both values are numbers (e.g. district codes), else as strings. */
function compare(a, b) {
  const numA = Number(a)
  const numB = Number(b)
  if (!Number.isNaN(numA) && !Number.isNaN(numB)) {
    return numA - numB
  }
  return String(a ?? '').localeCompare(String(b ?? ''))
}
