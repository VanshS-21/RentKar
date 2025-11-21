import React, { useState } from 'react';
import { format } from 'date-fns';
import StatusBadge from './StatusBadge';
import { Spinner } from './LoadingSkeleton';
import { Button } from './ui/button';

/**
 * @typedef {import('../types/borrowRequest').BorrowRequest} BorrowRequest
 */

/**
 * RequestCard component displays a summary of a borrow request
 * 
 * @param {Object} props
 * @param {BorrowRequest} props.request - The borrow request data
 * @param {'sent' | 'received'} props.viewType - Whether this is borrower or lender view
 * @param {Function} props.onAction - Callback for action buttons (action, requestId)
 * @param {Function} [props.onClick] - Optional callback when card is clicked
 * @returns {JSX.Element}
 */
const RequestCard = ({ request, viewType, onAction, onClick }) => {
  const [processingAction, setProcessingAction] = useState(null);
  const isSentView = viewType === 'sent';
  const otherParty = isSentView ? request.lender : request.borrower;

  const handleAction = async (action, requestId) => {
    setProcessingAction(action);
    try {
      await onAction(action, requestId);
    } finally {
      setProcessingAction(null);
    }
  };

  const formatDate = (dateString) => {
    try {
      return format(new Date(dateString), 'MMM d, yyyy');
    } catch {
      return dateString;
    }
  };

  const getActionButtons = () => {
    const isProcessing = processingAction !== null;

    if (isSentView) {
      // Borrower view actions
      if (request.status === 'PENDING') {
        return (
          <Button
            variant="outline"
            size="sm"
            onClick={(e) => {
              e.stopPropagation();
              handleAction('cancel', request.id);
            }}
            disabled={isProcessing}
            className="text-red-600 hover:text-red-700 transition-colors duration-200"
            aria-label="Cancel this request"
          >
            {processingAction === 'cancel' ? (
              <>
                <Spinner size="sm" className="mr-2" />
                Canceling...
              </>
            ) : (
              'Cancel'
            )}
          </Button>
        );
      }
      if (request.status === 'RETURNED') {
        return (
          <Button
            variant="default"
            size="sm"
            onClick={(e) => {
              e.stopPropagation();
              handleAction('confirm', request.id);
            }}
            disabled={isProcessing}
          >
            {processingAction === 'confirm' ? (
              <>
                <Spinner size="sm" className="mr-2" />
                Confirming...
              </>
            ) : (
              'Confirm Return'
            )}
          </Button>
        );
      }
    } else {
      // Lender view actions
      if (request.status === 'PENDING') {
        return (
          <div className="flex gap-2">
            <Button
              variant="default"
              size="sm"
              onClick={(e) => {
                e.stopPropagation();
                handleAction('approve', request.id);
              }}
              disabled={isProcessing}
            >
              {processingAction === 'approve' ? (
                <>
                  <Spinner size="sm" className="mr-2" />
                  Approving...
                </>
              ) : (
                'Approve'
              )}
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={(e) => {
                e.stopPropagation();
                handleAction('reject', request.id);
              }}
              disabled={isProcessing}
              className="text-red-600 hover:text-red-700"
            >
              {processingAction === 'reject' ? (
                <>
                  <Spinner size="sm" className="mr-2" />
                  Rejecting...
                </>
              ) : (
                'Reject'
              )}
            </Button>
          </div>
        );
      }
      if (request.status === 'APPROVED') {
        return (
          <Button
            variant="default"
            size="sm"
            onClick={(e) => {
              e.stopPropagation();
              handleAction('return', request.id);
            }}
            disabled={isProcessing}
          >
            {processingAction === 'return' ? (
              <>
                <Spinner size="sm" className="mr-2" />
                Processing...
              </>
            ) : (
              'Mark as Returned'
            )}
          </Button>
        );
      }
    }
    return null;
  };

  return (
    <div
      onClick={onClick}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          onClick?.();
        }
      }}
      role="button"
      tabIndex={0}
      aria-label={`View details for ${request.item.title} request`}
      className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-all duration-200 cursor-pointer focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
    >
      <div className="flex flex-col sm:flex-row">
        {/* Item Image */}
        <div className="w-full sm:w-32 h-32 bg-gray-200 flex-shrink-0">
          {request.item.imageUrl ? (
            <img
              src={request.item.imageUrl}
              alt={request.item.title}
              className="w-full h-full object-cover transition-transform duration-200 hover:scale-105"
              loading="lazy"
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-gray-400">
              <svg
                className="w-12 h-12"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                />
              </svg>
            </div>
          )}
        </div>

        {/* Request Details */}
        <div className="flex-1 p-4">
          <div className="flex justify-between items-start mb-2">
            <div>
              <h3 className="font-semibold text-lg truncate">{request.item.title}</h3>
              <p className="text-sm text-gray-600">
                {isSentView ? 'Lender' : 'Borrower'}: {otherParty.fullName}
              </p>
            </div>
            <StatusBadge status={request.status} />
          </div>

          <div className="text-sm text-gray-600 mb-3">
            <div className="flex items-center gap-4">
              <span>
                <span className="font-medium">Borrow:</span> {formatDate(request.borrowDate)}
              </span>
              <span>
                <span className="font-medium">Return:</span> {formatDate(request.returnDate)}
              </span>
            </div>
          </div>

          {request.requestMessage && (
            <p className="text-sm text-gray-700 mb-3 line-clamp-2">
              "{request.requestMessage}"
            </p>
          )}

          <div className="flex justify-end">
            {getActionButtons()}
          </div>
        </div>
      </div>
    </div>
  );
};

export default RequestCard;
