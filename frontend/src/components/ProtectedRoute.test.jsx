import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import * as fc from 'fast-check'
import ProtectedRoute from './ProtectedRoute'
import { AuthProvider } from '../contexts/AuthContext'
import authService from '../services/authService'

// Mock authService
vi.mock('../services/authService')

// Mock axios to prevent actual 401 redirects
vi.mock('../lib/axios', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    interceptors: {
      request: { use: vi.fn() },
      response: { use: vi.fn() },
    },
  },
}))

describe('ProtectedRoute Property-Based Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  /**
   * Feature: user-authentication, Property 26: Authenticated users access protected routes
   * Validates: Requirements 7.3
   */
  it('Property 26: Authenticated users access protected routes', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.record({
          token: fc.string({ minLength: 20, maxLength: 200 }).filter(s => s.trim().length > 0),
          userId: fc.integer({ min: 1, max: 10000 }),
          username: fc.string({ minLength: 3, maxLength: 20 }).filter(s => s.trim().length > 0),
          email: fc.emailAddress(),
          fullName: fc.string({ minLength: 3, maxLength: 50 }).filter(s => s.trim().length > 0),
          protectedContent: fc.string({ minLength: 5, maxLength: 50 }).filter(s => s.trim().length > 0),
        }),
        async (testData) => {
          // Clear previous state
          localStorage.clear()
          vi.clearAllMocks()
          
          // Setup authenticated state
          localStorage.setItem('token', testData.token)

          const mockUser = {
            id: testData.userId,
            username: testData.username,
            email: testData.email,
            fullName: testData.fullName,
          }

          // Mock successful authentication
          authService.getCurrentUser.mockResolvedValue({
            success: true,
            data: mockUser,
          })

          // Render ProtectedRoute with authenticated state
          const { unmount } = render(
            <MemoryRouter initialEntries={['/protected']}>
              <AuthProvider>
                <Routes>
                  <Route
                    path="/protected"
                    element={
                      <ProtectedRoute>
                        <div data-testid="protected-content">{testData.protectedContent}</div>
                      </ProtectedRoute>
                    }
                  />
                  <Route path="/login" element={<div>Login Page</div>} />
                </Routes>
              </AuthProvider>
            </MemoryRouter>
          )

          // Wait for authentication to complete
          await waitFor(
            () => {
              const protectedElement = screen.queryByTestId('protected-content')
              expect(protectedElement).not.toBeNull()
            },
            { timeout: 2000 }
          )

          // Property: Protected content should be rendered for authenticated users
          const protectedElement = screen.getByTestId('protected-content')
          expect(protectedElement).toBeInTheDocument()
          expect(protectedElement.textContent).toBe(testData.protectedContent)
          
          // Cleanup
          unmount()
        }
      ),
      { numRuns: 10 }
    )
  }, 60000)

  /**
   * Feature: user-authentication, Property 27: Unauthenticated users are redirected from protected routes
   * Validates: Requirements 7.4
   */
  it('Property 27: Unauthenticated users are redirected from protected routes', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.string({ minLength: 5, maxLength: 50 }).filter(s => s.trim().length > 0),
        async (protectedContent) => {
          // Clear localStorage to ensure unauthenticated state
          localStorage.clear()
          vi.clearAllMocks()

          // Mock no user (unauthenticated)
          authService.getCurrentUser.mockResolvedValue({
            success: false,
          })

          // Render ProtectedRoute without authentication
          const { unmount } = render(
            <MemoryRouter initialEntries={['/protected']}>
              <AuthProvider>
                <Routes>
                  <Route
                    path="/protected"
                    element={
                      <ProtectedRoute>
                        <div data-testid="protected-content">{protectedContent}</div>
                      </ProtectedRoute>
                    }
                  />
                  <Route path="/login" element={<div data-testid="login-page">Login Page</div>} />
                </Routes>
              </AuthProvider>
            </MemoryRouter>
          )

          // Wait for redirect to complete
          await waitFor(
            () => {
              const loginPage = screen.queryByTestId('login-page')
              expect(loginPage).not.toBeNull()
            },
            { timeout: 2000 }
          )

          // Property: Should redirect to login page
          const loginPage = screen.getByTestId('login-page')
          expect(loginPage).toBeInTheDocument()

          // Property: Protected content should NOT be rendered
          const protectedElement = screen.queryByTestId('protected-content')
          expect(protectedElement).not.toBeInTheDocument()
          
          // Cleanup
          unmount()
        }
      ),
      { numRuns: 10 }
    )
  }, 60000)

  /**
   * Feature: user-authentication, Property 19: 401 responses trigger login redirect
   * Validates: Requirements 4.5
   */
  it('Property 19: 401 responses trigger login redirect', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.record({
          token: fc.string({ minLength: 20, maxLength: 200 }).filter(s => s.trim().length > 0),
          errorMessage: fc.constantFrom(
            'Unauthorized',
            'Token expired',
            'Invalid token',
            'Authentication failed'
          ),
        }),
        async (testData) => {
          // Clear previous state
          localStorage.clear()
          vi.clearAllMocks()
          
          // Setup token in localStorage (simulating expired/invalid token)
          localStorage.setItem('token', testData.token)

          // Mock 401 response
          authService.getCurrentUser.mockResolvedValue({
            success: false,
            error: testData.errorMessage,
            status: 401,
          })

          // Render ProtectedRoute
          const { unmount } = render(
            <MemoryRouter initialEntries={['/protected']}>
              <AuthProvider>
                <Routes>
                  <Route
                    path="/protected"
                    element={
                      <ProtectedRoute>
                        <div data-testid="protected-content">Protected Content</div>
                      </ProtectedRoute>
                    }
                  />
                  <Route path="/login" element={<div data-testid="login-page">Login Page</div>} />
                </Routes>
              </AuthProvider>
            </MemoryRouter>
          )

          // Wait for redirect to login
          await waitFor(
            () => {
              const loginPage = screen.queryByTestId('login-page')
              expect(loginPage).not.toBeNull()
            },
            { timeout: 2000 }
          )

          // Property: Should redirect to login page on 401
          const loginPage = screen.getByTestId('login-page')
          expect(loginPage).toBeInTheDocument()

          // Property: Token should be cleared from localStorage
          expect(localStorage.getItem('token')).toBe(null)

          // Property: Protected content should NOT be rendered
          const protectedElement = screen.queryByTestId('protected-content')
          expect(protectedElement).not.toBeInTheDocument()
          
          // Cleanup
          unmount()
        }
      ),
      { numRuns: 10 }
    )
  }, 60000)
})
