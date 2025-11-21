import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, waitFor, act } from '@testing-library/react'
import { AuthProvider, useAuth } from './AuthContext'
import * as fc from 'fast-check'
import authService from '../services/authService'

// Mock authService
vi.mock('../services/authService')

describe('AuthContext Property-Based Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  /**
   * Feature: user-authentication, Property 11: Successful login stores token in localStorage
   * Validates: Requirements 3.1
   */
  it('Property 11: Successful login stores token in localStorage', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.record({
          username: fc.string({ minLength: 3, maxLength: 20 }).filter(s => s.trim().length > 0),
          password: fc.string({ minLength: 8, maxLength: 50 }).filter(s => s.trim().length > 0),
          token: fc.string({ minLength: 20, maxLength: 200 }).filter(s => s.trim().length > 0),
          userId: fc.integer({ min: 1, max: 10000 }),
          email: fc.emailAddress(),
        }),
        async (loginData) => {
          // Clear any previous state
          localStorage.clear()
          vi.clearAllMocks()
          
          // Setup mock response
          const mockUser = {
            id: loginData.userId,
            username: loginData.username,
            email: loginData.email,
          }

          authService.login.mockResolvedValue({
            success: true,
            data: {
              token: loginData.token,
              user: mockUser,
            },
          })

          authService.getCurrentUser.mockResolvedValue({
            success: false,
          })

          // Render hook
          const { result } = renderHook(() => useAuth(), {
            wrapper: AuthProvider,
          })

          // Wait for initialization
          await waitFor(
            () => {
              expect(result.current.isLoading).toBe(false)
            },
            { timeout: 3000 }
          )

          // Perform login
          let loginResult
          await act(async () => {
            loginResult = await result.current.login({
              username: loginData.username,
              password: loginData.password,
            })
          })

          // Verify login was successful
          expect(loginResult.success).toBe(true)

          // Property: Token should be stored in localStorage
          const storedToken = localStorage.getItem('token')
          expect(storedToken).toBe(loginData.token)

          // Additional verification: token should be in context state
          expect(result.current.token).toBe(loginData.token)
        }
      ),
      { numRuns: 100 }
    )
  }, 10000)

  /**
   * Feature: user-authentication, Property 12: Valid tokens restore session on refresh
   * Validates: Requirements 3.2
   */
  it('Property 12: Valid tokens restore session on refresh', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.record({
          token: fc.string({ minLength: 20, maxLength: 200 }).filter(s => s.trim().length > 0),
          userId: fc.integer({ min: 1, max: 10000 }),
          username: fc.string({ minLength: 3, maxLength: 20 }).filter(s => s.trim().length > 0),
          email: fc.emailAddress(),
          fullName: fc.string({ minLength: 3, maxLength: 50 }).filter(s => s.trim().length > 0),
        }),
        async (userData) => {
          // Clear any previous state
          localStorage.clear()
          vi.clearAllMocks()
          
          // Place valid token in localStorage
          localStorage.setItem('token', userData.token)

          const mockUser = {
            id: userData.userId,
            username: userData.username,
            email: userData.email,
            fullName: userData.fullName,
          }

          // Mock successful user fetch
          authService.getCurrentUser.mockResolvedValue({
            success: true,
            data: mockUser,
          })

          // Initialize context (simulating page refresh)
          const { result } = renderHook(() => useAuth(), {
            wrapper: AuthProvider,
          })

          // Wait for auth state to be restored
          await waitFor(
            () => {
              expect(result.current.isLoading).toBe(false)
              expect(result.current.isAuthenticated).toBe(true)
            },
            { timeout: 3000 }
          )

          // Property: Auth state should be restored
          expect(result.current.token).toBe(userData.token)
          expect(result.current.user).toEqual(mockUser)
        }
      ),
      { numRuns: 100 }
    )
  }, 10000)

  /**
   * Feature: user-authentication, Property 13: Expired tokens clear session
   * Validates: Requirements 3.3
   */
  it('Property 13: Expired tokens clear session', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.string({ minLength: 20, maxLength: 200 }).filter(s => s.trim().length > 0),
        async (expiredToken) => {
          // Clear any previous state
          localStorage.clear()
          vi.clearAllMocks()
          
          // Place expired token in localStorage
          localStorage.setItem('token', expiredToken)

          // Mock failed user fetch (token expired)
          authService.getCurrentUser.mockResolvedValue({
            success: false,
            error: 'Token expired',
            status: 401,
          })

          // Initialize context
          const { result } = renderHook(() => useAuth(), {
            wrapper: AuthProvider,
          })

          // Wait for initialization
          await waitFor(
            () => {
              expect(result.current.isLoading).toBe(false)
            },
            { timeout: 3000 }
          )

          // Property: Session should be cleared
          expect(result.current.isAuthenticated).toBe(false)
          expect(result.current.token).toBe(null)
          expect(result.current.user).toBe(null)
          expect(localStorage.getItem('token')).toBe(null)
        }
      ),
      { numRuns: 100 }
    )
  }, 10000)

  /**
   * Feature: user-authentication, Property 14: Logout clears all auth state
   * Validates: Requirements 3.4
   */
  it('Property 14: Logout clears all auth state', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.record({
          token: fc.string({ minLength: 20, maxLength: 200 }).filter(s => s.trim().length > 0),
          userId: fc.integer({ min: 1, max: 10000 }),
          username: fc.string({ minLength: 3, maxLength: 20 }).filter(s => s.trim().length > 0),
          email: fc.emailAddress(),
        }),
        async (userData) => {
          // Clear any previous state
          localStorage.clear()
          vi.clearAllMocks()
          
          // Setup authenticated state
          localStorage.setItem('token', userData.token)

          const mockUser = {
            id: userData.userId,
            username: userData.username,
            email: userData.email,
          }

          authService.getCurrentUser.mockResolvedValue({
            success: true,
            data: mockUser,
          })

          // Initialize context with authenticated state
          const { result } = renderHook(() => useAuth(), {
            wrapper: AuthProvider,
          })

          await waitFor(
            () => {
              expect(result.current.isLoading).toBe(false)
              expect(result.current.isAuthenticated).toBe(true)
            },
            { timeout: 3000 }
          )

          // Call logout
          act(() => {
            result.current.logout()
          })

          // Property: All auth state should be cleared
          await waitFor(() => {
            expect(result.current.isAuthenticated).toBe(false)
          })
          expect(result.current.token).toBe(null)
          expect(result.current.user).toBe(null)
          expect(localStorage.getItem('token')).toBe(null)
        }
      ),
      { numRuns: 100 }
    )
  }, 10000)

  /**
   * Feature: user-authentication, Property 25: App initialization restores auth state
   * Validates: Requirements 7.1
   */
  it('Property 25: App initialization restores auth state', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.record({
          token: fc.string({ minLength: 20, maxLength: 200 }).filter(s => s.trim().length > 0),
          userId: fc.integer({ min: 1, max: 10000 }),
          username: fc.string({ minLength: 3, maxLength: 20 }).filter(s => s.trim().length > 0),
          email: fc.emailAddress(),
          fullName: fc.string({ minLength: 3, maxLength: 50 }).filter(s => s.trim().length > 0),
          role: fc.constantFrom('USER', 'ADMIN'),
        }),
        async (userData) => {
          // Clear any previous state
          localStorage.clear()
          vi.clearAllMocks()
          
          // Place valid token in storage before app initialization
          localStorage.setItem('token', userData.token)

          const mockUser = {
            id: userData.userId,
            username: userData.username,
            email: userData.email,
            fullName: userData.fullName,
            role: userData.role,
          }

          // Mock successful user fetch
          authService.getCurrentUser.mockResolvedValue({
            success: true,
            data: mockUser,
          })

          // Initialize app (AuthContext)
          const { result } = renderHook(() => useAuth(), {
            wrapper: AuthProvider,
          })

          // Wait for initialization to complete
          await waitFor(
            () => {
              expect(result.current.isLoading).toBe(false)
              expect(result.current.isAuthenticated).toBe(true)
            },
            { timeout: 3000 }
          )

          // Property: Auth state should be restored from storage
          expect(result.current.token).toBe(userData.token)
          expect(result.current.user).toEqual(mockUser)
        }
      ),
      { numRuns: 100 }
    )
  }, 10000)
})
