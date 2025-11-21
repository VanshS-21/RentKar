# Implementation Plan - AI Description Generation

- [x] 1. Set up Gemini AI integration infrastructure






  - [x] 1.1 Add Gemini AI dependencies to pom.xml

    - Add Google Generative AI Java SDK dependency
    - Configure Maven for external API integration
    - _Requirements: 6.1_
  
  - [x] 1.2 Configure environment variables


    - Add GEMINI_API_KEY to application.properties
    - Add GEMINI_API_ENDPOINT configuration
    - Add GEMINI_MODEL configuration (default: gemini-pro)
    - Add AI_GENERATION_ENABLED feature flag
    - Add AI_RATE_LIMIT_PER_HOUR configuration
    - Add AI_REQUEST_TIMEOUT_MS configuration
    - Add AI_TEMPERATURE configuration
    - Add AI_MAX_TOKENS_TITLE and AI_MAX_TOKENS_DESCRIPTION
    - _Requirements: 6.1, 6.2_
  
  - [x] 1.3 Create AIGenerationRequest DTO


    - Add itemName field with validation
    - Add category field with validation
    - Add optional additionalInfo field
    - Add optional condition field
    - Add optional specifications field
    - _Requirements: 1.1, 2.1_
  
  - [x] 1.4 Create AIGenerationResponse DTO


    - Add content field for generated text
    - Add tokenCount field for usage tracking
    - Add responseTimeMs field for performance monitoring
    - Add success boolean field
    - Add errorMessage field
    - _Requirements: 11.2_

- [x] 2. Implement core AI service








  - [x] 2.1 Create AIService interface and implementation


    - Implement generateTitle method
    - Implement generateDescription method
    - Implement isAvailable method for feature flag checking
    - Add Gemini API client initialization
    - Add error handling for API failures
    - Add timeout configuration (30 seconds)
    - _Requirements: 1.1, 2.1, 5.1, 5.2, 6.1_
  
  - [x] 2.2 Create PromptBuilder utility class


    - Implement buildTitlePrompt method
    - Implement buildDescriptionPrompt method
    - Implement getCategoryInstructions method
    - Add prompt templates for titles and descriptions
    - Add category-specific instruction templates
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 9.1, 9.2, 9.3, 9.4_
  
  - [x] 2.3 Write property test for title length constraints


    - **Property 1: Title length constraints**
    - **Validates: Requirements 1.1**
    - Generate random item data, verify title length 3-200 characters
  
  - [x] 2.4 Write property test for category terminology

    - **Property 2: Category terminology inclusion**
    - **Validates: Requirements 1.2**
    - Generate random categories, verify category terms in titles
  
  - [x] 2.5 Write property test for context influence

    - **Property 3: Context influences output**
    - **Validates: Requirements 1.3**
    - Generate same item with different context, verify titles differ
  
  - [x] 2.6 Write property test for description length constraints

    - **Property 4: Description length constraints**
    - **Validates: Requirements 2.1, 2.4**
    - Generate random item data, verify description length 50-1000 characters
  
  - [x] 2.7 Write property test for additional details incorporation

    - **Property 5: Additional details incorporation**
    - **Validates: Requirements 2.2**
    - Generate requests with additional details, verify incorporation
  
  - [x] 2.8 Write property test for description formatting

    - **Property 6: Description formatting**
    - **Validates: Requirements 2.3, 12.2**
    - Generate random requests, verify proper sentence structure

- [x] 3. Implement rate limiting





  - [x] 3.1 Create RateLimiter interface and implementation


    - Implement allowRequest method
    - Implement getRemainingRequests method
    - Implement getResetTime method
    - Use in-memory cache for rate limit tracking
    - Implement sliding window algorithm
    - Add cleanup for expired entries
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_
  
  - [x] 3.2 Integrate rate limiting with AIService


    - Check rate limit before API calls
    - Return 429 error when limit exceeded
    - Include Retry-After header in response
    - Track requests per user ID
    - Fallback to IP-based limiting for unauthenticated users
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_
  
  - [x] 3.3 Write property test for generation count tracking


    - **Property 11: Generation count tracking**
    - **Validates: Requirements 4.3**
    - Generate series of requests, verify count tracking
  
  - [x] 3.4 Write property test for rate limit enforcement

    - **Property 12: Rate limit enforcement**
    - **Validates: Requirements 4.4, 7.1, 7.2**
    - Generate 11 requests, verify 11th is blocked with 429
  
  - [x] 3.5 Write property test for rate limit reset

    - **Property 13: Rate limit reset**
    - **Validates: Requirements 4.5, 7.3**
    - Verify requests allowed after cooldown period
  
  - [x] 3.6 Write property test for per-user rate limiting

    - **Property 18: Per-user rate limiting**
    - **Validates: Requirements 7.4**
    - Verify different users have independent rate limits
  
  - [x] 3.7 Write property test for IP-based rate limiting

    - **Property 19: IP-based rate limiting for unauthenticated users**
    - **Validates: Requirements 7.5**
    - Verify IP-based limiting for unauthenticated users

