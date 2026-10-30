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
  // Legacy class names that collide with Tailwind utilities. main.css's
  // `.highlight-section.fixed` is a fixed-width centered panel, not fixed positioning.
  blocklist: [ 'fixed' ],
  theme: {
    extend: {
      fontFamily: {
        sans: [ '"Source Sans Pro"', 'sans-serif' ],
      },
    },
  },
  plugins: [],
}
