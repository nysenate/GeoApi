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

// These types render their district number in a dedicated labeled line, so the
// generic "Code: {district}" line is redundant (mirrors the legacy displayCode()).
const TYPES_WITHOUT_CODE_LINE = [ 'SENATE', 'ASSEMBLY', 'CONGRESSIONAL', 'ZIP' ]

/** Whether a district result should show the generic "Code: {district}" line.
 *  Accepts the type in any casing (result keys are lowercase, enum names uppercase). */
export function showsCodeLine(type) {
  return !TYPES_WITHOUT_CODE_LINE.includes(String(type).toUpperCase())
}

/** The polygon color rotation from the legacy mapService:
 *  teal, orangered, green, red, yellow, cyan, pink, purple, darkblue. */
export const POLY_COLORS = [
  '#008080', '#ff4500', '#639A00', '#CC333F', '#EDC951',
  '#09AA91', '#F56991', '#524656', '#547980',
]

/** Formats an epoch-millis timestamp like the legacy medium date filter,
 *  e.g. "Sep 3, 2025, 12:05:08 PM". */
export function formatDateMedium(millis) {
  if (millis == null) {
    return ''
  }
  return new Date(millis).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'medium' })
}

/** Formats an epoch-millis timestamp like the legacy short date filter,
 *  e.g. "9/3/25, 12:05 PM". */
export function formatDateShort(millis) {
  if (millis == null) {
    return ''
  }
  return new Date(millis).toLocaleString(undefined, { dateStyle: 'short', timeStyle: 'short' })
}

function notEmpty(input) {
  return input != null && input !== '' && input !== 'null'
}
