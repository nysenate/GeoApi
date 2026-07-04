import React from 'react'
import { formatMemberName } from 'app/shared/formatters'

/**
 * Results panel content showing the member (senator etc.) for a selected
 * district, with contact info and office locations. Matches the legacy
 * map-member-results markup.
 */
export default function MemberResults({ member, districtName, onLocateOffice }) {
  const info = member?.info
  const offices = (member?.offices ?? []).filter((office) => office?.name)

  return (
    <div id="map-member-results">
      <div className="info-container" style={{ height: '70px' }}>
        {info?.imageUrl &&
          <div className="senator-pic-holder">
            <img src={info.imageUrl} alt={formatMemberName(info)} className="senator-pic"/>
          </div>
        }
        <div style={{ marginTop: '10px' }}>
          <p className="senator member-name">
            <a target="_blank" rel="noreferrer" href={info?.url}>{formatMemberName(info)}</a>
          </p>
          <p className="senate district">{districtName}</p><br/>
        </div>
      </div>

      {info?.email &&
        <div className="info-container slim">
          <div className="icon-mail icon-teal" style={{ marginRight: '10px' }}></div>
          <span className="member-email">{info.email}</span>
        </div>
      }

      {offices.map((office) =>
        <div key={office.name} className="info-container" style={{ fontSize: '14px' }}>
          <table style={{ width: '100%' }}>
            <tbody>
            <tr>
              <td><p style={{ fontSize: '16px', color: 'teal' }}>{office.name}</p></td>
              <td className="right-icon-placeholder">
                {office.point &&
                  <a title="Locate office" onClick={() => onLocateOffice(office)}>
                    <div className="icon-location icon-hover-teal"></div>
                  </a>
                }
              </td>
            </tr>
            </tbody>
          </table>
          <p>{office.address?.addr1}</p>
          <p>{office.address?.addr2}</p>
          <p>{office.address?.city}, {office.address?.state} {office.address?.zip5}</p>
          <p>Phone {office.phone}</p>
        </div>
      )}
    </div>
  )
}
