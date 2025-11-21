# Implementation Plan - User Authentication System

- [ ] 1. Set up backend authentication infrastructure










  - [x] 1.1 Create User entity with JPA annotations

    - Define User class with id, username, email, password, fullName, phone, role fields
    - Add JPA annotations (@Entity, @Table, @Column with constraints)
    - Add timestamps (createdAt, updatedAt) with automatic management
    - Create Role enum (USER, ADMIN)


    - _Requirements: 1.1, 6.2_
  
  - [x] 1.2 Create UserRepository interface

    - Extend JpaRepository<User, Long>

    - Add findByUsername and findByEmail query methods
    - Add existsByUsername and existsByEmail methods
    - _Requirements: 1.1, 1.2_
  
  - [x] 1.3 Add Spring Security and JWT dependencies to pom.xml

    - Add spring-boot-starter-security
    - Add jjwt-api, jjwt-impl, jjwt-jackson for JWT support
    - Add jqwik dependency for property-based testing
    - _Requirements: 2.1, 8.1_
  
  - [x] 1.4 Create JwtUtil class for token operations


    - Implement generateToken method with 24-hour expiration
    - Implement extractUsername method
    - Implement validateToken method with signature and expiration checks
    - Implement isTokenExpired method
    - Load JWT secret from application.properties
    - _Requirements: 2.1, 2.5, 8.1, 8.3_
  
  - [x] 1.5 Write property test for JWT token generation and validation


    - **Property 10: JWT tokens expire after 24 hours**
    - **Validates: Requirements 2.5**
    - Generate random user details, create token, verify expiration is 24 hours
  
  - [x] 1.6 Write property test for JWT token claims


    - **Property 9: JWT tokens contain required claims**
    - **Validates: Requirements 2.4**
    - Generate random users, create tokens, decode and verify userId, email, name present

- [x] 2. Implement authentication service layer

  - [x] 2.1 Create DTOs for authentication
    - Create RegisterRequest DTO with validation annotations
    - Create LoginRequest DTO
    - Create LoginResponse DTO with token and user fields
    - Create UserDTO for responses (excluding password)
    - _Requirements: 1.1, 1.3, 1.4, 2.1_
  
  - [x] 2.2 Create AuthService interface and implementation
    - Implement register method with email/password validation
    - Implement login method with credential verification
    - Implement getCurrentUser method
    - Use BCryptPasswordEncoder for password hashing (strength 10)
    - Check for duplicate email/username before registration
    - _Requirements: 1.1, 1.2, 2.1, 2.2, 6.1, 6.3_

  - [x] 2.3 Write property test for user registration
    - **Property 1: Valid registration creates user with hashed password**
    - **Validates: Requirements 1.1, 6.1**
    - Generate random valid registration data, verify user created with BCrypt hash
  
  - [x] 2.4 Write property test for duplicate email rejection
    - **Property 2: Duplicate email registration is rejected**
    - **Validates: Requirements 1.2**
    - Create user, attempt registration with same email, verify rejection
  
  - [x] 2.5 Write property test for email validation
    - **Property 3: Invalid email formats are rejected**
    - **Validates: Requirements 1.3**
    - Generate invalid email formats, verify all rejected
  
  - [x] 2.6 Write property test for password validation
    - **Property 4: Short passwords are rejected**
    - **Validates: Requirements 1.4**
    - Generate passwords < 8 characters, verify all rejected
  
  - [x] 2.7 Write property test for password exclusion from responses
    - **Property 5: User responses exclude passwords**
    - **Validates: Requirements 1.5, 6.4**
    - Generate random users, verify response DTOs never contain password field
  
  - [x] 2.8 Write property test for valid login
    - **Property 6: Valid credentials generate JWT token**
    - **Validates: Requirements 2.1**
    - Register random users, login with correct credentials, verify JWT returned
  
  - [x] 2.9 Write property test for incorrect password rejection
    - **Property 7: Incorrect passwords are rejected**
    - **Validates: Requirements 2.2**
    - Register user, attempt login with wrong password, verify rejection
  
  - [x] 2.10 Write property test for non-existent user rejection
    - **Property 8: Non-existent users are rejected**
    - **Validates: Requirements 2.3**
    - Generate random non-existent usernames, verify login rejection

