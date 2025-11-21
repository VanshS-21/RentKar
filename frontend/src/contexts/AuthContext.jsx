import { createContext, useContext, useState, useEffect } from 'react'
import authService from '../services/authService'

const AuthContext = createContext(null)

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const [token, setToken] = useState(null)

  // Check localStorage for token on initialization and restore auth state
  useEffect(() => {
    const initializeAuth = async () => {
      const storedToken = localStorage.getItem('token')
      
      if (storedToken) {
        setToken(storedToken)
        
        // Try to restore user data with the stored token
        try {
          const response = await authService.getCurrentUser()
          if (response.success) {
            setUser(response.data)
          } else {
            // Token is invalid or expired, clear it
            localStorage.removeItem('token')
            setToken(null)
          }
        } catch (error) {
          console.error('Failed to restore auth state:', error)
          localStorage.removeItem('token')
          setToken(null)
        }
      }
      
      setIsLoading(false)
    }

    initializeAuth()
  }, [])

  const login = async (credentials) => {
    try {
      const response = await authService.login(credentials)
      
      if (response.success) {
        const { token: newToken, user: userData } = response.data
        
        // Store token in state and localStorage
        setToken(newToken)
        setUser(userData)
        localStorage.setItem('token', newToken)
        
        return { success: true }
      } else {
        return {
          success: false,
          message: response.error || 'Login failed',
        }
      }
    } catch (error) {
      // Network errors should display user-friendly messages
      const isNetworkError = error.message && (
        error.message.includes('Network') ||
        error.message.includes('network') ||
        error.message.includes('fetch') ||
        error.code === 'ERR_NETWORK'
      )
      
      return {
        success: false,
        message: isNetworkError 
          ? 'An unexpected error occurred. Please check your connection and try again.' 
          : (error.message || 'Login failed'),
      }
    }
  }

  const register = async (userData) => {
    try {
      const response = await authService.register(userData)
      
      if (response.success) {
        return { success: true, data: response.data }
      } else {
        return {
          success: false,
          message: response.error || 'Registration failed',
        }
      }
    } catch (error) {
      // Network errors should display user-friendly messages
      const isNetworkError = error.message && (
        error.message.includes('Network') ||
        error.message.includes('network') ||
        error.message.includes('fetch') ||
        error.code === 'ERR_NETWORK'
      )
      
      return {
        success: false,
        message: isNetworkError 
          ? 'An unexpected error occurred. Please check your connection and try again.' 
          : (error.message || 'Registration failed'),
      }
    }
  }

  const logout = () => {
    // Clear all auth state
    setUser(null)
    setToken(null)
    localStorage.removeItem('token')
  }

  const value = {
    user,
    token,
    isLoading,
    isAuthenticated: !!user && !!token,
    login,
    register,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
