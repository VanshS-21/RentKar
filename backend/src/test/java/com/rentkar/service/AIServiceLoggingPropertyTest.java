package com.rentkar.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rentkar.dto.AIGenerationRequest;
import com.rentkar.dto.AIGenerationResponse;
import net.jqwik.api.*;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Property-based tests for AIService logging functionality
 */
public class AIServiceLoggingPropertyTest {
    
    // Feature: ai-description-generation, Property 29: Request logging includes required fields
    // Validates: Requirements 11.1
    @Property(tries = 100)
    void requestLoggingIncludesRequiredFields(
            @ForAll("validItemName") String itemName,
            @ForAll("validCategory") String category,
            @ForAll("validUserId") String userId,
            @ForAll("requestType") String requestType) {
        
        // Set up log capture
        Logger logger = (Logger) LoggerFactory.getLogger(AIServiceImpl.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        
        try {
            AIService service = createMockAIService();
            
            AIGenerationRequest request = new AIGenerationRequest();
            request.setItemName(itemName);
            request.setCategory(category);
            
            // Make request based on type
            if (requestType.equals("TITLE")) {
                service.generateTitle(request, userId);
            } else {
                service.generateDescription(request, userId);
            }
            
            // Get log messages
            List<ILoggingEvent> logsList = listAppender.list;
            List<String> messages = logsList.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .collect(Collectors.toList());
            
            // Find AI_REQUEST log entry
            String requestLog = messages.stream()
                    .filter(msg -> msg.contains("AI_REQUEST"))
                    .findFirst()
                    .orElse("");
            
            // Verify required fields are present
            assertThat(requestLog).isNotEmpty();
            assertThat(requestLog).contains("userId");
            assertThat(requestLog).contains("timestamp");
            assertThat(requestLog).contains("requestType");
            assertThat(requestLog).contains(requestType);
            
        } finally {
            logger.detachAppender(listAppender);
        }
    }
    
    // Feature: ai-description-generation, Property 30: Success logging includes metrics
    // Validates: Requirements 11.2
    @Property(tries = 100)
    void successLoggingIncludesMetrics(
            @ForAll("validItemName") String itemName,
            @ForAll("validCategory") String category,
            @ForAll("validUserId") String userId) {
        
        // Set up log capture
        Logger logger = (Logger) LoggerFactory.getLogger(AIServiceImpl.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        
        try {
            AIService service = createMockAIService();
            
            AIGenerationRequest request = new AIGenerationRequest();
            request.setItemName(itemName);
            request.setCategory(category);
            
            // Make successful request
            AIGenerationResponse response = service.generateTitle(request, userId);
            
            // Only check if request was successful
            if (response.isSuccess()) {
                // Get log messages
                List<ILoggingEvent> logsList = listAppender.list;
                List<String> messages = logsList.stream()
                        .map(ILoggingEvent::getFormattedMessage)
                        .collect(Collectors.toList());
                
                // Find AI_SUCCESS log entry
                String successLog = messages.stream()
                        .filter(msg -> msg.contains("AI_SUCCESS"))
                        .findFirst()
                        .orElse("");
                
                // Verify metrics are present
                assertThat(successLog).isNotEmpty();
                assertThat(successLog).contains("responseTimeMs");
                assertThat(successLog).contains("tokenCount");
            }
            
        } finally {
            logger.detachAppender(listAppender);
        }
    }
    
    // Feature: ai-description-generation, Property 31: Error logging includes details
    // Validates: Requirements 11.3
    @Property(tries = 100)
    void errorLoggingIncludesDetails(
            @ForAll("validItemName") String itemName,
            @ForAll("validCategory") String category,
            @ForAll("validUserId") String userId) {
        
        // Set up log capture
        Logger logger = (Logger) LoggerFactory.getLogger(AIServiceImpl.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        
        try {
            // Create a service that simulates errors
            AIService service = createErrorAIService();
            
            AIGenerationRequest request = new AIGenerationRequest();
            request.setItemName(itemName);
            request.setCategory(category);
            
            // Make request that will fail
            AIGenerationResponse response = service.generateTitle(request, userId);
            
            // Only check if request failed
            if (!response.isSuccess()) {
                // Get log messages
                List<ILoggingEvent> logsList = listAppender.list;
                List<String> messages = logsList.stream()
                        .map(ILoggingEvent::getFormattedMessage)
                        .collect(Collectors.toList());
                
                // Find AI_ERROR log entry
                String errorLog = messages.stream()
                        .filter(msg -> msg.contains("AI_ERROR"))
                        .findFirst()
                        .orElse("");
                
                // Verify error details are present
                assertThat(errorLog).isNotEmpty();
                assertThat(errorLog).contains("errorType");
                assertThat(errorLog).contains("errorMessage");
            }
            
        } finally {
            logger.detachAppender(listAppender);
        }
    }
    
    // Feature: ai-description-generation, Property 32: Logs exclude sensitive data
    // Validates: Requirements 11.4
    @Property(tries = 100)
    void logsExcludeSensitiveData(
            @ForAll("validItemName") String itemName,
            @ForAll("validCategory") String category,
            @ForAll("validUserId") String userId) {
        
        // Set up log capture
        Logger logger = (Logger) LoggerFactory.getLogger(AIServiceImpl.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        
        try {
            AIService service = createMockAIService();
            
            AIGenerationRequest request = new AIGenerationRequest();
            request.setItemName(itemName);
            request.setCategory(category);
            
            service.generateTitle(request, userId);
            
            // Get all log messages
            List<ILoggingEvent> logsList = listAppender.list;
            List<String> messages = logsList.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .collect(Collectors.toList());
            
            // Verify no sensitive data in logs
            for (String message : messages) {
                // Should not contain API keys (common patterns)
                assertThat(message).doesNotContain("AIza"); // Gemini API key prefix
                assertThat(message).doesNotContain("api-key");
                assertThat(message).doesNotContain("x-goog-api-key");
                
                // Should not contain full user IDs (should be sanitized)
                if (userId.length() > 8) {
                    assertThat(message).doesNotContain(userId);
                }
            }
            
        } finally {
            logger.detachAppender(listAppender);
        }
    }
    
    // Feature: ai-description-generation, Property 33: Structured logging format
    // Validates: Requirements 11.5
    @Property(tries = 100)
    void structuredLoggingFormat(
            @ForAll("validItemName") String itemName,
            @ForAll("validCategory") String category,
            @ForAll("validUserId") String userId) {
        
        // Set up log capture
        Logger logger = (Logger) LoggerFactory.getLogger(AIServiceImpl.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        
        try {
            AIService service = createMockAIService();
            
            AIGenerationRequest request = new AIGenerationRequest();
            request.setItemName(itemName);
            request.setCategory(category);
            
            service.generateTitle(request, userId);
            
            // Get log messages
            List<ILoggingEvent> logsList = listAppender.list;
            List<String> messages = logsList.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .collect(Collectors.toList());
            
            // Find AI-related log entries
            List<String> aiLogs = messages.stream()
                    .filter(msg -> msg.contains("AI_REQUEST") || 
                                  msg.contains("AI_SUCCESS") || 
                                  msg.contains("AI_ERROR"))
                    .collect(Collectors.toList());
            
            // Verify structured format (JSON-like with curly braces and key-value pairs)
            for (String log : aiLogs) {
                assertThat(log).contains("{");
                assertThat(log).contains("}");
                assertThat(log).contains(":");
            }
            
        } finally {
            logger.detachAppender(listAppender);
        }
    }
    
    // Arbitraries
    @Provide
    Arbitrary<String> validItemName() {
        return Arbitraries.of(
            "Laptop", "Textbook", "Calculator", "Guitar", "Bicycle",
            "Drill", "Camera", "Headphones", "Backpack", "Tent"
        );
    }
    
    @Provide
    Arbitrary<String> validCategory() {
        return Arbitraries.of(
            "Electronics", "Books", "Accessories", "Sports Equipment",
            "Musical Instruments", "Tools", "Other"
        );
    }
    
    @Provide
    Arbitrary<String> validUserId() {
        return Arbitraries.strings()
                .alpha()
                .numeric()
                .ofMinLength(5)
                .ofMaxLength(20);
    }
    
    @Provide
    Arbitrary<String> requestType() {
        return Arbitraries.of("TITLE", "DESCRIPTION");
    }
    
    // Helper methods
    private AIService createMockAIService() {
        // Create actual AIServiceImpl with mocked dependencies
        RateLimiter rateLimiter = mock(RateLimiter.class);
        when(rateLimiter.allowRequest(any())).thenReturn(true);
        when(rateLimiter.getRemainingRequests(any())).thenReturn(5);
        when(rateLimiter.getResetTime(any())).thenReturn(3600L);
        
        // Mock WebClient to return successful responses
        WebClient webClient = createMockWebClient(true);
        
        AIServiceImpl service = new AIServiceImpl(
            "test-api-key",
            "https://test-endpoint.com",
            "gemini-pro",
            true,
            30000,
            0.7,
            200,
            500,
            8,
            10,
            rateLimiter
        );
        
        // Inject mocked WebClient
        ReflectionTestUtils.setField(service, "webClient", webClient);
        
        return service;
    }
    
    private AIService createErrorAIService() {
        // Create actual AIServiceImpl with mocked dependencies that simulate errors
        RateLimiter rateLimiter = mock(RateLimiter.class);
        when(rateLimiter.allowRequest(any())).thenReturn(true);
        when(rateLimiter.getRemainingRequests(any())).thenReturn(5);
        
        // Mock WebClient to return error responses
        WebClient webClient = createMockWebClient(false);
        
        AIServiceImpl service = new AIServiceImpl(
            "test-api-key",
            "https://test-endpoint.com",
            "gemini-pro",
            true,
            30000,
            0.7,
            200,
            500,
            8,
            10,
            rateLimiter
        );
        
        // Inject mocked WebClient
        ReflectionTestUtils.setField(service, "webClient", webClient);
        
        return service;
    }
    
    @SuppressWarnings("unchecked")
    private WebClient createMockWebClient(boolean success) {
        WebClient webClient = mock(WebClient.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        
        if (success) {
            // Create successful response
            JsonObject response = new JsonObject();
            JsonArray candidates = new JsonArray();
            JsonObject candidate = new JsonObject();
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", "Generated content for testing");
            parts.add(part);
            content.add("parts", parts);
            candidate.add("content", content);
            candidates.add(candidate);
            response.add("candidates", candidates);
            
            JsonObject usageMetadata = new JsonObject();
            usageMetadata.addProperty("totalTokenCount", 50);
            response.add("usageMetadata", usageMetadata);
            
            Gson gson = new Gson();
            String responseJson = gson.toJson(response);
            
            when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(responseJson).timeout(Duration.ofMillis(30000)));
        } else {
            // Simulate error
            when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new RuntimeException("API Error")));
        }
        
        return webClient;
    }
}
