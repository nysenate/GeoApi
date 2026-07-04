import React from 'react'

/**
 * The collapsible results column on the right side of the map pages, using the
 * legacy stylesheet's rightcolumn/result-header classes. showTab controls whether
 * the "Show Results" tab appears while the pane is closed.
 */
export default function ResultsPane({ open, onToggle, showTab, children }) {
  return (
    <React.Fragment>
      <div id="showResultsTab" className={`result-header success ${showTab && !open ? 'visible' : ''}`}
           onClick={() => onToggle(true)}>
        <div style={{ float: 'left' }}>Show Results</div>
        <div className="icon-arrow-down2 icon-hover-white small-right-icon"></div>
      </div>

      <div id="rightcolumn" className={open ? 'open' : undefined}>
        <div className="innertube">
          <div id="resultsTab" className="result-header success" onClick={() => onToggle(false)}>
            <div style={{ float: 'left' }}>Results</div>
            <div className="icon-arrow-up2 icon-hover-white small-right-icon"></div>
          </div>
          <div id="district-results" className="scrollable-content">
            {children}
          </div>
        </div>
      </div>
    </React.Fragment>
  )
}
