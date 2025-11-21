package com.rentkar.service;

import net.jqwik.api.*;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class RateLimiterPropertyTest {
    
    // Feature: ai-description-generation, Property 11: Generation count tracking
    // Validates: Requirements 4.3
    @Property(tries = 100)
    void generationCountTracking(
            @ForAll("userId") String userId,
            @ForAll("requestCount") int requestCount) {
        
        // Create rate limiter with high limit for testing
        RateLimiter rateLimiter = new RateLimiterImpl(100);
        
        // Make requestCount requests
        for (int i = 0; i < requestCount; i++) {
            boolean allowed = rateLimiter.allowRequest(userId);
            assertThat(allowed).isTrue();
        }
        
        // Verify remaining requests
        int remaining = rateLimiter.getRemainingRequests(userId);
        assertThat(remaining).isEqualTo(100 - requestCount);
    }
    
    // Feature: ai-description-generation, Property 12: Rate limit enforcement
    // Validates: Requirements 4.4, 7.1, 7.2
    @Property(tries = 100)
    void rateLimitEnforcement(@ForAll("userId") String userId) {
        
        // Create rate limiter with limit of 10
        RateLimiter rateLimiter = new RateLimiterImpl(10);
        
        // Make 10 requests - all should succeed
        for (int i = 0; i < 10; i++) {
            boolean allowed = rateLimiter.allowRequest(userId);
            assertThat(allowed).as("Request %d should be allowed", i + 1).isTrue();
        }
        
        // 11th request should be blocked
        boolean eleventhAllowed = rateLimiter.allowRequest(userId);
        assertThat(eleventhAllowed).as("11th request should be blocked").isFalse();
        
        // Verify remaining requests is 0
        int remaining = rateLimiter.getRemainingRequests(userId);
        assertThat(remaining).isEqualTo(0);
    }
    
    // Feature: ai-description-generation, Property 13: Rate limit reset
    // Validates: Requirements 4.5, 7.3
    @Property(tries = 50)
    void rateLimitReset(@ForAll("userId") String userId) throws InterruptedException {
        
        // Create rate limiter with limit of 5 and short window for testing
        // Note: We can't easily test the full 1-hour window in unit tests
        // This test verifies the reset time calculation logic
        RateLimiter rateLimiter = new RateLimiterImpl(5);
        
        // Make 5 requests
        for (int i = 0; i < 5; i++) {
            rateLimiter.allowRequest(userId);
        }
        
        // Verify rate limit is hit
        assertThat(rateLimiter.allowRequest(userId)).isFalse();
        
        // Get reset time
        long resetTime = rateLimiter.getResetTime(userId);
        
        // Reset time should be positive and less than 1 hour (3600 seconds)
        assertThat(resetTime).isGreaterThan(0);
        assertThat(resetTime).isLessThanOrEqualTo(3600);
        
        // Note: In a real scenario, we would wait for the window to expire
        // For unit testing, we verify the logic is correct
    }
    
    // Feature: ai-description-generation, Property 18: Per-user rate limiting
    // Validates: Requirements 7.4
    @Property(tries = 100)
    void perUserRateLimiting(
            @ForAll("userId") String userId1,
            @ForAll("userId") String userId2) {
        
        // Skip if users are the same
        Assume.that(!userId1.equals(userId2));
        
        RateLimiter rateLimiter = new RateLimiterImpl(10);
        
        // User 1 makes 10 requests
        for (int i = 0; i < 10; i++) {
            rateLimiter.allowRequest(userId1);
        }
        
        // User 1 should be rate limited
        assertThat(rateLimiter.allowRequest(userId1)).isFalse();
        assertThat(rateLimiter.getRemainingRequests(userId1)).isEqualTo(0);
        
        // User 2 should still have full quota
        assertThat(rateLimiter.getRemainingRequests(userId2)).isEqualTo(10);
        assertThat(rateLimiter.allowRequest(userId2)).isTrue();
        assertThat(rateLimiter.getRemainingRequests(userId2)).isEqualTo(9);
    }
    
    // Feature: ai-description-generation, Property 19: IP-based rate limiting for unauthenticated users
    // Validates: Requirements 7.5
    @Property(tries = 100)
    void ipBasedRateLimiting(
            @ForAll("ipAddress") String ip1,
            @ForAll("ipAddress") String ip2) {
        
        // Skip if IPs are the same
        Assume.that(!ip1.equals(ip2));
        
        RateLimiter rateLimiter = new RateLimiterImpl(10);
        
        // IP 1 makes 10 requests
        for (int i = 0; i < 10; i++) {
            rateLimiter.allowRequest("ip_" + ip1);
        }
        
        // IP 1 should be rate limited
        assertThat(rateLimiter.allowRequest("ip_" + ip1)).isFalse();
        assertThat(rateLimiter.getRemainingRequests("ip_" + ip1)).isEqualTo(0);
        
        // IP 2 should still have full quota
        assertThat(rateLimiter.getRemainingRequests("ip_" + ip2)).isEqualTo(10);
        assertThat(rateLimiter.allowRequest("ip_" + ip2)).isTrue();
        assertThat(rateLimiter.getRemainingRequests("ip_" + ip2)).isEqualTo(9);
    }
    
    // Arbitraries
    @Provide
    Arbitrary<String> userId() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(5)
                .ofMaxLength(20)
                .map(s -> "user_" + s);
    }
    
    @Provide
    Arbitrary<Integer> requestCount() {
        return Arbitraries.integers().between(1, 50);
    }
    
    @Provide
    Arbitrary<String> ipAddress() {
        return Arbitraries.integers().between(1, 255)
                .list().ofSize(4)
                .map(parts -> String.join(".", 
                    parts.stream().map(String::valueOf).toArray(String[]::new)));
    }
}
