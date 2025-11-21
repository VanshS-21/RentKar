import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter, MemoryRouter } from 'react-router-dom'
import * as fc from 'fast-check'
import { AuthProvider } from '../contexts/AuthContext'
import LoginPage from './LoginPage'
import RegisterPage from './RegisterPage'
import authService from '../services/authService'
import toast from 'react-hot-toast'

// Mock the authService
vi.mock('../services/authService')

// Mock react-hot-toast
vi.mock('react-hot-toast', () => ({
  default: {
    success: vi.fn(),
    error: vi.fn(),
  },
}))

// Mock useNavigate
const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  }
})

// Helper to render components with necessary providers
const renderWithProviders = (component) => {
  return render(
    <MemoryRouter>
      <AuthProvider>{component}</AuthProvider>
    </MemoryRouter>
  )
}

describe('Authentication UI Property Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    // Mock getCurrentUser to avoid initialization issues
    authService.getCurrentUser.mockResolvedValue({
      success: false,
      error: 'Not authenticated',
    })
  })

  /**
   * Feature: user-authentication, Property 20: Authentication errors display messages
   * Validates: Requirements 5.2
   */
  describe('Property 20: Authentication errors display messages', () => {
    it('should display error messages for various authentication failures', async () => {
      await fc.assert(
        fc.asyncProperty(
          fc.record({
            errorMessage: fc.string({ minLength: 5, maxLength: 50 }).filter(s => s.trim().length >= 5),
            errorType: fc.constantFrom('login', 'register'),
          }),
          async ({ errorMessage, errorType }) => {
            // Clear any previous renders
            document.body.innerHTML = ''
            
            const user = userEvent.setup()

            if (errorType === 'login') {
              // Mock login failure
              authService.login.mockResolvedValue({
                success: false,
                error: errorMessage,
              })

              const { unmount, container } = renderWithProviders(<LoginPage />)

              // Fill in form
              const usernameInput = container.querySelector('input[name="username"]')
              const passwordInput = container.querySelector('input[name="password"]')
              const submitButton = container.querySelector('button[type="submit"]')
              
              await user.type(usernameInput, 'testuser')
              await user.type(passwordInput, 'password123')
              await user.click(submitButton)

              // Verify error message is displayed
              await waitFor(() => {
                expect(screen.getByText(errorMessage)).toBeInTheDocument()
              }, { timeout: 1000 })
              
              unmount()
            } else {
              // Mock register failure
              authService.register.mockResolvedValue({
                success: false,
                error: errorMessage,
              })

              const { unmount, container } = renderWithProviders(<RegisterPage />)

              // Fill in form
              const usernameInput = container.querySelector('input[name="username"]')
              const emailInput = container.querySelector('input[name="email"]')
              const passwordInput = container.querySelector('input[name="password"]')
              const fullNameInput = container.querySelector('input[name="fullName"]')
              const submitButton = container.querySelector('button[type="submit"]')
              
              await user.type(usernameInput, 'testuser')
              await user.type(emailInput, 'test@example.com')
              await user.type(passwordInput, 'password123')
              await user.type(fullNameInput, 'Test User')
              await user.click(submitButton)

              // Verify error message is displayed
              await waitFor(() => {
                expect(screen.getByText(errorMessage)).toBeInTheDocument()
              }, { timeout: 1000 })
              
              unmount()
            }
            
            // Final cleanup
            document.body.innerHTML = ''
          }
        ),
        { numRuns: 10, timeout: 20000 }
      )
    }, 25000)
  })

  /**
   * Feature: user-authentication, Property 21: Successful login redirects appropriately
   * Validates: Requirements 5.3
   */
  describe('Property 21: Successful login redirects appropriately', () => {
    it('should redirect to home or intended destination after successful login', async () => {
      await fc.assert(
        fc.asyncProperty(
          fc.record({
            username: fc.string({ minLength: 3, maxLength: 20 }).filter(s => s.trim().length >= 3),
            password: fc.string({ minLength: 8, maxLength: 30 }).filter(s => s.trim().length >= 8),
            token: fc.string({ minLength: 20, maxLength: 100 }).filter(s => s.trim().length >= 20),
            userId: fc.integer({ min: 1, max: 10000 }),
          }),
          async ({ username, password, token, userId }) => {
            // Clear any previous renders
            document.body.innerHTML = ''
            mockNavigate.mockClear()
            
            const user = userEvent.setup()

            // Mock successful login
            authService.login.mockResolvedValue({
              success: true,
              data: {
                token,
                user: {
                  id: userId,
                  username,
                  email: `${username}@example.com`,
                  fullName: 'Test User',
                },
              },
            })

            const { unmount, container } = renderWithProviders(<LoginPage />)

            // Fill in form
            const usernameInput = container.querySelector('input[name="username"]')
            const passwordInput = container.querySelector('input[name="password"]')
            const submitButton = container.querySelector('button[type="submit"]')
            
            await user.type(usernameInput, username)
            await user.type(passwordInput, password)
            await user.click(submitButton)

            // Verify navigation was called (successful login should trigger navigation)
            await waitFor(() => {
              expect(mockNavigate).toHaveBeenCalled()
            }, { timeout: 3000 })
            
            unmount()
            document.body.innerHTML = ''
          }
        ),
        { numRuns: 10, timeout: 20000 }
      )
    }, 30000)
  })

  /**
   * Feature: user-authentication, Property 22: Successful registration redirects to login
   * Validates: Requirements 5.4
   */
  describe('Property 22: Successful registration redirects to login', () => {
    it('should redirect to login page after successful registration', async () => {
      await fc.assert(
        fc.asyncProperty(
          fc.record({
            username: fc.string({ minLength: 3, maxLength: 20 }).filter(s => s.trim().length >= 3),
            email: fc.emailAddress(),
            password: fc.string({ minLength: 8, maxLength: 30 }).filter(s => s.trim().length >= 8),
            fullName: fc.string({ minLength: 3, maxLength: 50 }).filter(s => s.trim().length >= 3),
            userId: fc.integer({ min: 1, max: 10000 }),
          }),
          async ({ username, email, password, fullName, userId }) => {
            // Clear any previous renders
            document.body.innerHTML = ''
            mockNavigate.mockClear()
            
            const user = userEvent.setup()

            // Mock successful registration (returns user data, not token)
            authService.register.mockResolvedValue({
              success: true,
              data: {
                id: userId,
                username,
                email,
                fullName,
              },
            })

            const { unmount, container } = renderWithProviders(<RegisterPage />)

            // Fill in form
            const usernameInput = container.querySelector('input[name="username"]')
            const emailInput = container.querySelector('input[name="email"]')
            const passwordInput = container.querySelector('input[name="password"]')
            const fullNameInput = container.querySelector('input[name="fullName"]')
            const submitButton = container.querySelector('button[type="submit"]')
            
            await user.type(usernameInput, username)
            await user.type(emailInput, email)
            await user.type(passwordInput, password)
            await user.type(fullNameInput, fullName)
            await user.click(submitButton)

            // Verify navigation was called with '/login' as the first argument
            await waitFor(() => {
              expect(mockNavigate).toHaveBeenCalledWith('/login')
            }, { timeout: 3000 })
            
            unmount()
            document.body.innerHTML = ''
          }
        ),
        { numRuns: 10, timeout: 20000 }
      )
    }, 30000)
  })

  /**
   * Feature: user-authentication, Property 23: Network errors display friendly messages
   * Validates: Requirements 5.5
   */
  describe('Property 23: Network errors display friendly messages', () => {
    it('should display user-friendly messages for network errors', async () => {
      await fc.assert(
        fc.asyncProperty(
          fc.record({
            operationType: fc.constantFrom('login', 'register'),
          }),
          async ({ operationType }) => {
            // Clear any previous renders
            document.body.innerHTML = ''
            
            const user = userEvent.setup()

            if (operationType === 'login') {
              // Mock network error for login
              authService.login.mockRejectedValue(new Error('Network Error'))

              const { unmount, container } = renderWithProviders(<LoginPage />)

              // Fill in form
              const usernameInput = container.querySelector('input[name="username"]')
              const passwordInput = container.querySelector('input[name="password"]')
              const submitButton = container.querySelector('button[type="submit"]')
              
              await user.type(usernameInput, 'testuser')
              await user.type(passwordInput, 'password123')
              await user.click(submitButton)

              // Verify friendly error message is displayed
              await waitFor(() => {
                const errorText = container.textContent
                expect(errorText).toMatch(/unexpected error/i)
              }, { timeout: 2000 })
              
              unmount()
            } else {
              // Mock network error for registration
              authService.register.mockRejectedValue(new Error('Network Error'))

              const { unmount, container } = renderWithProviders(<RegisterPage />)

              // Fill in form
              const usernameInput = container.querySelector('input[name="username"]')
              const emailInput = container.querySelector('input[name="email"]')
              const passwordInput = container.querySelector('input[name="password"]')
              const fullNameInput = container.querySelector('input[name="fullName"]')
              const submitButton = container.querySelector('button[type="submit"]')
              
              await user.type(usernameInput, 'testuser')
              await user.type(emailInput, 'test@example.com')
              await user.type(passwordInput, 'password123')
              await user.type(fullNameInput, 'Test User')
              await user.click(submitButton)

              // Verify friendly error message is displayed
              await waitFor(() => {
                const errorText = container.textContent
                expect(errorText).toMatch(/unexpected error/i)
              }, { timeout: 2000 })
              
              unmount()
            }
            
            // Final cleanup
            document.body.innerHTML = ''
          }
        ),
        { numRuns: 10, timeout: 20000 }
      )
    }, 25000)
  })
})
