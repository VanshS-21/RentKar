# Design Document - AI Description Generation

## Overview

The AI Description Generation feature integrates Google's Gemini AI API to automatically generate item titles and descriptions for the RentKar platform. The system uses prompt engineering to create contextually relevant, category-specific content that helps users list items quickly and effectively. The feature includes rate limiting, error handling, and a user-friendly interface that allows users to generate, regenerate, and edit AI-suggested content.

The implementation consists of a backend AIService that interfaces with the Gemini API, REST endpoints for generation requests, and frontend components that integrate AI generation buttons into the item creation and editing forms.

## Architecture

### Backend Architecture

```
ItemController (REST API Layer)
    ↓
AIService (AI Integration Layer)
    ↓
Gemini AI API (External Service)
```

**Key Components:**
- **AIService**: Core service for interacting with Gemini API
- **PromptBuilder**: Utility for constructing effective prompts
- **RateLimiter**: Component for tracking and enforcing rate limits
- **AIController**: REST endpoints for AI generation (optional, can be part of ItemController)
- **Configuration**: Gemini API credentials and parameters

### Frontend Architecture

```
AddItemPage / EditItemPage
    ↓
AIGenerationButton Component
    ↓
itemService.generateContent()
    ↓
Backend API
```

**Key Components:**
- **AIGenerationButton**: Reusable button component with loading states
- **useAIGeneration**: Custom React hook for AI generation logic
- **itemService**: Extended with AI generation methods
- **Form Integration**: Seamless integration with existing item forms

## Components and Interfaces

### Backend Components

#### AIService Interface
```java
public interface AIService {
    /**
     * Generate an item title using AI
     * @param request Generation request with item details
     * @return Generated title
     */
    AIGenerationResponse generateTitle(AIGenerationRequest request);
    
    /**
     * Generate an item description using AI
     * @param request Generation request with item details
     * @return Generated description
     */
    AIGenerationResponse generateDescription(AIGenerationRequest request);
    
    /**
     * Check if AI generation is available
     * @return true if API is configured and available
     */
    boolean isAvailable();
}
```

#### AIGenerationRequest DTO
```java
public class AIGenerationRequest {
    @NotBlank(message = "Item name is required")
    private String itemName;
    
    @NotBlank(message = "Category is required")
    private String category;
    
    private String additionalInfo;
    private String condition;
    private String specifications;
    
    // Getters and setters
}
```

#### AIGenerationResponse DTO
```java
public class AIGenerationResponse {
    private String content;
    private int tokenCount;
    private long responseTimeMs;
    private boolean success;
    private String errorMessage;
    
    // Getters and setters
}
```

#### PromptBuilder Utility
```java
public class PromptBuilder {
    /**
     * Build a prompt for title generation
     */
    public static String buildTitlePrompt(AIGenerationRequest request);
    
    /**
     * Build a prompt for description generation
     */
    public static String buildDescriptionPrompt(AIGenerationRequest request);
    
    /**
     * Get category-specific instructions
     */
    private static String getCategoryInstructions(String category);
}
```

#### RateLimiter Component
```java
public interface RateLimiter {
    /**
     * Check if request is allowed
     * @param userId User ID or IP address
     * @return true if request is within rate limit
     */
    boolean allowRequest(String userId);
    
    /**
     * Get remaining requests for user
     * @param userId User ID or IP address
     * @return number of remaining requests
     */
    int getRemainingRequests(String userId);
    
    /**
     * Get time until rate limit resets
     * @param userId User ID or IP address
     * @return seconds until reset
     */
    long getResetTime(String userId);
}
```

### Frontend Components

#### AIGenerationButton Component
```typescript
interface AIGenerationButtonProps {
    type: 'title' | 'description';
    itemData: {
        itemName?: string;
        category?: string;
        additionalInfo?: string;
    };
    onGenerated: (content: string) => void;
    disabled?: boolean;
}

const AIGenerationButton: React.FC<AIGenerationButtonProps>
```