- [x] 3. Create authentication REST controller




  - [x] 3.1 Create AuthController with endpoints


    - Implement POST /api/auth/register endpoint
    - Implement POST /api/auth/login endpoint
    - Implement GET /api/auth/me endpoint (protected)
    - Add proper error handling and validation
    - Return standardized API responses
    - _Requirements: 1.1, 2.1, 5.2_


  
  - [x] 3.2 Write integration tests for AuthController





    - Test registration endpoint with valid/invalid data
    - Test login endpoint with correct/incorrect credentials
    - Test /me endpoint with valid/invalid tokens
    - Verify proper HTTP status codes and response formats
    - _Requirements: 1.1, 1.2, 2.1, 2.2_

- [ ] 4. Configure Spring Security with JWT filter




  - [x] 4.1 Create JwtAuthenticationFilter


    - Extract JWT from Authorization header
    - Validate token using JwtUtil
    - Set authentication in SecurityContext if valid
    - Handle token validation errors
    - _Requirements: 4.1, 8.1, 8.3, 8.4_
  
  - [x] 4.2 Create SecurityConfig class


    - Configure HTTP security with JWT filter
    - Whitelist public endpoints (/api/auth/register, /api/auth/login)
    - Protect all other endpoints
    - Disable CSRF for stateless API
    - Configure CORS
    - Configure BCryptPasswordEncoder bean
    - _Requirements: 4.1, 4.2, 6.1_
  
  - [x] 4.3 Write property test for protected resource access


    - **Property 15: Valid tokens grant access to protected resources**
    - **Validates: Requirements 4.1**
    - Generate valid tokens, verify access to protected endpoints
  
  - [x] 4.4 Write property test for missing token rejection

    - **Property 16: Missing tokens are rejected**
    - **Validates: Requirements 4.2**
    - Attempt access to protected endpoints without token, verify 401
  
  - [x] 4.5 Write property test for expired token rejection

    - **Property 17: Expired tokens are rejected**
    - **Validates: Requirements 4.3**
    - Generate expired tokens, verify 401 response
  
  - [x] 4.6 Write property test for malformed token rejection

    - **Property 18: Malformed tokens are rejected**
    - **Validates: Requirements 4.4**
    - Generate invalid/malformed tokens, verify 401 response
  
  - [x] 4.7 Write property test for token validation

    - **Property 28: Requests with tokens are validated**
    - **Validates: Requirements 8.1, 8.2**
    - Generate tokens with various signatures, verify validation
  
  - [x] 4.8 Write property test for user context extraction

    - **Property 30: Valid tokens provide user context**
    - **Validates: Requirements 8.4**
    - Generate valid tokens, verify user info available in request context
  
  - [x] 4.9 Write property test for early rejection of invalid tokens

    - **Property 31: Invalid tokens prevent execution**
    - **Validates: Requirements 8.5**
    - Generate invalid tokens, verify business logic never executes

- [x] 5. Checkpoint - Ensure all backend tests pass











  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Set up frontend authentication infrastructure





  - [x] 6.1 Install required npm packages


    - Install axios for HTTP requests
    - Install react-router-dom for routing
    - Install fast-check for property-based testing
    - _Requirements: 2.1, 7.1_
  

  - [x] 6.2 Create axios instance with interceptors

    - Configure base URL from environment variable
    - Add request interceptor to attach JWT token from localStorage
    - Add response interceptor to handle 401 errors
    - Implement automatic logout and redirect on 401
    - _Requirements: 4.5, 5.5_
  

  - [x] 6.3 Create authService API client

    - Implement register function
    - Implement login function
    - Implement getCurrentUser function
    - Handle API errors and return formatted responses
    - _Requirements: 1.1, 2.1_

- [x] 7. Implement AuthContext for state management




  - [x] 7.1 Create AuthContext and AuthProvider


    - Define AuthContext with user, token, isAuthenticated, isLoading state
    - Implement login function that calls API and stores token
    - Implement register function that calls API
    - Implement logout function that clears token and state
    - Check localStorage for token on initialization
    - Restore auth state if valid token exists
    - _Requirements: 3.1, 3.2, 3.4, 7.1_
  
  - [x] 7.2 Write property test for token storage on login


    - **Property 11: Successful login stores token in localStorage**
    - **Validates: Requirements 3.1**
    - Simulate successful logins, verify token in localStorage
  
  - [x] 7.3 Write property test for session restoration

    - **Property 12: Valid tokens restore session on refresh**
    - **Validates: Requirements 3.2**
    - Place valid tokens in localStorage, initialize context, verify auth state restored
  
  - [x] 7.4 Write property test for expired token handling

    - **Property 13: Expired tokens clear session**
    - **Validates: Requirements 3.3**
    - Place expired tokens in localStorage, verify session cleared and redirect
  
  - [x] 7.5 Write property test for logout

    - **Property 14: Logout clears all auth state**
    - **Validates: Requirements 3.4**
    - Set authenticated state, call logout, verify token removed and state cleared
  
  - [x] 7.6 Write property test for app initialization

    - **Property 25: App initialization restores auth state**
    - **Validates: Requirements 7.1**
    - Generate valid tokens, place in storage, initialize app, verify state restored