- [x] 4. Create REST endpoints




  - [x] 4.1 Add AI generation endpoints to ItemController

    - Implement POST /api/items/generate-title endpoint
    - Implement POST /api/items/generate-description endpoint
    - Add authentication requirement
    - Add request validation
    - Add error handling with proper HTTP status codes
    - Return standardized API responses
    - _Requirements: 1.1, 2.1, 5.1, 5.2, 5.3, 5.4_
  
  - [x] 4.2 Implement error response handling

    - Handle Gemini API errors (401, 429, 400, 503)
    - Handle validation errors
    - Handle rate limit errors with Retry-After header
    - Handle timeout errors
    - Return user-friendly error messages
    - _Requirements: 5.1, 5.2, 5.3, 5.4_
  


  - [x] 4.3 Write integration tests for AI endpoints


    - Test generate-title endpoint with valid data
    - Test generate-description endpoint with valid data
    - Test error handling for various API responses
    - Test rate limiting enforcement
    - Test authentication requirement
    - Verify proper HTTP status codes and response formats
    - _Requirements: 1.1, 2.1, 5.1, 7.1_

- [x] 5. Implement logging and monitoring




  - [x] 5.1 Add logging to AIService


    - Log request details (user ID, timestamp, request type)
    - Log success metrics (response time, token count)
    - Log error details (error type, message)
    - Use structured logging format (JSON)
    - Exclude sensitive data (API keys, user data)
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_
  
  - [x] 5.2 Add usage threshold monitoring


    - Track API usage per user
    - Log warnings when thresholds exceeded
    - Add metrics for monitoring dashboard
    - _Requirements: 6.4_
  
  - [x] 5.3 Write property test for request logging


    - **Property 29: Request logging includes required fields**
    - **Validates: Requirements 11.1**
    - Verify log entries contain user ID, timestamp, request type
  
  - [x] 5.4 Write property test for success logging

    - **Property 30: Success logging includes metrics**
    - **Validates: Requirements 11.2**
    - Verify successful requests log response time and token count
  
  - [x] 5.5 Write property test for error logging

    - **Property 31: Error logging includes details**
    - **Validates: Requirements 11.3**
    - Verify failed requests log error type and message
  
  - [x] 5.6 Write property test for log security

    - **Property 32: Logs exclude sensitive data**
    - **Validates: Requirements 11.4**
    - Verify logs don't contain API keys or sensitive user data
  
  - [x] 5.7 Write property test for structured logging

    - **Property 33: Structured logging format**
    - **Validates: Requirements 11.5**
    - Verify logs follow structured format (JSON)

- [x] 6. Implement prompt engineering tests





  - [x] 6.1 Write property test for prompt format instructions


    - **Property 20: Prompt includes format instructions**
    - **Validates: Requirements 8.1**
    - Verify prompts contain format instructions
  
  - [x] 6.2 Write property test for title prompt conciseness


    - **Property 21: Title prompts emphasize conciseness**
    - **Validates: Requirements 8.2**
    - Verify title prompts instruct model to be concise
  
  - [x] 6.3 Write property test for description prompt benefits


    - **Property 22: Description prompts highlight benefits**
    - **Validates: Requirements 8.3**
    - Verify description prompts mention benefits for borrowers
  
  - [x] 6.4 Write property test for audience context


    - **Property 23: Prompts include audience context**
    - **Validates: Requirements 8.4**
    - Verify prompts mention college students
  
  - [x] 6.5 Write property test for tone specification



    - **Property 24: Prompts specify tone**
    - **Validates: Requirements 8.5**
    - Verify prompts include tone instructions
  


  - [x] 6.6 Write property test for Electronics category prompts


    - **Property 25: Electronics prompts mention specifications**
    - **Validates: Requirements 9.1**
    - Verify Electronics prompts emphasize technical specifications
  
  - [x] 6.7 Write property test for Books category prompts


    - **Property 26: Books prompts mention subject matter**
    - **Validates: Requirements 9.2**
    - Verify Books prompts mention subject and edition
  
  - [x] 6.8 Write property test for Sports Equipment category prompts


    - **Property 27: Sports Equipment prompts mention usage**
    - **Validates: Requirements 9.3**
    - Verify Sports Equipment prompts highlight usage scenarios
  
  - [x] 6.9 Write property test for Tools category prompts


    - **Property 28: Tools prompts mention functionality**
    - **Validates: Requirements 9.4**
    - Verify Tools prompts describe functionality