#### useAIGeneration Hook
```typescript
interface UseAIGenerationReturn {
    generate: () => Promise<void>;
    regenerate: () => Promise<void>;
    loading: boolean;
    error: string | null;
    remainingRequests: number;
}

const useAIGeneration: (type: 'title' | 'description') => UseAIGenerationReturn
```

## Data Models

### Gemini API Request Format
```json
{
  "contents": [{
    "parts": [{
      "text": "prompt text here"
    }]
  }],
  "generationConfig": {
    "temperature": 0.7,
    "maxOutputTokens": 200,
    "topP": 0.8,
    "topK": 40
  }
}
```

### Gemini API Response Format
```json
{
  "candidates": [{
    "content": {
      "parts": [{
        "text": "generated content here"
      }]
    },
    "finishReason": "STOP"
  }],
  "usageMetadata": {
    "promptTokenCount": 50,
    "candidatesTokenCount": 100,
    "totalTokenCount": 150
  }
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Title length constraints
*For any* valid generation request, the generated title should be between 3 and 200 characters in length.
**Validates: Requirements 1.1**

### Property 2: Category terminology inclusion
*For any* generation request with a category, the generated title should contain at least one term related to that category.
**Validates: Requirements 1.2**

### Property 3: Context influences output
*For any* two generation requests with the same item name but different additional context, the generated titles should differ.
**Validates: Requirements 1.3**

### Property 4: Description length constraints
*For any* valid generation request, the generated description should be between 50 and 1000 characters in length.
**Validates: Requirements 2.1, 2.4**

### Property 5: Additional details incorporation
*For any* generation request with additional details, the generated description should reference those details.
**Validates: Requirements 2.2**

### Property 6: Description formatting
*For any* generated description, it should contain proper sentence structure with capital letters and punctuation.
**Validates: Requirements 2.3, 12.2**

### Property 7: Form field population
*For any* successful AI generation, the form fields should be populated with the generated content.
**Validates: Requirements 3.1**

### Property 8: User edits preservation
*For any* AI-generated content that is edited by the user, the edited content should be preserved and not overwritten.
**Validates: Requirements 3.3**

### Property 9: Regeneration triggers new API call
*For any* regeneration request, a new API call should be made with the same input parameters.
**Validates: Requirements 4.1**

### Property 10: Regeneration produces different results
*For any* two consecutive regeneration requests with the same input, the outputs should differ due to temperature variation.
**Validates: Requirements 4.2**

### Property 11: Generation count tracking
*For any* series of generation requests by a user, the system should accurately track the count for rate limiting.
**Validates: Requirements 4.3**

### Property 12: Rate limit enforcement
*For any* user making more than 10 generation requests within an hour, the 11th request should be blocked with a 429 error.
**Validates: Requirements 4.4, 7.1, 7.2**

### Property 13: Rate limit reset
*For any* user who has reached the rate limit, after the cooldown period expires, new requests should be allowed.
**Validates: Requirements 4.5, 7.3**

### Property 14: Error message display
*For any* API error response, a user-friendly error message should be displayed to the user.
**Validates: Requirements 5.1**

### Property 15: Form content preservation on error
*For any* error during AI generation, existing user-entered content in form fields should be preserved.
**Validates: Requirements 5.5**

### Property 16: Configuration loading
*For any* system startup, Gemini API credentials should be loaded from environment variables.
**Validates: Requirements 6.1**

### Property 17: Configuration parameters applied
*For any* API call, the configured temperature and max token settings should be included in the request.
**Validates: Requirements 6.2**

### Property 18: Per-user rate limiting
*For any* two different authenticated users, their rate limits should be tracked independently.
**Validates: Requirements 7.4**

### Property 19: IP-based rate limiting for unauthenticated users
*For any* unauthenticated user, rate limiting should be based on IP address.
**Validates: Requirements 7.5**

### Property 20: Prompt includes format instructions
*For any* generation request, the constructed prompt should include clear instructions about the desired output format.
**Validates: Requirements 8.1**

### Property 21: Title prompts emphasize conciseness
*For any* title generation request, the prompt should instruct the model to be concise and attention-grabbing.
**Validates: Requirements 8.2**

### Property 22: Description prompts highlight benefits
*For any* description generation request, the prompt should instruct the model to highlight benefits for borrowers.
**Validates: Requirements 8.3**

### Property 23: Prompts include audience context
*For any* generation request, the prompt should include context about the target audience being college students.
**Validates: Requirements 8.4**

### Property 24: Prompts specify tone
*For any* generation request, the prompt should specify the tone as friendly and informative.
**Validates: Requirements 8.5**

### Property 25: Electronics prompts mention specifications
*For any* generation request with category "Electronics", the prompt should emphasize technical specifications and condition.
**Validates: Requirements 9.1**

### Property 26: Books prompts mention subject matter
*For any* generation request with category "Books", the prompt should mention subject matter and edition information.
**Validates: Requirements 9.2**

### Property 27: Sports Equipment prompts mention usage
*For any* generation request with category "Sports Equipment", the prompt should highlight usage scenarios and condition.
**Validates: Requirements 9.3**

### Property 28: Tools prompts mention functionality
*For any* generation request with category "Tools", the prompt should describe functionality and applications.
**Validates: Requirements 9.4**

### Property 29: Request logging includes required fields
*For any* AI generation request, the log entry should include user ID, timestamp, and request type.
**Validates: Requirements 11.1**

### Property 30: Success logging includes metrics
*For any* successful AI generation, the log entry should include response time and token count.
**Validates: Requirements 11.2**

### Property 31: Error logging includes details
*For any* failed AI generation, the log entry should include error type and message.
**Validates: Requirements 11.3**

### Property 32: Logs exclude sensitive data
*For any* log entry, it should not contain sensitive user data or API keys.
**Validates: Requirements 11.4**

### Property 33: Structured logging format
*For any* log entry, it should follow a structured format (e.g., JSON) for easy parsing.
**Validates: Requirements 11.5**

### Property 34: Usage threshold warnings
*For any* API usage that exceeds the configured threshold, a warning should be logged.
**Validates: Requirements 6.4**

## Error Handling

### Backend Error Handling

**Gemini API Errors:**
- Connection timeout (30 seconds)
- Invalid API key (401 Unauthorized)
- Rate limit exceeded (429 Too Many Requests)
- Invalid request format (400 Bad Request)
- Service unavailable (503 Service Unavailable)
- Return appropriate HTTP status codes with error messages

**Validation Errors:**
- Missing required fields (item name, category)
- Invalid category values
- Return 400 Bad Request with validation details

**Rate Limiting Errors:**
- User exceeds 10 requests per hour
- Return 429 Too Many Requests with Retry-After header
- Include remaining requests and reset time in response

**Configuration Errors:**
- Missing API key
- Invalid API endpoint
- Log error and disable AI features gracefully
- Return 503 Service Unavailable

### Frontend Error Handling

**API Error Responses:**
- Display user-friendly error messages
- Show retry button for transient errors
- Preserve user-entered form data

**Network Errors:**
- Handle connection failures
- Display offline message
- Suggest checking internet connection

**Rate Limit Errors:**
- Display countdown timer until reset
- Disable generate button temporarily
- Show remaining requests count

**Validation Errors:**
- Highlight missing required fields
- Display inline validation messages
- Prevent API calls with invalid data

## Testing Strategy

### Unit Testing

**Backend Unit Tests:**
- Test AIService with mocked Gemini API
- Test PromptBuilder prompt construction
- Test RateLimiter logic
- Test error handling for various API responses
- Test configuration loading
- Mock external API calls

**Frontend Unit Tests:**
- Test AIGenerationButton component rendering
- Test useAIGeneration hook logic
- Test form integration
- Test error state handling
- Mock API calls

### Property-Based Testing

The system will use property-based testing to verify correctness properties across a wide range of inputs. For Java backend testing, we will use **jqwik**. For JavaScript/TypeScript frontend testing, we will use **fast-check**.

**Property Test Configuration:**
- Each property test should run a minimum of 100 iterations
- Use smart generators that constrain inputs to valid ranges
- Each property test must include a comment tag referencing the design document property
- Tag format: `// Feature: ai-description-generation, Property X: <property description>`