- [X] 8. Create authentication UI components











  - [x] 8.1 Create RegisterPage component



    - Build registration form with username, email, password, fullName, phone fields
    - Add client-side validation (email format, password length)
    - Display loading state during registration
    - Display error messages from API
    - Redirect to login on success with success message
    - _Requirements: 1.1, 1.3, 1.4, 5.1, 5.2, 5.4_
  

  - [x] 8.2 Create LoginPage component


    - Build login form with username and password fields
    - Add client-side validation
    - Display loading state during login
    - Display error messages from API
    - Redirect to home page on success
    - _Requirements: 2.1, 5.1, 5.2, 5.3_
  - [-] 8.3 Write property test for error message display






  - [x] 8.3 Write property test for error message display (duplicate)

    - **Property 20: Authentication errors display messages**
    - **Validates: Requirements 5.2**
    - Generate various auth errors, verify error messages displayed
  
  - [x] 8.4 Write property test for login redirect

    - **Property 21: Successful login redirects appropriately**
    - **Validates: Requirements 5.3**
    - Simulate successful logins, verify redirect to home or intended destination
  
  - [x] 8.5 Write property test for registration redirect

    - **Property 22: Successful registration redirects to login**
    - **Validates: Requirements 5.4**
    - Simulate successful registrations, verify redirect to login page
  
  - [x] 8.6 Write property test for network error handling

    - **Property 23: Network errors display friendly messages**
    - **Validates: Requirements 5.5**
    - Simulate network errors, verify user-friendly messages displayed

- [ ] 9. Implement protected route functionality










  - [x] 9.1 Create ProtectedRoute component


    - Check authentication state from AuthContext
    - Render children if authenticated
    - Redirect to login if not authenticated
    - Preserve intended destination for post-login redirect
    - _Requirements: 4.2, 7.3, 7.4_
  
  - [x] 9.2 Write property test for authenticated route access


    - **Property 26: Authenticated users access protected routes**
    - **Validates: Requirements 7.3**
    - Generate authenticated states, verify protected components render
  
  - [x] 9.3 Write property test for unauthenticated route redirect

    - **Property 27: Unauthenticated users are redirected from protected routes**
    - **Validates: Requirements 7.4**
    - Generate unauthenticated states, verify redirect to login
  
  - [x] 9.4 Write property test for 401 redirect

    - **Property 19: 401 responses trigger login redirect**
    - **Validates: Requirements 4.5**
    - Simulate 401 responses, verify redirect to login page

- [x] 10. Integrate authentication into application








  - [x] 10.1 Wrap App with AuthProvider



    - Update main.jsx to wrap App with AuthProvider
    - Ensure AuthContext is available throughout app
    - _Requirements: 7.1_
  

  - [x] 10.2 Set up routing with protected routes


    - Configure React Router with public and protected routes
    - Use ProtectedRoute for pages requiring authentication
    - Add login and register routes
    - _Requirements: 4.2, 7.3, 7.4_

  
  - [x] 10.3 Create basic navigation component


    - Display login/register links when not authenticated
    - Display user info and logout button when authenticated
    - _Requirements: 3.4_

- [x] 11. Configure environment variables and application properties





  - [x] 11.1 Configure backend application.properties


    - Set JWT secret key (use strong random value)
    - Set JWT expiration time (86400000 ms = 24 hours)
    - Configure CORS allowed origins
    - Configure BCrypt strength (10)
    - _Requirements: 2.5, 6.1_
  

  - [x] 11.2 Configure frontend environment variables

    - Set VITE_API_BASE_URL to backend URL
    - Create .env.example file with template
    - _Requirements: 2.1_

- [x] 12. Final checkpoint - Ensure all tests pass



  - Ensure all tests pass, ask the user if questions arise.
