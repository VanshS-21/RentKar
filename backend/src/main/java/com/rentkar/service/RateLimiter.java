package com.rentkar.service;

/**
 * Interface for rate limiting AI generation requests
 */
public interface RateLimiter {
    
    /**
     * Check if request is allowed for the given user/IP
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
