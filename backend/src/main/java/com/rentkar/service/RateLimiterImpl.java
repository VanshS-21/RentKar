package com.rentkar.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of RateLimiter using sliding window algorithm
 */
@Service
public class RateLimiterImpl implements RateLimiter {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimiterImpl.class);
    
    private final int maxRequestsPerHour;
    private final long windowSizeMs = 3600000; // 1 hour in milliseconds
    
    // Map of userId -> list of request timestamps
    private final Map<String, LinkedList<Long>> requestTimestamps;
    
    public RateLimiterImpl(@Value("${ai.rate-limit.per-hour}") int maxRequestsPerHour) {
        this.maxRequestsPerHour = maxRequestsPerHour;
        this.requestTimestamps = new ConcurrentHashMap<>();
        logger.info("RateLimiter initialized - max requests per hour: {}", maxRequestsPerHour);
    }
    
    @Override
    public boolean allowRequest(String userId) {
        if (userId == null || userId.isEmpty()) {
            logger.warn("Rate limit check called with null or empty userId");
            return false;
        }
        
        long now = Instant.now().toEpochMilli();
        
        // Get or create timestamp list for this user
        LinkedList<Long> timestamps = requestTimestamps.computeIfAbsent(
            userId, 
            k -> new LinkedList<>()
        );
        
        // Synchronized to prevent race conditions
        synchronized (timestamps) {
            // Remove expired timestamps (older than 1 hour)
            cleanupExpiredEntries(timestamps, now);
            
            // Check if user has exceeded rate limit
            if (timestamps.size() >= maxRequestsPerHour) {
                logger.debug("Rate limit exceeded for user: {}", userId);
                return false;
            }
            
            // Add current timestamp
            timestamps.add(now);
            logger.debug("Request allowed for user: {} ({}/{})", 
                userId, timestamps.size(), maxRequestsPerHour);
            return true;
        }
    }
    
    @Override
    public int getRemainingRequests(String userId) {
        if (userId == null || userId.isEmpty()) {
            return 0;
        }
        
        long now = Instant.now().toEpochMilli();
        LinkedList<Long> timestamps = requestTimestamps.get(userId);
        
        if (timestamps == null) {
            return maxRequestsPerHour;
        }
        
        synchronized (timestamps) {
            cleanupExpiredEntries(timestamps, now);
            int remaining = maxRequestsPerHour - timestamps.size();
            return Math.max(0, remaining);
        }
    }
    
    @Override
    public long getResetTime(String userId) {
        if (userId == null || userId.isEmpty()) {
            return 0;
        }
        
        long now = Instant.now().toEpochMilli();
        LinkedList<Long> timestamps = requestTimestamps.get(userId);
        
        if (timestamps == null || timestamps.isEmpty()) {
            return 0;
        }
        
        synchronized (timestamps) {
            cleanupExpiredEntries(timestamps, now);
            
            if (timestamps.isEmpty()) {
                return 0;
            }
            
            // Get the oldest timestamp
            long oldestTimestamp = timestamps.getFirst();
            long resetTime = oldestTimestamp + windowSizeMs;
            long secondsUntilReset = (resetTime - now) / 1000;
            
            return Math.max(0, secondsUntilReset);
        }
    }
    
    /**
     * Remove timestamps older than the sliding window
     */
    private void cleanupExpiredEntries(LinkedList<Long> timestamps, long now) {
        long cutoffTime = now - windowSizeMs;
        
        // Remove all timestamps older than cutoff
        while (!timestamps.isEmpty() && timestamps.getFirst() < cutoffTime) {
            timestamps.removeFirst();
        }
    }
}