- [x] 7. Checkpoint - Ensure all backend tests pass





  - Ensure all tests pass, ask the user if questions arise.

- [x] 8. Set up frontend AI integration





  - [x] 8.1 Install required npm packages


    - Verify axios is installed for API calls
    - Install any additional dependencies if needed
    - _Requirements: 1.1, 2.1_
  
  - [x] 8.2 Extend itemService with AI generation methods


    - Implement generateTitle function
    - Implement generateDescription function
    - Handle API errors and return formatted responses
    - Add timeout handling
    - _Requirements: 1.1, 2.1, 5.1, 5.2_
  
  - [x] 8.3 Create useAIGeneration custom hook


    - Implement generate function
    - Implement regenerate function
    - Add loading state management
    - Add error state management
    - Track remaining requests
    - Handle rate limit errors
    - _Requirements: 3.4, 3.5, 4.1, 4.2, 4.4, 4.5, 5.1_

- [x] 9. Create AI generation UI components






  - [x] 9.1 Create AIGenerationButton component


    - Add button with AI icon
    - Show loading spinner during generation
    - Display error messages
    - Show remaining requests count
    - Add tooltip explaining feature
    - Disable button when loading or rate limited
    - _Requirements: 3.4, 3.5, 4.4, 10.1, 10.2, 10.3, 10.5_
  
  - [x] 9.2 Integrate AI buttons into AddItemPage


    - Add "Generate with AI" button next to title field
    - Add "Generate with AI" button next to description field
    - Populate form fields with generated content
    - Allow editing of generated content
    - Preserve user edits
    - Show loading state during generation
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 10.1_
  
  - [x] 9.3 Integrate AI buttons into EditItemPage


    - Add "Generate with AI" button next to title field
    - Add "Generate with AI" button next to description field
    - Pre-fill generation request with existing item data
    - Preserve user edits
    - _Requirements: 3.1, 3.2, 3.3, 10.1_
  
  - [x] 9.4 Add regenerate functionality

    - Add "Regenerate" button after initial generation
    - Track regeneration count
    - Show rate limit message when limit reached
    - Display countdown timer for rate limit reset
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_
  
  - [x] 9.5 Write property test for form field population


    - **Property 7: Form field population**
    - **Validates: Requirements 3.1**
    - Verify successful generation populates form fields
  

  - [x] 9.6 Write property test for user edits preservation

    - **Property 8: User edits preservation**
    - **Validates: Requirements 3.3**
    - Verify edited content is preserved
  


  - [x] 9.7 Write property test for regeneration API calls
    - **Property 9: Regeneration triggers new API call**
    - **Validates: Requirements 4.1**
    - Verify regeneration makes new API call
  
  - [x] 9.8 Write property test for regeneration variation
    - **Property 10: Regeneration produces different results**
    - **Validates: Requirements 4.2**
    - Verify consecutive regenerations produce different outputs

- [x] 10. Implement error handling UI





  - [x] 10.1 Add error message display


    - Show user-friendly error messages for API failures
    - Display timeout messages with retry suggestion
    - Show rate limit messages with reset time
    - Display generic error for configuration issues
    - _Requirements: 5.1, 5.2, 5.3, 5.4_
  
  - [x] 10.2 Implement form content preservation on error


    - Preserve user-entered content when errors occur
    - Don't clear form fields on API failures
    - Maintain form state across regeneration attempts
    - _Requirements: 5.5_
  
  - [x] 10.3 Write property test for error message display


    - **Property 14: Error message display**
    - **Validates: Requirements 5.1**
    - Verify user-friendly error messages are displayed
  
  - [x] 10.4 Write property test for form preservation on error


    - **Property 15: Form content preservation on error**
    - **Validates: Requirements 5.5**
    - Verify form content is preserved when errors occur

