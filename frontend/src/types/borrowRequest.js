/**
 * @file Type definitions for Borrow Request entities
 * These JSDoc type definitions provide type safety and IDE autocomplete for JavaScript
 */

/**
 * Request status enum values
 * @typedef {'PENDING' | 'APPROVED' | 'REJECTED' | 'RETURNED' | 'COMPLETED'} RequestStatus
 */

/**
 * User information DTO
 * @typedef {Object} UserDTO
 * @property {number} id - User ID
 * @property {string} username - Username
 * @property {string} email - Email address
 * @property {string} fullName - Full name
 * @property {string} [phone] - Phone number (optional)
 */

/**
 * Item owner information DTO
 * @typedef {Object} ItemOwnerDTO
 * @property {number} id - User ID
 * @property {string} username - Username
 * @property {string} fullName - Full name
 */

/**
 * Item information DTO
 * @typedef {Object} ItemDTO
 * @property {number} id - Item ID
 * @property {string} title - Item title
 * @property {string} description - Item description
 * @property {string} category - Item category
 * @property {string} imageUrl - Cloudinary image URL
 * @property {string} status - Item status (AVAILABLE, BORROWED, UNAVAILABLE)
 * @property {ItemOwnerDTO} owner - Item owner information
 */

/**
 * Borrow Request entity
 * @typedef {Object} BorrowRequest
 * @property {number} id - Request ID
 * @property {ItemDTO} item - Item being requested
 * @property {UserDTO} borrower - User requesting to borrow
 * @property {UserDTO} lender - Item owner (lender)
 * @property {RequestStatus} status - Current request status
 * @property {string} [requestMessage] - Optional message from borrower
 * @property {string} [responseMessage] - Optional response from lender
 * @property {string} borrowDate - Borrow date (YYYY-MM-DD)
 * @property {string} returnDate - Return date (YYYY-MM-DD)
 * @property {string} [returnedAt] - Timestamp when marked as returned (ISO 8601)
 * @property {string} [completedAt] - Timestamp when completed (ISO 8601)
 * @property {string} createdAt - Creation timestamp (ISO 8601)
 * @property {string} updatedAt - Last update timestamp (ISO 8601)
 */

/**
 * Create Borrow Request DTO
 * @typedef {Object} CreateBorrowRequestDTO
 * @property {string} borrowDate - Borrow date (YYYY-MM-DD)
 * @property {string} returnDate - Return date (YYYY-MM-DD)
 * @property {string} [requestMessage] - Optional message to lender (max 500 chars)
 */

/**
 * Request Statistics DTO
 * @typedef {Object} RequestStatistics
 * @property {number} pendingCount - Count of pending requests
 * @property {number} approvedCount - Count of approved requests
 * @property {number} rejectedCount - Count of rejected requests
 * @property {number} returnedCount - Count of returned requests
 * @property {number} completedCount - Count of completed requests
 * @property {number} totalSent - Total requests sent by user
 * @property {number} totalReceived - Total requests received by user
 */

// Export empty object to make this a module
export {};
