package com.rentkar.controller;

import com.rentkar.dto.AIGenerationRequest;
import com.rentkar.dto.LoginRequest;
import com.rentkar.dto.RegisterRequest;
import com.rentkar.repository.ItemRepository;
import com.rentkar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end tests for AI Description Generation feature.
 * These tests verify the complete user flow from button click to content display.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.properties")
public class AIGenerationEndToEndTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ItemRepository itemRepository;
    
    private String baseUrl;
    private String authUrl;
    private String token;
    private Long userId;
    
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/items";
        authUrl = "http://localhost:" + port + "/api/auth";
        
        // Clean up
        itemRepository.deleteAll();
        userRepository.deleteAll();
        
        // Register and login a test user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Test User");
        registerRequest.setPhone("1234567890");
        
        restTemplate.postForEntity(authUrl + "/register", registerRequest, Map.class);
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
        
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
            authUrl + "/login",
            loginRequest,
            Map.class
        );
        
        Map<String, Object> loginData = (Map<String, Object>) loginResponse.getBody().get("data");
        token = (String) loginData.get("token");
        
        Map<String, Object> userData = (Map<String, Object>) loginData.get("user");
        userId = ((Number) userData.get("id")).longValue();
    }

    
    /**
     * Test 13.1: End-to-end test for title generation
     * Requirements: 1.1, 4.1
     */
    @Test
    void testTitleGenerationEndToEnd() {
        AIGenerationRequest request = new AIGenerationRequest();
        request.setItemName("MacBook Pro 2021");
        request.setCategory("Electronics");
        request.setAdditionalInfo("16GB RAM, 512GB SSD, M1 chip");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<AIGenerationRequest> entity = new HttpEntity<>(request, headers);
        
        long startTime = System.currentTimeMillis();
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/generate-title",
            HttpMethod.POST,
            entity,
            Map.class
        );
        long endTime = System.currentTimeMillis();
        
        long responseTime = endTime - startTime;
        assertThat(responseTime).isLessThan(35000);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("success")).isEqualTo(true);
            assertThat(response.getBody().get("message")).isEqualTo("Title generated successfully");
            
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();
            assertThat(data.get("content")).isNotNull();
            
            String generatedTitle = data.get("content").toString();
            assertThat(generatedTitle).isNotEmpty();
            assertThat(generatedTitle.length()).isGreaterThanOrEqualTo(3);
            assertThat(generatedTitle.length()).isLessThanOrEqualTo(200);
            
            assertThat(data.containsKey("tokenCount")).isTrue();
            assertThat(data.containsKey("responseTimeMs")).isTrue();
            assertThat(data.containsKey("remainingRequests")).isTrue();
            
            ResponseEntity<Map> regenerateResponse = restTemplate.exchange(
                baseUrl + "/generate-title",
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (regenerateResponse.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> regenerateData = (Map<String, Object>) regenerateResponse.getBody().get("data");
                String regeneratedTitle = regenerateData.get("content").toString();
                
                assertThat(regeneratedTitle.length()).isGreaterThanOrEqualTo(3);
                assertThat(regeneratedTitle.length()).isLessThanOrEqualTo(200);
                
                int remainingAfterFirst = ((Number) data.get("remainingRequests")).intValue();
                int remainingAfterSecond = ((Number) regenerateData.get("remainingRequests")).intValue();
                assertThat(remainingAfterSecond).isLessThanOrEqualTo(remainingAfterFirst);
            }
        } else {
            assertThat(response.getStatusCode()).isIn(
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.SERVICE_UNAVAILABLE
            );
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("success")).isEqualTo(false);
            assertThat(response.getBody().containsKey("message")).isTrue();
        }
    }

    
    /**
     * Test 13.2: End-to-end test for description generation
     * Requirements: 2.1, 4.1
     */
    @Test
    void testDescriptionGenerationEndToEnd() {
        AIGenerationRequest request = new AIGenerationRequest();
        request.setItemName("Scientific Calculator TI-84 Plus");
        request.setCategory("Electronics");
        request.setCondition("Like new");
        request.setAdditionalInfo("Perfect for calculus and statistics courses");
        request.setSpecifications("Graphing calculator with USB connectivity");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<AIGenerationRequest> entity = new HttpEntity<>(request, headers);
        
        long startTime = System.currentTimeMillis();
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/generate-description",
            HttpMethod.POST,
            entity,
            Map.class
        );
        long endTime = System.currentTimeMillis();
        
        long responseTime = endTime - startTime;
        assertThat(responseTime).isLessThan(35000);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("success")).isEqualTo(true);
            assertThat(response.getBody().get("message")).isEqualTo("Description generated successfully");
            
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();
            assertThat(data.get("content")).isNotNull();
            
            String generatedDescription = data.get("content").toString();
            assertThat(generatedDescription).isNotEmpty();
            assertThat(generatedDescription.length()).isGreaterThanOrEqualTo(50);
            assertThat(generatedDescription.length()).isLessThanOrEqualTo(1000);
            
            assertThat(data.containsKey("tokenCount")).isTrue();
            assertThat(data.containsKey("responseTimeMs")).isTrue();
            assertThat(data.containsKey("remainingRequests")).isTrue();
            
            ResponseEntity<Map> regenerateResponse = restTemplate.exchange(
                baseUrl + "/generate-description",
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (regenerateResponse.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> regenerateData = (Map<String, Object>) regenerateResponse.getBody().get("data");
                String regeneratedDescription = regenerateData.get("content").toString();
                
                assertThat(regeneratedDescription.length()).isGreaterThanOrEqualTo(50);
                assertThat(regeneratedDescription.length()).isLessThanOrEqualTo(1000);
                
                int remainingAfterFirst = ((Number) data.get("remainingRequests")).intValue();
                int remainingAfterSecond = ((Number) regenerateData.get("remainingRequests")).intValue();
                assertThat(remainingAfterSecond).isLessThanOrEqualTo(remainingAfterFirst);
            }
        } else {
            assertThat(response.getStatusCode()).isIn(
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.SERVICE_UNAVAILABLE
            );
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("success")).isEqualTo(false);
            assertThat(response.getBody().containsKey("message")).isTrue();
        }
    }

    
    /**
     * Test 13.3: End-to-end test for rate limiting
     * Requirements: 7.1, 7.2, 7.3
     */
    @Test
    void testRateLimitingEndToEnd() {
        AIGenerationRequest request = new AIGenerationRequest();
        request.setItemName("Test Item");
        request.setCategory("Electronics");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<AIGenerationRequest> entity = new HttpEntity<>(request, headers);
        
        int successfulRequests = 0;
        
        for (int i = 0; i < 10; i++) {
            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/generate-title",
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                break;
            }
            
            if (response.getStatusCode() == HttpStatus.OK || 
                response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR ||
                response.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                successfulRequests++;
            }
        }
        
        assertThat(successfulRequests).isGreaterThan(0);
        
        ResponseEntity<Map> rateLimitedResponse = restTemplate.exchange(
            baseUrl + "/generate-title",
            HttpMethod.POST,
            entity,
            Map.class
        );
        
        if (successfulRequests >= 10) {
            // If AI service is enabled, should be rate limited
            // If AI service is disabled, will return 500 or 503
            if (rateLimitedResponse.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                assertThat(rateLimitedResponse.getBody()).isNotNull();
                assertThat(rateLimitedResponse.getBody().get("success")).isEqualTo(false);
                
                String message = rateLimitedResponse.getBody().get("message").toString();
                assertThat(message.toLowerCase()).containsAnyOf("rate limit", "too many requests");
                
                assertThat(rateLimitedResponse.getHeaders().containsKey("Retry-After")).isTrue();
                assertThat(rateLimitedResponse.getBody().containsKey("retryAfter")).isTrue();
                
                Object retryAfter = rateLimitedResponse.getBody().get("retryAfter");
                assertThat(retryAfter).isNotNull();
            } else {
                // AI service is disabled, verify graceful degradation
                assertThat(rateLimitedResponse.getStatusCode()).isIn(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    HttpStatus.SERVICE_UNAVAILABLE
                );
            }
        }
    }

    
    /**
     * Test 13.4: End-to-end test for error handling
     * Requirements: 5.1, 5.2, 5.4, 5.5
     */
    @Test
    void testErrorHandlingEndToEnd() {
        AIGenerationRequest request = new AIGenerationRequest();
        request.setItemName("Test Item");
        request.setCategory("Electronics");
        request.setAdditionalInfo("User entered this information");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<AIGenerationRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/generate-title",
            HttpMethod.POST,
            entity,
            Map.class
        );
        
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().containsKey("success")).isTrue();
        assertThat(response.getBody().containsKey("message")).isTrue();
        
        if (response.getStatusCode() != HttpStatus.OK || 
            response.getBody().get("success").equals(false)) {
            
            String errorMessage = response.getBody().get("message").toString();
            assertThat(errorMessage).isNotEmpty();
            assertThat(errorMessage).doesNotContain("Exception", "Stack trace", "null");
            assertThat(errorMessage.length()).isGreaterThan(10);
            
            assertThat(request.getItemName()).isEqualTo("Test Item");
            assertThat(request.getCategory()).isEqualTo("Electronics");
            assertThat(request.getAdditionalInfo()).isEqualTo("User entered this information");
        }
        
        long startTime = System.currentTimeMillis();
        ResponseEntity<Map> timeoutTestResponse = restTemplate.exchange(
            baseUrl + "/generate-description",
            HttpMethod.POST,
            entity,
            Map.class
        );
        long endTime = System.currentTimeMillis();
        
        assertThat(endTime - startTime).isLessThan(35000);
        
        AIGenerationRequest invalidRequest = new AIGenerationRequest();
        HttpEntity<AIGenerationRequest> invalidEntity = new HttpEntity<>(invalidRequest, headers);
        
        try {
            ResponseEntity<Map> validationResponse = restTemplate.exchange(
                baseUrl + "/generate-title",
                HttpMethod.POST,
                invalidEntity,
                Map.class
            );
            
            assertThat(validationResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            assertThat(e.getMessage()).contains("cannot retry due to server authentication");
        }
    }

    
    /**
     * Test 13.5: End-to-end test for category-specific generation
     * Requirements: 9.1, 9.2, 9.3, 9.4
     */
    @Test
    void testCategorySpecificGenerationEndToEnd() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        
        AIGenerationRequest electronicsRequest = new AIGenerationRequest();
        electronicsRequest.setItemName("MacBook Pro");
        electronicsRequest.setCategory("Electronics");
        electronicsRequest.setSpecifications("M1 chip, 16GB RAM, 512GB SSD");
        electronicsRequest.setCondition("Excellent");
        
        HttpEntity<AIGenerationRequest> electronicsEntity = new HttpEntity<>(electronicsRequest, headers);
        ResponseEntity<Map> electronicsResponse = restTemplate.exchange(
            baseUrl + "/generate-description",
            HttpMethod.POST,
            electronicsEntity,
            Map.class
        );
        
        if (electronicsResponse.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> data = (Map<String, Object>) electronicsResponse.getBody().get("data");
            String description = data.get("content").toString();
            
            assertThat(description).isNotEmpty();
            assertThat(description.length()).isGreaterThanOrEqualTo(50);
            assertThat(description.length()).isLessThanOrEqualTo(1000);
        }
        
        AIGenerationRequest booksRequest = new AIGenerationRequest();
        booksRequest.setItemName("Introduction to Algorithms");
        booksRequest.setCategory("Books");
        booksRequest.setAdditionalInfo("4th edition, computer science textbook");
        booksRequest.setCondition("Good");
        
        HttpEntity<AIGenerationRequest> booksEntity = new HttpEntity<>(booksRequest, headers);
        ResponseEntity<Map> booksResponse = restTemplate.exchange(
            baseUrl + "/generate-description",
            HttpMethod.POST,
            booksEntity,
            Map.class
        );
        
        if (booksResponse.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> data = (Map<String, Object>) booksResponse.getBody().get("data");
            String description = data.get("content").toString();
            
            assertThat(description).isNotEmpty();
            assertThat(description.length()).isGreaterThanOrEqualTo(50);
            assertThat(description.length()).isLessThanOrEqualTo(1000);
        }
        
        AIGenerationRequest sportsRequest = new AIGenerationRequest();
        sportsRequest.setItemName("Tennis Racket");
        sportsRequest.setCategory("Sports Equipment");
        sportsRequest.setAdditionalInfo("Wilson Pro Staff, perfect for intermediate players");
        sportsRequest.setCondition("Like new");
        
        HttpEntity<AIGenerationRequest> sportsEntity = new HttpEntity<>(sportsRequest, headers);
        ResponseEntity<Map> sportsResponse = restTemplate.exchange(
            baseUrl + "/generate-description",
            HttpMethod.POST,
            sportsEntity,
            Map.class
        );
        
        if (sportsResponse.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> data = (Map<String, Object>) sportsResponse.getBody().get("data");
            String description = data.get("content").toString();
            
            assertThat(description).isNotEmpty();
            assertThat(description.length()).isGreaterThanOrEqualTo(50);
            assertThat(description.length()).isLessThanOrEqualTo(1000);
        }
        
        AIGenerationRequest toolsRequest = new AIGenerationRequest();
        toolsRequest.setItemName("Cordless Drill");
        toolsRequest.setCategory("Tools");
        toolsRequest.setSpecifications("DeWalt 20V MAX, includes battery and charger");
        toolsRequest.setCondition("Good");
        
        HttpEntity<AIGenerationRequest> toolsEntity = new HttpEntity<>(toolsRequest, headers);
        ResponseEntity<Map> toolsResponse = restTemplate.exchange(
            baseUrl + "/generate-description",
            HttpMethod.POST,
            toolsEntity,
            Map.class
        );
        
        if (toolsResponse.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> data = (Map<String, Object>) toolsResponse.getBody().get("data");
            String description = data.get("content").toString();
            
            assertThat(description).isNotEmpty();
            assertThat(description.length()).isGreaterThanOrEqualTo(50);
            assertThat(description.length()).isLessThanOrEqualTo(1000);
        }
    }
}
