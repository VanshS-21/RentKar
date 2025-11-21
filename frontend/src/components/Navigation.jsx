import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { Button } from './ui/button'
import { useEffect, useState } from 'react'
import borrowRequestService from '../services/borrowRequestService'

/**
 * Navigation component that displays different content based on authentication state.
 * Shows login/register links for unauthenticated users.
 * Shows user info and logout button for authenticated users.
 * Displays notification badge for pending incoming requests.
 * Requirements: 14.1, 14.2
 */
const Navigation = () => {
  const { user, isAuthenticated, logout } = useAuth()
  const location = useLocation()
  const [pendingCount, setPendingCount] = useState(0)

  useEffect(() => {
    if (isAuthenticated) {
      fetchPendingCount()
      // Poll for updates every 30 seconds
      const interval = setInterval(fetchPendingCount, 30000)
      return () => clearInterval(interval)
    }
  }, [isAuthenticated])

  // Clear badge when viewing incoming requests page
  useEffect(() => {
    if (location.pathname === '/requests/received') {
      setPendingCount(0)
    } else if (isAuthenticated) {
      fetchPendingCount()
    }
  }, [location.pathname, isAuthenticated])

  const fetchPendingCount = async () => {
    const result = await borrowRequestService.getStatistics()
    if (result.success && result.data) {
      // Get pending incoming requests count
      const received = await borrowRequestService.getReceivedRequests('PENDING')
      if (received.success && received.data) {
        setPendingCount(received.data.length)
      }
    }
  }

  const handleLogout = () => {
    logout()
  }

  return (
    <nav className="bg-card border-b">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          {/* Logo/Brand */}
          <Link to="/" className="flex items-center space-x-2">
            <span className="text-2xl">🎒</span>
            <span className="text-xl font-bold">RentKar</span>
          </Link>

          {/* Navigation Items */}
          <div className="flex items-center space-x-4">
            {isAuthenticated ? (
              // Authenticated User Navigation
              <>
                <Link to="/items">
                  <Button variant="ghost" size="sm">
                    Browse Items
                  </Button>
                </Link>
                <Link to="/my-items">
                  <Button variant="ghost" size="sm">
                    My Items
                  </Button>
                </Link>
                <Link to="/requests/sent">
                  <Button variant="ghost" size="sm">
                    My Requests
                  </Button>
                </Link>
                <Link to="/requests/received" className="relative">
                  <Button variant="ghost" size="sm">
                    Incoming Requests
                    {pendingCount > 0 && (
                      <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs font-bold rounded-full h-5 w-5 flex items-center justify-center">
                        {pendingCount > 9 ? '9+' : pendingCount}
                      </span>
                    )}
                  </Button>
                </Link>
                <Link to="/items/new">
                  <Button variant="default" size="sm">
                    Add Item
                  </Button>
                </Link>
                <div className="flex items-center space-x-2 ml-4">
                  <span className="text-sm text-muted-foreground">
                    Welcome,
                  </span>
                  <span className="text-sm font-medium">
                    {user?.fullName || user?.username}
                  </span>
                </div>
                <Button
                  onClick={handleLogout}
                  variant="outline"
                  size="sm"
                >
                  Logout
                </Button>
              </>
            ) : (
              // Unauthenticated User Navigation
              <>
                <Link to="/login">
                  <Button variant="ghost" size="sm">
                    Log In
                  </Button>
                </Link>
                <Link to="/register">
                  <Button size="sm">
                    Sign Up
                  </Button>
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  )
}

export default Navigation
