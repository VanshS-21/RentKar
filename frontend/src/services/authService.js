import axiosInstance from '../lib/axios';

/**
 * Authentication service for handling user registration, login, and user data retrieval
 */
const authService = {
  /**
   * Register a new user
   * @param {Object} data - Registration data
   * @param {string} data.username - Username
   * @param {string} data.email - Email address
   * @param {string} data.password - Password
   * @param {string} data.fullName - Full name
   * @param {string} [data.phone] - Phone number (optional)
   * @returns {Promise<Object>} Response with user data
   */
  async register(data) {
    try {
      const response = await axiosInstance.post('/auth/register', data);
      return {
        success: true,
        data: response.data,
        message: 'Registration successful',
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Registration failed. Please try again.',
        status: error.response?.status,
      };
    }
  },

  /**
   * Login with username and password
   * @param {Object} credentials - Login credentials
   * @param {string} credentials.username - Username
   * @param {string} credentials.password - Password
   * @returns {Promise<Object>} Response with token and user data
   */
  async login(credentials) {
    try {
      const response = await axiosInstance.post('/auth/login', credentials);
      
      // Backend returns { success, message, data: { token, type, user } }
      // We need to return the inner data object
      return {
        success: true,
        data: response.data.data, // Extract the inner data object
        message: response.data.message || 'Login successful',
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Login failed. Please check your credentials.',
        status: error.response?.status,
      };
    }
  },

  /**
   * Get current authenticated user
   * @returns {Promise<Object>} Response with user data
   */
  async getCurrentUser() {
    try {
      const response = await axiosInstance.get('/auth/me');
      // Backend returns { success, message, data: user }
      return {
        success: true,
        data: response.data.data, // Extract the inner data object
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to fetch user data.',
        status: error.response?.status,
      };
    }
  },
};

export default authService;
