# Design Document - User Authentication System

## Overview

The User Authentication System provides secure registration, login, and session management for the RentKar platform using JWT-based stateless authentication. The system consists of backend components (Spring Boot with Spring Security) and frontend components (React with Context API) that work together to authenticate users and protect resources.

The backend implements a layered architecture with User entity, UserRepository for data access, AuthService for business logic, and AuthController for REST endpoints. JWT tokens are generated upon successful login and validated on every request to protected resources. Passwords are securely hashed using BCrypt before storage.

The frontend provides registration and login forms with validation, an AuthContext for global state management, and a ProtectedRoute component for securing pages. Tokens are stored in browser local storage and automatically included in API requests via Axios interceptors.

## Architecture

### Backend Architecture

**Layered Structure:**
```
AuthController (REST API Layer)
    ↓
AuthService (Business Logic Layer)
    ↓
UserRepository (Data Access Layer)
    ↓
MySQL Database
```

**Spring Security Filter Chain:**
```
HTTP Request
    ↓
JwtAuthenticationFilter (validates token)
    ↓
Spring Security Context (sets authentication)
    ↓
Controller (processes request)
```

**Key Components:**
- **User Entity**: JPA entity representing user data with BCrypt-hashed passwords
- **UserRepository**: Spring Data JPA repository for database operations
- **AuthService**: Business logic for registration, login, and user management
- **AuthController**: REST endpoints for authentication operations
- **JwtUtil**: Utility class for generating and validating JWT tokens
- **JwtAuthenticationFilter**: Filter that intercepts requests and validates JWT tokens
- **SecurityConfig**: Spring Security configuration defining protected routes and authentication rules

### Frontend Architecture

**Component Hierarchy:**
```
App
    ↓
AuthProvider (wraps entire app)
    ↓
Router
    ↓
ProtectedRoute (wraps protected pages)
    ↓
Protected Components
```

**Key Components:**
- **AuthContext**: React Context providing authentication state and functions
- **AuthProvider**: Context provider managing auth state and token storage
- **LoginPage**: Form for user login with validation
- **RegisterPage**: Form for user registration with validation
- **ProtectedRoute**: Component that redirects unauthenticated users
- **authService**: API client for authentication endpoints
- **axiosInstance**: Configured Axios instance with token interceptor

## Components and Interfaces

### Backend Components

#### User Entity
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password; // BCrypt hashed
    
    private String fullName;
    private String phone;
    
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### UserRepository Interface
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

#### AuthService Interface
```java
public interface AuthService {
    UserDTO register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    UserDTO getCurrentUser(String username);
}
```

