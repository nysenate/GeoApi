import React from 'react'
import { formatMemberName, POLY_COLORS } from 'app/shared/formatters'

// These types render their district number in a dedicated labeled line, so the
// generic "Code: {district}" line is redundant (mirrors the legacy displayCode()).
const TYPES_WITHOUT_CODE_LINE = [ 'SENATE', 'ASSEMBLY', 'CONGRESSIONAL', 'ZIP' ]

/**
 * Results panel content listing the districts that intersect the selected source
 * district, with their coverage percentages. Clicking an overlap shows its full
 * district map via onShowOverlap(index, overlap); clicking the header restores
 * the coverage view via onShowCoverage. Matches the legacy multi-senate-results
 * markup.
 */
export default function IntersectResults({ data, onShowOverlap, onShowCoverage }) {
  const { overlaps, intersectTypeDisplayName, intersectType } = data

  return (
    <div id="multi-senate-results">
      <div className="info-container title connected-bottom">
        <p className="member-name" onClick={onShowCoverage} style={{ cursor: 'pointer' }}>
          {`${overlaps.length} ${intersectTypeDisplayName} District Matches`}
          &nbsp;<a title="Show Map" className="icon-map"></a>
        </p>
      </div>
      {overlaps.map((overlap, i) => {
        const color = POLY_COLORS[i % POLY_COLORS.length]
        return (
          <div key={overlap.district} className="info-container connected clickable slim2"
               title="Show full district map"
               onClick={() => onShowOverlap(i, overlap)}>
            <table style={{ width: '100%' }}>
              <tbody>
              <tr>
                <td>
                  <div className="small-box" style={{ backgroundColor: color }}>
                    {overlap.areaPercentage >= 1 ? Number(overlap.areaPercentage).toFixed(0) : '<1'}%
                  </div>
                  {overlap.member != null &&
                    <div className="senator" style={{ height: '56px' }}>
                      {overlap.member.info?.imageUrl &&
                        <div className="senator-pic-holder" style={{ width: '50px', height: '50px' }}>
                          <a target="_blank" rel="noreferrer" href={overlap.member.info?.url}
                             onClick={(e) => e.stopPropagation()}>
                            <img src={overlap.member.info.imageUrl}
                                 alt={formatMemberName(overlap.member.info)} className="senator-pic"/>
                          </a>
                        </div>
                      }
                      <div style={{ lineHeight: '25px' }}>
                        <p className="senator member-name" style={{ fontSize: '16px' }}>
                          <a target="_blank" rel="noreferrer" href={overlap.member.info?.url}
                             onClick={(e) => e.stopPropagation()}>{formatMemberName(overlap.member.info)}</a>
                        </p>
                        <p style={{ fontSize: '16px', color: color }} className="senate district">
                          {intersectTypeDisplayName} District {overlap.district}
                        </p>
                      </div>
                    </div>
                  }
                  {overlap.member == null &&
                    <div style={{ paddingLeft: '10px' }}>
                      {overlap.name &&
                        <p className="district-name" style={{ color: color }}>{overlap.name}</p>
                      }
                      {!TYPES_WITHOUT_CODE_LINE.includes(intersectType) &&
                        <p className="district" style={{ color: color }}>Code: {overlap.district}</p>
                      }
                    </div>
                  }
                </td>
                <td className="right-icon-placeholder">
                  <a title="Show Map">
                    <div className="icon-map"></div>
                  </a>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        )
      })}
    </div>
  )
}
