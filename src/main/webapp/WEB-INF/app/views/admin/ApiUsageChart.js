import React from 'react'

/**
 * Area chart of api requests per hour, replacing the legacy Highcharts
 * areaspline. Renders as a scalable SVG with a hover crosshair + tooltip.
 * Gaps between the returned hourly counts are filled with zeros, like the
 * legacy chart did.
 */

const HOUR_MILLIS = 3600000

// Chart geometry in viewBox units.
const WIDTH = 900
const HEIGHT = 300
const MARGIN = { top: 16, right: 16, bottom: 36, left: 56 }
const PLOT_WIDTH = WIDTH - MARGIN.left - MARGIN.right
const PLOT_HEIGHT = HEIGHT - MARGIN.top - MARGIN.bottom

// The series color from the legacy chart (SAGE's red accent).
const SERIES_COLOR = '#CC333F'
const Y_TICK_COUNT = 4

export default function ApiUsageChart({ rangeFrom, rangeTo, usageCounts }) {
  const svgRef = React.useRef()
  const [ hoverIndex, setHoverIndex ] = React.useState(null)

  const points = buildHourlySeries(rangeFrom, rangeTo, usageCounts)
  if (points.length === 0) {
    return <p style={{ color: '#444' }}>No api requests were made in the selected time frame.</p>
  }

  const yMax = niceCeiling(Math.max(...points.map((p) => p.count)))
  const xStep = points.length > 1 ? PLOT_WIDTH / (points.length - 1) : 0
  const toX = (index) => MARGIN.left + (points.length > 1 ? index * xStep : PLOT_WIDTH / 2)
  const toY = (count) => MARGIN.top + PLOT_HEIGHT * (1 - count / yMax)

  const linePath = points
    .map((p, i) => `${i === 0 ? 'M' : 'L'}${toX(i).toFixed(2)},${toY(p.count).toFixed(2)}`)
    .join('')
  const areaPath = `${linePath}L${toX(points.length - 1).toFixed(2)},${toY(0)}` +
    `L${toX(0).toFixed(2)},${toY(0)}Z`

  const yTicks = Array.from({ length: Y_TICK_COUNT + 1 }, (unused, i) => (yMax / Y_TICK_COUNT) * i)
  const xTickIndexes = pickTickIndexes(points.length, 6)
  const showHours = (points[points.length - 1].time - points[0].time) <= 3 * 24 * HOUR_MILLIS

  // Maps a mouse position to the nearest point index (mouse coordinates are in
  // page pixels; the SVG scales to its container, so convert through viewBox units).
  const onMouseMove = (event) => {
    const rect = svgRef.current.getBoundingClientRect()
    const x = (event.clientX - rect.left) * (WIDTH / rect.width)
    const index = xStep > 0 ? Math.round((x - MARGIN.left) / xStep) : 0
    setHoverIndex(Math.max(0, Math.min(points.length - 1, index)))
  }

  const hovered = hoverIndex != null ? points[hoverIndex] : null

  return (
    <svg ref={svgRef} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} style={{ width: '100%', height: 'auto' }}
         onMouseMove={onMouseMove} onMouseLeave={() => setHoverIndex(null)}>
      {/* Grid and y axis */}
      {yTicks.map((tick) => (
        <g key={tick}>
          <line x1={MARGIN.left} x2={WIDTH - MARGIN.right} y1={toY(tick)} y2={toY(tick)}
                stroke="#e4e4e4" strokeWidth="1"/>
          <text x={MARGIN.left - 8} y={toY(tick) + 4} textAnchor="end"
                fontSize="11" fill="#666">{tick}</text>
        </g>
      ))}
      <text transform={`translate(14, ${MARGIN.top + PLOT_HEIGHT / 2}) rotate(-90)`}
            textAnchor="middle" fontSize="12" fill="#666">Requests per hour</text>

      {/* X axis */}
      <line x1={MARGIN.left} x2={WIDTH - MARGIN.right} y1={toY(0)} y2={toY(0)}
            stroke="#bbb" strokeWidth="1"/>
      {xTickIndexes.map((index) => (
        <text key={index} x={toX(index)} y={HEIGHT - 12} textAnchor="middle"
              fontSize="11" fill="#666">{formatTime(points[index].time, showHours)}</text>
      ))}

      {/* Series */}
      <path d={areaPath} fill={SERIES_COLOR} fillOpacity="0.2"/>
      <path d={linePath} fill="none" stroke={SERIES_COLOR} strokeWidth="2"/>

      {/* Hover crosshair + tooltip */}
      {hovered &&
        <g>
          <line x1={toX(hoverIndex)} x2={toX(hoverIndex)}
                y1={MARGIN.top} y2={toY(0)} stroke="#999" strokeDasharray="3,3"/>
          <circle cx={toX(hoverIndex)} cy={toY(hovered.count)} r="4"
                  fill={SERIES_COLOR} stroke="#fff" strokeWidth="2"/>
          <Tooltip x={toX(hoverIndex)} y={toY(hovered.count)}
                   lines={[ `${hovered.count} request${hovered.count === 1 ? '' : 's'}`,
                     formatTime(hovered.time, true) ]}/>
        </g>
      }
    </svg>
  )
}

function Tooltip({ x, y, lines }) {
  const width = Math.max(...lines.map((line) => line.length)) * 6.5 + 16
  const height = lines.length * 16 + 10
  // Flip the tooltip to the other side of the point near the chart edges.
  const boxX = x + 10 + width > WIDTH ? x - 10 - width : x + 10
  const boxY = Math.max(y - height - 6, 2)

  return (
    <g>
      <rect x={boxX} y={boxY} width={width} height={height} rx="3"
            fill="#fff" stroke="#ccc"/>
      {lines.map((line, i) => (
        <text key={line} x={boxX + 8} y={boxY + 18 + i * 16} fontSize="11" fill="#333">{line}</text>
      ))}
    </g>
  )
}

/** Fills the hour slots between rangeFrom and rangeTo with the returned
 *  counts, defaulting the missing hours to zero (like the legacy chart). */
function buildHourlySeries(rangeFrom, rangeTo, usageCounts) {
  const points = []
  let next = rangeFrom
  for (const { time, count } of usageCounts) {
    while (next < time && next < rangeTo) {
      points.push({ time: next, count: 0 })
      next += HOUR_MILLIS
    }
    points.push({ time, count })
    next = time + HOUR_MILLIS
  }
  return points
}

/** The smallest 1/2/5 × 10^k value that fits the max count, so the y axis
 *  ends on a round number. */
function niceCeiling(value) {
  if (value <= 4) {
    return 4
  }
  const magnitude = Math.pow(10, Math.floor(Math.log10(value)))
  for (const step of [ 1, 2, 5, 10 ]) {
    if (value <= step * magnitude) {
      return step * magnitude
    }
  }
  return 10 * magnitude
}

/** Around `count` evenly spaced indexes into the series for x axis labels. */
function pickTickIndexes(length, count) {
  if (length <= count) {
    return Array.from({ length }, (unused, i) => i)
  }
  const step = (length - 1) / (count - 1)
  return Array.from({ length: count }, (unused, i) => Math.round(i * step))
}

function formatTime(millis, showHours) {
  const date = new Date(millis)
  return showHours
    ? date.toLocaleString(undefined, { month: 'short', day: 'numeric', hour: 'numeric' })
    : date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })
}