**Backend Property Tests (using jqwik):**
- Property 1: Generate random item data, verify title length constraints
- Property 2: Generate random categories, verify category terms in titles
- Property 3: Generate same item with different context, verify titles differ
- Property 4: Generate random item data, verify description length constraints
- Property 5: Generate requests with additional details, verify incorporation
- Property 6: Generate random requests, verify description formatting
- Property 7-8: Test form field population and preservation
- Property 9-13: Test regeneration and rate limiting logic
- Property 14-15: Test error handling and form preservation
- Property 16-19: Test configuration and rate limiting
- Property 20-28: Test prompt construction for various scenarios
- Property 29-34: Test logging and monitoring

**Frontend Property Tests (using fast-check):**
- Generate random item data, verify UI updates correctly
- Generate random error scenarios, verify error handling
- Generate random rate limit scenarios, verify UI feedback
- Test button states across various conditions

### Integration Testing

**Backend Integration Tests:**
- Test complete AI generation flow with real API (optional, use test API key)
- Test rate limiting across multiple requests
- Test error handling with various API responses
- Test configuration loading from environment
- Use mocked Gemini API for consistent testing

**Frontend Integration Tests:**
- Test complete user flow: click button → loading → content displayed
- Test regeneration flow
- Test error scenarios
- Test rate limit UI feedback
- Mock backend API responses

