/** Formats a member's display name from a MemberInfo object.
 *  Default: "First Last". Pass inverted=true for "Last, First". */
export function formatMemberName(info, inverted = false) {
  if (info == null) {
    return ''
  }
  return inverted ? `${info.nameEnd}, ${info.nameStart}` : `${info.nameStart} ${info.nameEnd}`
}

/** Formats an AddressView as [line1, line2], omitting empty parts. */
export function formatAddressLines(address) {
  if (address == null) {
    return []
  }
  const line1 = [ address.addr1, address.addr2 ].filter(notEmpty).join(' ')
  const cityState = [ address.city, address.state ].filter(notEmpty).join(', ')
  const zip = notEmpty(address.zip5)
    ? (notEmpty(address.zip4) ? `${address.zip5}-${address.zip4}` : address.zip5)
    : ''
  const line2 = [ cityState, zip ].filter(notEmpty).join(' ')
  return [ line1, line2 ].filter(notEmpty)
}

/** Returns the display title for a district's map: "name - Member Name". */
export function getMapName(district) {
  if (district == null) {
    return ''
  }
  return (district.name ?? '') +
    (district.member ? ` - ${formatMemberName(district.member.info)}` : '')
}

/** The polygon color rotation from the legacy mapService:
 *  teal, orangered, green, red, yellow, cyan, pink, purple, darkblue. */
export const POLY_COLORS = [
  '#008080', '#ff4500', '#639A00', '#CC333F', '#EDC951',
  '#09AA91', '#F56991', '#524656', '#547980',
]

function notEmpty(input) {
  return input != null && input !== '' && input !== 'null'
}
