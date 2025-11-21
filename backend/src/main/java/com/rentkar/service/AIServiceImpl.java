package com.rentkar.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rentkar.dto.AIGenerationRequest;
import com.rentkar.dto.AIGenerationResponse;
import com.rentkar.exception.RateLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

/**
 * Implementation of AIService using Google Gemini API
 */
@Service
public class AIServiceImpl implements AIService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIServiceImpl.class);
    
    private final WebClient webClient;
    private final Gson gson;
    private final boolean enabled;
    private final double temperature;
    private final int maxTokensTitle;
    private final int maxTokensDescription;
    private final RateLimiter rateLimiter;
    private final int usageThresholdWarning;
    private final int rateLimitPerHour;
    
    public AIServiceImpl(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.api-endpoint}") String apiEndpoint,
            @Value("${gemini.model}") String model,
            @Value("${ai.generation.enabled}") boolean enabled,
            @Value("${ai.request.timeout-ms}") int timeoutMs,
            @Value("${ai.temperature}") double temperature,
            @Value("${ai.max-tokens.title}") int maxTokensTitle,
            @Value("${ai.max-tokens.description}") int maxTokensDescription,
            @Value("${ai.usage.threshold.warning}") int usageThresholdWarning,
            @Value("${ai.rate-limit.per-hour}") int rateLimitPerHour,
            RateLimiter rateLimiter) {
        
        this.enabled = enabled;
        this.temperature = temperature;
        this.maxTokensTitle = maxTokensTitle;
        this.maxTokensDescription = maxTokensDescription;
        this.usageThresholdWarning = usageThresholdWarning;
        this.rateLimitPerHour = rateLimitPerHour;
        this.rateLimiter = rateLimiter;
        this.gson = new Gson();
        
        // Initialize WebClient for Gemini API
        this.webClient = WebClient.builder()
                .baseUrl(apiEndpoint + "/models/" + model + ":generateContent")
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("x-goog-api-key", apiKey)
                .build();
        
        logger.info("AIService initialized - enabled: {}, timeout: {}ms", enabled, timeoutMs);
    }
    
    @Override
    public AIGenerationResponse generateTitle(AIGenerationRequest request, String userId) {
        if (!enabled) {
            return new AIGenerationResponse("AI generation is currently disabled");
        }
        
        // Log request details
        logRequest(userId, "TITLE");
        
        // Check rate limit
        checkRateLimit(userId);
        
        String prompt = PromptBuilder.buildTitlePrompt(request);
        AIGenerationResponse response = callGeminiAPI(prompt, maxTokensTitle, userId, "TITLE");
        
        return response;
    }
    
    @Override
    public AIGenerationResponse generateDescription(AIGenerationRequest request, String userId) {
        if (!enabled) {
            return new AIGenerationResponse("AI generation is currently disabled");
        }
        
        // Log request details
        logRequest(userId, "DESCRIPTION");
        
        // Check rate limit
        checkRateLimit(userId);
        
        String prompt = PromptBuilder.buildDescriptionPrompt(request);
        AIGenerationResponse response = callGeminiAPI(prompt, maxTokensDescription, userId, "DESCRIPTION");
        
        return response;
    }
    
    @Override
    public boolean isAvailable() {
        return enabled;
    }
    
    @Override
    public int getRemainingRequests(String userId) {
        if (userId == null || userId.isEmpty()) {
            return 0;
        }
        return rateLimiter.getRemainingRequests(userId);
    }
    
    /**
     * Check rate limit and throw exception if exceeded
     */
    private void checkRateLimit(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID is required for rate limiting");
        }
        
        if (!rateLimiter.allowRequest(userId)) {
            long retryAfter = rateLimiter.getResetTime(userId);
            logger.warn("Rate limit exceeded for user: {}, retry after: {}s", userId, retryAfter);
            throw new RateLimitExceededException(
                "Rate limit exceeded. Please try again later.", 
                retryAfter
            );
        }
    }
    
    /**
     * Call Gemini API with the given prompt
     */
    private AIGenerationResponse callGeminiAPI(String prompt, int maxTokens, String userId, String requestType) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Build request body
            JsonObject requestBody = buildRequestBody(prompt, maxTokens);
            
            // Make API call
            String responseBody = webClient.post()
                    .bodyValue(requestBody.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(30000))
                    .block();
            
            // Parse response
            JsonObject response = gson.fromJson(responseBody, JsonObject.class);
            String generatedText = extractGeneratedText(response);
            int tokenCount = extractTokenCount(response);
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            // Log success with metrics
            logSuccess(userId, requestType, responseTime, tokenCount);
            
            return new AIGenerationResponse(generatedText, tokenCount, responseTime);
            
        } catch (WebClientResponseException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            String errorMessage = handleApiError(e);
            
            // Log error details
            logError(userId, requestType, e.getClass().getSimpleName(), errorMessage);
            
            return new AIGenerationResponse(errorMessage);
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            
            // Log error details
            logError(userId, requestType, e.getClass().getSimpleName(), "Unexpected error during AI generation");
            
            return new AIGenerationResponse("An unexpected error occurred. Please try again.");
        }
    }
    
    /**
     * Build Gemini API request body
     */
    private JsonObject buildRequestBody(String prompt, int maxTokens) {
        JsonObject requestBody = new JsonObject();
        
        // Build contents array
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);
        
        // Build generation config
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", temperature);
        generationConfig.addProperty("maxOutputTokens", maxTokens);
        generationConfig.addProperty("topP", 0.8);
        generationConfig.addProperty("topK", 40);
        requestBody.add("generationConfig", generationConfig);
        
        return requestBody;
    }
    
    /**
     * Extract generated text from Gemini API response
     */
    private String extractGeneratedText(JsonObject response) {
        try {
            return response.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString()
                    .trim();
        } catch (Exception e) {
            logger.error("Failed to extract generated text from response", e);
            throw new RuntimeException("Failed to parse AI response");
        }
    }
    
    /**
     * Extract token count from Gemini API response
     */
    private int extractTokenCount(JsonObject response) {
        try {
            if (response.has("usageMetadata")) {
                return response.getAsJsonObject("usageMetadata")
                        .get("totalTokenCount").getAsInt();
            }
            return 0;
        } catch (Exception e) {
            logger.warn("Failed to extract token count from response", e);
            return 0;
        }
    }
    
    /**
     * Handle API errors and return user-friendly messages
     */
    private String handleApiError(WebClientResponseException e) {
        int statusCode = e.getStatusCode().value();
        
        switch (statusCode) {
            case 401:
                return "AI service authentication failed. Please contact support.";
            case 429:
                return "AI service rate limit exceeded. Please try again later.";
            case 400:
                return "Invalid request to AI service. Please check your input.";
            case 503:
                return "AI service is temporarily unavailable. Please try again later.";
            default:
                return "AI service error. Please try again.";
        }
    }
    
    /**
     * Log AI generation request details in structured format
     * Excludes sensitive data (API keys, user data)
     */
    private void logRequest(String userId, String requestType) {
        String sanitizedUserId = sanitizeUserId(userId);
        String timestamp = java.time.Instant.now().toString();
        
        // Structured logging in JSON-like format
        logger.info("AI_REQUEST: {{\"userId\": \"{}\", \"timestamp\": \"{}\", \"requestType\": \"{}\"}}",
                sanitizedUserId, timestamp, requestType);
    }
    
    /**
     * Log successful AI generation with metrics
     */
    private void logSuccess(String userId, String requestType, long responseTimeMs, int tokenCount) {
        String sanitizedUserId = sanitizeUserId(userId);
        String timestamp = java.time.Instant.now().toString();
        
        // Structured logging in JSON-like format
        logger.info("AI_SUCCESS: {{\"userId\": \"{}\", \"timestamp\": \"{}\", \"requestType\": \"{}\", \"responseTimeMs\": {}, \"tokenCount\": {}}}",
                sanitizedUserId, timestamp, requestType, responseTimeMs, tokenCount);
        
        // Check usage threshold and log warning if exceeded
        checkUsageThreshold(userId);
    }
    
    /**
     * Check if user has exceeded usage threshold and log warning
     */
    private void checkUsageThreshold(String userId) {
        if (userId == null || userId.isEmpty()) {
            return;
        }
        
        int remainingRequests = rateLimiter.getRemainingRequests(userId);
        int usedRequests = rateLimitPerHour - remainingRequests;
        
        if (usedRequests >= usageThresholdWarning) {
            String sanitizedUserId = sanitizeUserId(userId);
            String timestamp = java.time.Instant.now().toString();
            
            logger.warn("AI_USAGE_THRESHOLD: {{\"userId\": \"{}\", \"timestamp\": \"{}\", \"usedRequests\": {}, \"threshold\": {}, \"limit\": {}}}",
                    sanitizedUserId, timestamp, usedRequests, usageThresholdWarning, rateLimitPerHour);
        }
    }
    
    /**
     * Log AI generation errors with details
     * Excludes sensitive data
     */
    private void logError(String userId, String requestType, String errorType, String errorMessage) {
        String sanitizedUserId = sanitizeUserId(userId);
        String timestamp = java.time.Instant.now().toString();
        
        // Structured logging in JSON-like format
        logger.error("AI_ERROR: {{\"userId\": \"{}\", \"timestamp\": \"{}\", \"requestType\": \"{}\", \"errorType\": \"{}\", \"errorMessage\": \"{}\"}}",
                sanitizedUserId, timestamp, requestType, errorType, errorMessage);
    }
    
    /**
     * Sanitize user ID to exclude sensitive data
     * Returns a hashed or truncated version for logging
     */
    private String sanitizeUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            return "ANONYMOUS";
        }
        // Return only first 8 characters or hash to avoid logging full user IDs
        return userId.length() > 8 ? userId.substring(0, 8) + "..." : userId;
    }
}
