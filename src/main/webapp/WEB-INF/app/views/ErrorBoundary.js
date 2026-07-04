import React from 'react'

export default class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props)
    this.state = { hasError: false }
  }

  static getDerivedStateFromError(error) {
    return { hasError: true }
  }

  componentDidCatch(error, errorInfo) {
    console.error(error, errorInfo)
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="m-8 text-center">
          <h1 className="text-2xl font-semibold">Something went wrong.</h1>
          <p className="mt-4">Try refreshing the page. If the problem persists, please contact the SAGE team.</p>
        </div>
      )
    }
    return this.props.children
  }
}
