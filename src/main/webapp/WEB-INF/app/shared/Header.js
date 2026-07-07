import React from 'react'
import { Link, useLocation } from 'react-router-dom'

/**
 * The blue SAGE header bar with the tool menu, styled by the legacy main.css.
 * Converted tools are React Router links; the active one is derived from the
 * current route. Tools that have not been converted yet render as inert links
 * so the menu looks the same as the legacy page.
 */
const MENU_ITEMS = [
  { label: 'District Lookup', icon: 'icon-search', path: '/' },
  { label: 'District Maps', icon: 'icon-map', path: '/maps' },
  { label: 'USPS Lookup', icon: 'icon-mail', path: '/usps' },
  { label: 'Street Finder', icon: 'icon-directions', path: '/street' },
  { label: 'Reverse Geocode', icon: 'icon-target', path: '/revgeo' },
  { label: 'Batch', icon: 'icon-upload', path: '/job' },
  { label: 'Developer API', href: '/docs/html/index.html', newTab: true },
]

export default function Header() {
  const location = useLocation()

  return (
    <div id="header">
      <div id="sageLogoText">
        <Link to="/">SAGE</Link>
      </div>
      <ul className="top-method-header">
        {MENU_ITEMS.map((item) => {
          const active = item.path != null && item.path === location.pathname
          const content = (
            <React.Fragment>
              {active && item.icon &&
                <React.Fragment>
                  <div className={`icon-white-no-hover ${item.icon}`}></div>&nbsp;&nbsp;
                </React.Fragment>
              }
              {item.label}
            </React.Fragment>
          )
          return (
            <li key={item.label}>
              {item.path != null
                ? <Link className={active ? 'active' : undefined} to={item.path}>{content}</Link>
                : <a href={item.href}
                     target={item.newTab ? '_blank' : undefined}
                     rel={item.newTab ? 'noreferrer' : undefined}>{content}</a>
              }
            </li>
          )
        })}
      </ul>
    </div>
  )
}
