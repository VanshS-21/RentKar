import { useState, useCallback } from 'react';
import itemService from '../services/itemService';

/**
 * Custom hook for AI content generation
 * @param {string} type - Type of content to generate ('title' or 'description')
 * @param {Object} itemData - Item data for generation
 * @param {Function} onGenerated - Callback when content is generated
 * @returns {Object} Hook state and functions
 */
const useAIGeneration = (type, itemData, onGenerated) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [remainingRequests, setRemainingRequests] = useState(null);
  const [retryAfter, setRetryAfter] = useState(null);
  const [success, setSuccess] = useState(false);

  /**
   * Generate content using AI
   */
  const generate = useCallback(async () => {
    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      // Prepare request data
      const requestData = {
        itemName: itemData.itemName || itemData.title || '',
        category: itemData.category || '',
        additionalInfo: itemData.additionalInfo || '',
        condition: itemData.condition || '',
        specifications: itemData.specifications || '',
      };

      // Call appropriate service method
      const result = type === 'title' 
        ? await itemService.generateTitle(requestData)
        : await itemService.generateDescription(requestData);

      if (result.success) {
        // Extract generated content from response
        const content = result.data?.content || '';
        
        // Update remaining requests if available
        if (result.data?.remainingRequests !== undefined) {
          setRemainingRequests(result.data.remainingRequests);
        }

        // Call the callback with generated content
        if (onGenerated) {
          onGenerated(content);
        }

        // Show success state
        setSuccess(true);
        
        // Auto-hide success message after 3 seconds
        setTimeout(() => {
          setSuccess(false);
        }, 3000);

        return { success: true, content };
      } else {
        // Handle errors
        setError(result.error);

        // Handle rate limiting
        if (result.isRateLimited) {
          setRetryAfter(result.retryAfter);
        }

        return { success: false, error: result.error };
      }
    } catch (err) {
      const errorMessage = 'An unexpected error occurred. Please try again.';
      setError(errorMessage);
      return { success: false, error: errorMessage };
    } finally {
      setLoading(false);
    }
  }, [type, itemData, onGenerated]);

  /**
   * Regenerate content (same as generate but can be called multiple times)
   */
  const regenerate = useCallback(async () => {
    return await generate();
  }, [generate]);

  /**
   * Clear error state
   */
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  /**
   * Check if rate limited
   */
  const isRateLimited = retryAfter !== null && retryAfter > 0;

  return {
    generate,
    regenerate,
    loading,
    error,
    success,
    remainingRequests,
    retryAfter,
    isRateLimited,
    clearError,
  };
};

export default useAIGeneration;