- [x] 11. Add graceful degradation




  - [x] 11.1 Implement feature availability checking


    - Check if AI generation is enabled via backend
    - Hide AI buttons when feature is unavailable
    - Show manual entry instructions when AI is disabled
    - _Requirements: 6.3, 10.4_
  
  - [x] 11.2 Add fallback UI for disabled AI


    - Display message explaining AI is unavailable
    - Ensure item creation works without AI
    - Provide helpful tips for manual entry
    - _Requirements: 6.3_
  
  - [x] 11.3 Write property test for configuration loading


    - **Property 16: Configuration loading**
    - **Validates: Requirements 6.1**
    - Verify credentials loaded from environment variables
  
  - [x] 11.4 Write property test for configuration parameters

    - **Property 17: Configuration parameters applied**
    - **Validates: Requirements 6.2**
    - Verify configured temperature and max tokens in API requests

- [x] 12. Add visual polish and UX improvements






  - [x] 12.1 Style AI generation buttons

    - Add AI icon (sparkle or robot icon)
    - Use distinct color scheme for AI features
    - Add hover effects and transitions
    - Ensure accessibility (ARIA labels, keyboard navigation)
    - _Requirements: 10.1, 10.2, 10.3_
  

  - [x] 12.2 Add loading animations

    - Show spinner during generation
    - Add progress indicator for longer requests
    - Display "Generating..." text
    - _Requirements: 3.4_
  

  - [x] 12.3 Add success feedback

    - Show brief success message after generation
    - Highlight generated content briefly
    - Add smooth transition when content appears
    - _Requirements: 3.5_
  

  - [x] 12.4 Add rate limit UI feedback


    - Display remaining requests count
    - Show countdown timer when rate limited
    - Use color coding (green → yellow → red)
    - Add tooltip explaining rate limits
    - _Requirements: 4.4, 4.5, 7.2_

- [x] 13. Testing and validation





  - [x] 13.1 Write end-to-end test for title generation


    - Test complete flow: click button → loading → title displayed
    - Verify title meets length constraints
    - Test regeneration functionality
    - _Requirements: 1.1, 4.1_
  
  - [x] 13.2 Write end-to-end test for description generation


    - Test complete flow: click button → loading → description displayed
    - Verify description meets length constraints
    - Test regeneration functionality
    - _Requirements: 2.1, 4.1_
  
  - [x] 13.3 Write end-to-end test for rate limiting


    - Generate 10 requests successfully
    - Verify 11th request is blocked
    - Verify rate limit resets after cooldown
    - _Requirements: 7.1, 7.2, 7.3_
  
  - [x] 13.4 Write end-to-end test for error handling


    - Test with invalid API key
    - Test with network timeout
    - Test with API unavailable
    - Verify error messages and form preservation
    - _Requirements: 5.1, 5.2, 5.4, 5.5_
  
  - [x] 13.5 Write end-to-end test for category-specific generation


    - Test generation for Electronics category
    - Test generation for Books category
    - Test generation for Sports Equipment category
    - Test generation for Tools category
    - Verify category-appropriate content
    - _Requirements: 9.1, 9.2, 9.3, 9.4_

- [x] 14. Documentation and configuration






  - [x] 14.1 Update environment configuration files

    - Add Gemini API key to application-example.properties
    - Add all AI configuration parameters with defaults
    - Document required environment variables
    - _Requirements: 6.1, 6.2_
  
  - [x] 14.2 Update API documentation


    - Document /api/items/generate-title endpoint
    - Document /api/items/generate-description endpoint
    - Add request/response examples
    - Document error responses
    - Document rate limiting
    - _Requirements: 1.1, 2.1, 7.1, 7.2_
  
  - [x] 14.3 Create user guide for AI features


    - Explain how to use AI generation
    - Document rate limits
    - Provide tips for best results
    - Explain regeneration feature
    - _Requirements: 4.1, 7.1_
  
  - [x] 14.4 Update README with AI setup instructions


    - Document Gemini API key setup
    - Explain configuration options
    - Add troubleshooting section
    - _Requirements: 6.1_

- [x] 15. Final checkpoint - Ensure all tests pass





  - Ensure all tests pass, ask the user if questions arise.
