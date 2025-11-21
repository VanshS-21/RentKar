import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { Button } from './ui/button'

/**
 * Navigation component that displays different content based on authentication state.
 * Shows login/register links for unauthenticated users.
 * Shows user info and logout button for authenticated users.
 */
const Navigation = () => {
  const { user, isAuthenticated, logout } = useAuth()

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
                <div className="flex items-center space-x-2">
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
