import axiosInstance from '../lib/axios';

/**
 * Item service for handling item CRUD operations, search, and filtering
 */
const itemService = {
  /**
   * Create a new item
   * @param {Object} data - Item data
   * @param {string} data.title - Item title (3-200 characters)
   * @param {string} data.description - Item description
   * @param {string} data.category - Item category
   * @param {string} data.imageUrl - Cloudinary image URL
   * @returns {Promise<Object>} Response with created item data
   */
  async createItem(data) {
    try {
      const response = await axiosInstance.post('/items', data);
      return {
        success: true,
        data: response.data,
        message: 'Item created successfully',
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to create item. Please try again.',
        status: error.response?.status,
        validationErrors: error.response?.data?.errors,
      };
    }
  },

  /**
   * Get items with optional filters and pagination
   * @param {Object} params - Query parameters
   * @param {string} [params.search] - Search keyword for title/description
   * @param {string} [params.category] - Filter by category
   * @param {string} [params.status] - Filter by status (AVAILABLE, BORROWED, UNAVAILABLE)
   * @param {number} [params.page=0] - Page number (0-indexed)
   * @param {number} [params.size=10] - Page size
   * @returns {Promise<Object>} Response with paginated items
   */
  async getItems(params = {}) {
    try {
      const response = await axiosInstance.get('/items', { params });
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to fetch items.',
        status: error.response?.status,
      };
    }
  },

  /**
   * Get item by ID
   * @param {number} itemId - Item ID
   * @returns {Promise<Object>} Response with item details
   */
  async getItemById(itemId) {
    try {
      const response = await axiosInstance.get(`/items/${itemId}`);
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to fetch item details.',
        status: error.response?.status,
      };
    }
  },

  /**
   * Update an item (owner only)
   * @param {number} itemId - Item ID
   * @param {Object} data - Updated item data
   * @param {string} [data.title] - Updated title
   * @param {string} [data.description] - Updated description
   * @param {string} [data.category] - Updated category
   * @param {string} [data.status] - Updated status
   * @param {string} [data.imageUrl] - Updated image URL
   * @returns {Promise<Object>} Response with updated item data
   */
  async updateItem(itemId, data) {
    try {
      const response = await axiosInstance.put(`/items/${itemId}`, data);
      return {
        success: true,
        data: response.data,
        message: 'Item updated successfully',
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to update item.',
        status: error.response?.status,
        validationErrors: error.response?.data?.errors,
      };
    }
  },

  /**
   * Delete an item (owner only)
   * @param {number} itemId - Item ID
   * @returns {Promise<Object>} Response with success status
   */
  async deleteItem(itemId) {
    try {
      await axiosInstance.delete(`/items/${itemId}`);
      return {
        success: true,
        message: 'Item deleted successfully',
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to delete item.',
        status: error.response?.status,
      };
    }
  },

  /**
   * Upload image to Cloudinary via backend
   * @param {File} file - Image file to upload
   * @returns {Promise<Object>} Response with Cloudinary URL
   */
  async uploadImage(file) {
    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await axiosInstance.post('/items/upload-image', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      return {
        success: true,
        data: response.data,
        message: 'Image uploaded successfully',
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to upload image.',
        status: error.response?.status,
      };
    }
  },

  /**
   * Get current user's items
   * @param {Object} params - Query parameters
   * @param {number} [params.page=0] - Page number (0-indexed)
   * @param {number} [params.size=10] - Page size
   * @returns {Promise<Object>} Response with paginated user items
   */
  async getMyItems(params = {}) {
    try {
      const response = await axiosInstance.get('/items/my-items', { params });
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to fetch your items.',
        status: error.response?.status,
      };
    }
  },

  /**
   * Generate AI title for an item
   * @param {Object} data - Item data for title generation
   * @param {string} data.itemName - Item name
   * @param {string} data.category - Item category
   * @param {string} [data.additionalInfo] - Additional context
   * @param {string} [data.condition] - Item condition
   * @param {string} [data.specifications] - Item specifications
   * @returns {Promise<Object>} Response with generated title
   */
  async generateTitle(data) {
    try {
      const response = await axiosInstance.post('/items/generate-title', data, {
        timeout: 30000, // 30 second timeout
      });
      // Backend returns { success, message, data: { content, tokenCount, ... } }
      // On success, extract the inner data object
      if (response.data.success && response.data.data) {
        return {
          success: true,
          data: response.data.data,
          message: response.data.message || 'Title generated successfully',
        };
      }
      // Handle error response from backend
      return {
        success: false,
        error: response.data.message || 'Failed to generate title',
      };
    } catch (error) {
      // Handle timeout errors
      if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
        return {
          success: false,
          error: 'The AI service took too long to respond. Please try again in a moment.',
          status: 408,
          isTimeout: true,
        };
      }

      // Handle rate limit errors
      if (error.response?.status === 429) {
        const retryAfter = error.response.headers['retry-after'];
        return {
          success: false,
          error: error.response?.data?.message || 'You\'ve reached the generation limit. Please wait before trying again.',
          status: 429,
          retryAfter: retryAfter ? parseInt(retryAfter) : null,
          isRateLimited: true,
        };
      }

      // Handle authentication/authorization errors
      if (error.response?.status === 401 || error.response?.status === 403) {
        return {
          success: false,
          error: 'Authentication failed. Please log in again.',
          status: error.response.status,
        };
      }

      // Handle validation errors
      if (error.response?.status === 400) {
        return {
          success: false,
          error: error.response?.data?.message || 'Invalid request. Please check your input and try again.',
          status: 400,
          validationErrors: error.response?.data?.errors,
        };
      }

      // Handle service unavailable errors
      if (error.response?.status === 503) {
        return {
          success: false,
          error: 'AI generation is temporarily unavailable. Please try again later.',
          status: 503,
        };
      }

      // Handle network errors
      if (!error.response) {
        return {
          success: false,
          error: 'Unable to connect to the server. Please check your internet connection.',
          status: 0,
        };
      }

      // Handle other API errors
      return {
        success: false,
        error: error.response?.data?.message || 'An unexpected error occurred. Please try again.',
        status: error.response?.status,
        validationErrors: error.response?.data?.errors,
      };
    }
  },

  /**
   * Check if AI generation is available
   * @returns {Promise<Object>} Response with availability status
   */
  async checkAIAvailability() {
    try {
      const response = await axiosInstance.get('/items/ai-available');
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        available: false,
        error: error.response?.data?.message || 'Failed to check AI availability.',
        status: error.response?.status,
      };
    }
  },

  /**
   * Generate AI description for an item
   * @param {Object} data - Item data for description generation
   * @param {string} data.itemName - Item name
   * @param {string} data.category - Item category
   * @param {string} [data.additionalInfo] - Additional context
   * @param {string} [data.condition] - Item condition
   * @param {string} [data.specifications] - Item specifications
   * @returns {Promise<Object>} Response with generated description
   */
  async generateDescription(data) {
    try {
      const response = await axiosInstance.post('/items/generate-description', data, {
        timeout: 30000, // 30 second timeout
      });
      // Backend returns { success, message, data: { content, tokenCount, ... } }
      // On success, extract the inner data object
      if (response.data.success && response.data.data) {
        return {
          success: true,
          data: response.data.data,
          message: response.data.message || 'Description generated successfully',
        };
      }
      // Handle error response from backend
      return {
        success: false,
        error: response.data.message || 'Failed to generate description',
      };
    } catch (error) {
      // Handle timeout errors
      if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
        return {
          success: false,
          error: 'The AI service took too long to respond. Please try again in a moment.',
          status: 408,
          isTimeout: true,
        };
      }

      // Handle rate limit errors
      if (error.response?.status === 429) {
        const retryAfter = error.response.headers['retry-after'];
        return {
          success: false,
          error: error.response?.data?.message || 'You\'ve reached the generation limit. Please wait before trying again.',
          status: 429,
          retryAfter: retryAfter ? parseInt(retryAfter) : null,
          isRateLimited: true,
        };
      }

      // Handle authentication/authorization errors
      if (error.response?.status === 401 || error.response?.status === 403) {
        return {
          success: false,
          error: 'Authentication failed. Please log in again.',
          status: error.response.status,
        };
      }

      // Handle validation errors
      if (error.response?.status === 400) {
        return {
          success: false,
          error: error.response?.data?.message || 'Invalid request. Please check your input and try again.',
          status: 400,
          validationErrors: error.response?.data?.errors,
        };
      }

      // Handle service unavailable errors
      if (error.response?.status === 503) {
        return {
          success: false,
          error: 'AI generation is temporarily unavailable. Please try again later.',
          status: 503,
        };
      }

      // Handle network errors
      if (!error.response) {
        return {
          success: false,
          error: 'Unable to connect to the server. Please check your internet connection.',
          status: 0,
        };
      }

      // Handle other API errors
      return {
        success: false,
        error: error.response?.data?.message || 'An unexpected error occurred. Please try again.',
        status: error.response?.status,
        validationErrors: error.response?.data?.errors,
      };
    }
  },
};

export default itemService;
