import axiosInstance from '../lib/axios';

/**
 * @typedef {import('../types/borrowRequest').BorrowRequest} BorrowRequest
 * @typedef {import('../types/borrowRequest').CreateBorrowRequestDTO} CreateBorrowRequestDTO
 * @typedef {import('../types/borrowRequest').RequestStatistics} RequestStatistics
 * @typedef {import('../types/borrowRequest').RequestStatus} RequestStatus
 */

/**
 * Borrow Request service for handling the complete borrow workflow
 */
const borrowRequestService = {
  /**
   * Create a new borrow request
   * @param {number} itemId - Item ID to borrow
   * @param {CreateBorrowRequestDTO} data - Request data
   * @returns {Promise<{success: boolean, data?: BorrowRequest, message?: string, error?: string, status?: number, validationErrors?: Object}>} Response with created request data
   */
  async createRequest(itemId, data) {
    try {
      const response = await axiosInstance.post(`/requests?itemId=${itemId}`, data);
      return {
        success: true,
        data: response.data,
        message: 'Borrow request created successfully',
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to create borrow request. Please try again.',
        status: error.response?.status,
        validationErrors: error.response?.data?.errors,
      };
    }
  },

  /**
   * Get all requests sent by the current user (borrower view)
   * @param {RequestStatus} [status] - Optional status filter
   * @returns {Promise<{success: boolean, data?: BorrowRequest[], error?: string, status?: number}>} Response with list of sent requests
   */
  async getSentRequests(status = null) {
    try {
      const params = status ? { status } : {};
      const response = await axiosInstance.get('/requests/sent', { params });
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to fetch sent requests.',
        status: error.response?.status,
      };
    }
  },

  /**
   * Get all requests received by the current user (lender view)
   * @param {RequestStatus} [status] - Optional status filter
   * @returns {Promise<{success: boolean, data?: BorrowRequest[], error?: string, status?: number}>} Response with list of received requests
   */
  async getReceivedRequests(status = null) {
    try {
      const params = status ? { status } : {};
      const response = await axiosInstance.get('/requests/received', { params });
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to fetch received requests.',
        status: error.response?.status,
      };
    }
  },

  /**
   * Get request by ID
   * @param {number} requestId - Request ID
   * @returns {Promise<{success: boolean, data?: BorrowRequest, error?: string, status?: number}>} Response with request details
   */
  async getRequestById(requestId) {
    try {
      const response = await axiosInstance.get(`/requests/${requestId}`);
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to fetch request details.',
        status: error.response?.status,
      };
    }
  },

  /**
   * Approve a borrow request (lender only)
   * @param {number} requestId - Request ID
   * @param {string} [responseMessage] - Optional response message
   * @returns {Promise<{success: boolean, data?: BorrowRequest, message?: string, error?: string, status?: number}>} Response with updated request data
   */
  async approveRequest(requestId, responseMessage = null) {
    try {
      const data = responseMessage ? { responseMessage } : {};
      const response = await axiosInstance.post(`/requests/${requestId}/approve`, data);
      return {
        success: true,
        data: response.data,
        message: 'Request approved successfully',
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to approve request.',
        status: error.response?.status,
      };
    }
  },

  /**
   * Reject a borrow request (lender only)
   * @param {number} requestId - Request ID
   * @param {string} [responseMessage] - Optional rejection reason
   * @returns {Promise<{success: boolean, data?: BorrowRequest, message?: string, error?: string, status?: number}>} Response with updated request data
   */
  async rejectRequest(requestId, responseMessage = null) {
    try {
      const data = responseMessage ? { responseMessage } : {};
      const response = await axiosInstance.post(`/requests/${requestId}/reject`, data);
      return {
        success: true,
        data: response.data,
        message: 'Request rejected',
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to reject request.',
        status: error.response?.status,
      };
    }
  },

  /**
   * Mark item as returned (lender only)
   * @param {number} requestId - Request ID
   * @returns {Promise<{success: boolean, data?: BorrowRequest, message?: string, error?: string, status?: number}>} Response with updated request data
   */
  async markAsReturned(requestId) {
    try {
      const response = await axiosInstance.post(`/requests/${requestId}/return`);
      return {
        success: true,
        data: response.data,
        message: 'Item marked as returned',
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to mark item as returned.',
        status: error.response?.status,
      };
    }
  },

  /**
   * Confirm return and complete transaction (borrower only)
   * @param {number} requestId - Request ID
   * @returns {Promise<{success: boolean, data?: BorrowRequest, message?: string, error?: string, status?: number}>} Response with updated request data
   */
  async confirmReturn(requestId) {
    try {
      const response = await axiosInstance.post(`/requests/${requestId}/confirm`);
      return {
        success: true,
        data: response.data,
        message: 'Return confirmed successfully',
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to confirm return.',
        status: error.response?.status,
      };
    }
  },

  /**
   * Cancel a pending request (borrower only)
   * @param {number} requestId - Request ID
   * @returns {Promise<{success: boolean, message?: string, error?: string, status?: number}>} Response with success status
   */
  async cancelRequest(requestId) {
    try {
      await axiosInstance.delete(`/requests/${requestId}`);
      return {
        success: true,
        message: 'Request canceled successfully',
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to cancel request.',
        status: error.response?.status,
      };
    }
  },

  /**
   * Get request statistics for the current user
   * @returns {Promise<{success: boolean, data?: RequestStatistics, error?: string, status?: number}>} Response with statistics data
   */
  async getStatistics() {
    try {
      const response = await axiosInstance.get('/requests/statistics');
      return {
        success: true,
        data: response.data,
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to fetch statistics.',
        status: error.response?.status,
      };
    }
  },
};

export default borrowRequestService;
