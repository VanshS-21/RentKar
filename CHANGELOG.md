# RentKar Changelog

All notable changes to this project will be documented in this file.

## [Week 6-7] - 2024-11-21 - Borrow Workflow Complete

### Added
- **Complete Borrow Request Lifecycle**
  - Request creation with borrow/return dates and optional messages
  - Status tracking: PENDING → APPROVED → REJECTED / RETURNED → COMPLETED
  - Authorization checks for all workflow actions
  - Comprehensive validation (dates, item availability, ownership)

- **Backend Services**
  - `BorrowRequest` entity with JPA annotations and relationships
  - `BorrowRequestRepository` with custom queries for filtering
  - `BorrowRequestService` and `BorrowRequestServiceImpl` with all workflow methods
  - `BorrowRequestMapper` for entity-DTO conversion
  - Date validation (no past dates, return after borrow)
  - Item availability validation
  - Self-borrowing prevention

- **REST API Endpoints** (10 endpoints)
  - `POST /api/requests` - Create new borrow request
  - `GET /api/requests/sent` - Get sent requests (borrower view)
  - `GET /api/requests/received` - Get received requests (lender view)
  - `GET /api/requests/{id}` - Get request details
  - `POST /api/requests/{id}/approve` - Approve request (lender only)
  - `POST /api/requests/{id}/reject` - Reject request (lender only)
  - `POST /api/requests/{id}/return` - Mark as returned (lender only)
  - `POST /api/requests/{id}/confirm` - Confirm return (borrower only)
  - `DELETE /api/requests/{id}` - Cancel request (borrower only)
  - `GET /api/requests/statistics` - Get request statistics

- **Frontend Pages and Components**
  - `MyRequestsPage` - Borrower view with sent requests
  - `IncomingRequestsPage` - Lender view with received requests
  - `RequestCard` - Request summary card component
  - `RequestDetailModal` - Full request details with actions
  - `StatusBadge` - Color-coded status indicators
  - Request creation form on `ItemDetailPage`
  - Statistics dashboard with real-time counts
  - Status filtering for both views

- **User Experience Features**
  - Toast notifications for all status changes
  - Optimistic UI updates with error rollback
  - Loading states for all actions
  - Confirmation dialogs for destructive actions
  - Contact information sharing (only for approved requests)
  - Request history with timestamps
  - Empty states with helpful messages

- **Testing**
  - 32 correctness properties validated with property-based testing
  - Backend: All tests passing (using jqwik for PBT)
  - Frontend: All tests passing (using fast-check for PBT)
  - Integration tests for all endpoints
  - End-to-end tests for complete workflows (approve, reject, cancel)
  - Authorization tests for all actions

- **Documentation**
  - Complete Borrow Workflow User Guide (`BORROW_WORKFLOW_GUIDE.md`)
  - API documentation updated with all 10 borrow request endpoints
  - Authorization matrix showing who can perform each action
  - Status transition diagrams
  - FAQ section with 25+ common questions
  - Best practices for borrowers and lenders

- **UI Polish and Accessibility**
  - ARIA labels for all interactive elements
  - Keyboard navigation support (Enter/Space on cards)
  - Focus-visible styles with ring indicators
  - Role and aria-modal attributes for dialogs
  - Responsive design (mobile-first approach)
  - Smooth transitions and animations
  - Hover effects on cards and buttons
  - Reduced-motion support for accessibility
  - Lazy loading for images

### Features
- Complete peer-to-peer borrowing system
- Borrower and lender workflows
- Request approval/rejection with messages
- Return confirmation process
- Request cancellation
- Real-time statistics
- Status filtering
- Authorization and validation
- Notifications
- Accessibility improvements

### Technical Improvements
- Optimistic UI updates for better UX
- Error rollback on failed operations
- Comprehensive error handling
- Authorization matrix implementation
- Item status synchronization
- Contact information privacy controls

---

## [Week 5] - 2024-11-21 - AI Integration Complete

### Added
- **AI-Powered Content Generation**
  - Google Gemini API integration for automatic title and description generation
  - Category-specific prompt engineering for Electronics, Books, Sports Equipment, Tools, etc.
  - Smart prompts that emphasize relevant features based on item category
  - Temperature variation for regeneration to produce different results

- **Backend Services**
  - `AIService` and `AIServiceImpl` for Gemini API integration
  - `PromptBuilder` utility class for constructing category-specific prompts
  - `RateLimiter` and `RateLimiterImpl` with sliding window algorithm
  - Rate limiting: 10 requests per hour per user
  - Per-user and IP-based rate limiting support
  - Comprehensive error handling for API failures, timeouts, and rate limits