#### AuthController Endpoints
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    POST /register - Register new user
    POST /login - Login and receive JWT token
    GET /me - Get current authenticated user
}
```

#### JwtUtil Interface
```java
public class JwtUtil {
    String generateToken(UserDetails userDetails);
    String extractUsername(String token);
    boolean validateToken(String token, UserDetails userDetails);
    boolean isTokenExpired(String token);
}
```

### Frontend Components

#### AuthContext Interface
```typescript
interface AuthContextType {
    user: User | null;
    token: string | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: (credentials: LoginCredentials) => Promise<void>;
    register: (userData: RegisterData) => Promise<void>;
    logout: () => void;
}
```

#### API Service Interface
```typescript
interface AuthService {
    register(data: RegisterData): Promise<ApiResponse<User>>;
    login(credentials: LoginCredentials): Promise<ApiResponse<LoginResponse>>;
    getCurrentUser(): Promise<ApiResponse<User>>;
}
```

## Data Models

### User Model
```typescript
interface User {
    id: number;
    username: string;
    email: string;
    fullName: string;
    phone?: string;
    role: 'USER' | 'ADMIN';
    createdAt: string;
}
```

### Registration Request
```typescript
interface RegisterRequest {
    username: string;
    email: string;
    password: string;
    fullName: string;
    phone?: string;
}
```

### Login Request
```typescript
interface LoginRequest {
    username: string;
    password: string;
}
```

### Login Response
```typescript
interface LoginResponse {
    token: string;
    type: 'Bearer';
    user: User;
}
```

### JWT Token Payload
```typescript
interface JwtPayload {
    sub: string; // username
    userId: number;
    email: string;
    role: string;
    iat: number; // issued at
    exp: number; // expiration
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*


### Property 1: Valid registration creates user with hashed password
*For any* valid registration data (valid email format, password ≥ 8 characters, unique email), submitting the registration should create a User entity in the database with a BCrypt-hashed password.
**Validates: Requirements 1.1, 6.1**

### Property 2: Duplicate email registration is rejected
*For any* email that already exists in the system, attempting to register with that email should be rejected with an appropriate error message, regardless of other field values.
**Validates: Requirements 1.2**

### Property 3: Invalid email formats are rejected
*For any* string that does not match valid email format patterns, registration should be rejected with a validation error.
**Validates: Requirements 1.3**

### Property 4: Short passwords are rejected
*For any* password with length less than 8 characters, registration should be rejected with a password strength error.
**Validates: Requirements 1.4**

### Property 5: User responses exclude passwords
*For any* successful registration or user data retrieval, the response payload should not contain password fields.
**Validates: Requirements 1.5, 6.4**

### Property 6: Valid credentials generate JWT token
*For any* registered user with correct username and password, login should succeed and return a valid JWT token containing user information.
**Validates: Requirements 2.1**

### Property 7: Incorrect passwords are rejected
*For any* registered user, providing an incorrect password should result in authentication failure with an appropriate error.
**Validates: Requirements 2.2**

### Property 8: Non-existent users are rejected
*For any* username or email that does not exist in the database, login attempts should be rejected with an authentication error.
**Validates: Requirements 2.3**

### Property 9: JWT tokens contain required claims
*For any* successful login, the generated JWT token should contain user ID, email, and name in the payload when decoded.
**Validates: Requirements 2.4**

### Property 10: JWT tokens expire after 24 hours
*For any* generated JWT token, the expiration claim should be set to exactly 24 hours from the issued-at time.
**Validates: Requirements 2.5**

### Property 11: Successful login stores token in localStorage
*For any* successful login in the frontend, the JWT token should be stored in browser localStorage.
**Validates: Requirements 3.1**

### Property 12: Valid tokens restore session on refresh
*For any* unexpired token in localStorage, refreshing the page should restore the authenticated session without requiring re-login.
**Validates: Requirements 3.2**

### Property 13: Expired tokens clear session
*For any* expired token, the system should clear authentication state and redirect to the login page.
**Validates: Requirements 3.3**

### Property 14: Logout clears all auth state
*For any* authenticated user, calling logout should remove the token from localStorage and clear all session state.
**Validates: Requirements 3.4**

### Property 15: Valid tokens grant access to protected resources
*For any* protected API endpoint and valid JWT token, the request should be authorized and processed successfully.
**Validates: Requirements 4.1**

### Property 16: Missing tokens are rejected
*For any* protected API endpoint, requests without an Authorization header should be rejected with 401 Unauthorized.
**Validates: Requirements 4.2**

### Property 17: Expired tokens are rejected
*For any* protected API endpoint, requests with expired tokens should be rejected with 401 Unauthorized.
**Validates: Requirements 4.3**

### Property 18: Malformed tokens are rejected
*For any* protected API endpoint, requests with invalid or malformed tokens should be rejected with 401 Unauthorized.
**Validates: Requirements 4.4**

### Property 19: 401 responses trigger login redirect
*For any* API request that returns 401 Unauthorized, the frontend should redirect the user to the login page.
**Validates: Requirements 4.5**

### Property 20: Authentication errors display messages
*For any* authentication operation failure (registration, login), a specific error message should be displayed to the user.
**Validates: Requirements 5.2**

### Property 21: Successful login redirects appropriately
*For any* successful login, the user should be redirected to the home page or their intended destination.
**Validates: Requirements 5.3**

### Property 22: Successful registration redirects to login
*For any* successful registration, the user should see a success message and be redirected to the login page.
**Validates: Requirements 5.4**

### Property 23: Network errors display friendly messages
*For any* network error during authentication operations, a user-friendly error message should be displayed.
**Validates: Requirements 5.5**

### Property 24: BCrypt validates login passwords
*For any* login attempt, the system should use BCrypt comparison to validate the provided password against the stored hash.
**Validates: Requirements 6.3**

### Property 25: App initialization restores auth state
*For any* valid token in localStorage when the application initializes, the AuthContext should restore the authenticated state.
**Validates: Requirements 7.1**

### Property 26: Authenticated users access protected routes
*For any* ProtectedRoute component, authenticated users should be able to access and render the protected component.
**Validates: Requirements 7.3**

### Property 27: Unauthenticated users are redirected from protected routes
*For any* ProtectedRoute component, unauthenticated users should be redirected to the login page.
**Validates: Requirements 7.4**

### Property 28: Requests with tokens are validated
*For any* request containing a JWT token in the Authorization header, the system should extract and validate the token signature.
**Validates: Requirements 8.1, 8.2**

### Property 29: Token validation checks expiration
*For any* JWT token validation, the system should verify the token has not expired.
**Validates: Requirements 8.3**

### Property 30: Valid tokens provide user context
*For any* valid JWT token, the system should extract user information and make it available to the request handler.
**Validates: Requirements 8.4**

### Property 31: Invalid tokens prevent execution
*For any* request with an invalid token, the system should reject the request before executing any business logic.
**Validates: Requirements 8.5**

## Error Handling

### Backend Error Handling

**Validation Errors:**
- Email format validation using regex pattern
- Password length validation (minimum 8 characters)
- Required field validation using Jakarta Validation annotations
- Return 400 Bad Request with detailed error messages

**Authentication Errors:**
- Invalid credentials return 401 Unauthorized
- Expired tokens return 401 Unauthorized
- Malformed tokens return 401 Unauthorized
- Generic error messages to prevent user enumeration ("Invalid credentials" instead of "User not found")

**Duplicate Registration:**
- Check for existing username/email before creating user
- Return 400 Bad Request with specific message ("Email already exists")

**Token Errors:**
- JWT parsing exceptions caught and converted to 401 responses
- Signature validation failures return 401
- Expired token exceptions return 401

**Database Errors:**
- Catch DataAccessException and return 500 Internal Server Error
- Log detailed error information for debugging
- Return generic error message to client

### Frontend Error Handling

**API Error Handling:**
- Axios interceptor catches all API errors
- 401 errors trigger automatic logout and redirect to login
- Network errors display "Connection failed" message
- Validation errors display field-specific messages

**Form Validation:**
- Client-side validation before API calls
- Email format validation using regex
- Password length validation
- Required field validation
- Display inline error messages below form fields

**Loading States:**
- Display loading spinner during API calls
- Disable form submission while loading
- Prevent duplicate submissions

**Token Expiration:**
- Axios interceptor detects 401 responses
- Automatically clear auth state
- Redirect to login page
- Display "Session expired" message

## Testing Strategy

### Unit Testing

**Backend Unit Tests:**
- Test AuthService registration logic with valid/invalid inputs
- Test AuthService login logic with correct/incorrect credentials
- Test JwtUtil token generation and validation
- Test password hashing with BCrypt
- Test UserRepository query methods
- Mock dependencies to isolate unit behavior

**Frontend Unit Tests:**
- Test AuthContext state management
- Test form validation logic
- Test API service functions
- Test ProtectedRoute rendering logic
- Mock API calls and localStorage

### Property-Based Testing

The system will use property-based testing to verify correctness properties across a wide range of inputs. For Java backend testing, we will use **jqwik** (a property-based testing library for JUnit 5). For JavaScript/TypeScript frontend testing, we will use **fast-check**.

**Property Test Configuration:**
- Each property test should run a minimum of 100 iterations
- Use smart generators that constrain inputs to valid ranges
- Each property test must include a comment tag referencing the design document property
- Tag format: `// Feature: user-authentication, Property X: <property description>`

**Backend Property Tests (using jqwik):**
- Property 1: Generate random valid registration data, verify user creation with hashed password
- Property 2: Generate random existing emails, verify registration rejection
- Property 3: Generate random invalid email formats, verify rejection
- Property 4: Generate random passwords < 8 chars, verify rejection
- Property 5: Generate random user data, verify password exclusion from responses
- Property 6: Generate random valid credentials, verify JWT generation
- Property 7: Generate random incorrect passwords, verify rejection
- Property 8: Generate random non-existent usernames, verify rejection
- Property 9: Generate random users, verify JWT payload contains required claims
- Property 10: Generate random tokens, verify 24-hour expiration
- Property 15: Generate random valid tokens, verify access granted
- Property 16: Generate requests without tokens, verify 401 response
- Property 17: Generate expired tokens, verify 401 response
- Property 18: Generate malformed tokens, verify 401 response
- Property 24: Generate random passwords, verify BCrypt comparison
- Property 28: Generate random valid tokens, verify signature validation
- Property 29: Generate random tokens, verify expiration checking
- Property 30: Generate random valid tokens, verify user context extraction
- Property 31: Generate random invalid tokens, verify early rejection

**Frontend Property Tests (using fast-check):**
- Property 11: Generate random successful logins, verify token storage
- Property 12: Generate random valid tokens, verify session restoration
- Property 13: Generate random expired tokens, verify session clearing
- Property 14: Generate random authenticated states, verify logout clears state
- Property 19: Generate random 401 responses, verify redirect
- Property 20: Generate random auth errors, verify error display
- Property 21: Generate random successful logins, verify redirect
- Property 22: Generate random successful registrations, verify redirect
- Property 23: Generate random network errors, verify friendly messages
- Property 25: Generate random valid tokens at init, verify state restoration
- Property 26: Generate random authenticated states, verify route access
- Property 27: Generate random unauthenticated states, verify redirect

### Integration Testing

**Backend Integration Tests:**
- Test complete registration flow from controller to database
- Test complete login flow with JWT generation
- Test protected endpoint access with valid/invalid tokens
- Test Spring Security filter chain behavior
- Use TestRestTemplate for API testing
- Use H2 in-memory database for test isolation

**Frontend Integration Tests:**
- Test complete registration flow with form submission
- Test complete login flow with token storage
- Test protected route navigation
- Test logout and session clearing
- Mock backend API responses
- Test Axios interceptor behavior

### End-to-End Testing

- Test user registration → login → access protected page flow
- Test session persistence across page refreshes
- Test token expiration and re-login
- Test logout and re-authentication
- Test error scenarios (network failures, invalid inputs)

## Security Considerations

**Password Security:**
- BCrypt hashing with work factor 10 (configurable)
- Passwords never logged or exposed in responses
- Password validation enforces minimum length

**Token Security:**
- JWT signed with strong secret key (256-bit minimum)
- Tokens expire after 24 hours
- Tokens validated on every protected request
- Token secret stored in environment variables, never in code

**API Security:**
- CORS configured to allow only frontend origin
- Spring Security protects all endpoints by default
- Public endpoints explicitly whitelisted
- Rate limiting on authentication endpoints (future enhancement)

**Input Validation:**
- All inputs validated on backend
- Email format validation
- SQL injection prevented by JPA parameterized queries
- XSS prevention through input sanitization

**Error Messages:**
- Generic authentication errors to prevent user enumeration
- Detailed validation errors only for format issues
- Sensitive information never exposed in error messages

## Performance Considerations

**Token Validation:**
- JWT validation is stateless and fast
- No database lookup required for token validation
- User information embedded in token payload

**Password Hashing:**
- BCrypt work factor balanced for security and performance
- Hashing occurs only during registration and login
- Validation uses efficient BCrypt comparison

**Database Queries:**
- Indexed columns: username, email
- Efficient queries using Spring Data JPA
- Connection pooling with HikariCP

**Frontend Performance:**
- Token stored in localStorage for fast access
- Axios interceptor adds token to requests automatically
- AuthContext prevents unnecessary re-renders
- Protected routes lazy-loaded

## Deployment Considerations

**Environment Variables:**
- `JWT_SECRET`: Secret key for signing tokens (required)
- `JWT_EXPIRATION`: Token expiration time in milliseconds (default: 86400000 = 24 hours)
- `BCRYPT_STRENGTH`: BCrypt work factor (default: 10)
- `CORS_ALLOWED_ORIGINS`: Frontend URL for CORS (required)

**Database Setup:**
- Users table must be created before deployment
- Indexes on username and email columns
- Consider unique constraints at database level

**Frontend Configuration:**
- API base URL configured via environment variable
- Token storage strategy (localStorage vs sessionStorage)
- Redirect URLs for login/logout

**Security Checklist:**
- JWT secret is strong and unique per environment
- CORS properly configured for production domain
- HTTPS enforced in production
- Sensitive data not logged
- Error messages don't leak information
