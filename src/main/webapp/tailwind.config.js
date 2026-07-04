/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './WEB-INF/app/**/*.js',
    './WEB-INF/app/index.html',
  ],
  // The legacy stylesheets (css/main.css etc.) are loaded globally during the
  // migration; preflight's resets would override their base element styles.
  corePlugins: {
    preflight: false,
  },
  theme: {
    extend: {
      fontFamily: {
        sans: [ '"Source Sans Pro"', 'sans-serif' ],
      },
    },
  },
  plugins: [],
}