### End-to-End Testing

- Test user creates item with AI-generated title and description
- Test user regenerates content multiple times
- Test rate limit is enforced and resets correctly
- Test error handling when API is unavailable
- Test AI generation works across different categories

## Prompt Engineering

### Title Generation Prompt Template

```
You are an AI assistant helping college students list items for borrowing on a peer-to-peer sharing platform.

Generate a concise, attention-grabbing title for the following item:
- Item Name: {itemName}
- Category: {category}
- Additional Info: {additionalInfo}

Requirements:
- Length: 3-200 characters
- Be specific and descriptive
- Include key identifying features
- Use clear, professional language
- Target audience: college students
{categorySpecificInstructions}

Generate only the title, no additional text.
```

### Description Generation Prompt Template

```
You are an AI assistant helping college students list items for borrowing on a peer-to-peer sharing platform.

Generate a compelling description for the following item:
- Item Name: {itemName}
- Category: {category}
- Condition: {condition}
- Specifications: {specifications}
- Additional Info: {additionalInfo}

Requirements:
- Length: 50-1000 characters
- Highlight key features and benefits for borrowers
- Mention condition and any important details
- Use friendly, informative tone
- Structure with clear sentences
- Target audience: college students
{categorySpecificInstructions}

Generate only the description, no additional text.
```

### Category-Specific Instructions

**Electronics:**
```
- Emphasize technical specifications (model, features, capacity)
- Mention condition clearly (new, like-new, good, fair)
- Highlight any accessories included
```

**Books:**
```
- Mention subject matter and topic
- Include edition information if relevant
- Note condition (highlighting, annotations, wear)
```

**Sports Equipment:**
```
- Describe usage scenarios and activities
- Mention size/fit information
- Note condition and any wear
```

**Tools:**
```
- Describe functionality and applications
- Mention brand and model if relevant
- Note condition and completeness
```

**Musical Instruments:**
```
- Specify instrument type and brand
- Mention skill level suitability
- Note condition and any accessories
```

**Accessories:**
```
- Describe compatibility and use cases
- Mention brand and model
- Note condition and completeness
```

**Other:**
```
- Be descriptive about the item's purpose
- Highlight unique features
- Mention condition clearly
```

## Performance Considerations

**API Response Time:**
- Gemini API typically responds in 1-3 seconds
- Implement 30-second timeout for API calls
- Show loading indicator immediately on button click
- Cache prompts to avoid reconstruction overhead

**Rate Limiting:**
- Use in-memory cache for rate limit tracking (Redis for production)
- Implement sliding window algorithm for accurate rate limiting
- Clean up expired rate limit entries periodically
- Consider user experience when setting limits (10 per hour is reasonable)

