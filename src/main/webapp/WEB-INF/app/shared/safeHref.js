const SAFE_PROTOCOLS = [ 'http:', 'https:', 'mailto:' ]

/**
 * Returns url if it is safe to use as a link href, otherwise undefined.
 * React renders a javascript: URL in an href as-is, so links built from member
 * and DoH data, which come from outside sources, go through this.
 */
export default function safeHref(url) {
  if (url == null || url === '') {
    return undefined
  }
  try {
    return SAFE_PROTOCOLS.includes(new URL(url, window.location.origin).protocol) ? url : undefined
  } catch (e) {
    return undefined
  }
}
