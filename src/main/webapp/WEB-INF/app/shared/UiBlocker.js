import React from 'react'

/**
 * Full-page loading overlay, replicating the jQuery blockUI look the legacy
 * pages used while requests were in flight.
 */
export default function UiBlocker({ message }) {
  return (
    <React.Fragment>
      <div className="ui-block-overlay"></div>
      <div className="ui-block-message">{message}</div>
    </React.Fragment>
  )
}