**Frontend Performance:**
- Debounce regenerate button clicks (500ms)
- Disable button during API calls to prevent duplicate requests
- Use optimistic UI updates where appropriate
- Lazy load AI generation components

**Cost Optimization:**
- Monitor token usage per request
- Set appropriate max token limits (200 for titles, 500 for descriptions)
- Log usage for cost tracking
- Consider caching common generations (optional)

## Security Considerations

**API Key Protection:**
- Store Gemini API key in environment variables
- Never expose API key in frontend code
- Never log API key in application logs
- Rotate API keys periodically

**Rate Limiting:**
- Implement per-user rate limiting to prevent abuse
- Use IP-based limiting for unauthenticated users
- Monitor for unusual usage patterns
- Consider CAPTCHA for repeated failures

**Input Validation:**
- Validate all user inputs before sending to API
- Sanitize inputs to prevent prompt injection
- Limit input lengths to prevent excessive token usage
- Validate category values against allowed list

**Output Sanitization:**
- Validate AI-generated content length
- Check for inappropriate content (optional)
- Escape HTML in generated content
- Truncate overly long responses

## Deployment Considerations

**Environment Variables:**
- `GEMINI_API_KEY`: Gemini AI API key (required)
- `GEMINI_API_ENDPOINT`: API endpoint URL (default: https://generativelanguage.googleapis.com/v1beta)
- `GEMINI_MODEL`: Model name (default: gemini-pro)
- `AI_GENERATION_ENABLED`: Feature flag (default: true)
- `AI_RATE_LIMIT_PER_HOUR`: Requests per hour (default: 10)
- `AI_REQUEST_TIMEOUT_MS`: API timeout (default: 30000)
- `AI_TEMPERATURE`: Generation temperature (default: 0.7)
- `AI_MAX_TOKENS_TITLE`: Max tokens for titles (default: 200)
- `AI_MAX_TOKENS_DESCRIPTION`: Max tokens for descriptions (default: 500)

**Monitoring:**
- Track API response times
- Monitor error rates
- Log token usage for cost tracking
- Alert on high error rates or slow responses
- Track rate limit hits per user

**Graceful Degradation:**
- If API key is not configured, hide AI generation buttons
- If API is unavailable, show error message and allow manual entry
- If rate limit is reached, show clear message with reset time
- Never block item creation if AI generation fails

**Testing in Production:**
- Use separate API key for production
- Monitor initial rollout closely
- Implement feature flag for easy disable
- Have rollback plan ready

## API Integration Details

### Gemini API Configuration

**Endpoint:**
```
POST https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
```

**Headers:**
```
Content-Type: application/json
x-goog-api-key: {GEMINI_API_KEY}
```

**Request Body:**
```json
{
  "contents": [{
    "parts": [{
      "text": "{prompt}"
    }]
  }],
  "generationConfig": {
    "temperature": 0.7,
    "maxOutputTokens": 200,
    "topP": 0.8,
    "topK": 40
  }
}
```

**Response Parsing:**
```java
String generatedText = response
    .getCandidates()
    .get(0)
    .getContent()
    .getParts()
    .get(0)
    .getText();
```

### Error Response Handling

**401 Unauthorized:**
```json
{
  "error": {
    "code": 401,
    "message": "API key not valid",
    "status": "UNAUTHENTICATED"
  }
}
```

**429 Too Many Requests:**
```json
{
  "error": {
    "code": 429,
    "message": "Resource has been exhausted",
    "status": "RESOURCE_EXHAUSTED"
  }
}
```

**400 Bad Request:**
```json
{
  "error": {
    "code": 400,
    "message": "Invalid request",
    "status": "INVALID_ARGUMENT"
  }
}
```

## Future Enhancements

**Potential Improvements:**
- Support for multiple languages
- User feedback on AI-generated content quality
- Learning from user edits to improve prompts
- Batch generation for multiple items
- AI-powered category suggestion
- Image analysis for automatic description generation
- Personalized prompts based on user history
- A/B testing different prompt templates
- Integration with other AI models for comparison
