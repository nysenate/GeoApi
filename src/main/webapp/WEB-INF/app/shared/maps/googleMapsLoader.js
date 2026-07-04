let googleMapsPromise = null

/**
 * Loads the Google Maps script (url comes from globals.googleMapsUrl, which includes
 * the API key). Safe to call from multiple components; the script is only loaded once.
 * Resolves with the google.maps namespace.
 */
export default function loadGoogleMaps(googleMapsUrl) {
  if (!googleMapsPromise) {
    googleMapsPromise = new Promise((resolve, reject) => {
      const callbackName = '__sageGoogleMapsCallback'
      window[callbackName] = () => {
        delete window[callbackName]
        resolve(window.google.maps)
      }
      const script = document.createElement('script')
      const separator = googleMapsUrl.includes('?') ? '&' : '?'
      script.src = `${googleMapsUrl}${separator}loading=async&callback=${callbackName}`
      script.async = true
      script.onerror = () => reject(new Error('Failed to load the Google Maps script.'))
      document.head.appendChild(script)
    })
  }
  return googleMapsPromise
}
