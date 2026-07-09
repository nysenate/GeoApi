/**
 * Builds the HTML string shown in an office marker's info window, matching the
 * legacy mapService.setOfficeMarkers content.
 */
export default function officeInfoContent(office) {
  return "<div style='width:160px;'>" +
    `<p style='color:teal;font-size:18px;'>${office.name}</p>` +
    `<p>${office.address?.addr1 ?? ''}</p>` +
    `<p>${office.address?.city ?? ''}, NY ${office.address?.zip5 ?? ''}</p>` +
    `<p>Phone ${office.phone ?? ''}</p>` +
    '</div>'
}
