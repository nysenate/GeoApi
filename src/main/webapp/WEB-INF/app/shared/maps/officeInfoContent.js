/**
 * Builds the element shown in an office marker's info window, matching the
 * legacy mapService.setOfficeMarkers content. The office data comes from outside
 * sources, so it is set as text rather than parsed as HTML.
 */
export default function officeInfoContent(office) {
  const container = document.createElement('div')
  container.style.width = '160px'
  const addLine = (text, style) => {
    const p = document.createElement('p')
    p.textContent = text
    Object.assign(p.style, style)
    container.appendChild(p)
  }
  addLine(office.name ?? '', { color: 'teal', fontSize: '18px' })
  addLine(office.address?.addr1 ?? '')
  addLine(`${office.address?.city ?? ''}, NY ${office.address?.zip5 ?? ''}`)
  addLine(`Phone ${office.phone ?? ''}`)
  return container
}
