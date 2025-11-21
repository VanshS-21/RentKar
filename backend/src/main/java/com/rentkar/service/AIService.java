package com.rentkar.service;

import com.rentkar.dto.AIGenerationRequest;
import com.rentkar.dto.AIGenerationResponse;

/**
 * Service interface for AI-powered content generation using Google Gemini API
 */
public interface AIService {
    
    /**
     * Generate an item title using AI
     * @param request Generation request with item details
     * @param userId User ID for rate limiting (null for IP-based)
     * @return Generated title response
     */
    AIGenerationResponse generateTitle(AIGenerationRequest request, String userId);
    
    /**
     * Generate an item description using AI
     * @param request Generation request with item details
     * @param userId User ID for rate limiting (null for IP-based)
     * @return Generated description response
     */
    AIGenerationResponse generateDescription(AIGenerationRequest request, String userId);
    
    /**
     * Check if AI generation is available
     * @return true if API is configured and available
     */
    boolean isAvailable();
    
    /**
     * Get remaining requests for user
     * @param userId User ID or IP address
     * @return number of remaining requests
     */
    int getRemainingRequests(String userId);
}
