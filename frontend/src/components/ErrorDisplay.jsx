import React from 'react';
import { Button } from './ui/button';

/**
 * ErrorDisplay component for showing user-friendly error messages
 * with retry options for API errors
 * 
 * @param {Object} props
 * @param {string} props.message - Error message to display
 * @param {Function} [props.onRetry] - Optional retry callback
 * @param {string} [props.title] - Optional error title
 * @param {boolean} [props.fullPage] - Whether to display as full page error
 * @returns {JSX.Element}
 */
const ErrorDisplay = ({ 
  message, 
  onRetry, 
  title = 'Error', 
  fullPage = false 
}) => {
  const content = (
    <div className={`text-center ${fullPage ? 'py-12' : 'py-8'}`}>
      <div className="text-6xl mb-4">😕</div>
      <h3 className="text-xl font-semibold mb-2 text-gray-900">{title}</h3>
      <p className="text-gray-600 mb-6 max-w-md mx-auto">
        {message}
      </p>
      {onRetry && (
        <Button onClick={onRetry} size="lg">
          Try Again
        </Button>
      )}
    </div>
  );

  if (fullPage) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center p-4">
        <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-6">
          {content}
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-sm p-6">
      {content}
    </div>
  );
};

export default ErrorDisplay;
