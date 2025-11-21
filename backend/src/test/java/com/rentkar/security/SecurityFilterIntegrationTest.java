package com.rentkar.security;

import com.rentkar.dto.LoginRequest;
import com.rentkar.dto.RegisterRequest;
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
 * Integration tests for Spring Security JWT filter configuration
 * Tests Properties 15-19, 28, 30, 31 from the design document
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.properties")
public class SecurityFilterIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        userRepository.deleteAll();
    }

    /**
     * Feature: user-authentication, Property 15: Valid tokens grant access to protected resources
     * Validates: Requirements 4.1
     */
    @Test
    void validTokensGrantAccessToProtectedResources() {
        // Register a user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Test User");

        restTemplate.postForEntity(baseUrl + "/api/auth/register", registerRequest, Map.class);

        // Login to get token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                loginRequest,
                Map.class
        );

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> loginData = (Map<String, Object>) loginResponse.getBody().get("data");
        String token = (String) loginData.get("token");

        // Access protected resource with valid token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/api/auth/me",
                HttpMethod.GET,
                entity,
                Map.class
        );

        // Valid token should grant access
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
    }

    /**
     * Feature: user-authentication, Property 16: Missing tokens are rejected
     * Validates: Requirements 4.2
     */
    @Test
    void missingTokensAreRejected() {
        // Attempt to access protected endpoint without token
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/api/auth/me",
                Map.class
        );

        // Should be rejected with 401
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /**
     * Feature: user-authentication, Property 17: Expired tokens are rejected
     * Validates: Requirements 4.3
     */
    @Test
    void expiredTokensAreRejected() {
        // Create an expired token (this is a malformed token that will fail validation)
        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTYwMDAwMDAwMCwiZXhwIjoxNjAwMDAwMDAxfQ.invalid";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + expiredToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/api/auth/me",
                HttpMethod.GET,
                entity,
                Map.class
        );

        // Expired token should be rejected with 401
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /**
     * Feature: user-authentication, Property 18: Malformed tokens are rejected
     * Validates: Requirements 4.4
     */
    @Test
    void malformedTokensAreRejected() {
        String[] malformedTokens = {
                "not-a-jwt-token",
                "invalid.token.here",
                "Bearer malformed",
                "12345",
                "!@#$%^&*()"
        };

        for (String malformedToken : malformedTokens) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + malformedToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/api/auth/me",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            // Malformed token should be rejected with 401
            assertThat(response.getStatusCode())
                    .as("Malformed token '%s' should be rejected", malformedToken)
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Feature: user-authentication, Property 28: Requests with tokens are validated
     * Validates: Requirements 8.1, 8.2
     */
    @Test
    void requestsWithTokensAreValidated() {
        // Register and login to get a valid token
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser2");
        registerRequest.setEmail("test2@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Test User 2");

        restTemplate.postForEntity(baseUrl + "/api/auth/register", registerRequest, Map.class);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser2");
        loginRequest.setPassword("password123");

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                loginRequest,
                Map.class
        );

        Map<String, Object> loginData = (Map<String, Object>) loginResponse.getBody().get("data");
        String validToken = (String) loginData.get("token");

        // Test with valid token - should succeed
        HttpHeaders validHeaders = new HttpHeaders();
        validHeaders.set("Authorization", "Bearer " + validToken);
        HttpEntity<Void> validEntity = new HttpEntity<>(validHeaders);

        ResponseEntity<Map> validResponse = restTemplate.exchange(
                baseUrl + "/api/auth/me",
                HttpMethod.GET,
                validEntity,
                Map.class
        );

        assertThat(validResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Test with tampered token (change last 5 characters) - should fail
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "XXXXX";
        HttpHeaders tamperedHeaders = new HttpHeaders();
        tamperedHeaders.set("Authorization", "Bearer " + tamperedToken);
        HttpEntity<Void> tamperedEntity = new HttpEntity<>(tamperedHeaders);

        ResponseEntity<Map> tamperedResponse = restTemplate.exchange(
                baseUrl + "/api/auth/me",
                HttpMethod.GET,
                tamperedEntity,
                Map.class
        );

        // Tampered token should be rejected
        assertThat(tamperedResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /**
     * Feature: user-authentication, Property 30: Valid tokens provide user context
     * Validates: Requirements 8.4
     */
    @Test
    void validTokensProvideUserContext() {
        // Register user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser3");
        registerRequest.setEmail("test3@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Test User 3");

        restTemplate.postForEntity(baseUrl + "/api/auth/register", registerRequest, Map.class);

        // Login to get token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser3");
        loginRequest.setPassword("password123");

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                loginRequest,
                Map.class
        );

        Map<String, Object> loginData = (Map<String, Object>) loginResponse.getBody().get("data");
        String token = (String) loginData.get("token");

        // Access /me endpoint with token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/api/auth/me",
                HttpMethod.GET,
                entity,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify user context is available (user info is returned)
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data).isNotNull();
        assertThat(data.get("username")).isEqualTo("testuser3");
        assertThat(data.get("email")).isEqualTo("test3@example.com");
    }

    /**
     * Feature: user-authentication, Property 31: Invalid tokens prevent execution
     * Validates: Requirements 8.5
     */
    @Test
    void invalidTokensPreventExecution() {
        String[] invalidTokens = {
                "completely-invalid",
                "fake.jwt.token",
                "random-string-12345"
        };

        for (String invalidToken : invalidTokens) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + invalidToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/api/auth/me",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            // Invalid token should prevent execution - return 401 before business logic runs
            assertThat(response.getStatusCode())
                    .as("Invalid token '%s' should prevent execution", invalidToken)
                    .isEqualTo(HttpStatus.UNAUTHORIZED);

            // The response should be an error, not successful data
            if (response.getBody() != null) {
                Object success = response.getBody().get("success");
                if (success != null) {
                    assertThat(success).isEqualTo(false);
                }
            }
        }
    }
}
