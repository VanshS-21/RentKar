import React from 'react';

/**
 * LoadingSkeleton component for displaying loading states
 * 
 * @param {Object} props
 * @param {'card' | 'list' | 'detail'} [props.type] - Type of skeleton to display
 * @param {number} [props.count] - Number of skeleton items to show
 * @returns {JSX.Element}
 */
const LoadingSkeleton = ({ type = 'card', count = 3 }) => {
  if (type === 'card') {
    return (
      <div className="grid grid-cols-1 gap-4">
        {[...Array(count)].map((_, index) => (
          <div
            key={index}
            className="bg-white rounded-lg shadow-md overflow-hidden animate-pulse"
          >
            <div className="flex">
              <div className="w-32 h-32 bg-gray-200 flex-shrink-0" />
              <div className="flex-1 p-4 space-y-3">
                <div className="h-6 bg-gray-200 rounded w-3/4" />
                <div className="h-4 bg-gray-200 rounded w-1/2" />
                <div className="h-4 bg-gray-200 rounded w-2/3" />
                <div className="flex justify-end gap-2 mt-4">
                  <div className="h-9 bg-gray-200 rounded w-20" />
                  <div className="h-9 bg-gray-200 rounded w-20" />
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (type === 'list') {
    return (
      <div className="space-y-3">
        {[...Array(count)].map((_, index) => (
          <div
            key={index}
            className="bg-white rounded-lg p-4 animate-pulse"
          >
            <div className="h-5 bg-gray-200 rounded w-3/4 mb-2" />
            <div className="h-4 bg-gray-200 rounded w-1/2" />
          </div>
        ))}
      </div>
    );
  }

  if (type === 'detail') {
    return (
      <div className="bg-white rounded-lg shadow-xl p-6 animate-pulse">
        <div className="space-y-6">
          <div className="h-8 bg-gray-200 rounded w-1/2" />
          <div className="h-4 bg-gray-200 rounded w-1/3" />
          <div className="space-y-3">
            <div className="h-4 bg-gray-200 rounded w-full" />
            <div className="h-4 bg-gray-200 rounded w-5/6" />
            <div className="h-4 bg-gray-200 rounded w-4/6" />
          </div>
          <div className="flex gap-4">
            <div className="w-24 h-24 bg-gray-200 rounded" />
            <div className="flex-1 space-y-2">
              <div className="h-5 bg-gray-200 rounded w-3/4" />
              <div className="h-4 bg-gray-200 rounded w-1/2" />
            </div>
          </div>
        </div>
      </div>
    );
  }

  return null;
};

/**
 * ButtonSkeleton component for loading state on buttons
 * 
 * @param {Object} props
 * @param {string} [props.className] - Additional CSS classes
 * @returns {JSX.Element}
 */
export const ButtonSkeleton = ({ className = '' }) => (
  <div className={`h-10 bg-gray-200 rounded animate-pulse ${className}`} />
);

/**
 * Spinner component for inline loading indicators
 * 
 * @param {Object} props
 * @param {'sm' | 'md' | 'lg'} [props.size] - Size of spinner
 * @param {string} [props.className] - Additional CSS classes
 * @returns {JSX.Element}
 */
export const Spinner = ({ size = 'md', className = '' }) => {
  const sizeClasses = {
    sm: 'w-4 h-4',
    md: 'w-6 h-6',
    lg: 'w-8 h-8',
  };

  return (
    <div className={`inline-block ${className}`}>
      <svg
        className={`animate-spin ${sizeClasses[size]}`}
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
      >
        <circle
          className="opacity-25"
          cx="12"
          cy="12"
          r="10"
          stroke="currentColor"
          strokeWidth="4"
        />
        <path
          className="opacity-75"
          fill="currentColor"
          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
        />
      </svg>
    </div>
  );
};

export default LoadingSkeleton;
