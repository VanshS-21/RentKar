/**
 * Cloudinary upload utility with validation and error handling
 */

// Maximum file size: 5MB
const MAX_FILE_SIZE = 5 * 1024 * 1024;

// Allowed image MIME types
const ALLOWED_IMAGE_TYPES = [
  'image/jpeg',
  'image/jpg',
  'image/png',
  'image/gif',
  'image/webp',
];

/**
 * Validate file size
 * @param {File} file - File to validate
 * @returns {Object} Validation result
 */
export const validateFileSize = (file) => {
  if (file.size > MAX_FILE_SIZE) {
    return {
      valid: false,
      error: `File size exceeds 5MB limit. Your file is ${(file.size / 1024 / 1024).toFixed(2)}MB.`,
    };
  }
  return { valid: true };
};

/**
 * Validate file type (images only)
 * @param {File} file - File to validate
 * @returns {Object} Validation result
 */
export const validateFileType = (file) => {
  if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
    return {
      valid: false,
      error: `Invalid file type. Only images (JPEG, PNG, GIF, WebP) are allowed. You uploaded: ${file.type || 'unknown type'}.`,
    };
  }
  return { valid: true };
};

/**
 * Validate image file (size and type)
 * @param {File} file - File to validate
 * @returns {Object} Validation result with error message if invalid
 */
export const validateImageFile = (file) => {
  if (!file) {
    return {
      valid: false,
      error: 'No file provided.',
    };
  }

  // Validate file type first
  const typeValidation = validateFileType(file);
  if (!typeValidation.valid) {
    return typeValidation;
  }

  // Validate file size
  const sizeValidation = validateFileSize(file);
  if (!sizeValidation.valid) {
    return sizeValidation;
  }

  return { valid: true };
};

/**
 * Upload image to Cloudinary via backend API
 * @param {File} file - Image file to upload
 * @param {Function} onProgress - Progress callback (optional)
 * @returns {Promise<Object>} Upload result with URL or error
 */
export const uploadImageToCloudinary = async (file, onProgress = null) => {
  // Validate file before upload
  const validation = validateImageFile(file);
  if (!validation.valid) {
    return {
      success: false,
      error: validation.error,
    };
  }

  try {
    const formData = new FormData();
    formData.append('file', file);

    // Get token from localStorage for authentication
    const token = localStorage.getItem('token');
    if (!token) {
      return {
        success: false,
        error: 'Authentication required. Please log in.',
      };
    }

    // Create XMLHttpRequest for progress tracking
    return new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest();

      // Track upload progress
      if (onProgress) {
        xhr.upload.addEventListener('progress', (event) => {
          if (event.lengthComputable) {
            const percentComplete = Math.round((event.loaded / event.total) * 100);
            onProgress(percentComplete);
          }
        });
      }

      // Handle completion
      xhr.addEventListener('load', () => {
        if (xhr.status >= 200 && xhr.status < 300) {
          try {
            const response = JSON.parse(xhr.responseText);
            resolve({
              success: true,
              data: response,
              url: response.url || response.imageUrl,
            });
          } catch (error) {
            reject({
              success: false,
              error: 'Failed to parse server response.',
            });
          }
        } else {
          try {
            const errorResponse = JSON.parse(xhr.responseText);
            reject({
              success: false,
              error: errorResponse.message || 'Upload failed.',
              status: xhr.status,
            });
          } catch (error) {
            reject({
              success: false,
              error: `Upload failed with status ${xhr.status}.`,
              status: xhr.status,
            });
          }
        }
      });

      // Handle errors
      xhr.addEventListener('error', () => {
        reject({
          success: false,
          error: 'Network error occurred during upload.',
        });
      });

      // Handle abort
      xhr.addEventListener('abort', () => {
        reject({
          success: false,
          error: 'Upload was cancelled.',
        });
      });

      // Configure and send request
      const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
      xhr.open('POST', `${apiBaseUrl}/items/upload-image`);
      xhr.setRequestHeader('Authorization', `Bearer ${token}`);
      xhr.send(formData);
    });
  } catch (error) {
    return {
      success: false,
      error: error.message || 'An unexpected error occurred during upload.',
    };
  }
};

/**
 * Create a preview URL for an image file
 * @param {File} file - Image file
 * @returns {string} Object URL for preview
 */
export const createImagePreview = (file) => {
  return URL.createObjectURL(file);
};

/**
 * Revoke a preview URL to free memory
 * @param {string} url - Object URL to revoke
 */
export const revokeImagePreview = (url) => {
  if (url && url.startsWith('blob:')) {
    URL.revokeObjectURL(url);
  }
};

/**
 * Format file size for display
 * @param {number} bytes - File size in bytes
 * @returns {string} Formatted file size
 */
export const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
};

export default {
  validateFileSize,
  validateFileType,
  validateImageFile,
  uploadImageToCloudinary,
  createImagePreview,
  revokeImagePreview,
  formatFileSize,
  MAX_FILE_SIZE,
  ALLOWED_IMAGE_TYPES,
};