- **REST API Endpoints**
  - `POST /api/items/generate-title` - Generate AI-powered item titles
  - `POST /api/items/generate-description` - Generate AI-powered item descriptions
  - Both endpoints include rate limiting and authentication
  - Proper HTTP status codes (200, 408, 429, 503) with detailed error messages

- **Frontend Components**
  - `AIGenerationButton` component with loading states and error handling
  - `useAIGeneration` custom React hook for AI generation logic
  - Integration into `AddItemPage` and `EditItemPage`
  - Regeneration functionality with "Regenerate" button
  - Rate limit UI feedback with countdown timer
  - Remaining requests counter
  - User-friendly error messages for all error types

- **Testing**
  - 33 correctness properties validated with property-based testing
  - Backend: 97 tests passing (using jqwik for PBT)
  - Frontend: 34 tests passing (using fast-check for PBT)
  - Integration tests for AI endpoints
  - End-to-end tests for complete AI generation flow

- **Documentation**
  - Complete AI Generation User Guide (`AI_GENERATION_GUIDE.md`)
  - API documentation updated with AI endpoints
  - Setup instructions for Gemini API key configuration
  - Troubleshooting guide for common issues

### Features
- **Automatic Content Generation**: Users can generate titles (3-200 chars) and descriptions (50-1000 chars) with one click
- **Category Optimization**: AI tailors content based on item category (e.g., Electronics emphasizes specs, Books mention subject matter)
- **Regeneration**: Users can regenerate content multiple times for different suggestions
- **Rate Limiting**: Fair usage with 10 requests/hour per user, with clear UI feedback
- **Error Handling**: Comprehensive error handling with user-friendly messages
- **Graceful Degradation**: Application works normally when AI service is unavailable
- **Form Preservation**: User-entered content is preserved on errors
- **Loading States**: Clear visual feedback during AI generation
- **Structured Logging**: JSON-formatted logs with request tracking (excludes sensitive data)

### Configuration
- New environment variables in `application.properties`:
  - `gemini.api-key` - Google Gemini API key (required)
  - `gemini.api-endpoint` - API endpoint URL
  - `gemini.model` - Model name (default: gemini-pro)
  - `ai.generation.enabled` - Feature flag (default: true)
  - `ai.rate-limit.per-hour` - Requests per hour (default: 10)
  - `ai.request.timeout-ms` - Request timeout (default: 30000)
  - `ai.temperature` - Generation temperature (default: 0.7)
  - `ai.max-tokens.title` - Max tokens for titles (default: 200)
  - `ai.max-tokens.description` - Max tokens for descriptions (default: 500)

### Technical Details
- **Prompt Engineering**: Category-specific instructions for quality content
- **Rate Limiting Algorithm**: Sliding window with in-memory cache
- **Error Types Handled**: Timeout (408), Rate Limit (429), API Unavailable (503), Generic (500)
- **Security**: API keys stored in environment variables, never logged
- **Performance**: Typical response time 1-3 seconds, 30-second timeout
- **Testing Framework**: jqwik (Java) and fast-check (JavaScript) for property-based testing

### Project Status
- Overall Progress: 50% (Week 5 of 12 complete)
- Total Tests: 131 passing (97 backend + 34 frontend)
- All 33 AI correctness properties validated
- Production-ready: Authentication, Item Management, and AI Integration

---

## [Week 3-4] - 2024-11-15 - Item Management Complete

### Added
- Full CRUD operations for items with owner-based authorization
- Cloudinary integration for image storage (5MB limit, image validation)
- Advanced search with keyword matching (title and description)
- Multi-criteria filtering (category, status, search)
- Pagination for large result sets with metadata
- Image preview before upload
- "My Items" page for managing user's listings
- 39 correctness properties validated with property-based testing
- 84 tests passing (56 backend + 28 frontend)

---

## [Week 2] - 2024-11-08 - Authentication System Complete

### Added
- JWT-based stateless authentication with 24-hour token expiration
- Secure password hashing with BCrypt (strength 10)
- User registration and login endpoints
- Protected routes with automatic redirect
- Session persistence across page refreshes
- Comprehensive error handling
- Property-based testing for correctness validation
- 35 tests passing (27 backend + 8 frontend)

---

## [Week 1] - 2024-11-01 - Project Setup Complete

### Added
- Project planning and requirements documentation
- System architecture design
- API contract definition
- UI wireframes
- Development environment setup
- Spring Boot backend with MySQL database
- React frontend with Vite and TailwindCSS
- External services configuration (Cloudinary, Gemini AI)
- Git repository initialization

---

## Next Steps

### Week 6-7: Borrow Workflow
- Design and implement BorrowRequest entity
- Create borrow request endpoints (create, approve, reject, return)
- Build "My Requests" and "Incoming Requests" pages
- Implement request status tracking
- Add notifications for request updates

---

*This changelog follows [Keep a Changelog](https://keepachangelog.com/) format.*
