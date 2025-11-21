import React, { useEffect, useState } from 'react';
import { format } from 'date-fns';
import StatusBadge from './StatusBadge';
import { Spinner } from './LoadingSkeleton';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';

/**
 * @typedef {import('../types/borrowRequest').BorrowRequest} BorrowRequest
 */

/**
 * RequestDetailModal component displays full request information with action buttons
 * 
 * @param {Object} props
 * @param {boolean} props.isOpen - Whether modal is open
 * @param {Function} props.onClose - Callback to close modal
 * @param {BorrowRequest} props.request - The borrow request data
 * @param {'sent' | 'received'} props.viewType - Whether this is borrower or lender view
 * @param {Function} props.onAction - Callback for actions (action, requestId, data)
 * @param {boolean} [props.isProcessing] - Whether an action is being processed
 * @returns {JSX.Element}
 */
const RequestDetailModal = ({ isOpen, onClose, request, viewType, onAction, isProcessing = false }) => {
  const [responseMessage, setResponseMessage] = useState('');
  const [showMessageInput, setShowMessageInput] = useState(false);
  const [currentAction, setCurrentAction] = useState(null);

  const isSentView = viewType === 'sent';
  const otherParty = isSentView ? request.lender : request.borrower;

  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === 'Escape' && isOpen && !isProcessing) {
        onClose();
      }
    };

    if (isOpen) {
      document.addEventListener('keydown', handleEscape);
      document.body.style.overflow = 'hidden';
    }

    return () => {
      document.removeEventListener('keydown', handleEscape);
      document.body.style.overflow = 'unset';
    };
  }, [isOpen, isProcessing, onClose]);

  const formatDateTime = (dateTimeString) => {
    try {
      return format(new Date(dateTimeString), 'MMM d, yyyy h:mm a');
    } catch {
      return dateTimeString;
    }
  };

  const formatDate = (dateString) => {
    try {
      return format(new Date(dateString), 'MMMM d, yyyy');
    } catch {
      return dateString;
    }
  };

  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget && !isProcessing) {
      onClose();
    }
  };

  const handleActionClick = (action) => {
    if (action === 'approve' || action === 'reject') {
      setCurrentAction(action);
      setShowMessageInput(true);
    } else {
      onAction(action, request.id);
    }
  };

  const handleSubmitWithMessage = () => {
    if (currentAction) {
      onAction(currentAction, request.id, { responseMessage: responseMessage || null });
      setShowMessageInput(false);
      setResponseMessage('');
      setCurrentAction(null);
    }
  };

  const getActionButtons = () => {
    if (showMessageInput) {
      return (
        <div className="space-y-3">
          <div>
            <Label htmlFor="responseMessage">
              {currentAction === 'approve' ? 'Response Message (Optional)' : 'Rejection Reason (Optional)'}
            </Label>
            <Input
              id="responseMessage"
              value={responseMessage}
              onChange={(e) => setResponseMessage(e.target.value)}
              placeholder={currentAction === 'approve' ? 'Add a message...' : 'Reason for rejection...'}
              maxLength={500}
              disabled={isProcessing}
            />
          </div>
          <div className="flex gap-2">
            <Button
              variant="outline"
              onClick={() => {
                setShowMessageInput(false);
                setResponseMessage('');
                setCurrentAction(null);
              }}
              disabled={isProcessing}
            >
              Cancel
            </Button>
            <Button
              variant={currentAction === 'approve' ? 'default' : 'destructive'}
              onClick={handleSubmitWithMessage}
              disabled={isProcessing}
            >
              {isProcessing ? (
                <>
                  <Spinner size="sm" className="mr-2" />
                  Processing...
                </>
              ) : (
                currentAction === 'approve' ? 'Approve Request' : 'Reject Request'
              )}
            </Button>
          </div>
        </div>
      );
    }

    if (isSentView) {
      // Borrower view actions
      if (request.status === 'PENDING') {
        return (
          <Button
            variant="destructive"
            onClick={() => handleActionClick('cancel')}
            disabled={isProcessing}
          >
            {isProcessing ? (
              <>
                <Spinner size="sm" className="mr-2" />
                Canceling...
              </>
            ) : (
              'Cancel Request'
            )}
          </Button>
        );
      }
      if (request.status === 'RETURNED') {
        return (
          <Button
            variant="default"
            onClick={() => handleActionClick('confirm')}
            disabled={isProcessing}
          >
            {isProcessing ? (
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
              onClick={() => handleActionClick('approve')}
              disabled={isProcessing}
            >
              {isProcessing ? (
                <>
                  <Spinner size="sm" className="mr-2" />
                  Processing...
                </>
              ) : (
                'Approve'
              )}
            </Button>
            <Button
              variant="destructive"
              onClick={() => handleActionClick('reject')}
              disabled={isProcessing}
            >
              {isProcessing ? (
                <>
                  <Spinner size="sm" className="mr-2" />
                  Processing...
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
            onClick={() => handleActionClick('return')}
            disabled={isProcessing}
          >
            {isProcessing ? (
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

  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4 overflow-y-auto transition-opacity duration-200"
      onClick={handleBackdropClick}
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-title"
    >
      <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full my-8 transform transition-all duration-200 ease-out scale-100 opacity-100">
        {/* Header */}
        <div className="flex justify-between items-start p-6 border-b">
          <div>
            <h2 id="modal-title" className="text-2xl font-semibold text-gray-900">Request Details</h2>
            <p className="text-sm text-gray-500 mt-1">Created {formatDateTime(request.createdAt)}</p>
          </div>
          <button
            onClick={onClose}
            disabled={isProcessing}
            className="text-gray-400 hover:text-gray-600 disabled:opacity-50 transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 rounded"
            aria-label="Close modal"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6">
          {/* Status */}
          <div>
            <h3 className="text-sm font-medium text-gray-700 mb-2">Status</h3>
            <StatusBadge status={request.status} />
          </div>

          {/* Item Information */}
          <div>
            <h3 className="text-sm font-medium text-gray-700 mb-2">Item</h3>
            <div className="flex gap-4 bg-gray-50 p-4 rounded-lg">
              <div className="w-24 h-24 bg-gray-200 rounded flex-shrink-0">
                {request.item.imageUrl ? (
                  <img
                    src={request.item.imageUrl}
                    alt={request.item.title}
                    className="w-full h-full object-cover rounded"
                  />
                ) : (
                  <div className="w-full h-full flex items-center justify-center text-gray-400">
                    <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                  </div>
                )}
              </div>
              <div>
                <h4 className="font-semibold text-lg">{request.item.title}</h4>
                <p className="text-sm text-gray-600">{request.item.category}</p>
                <p className="text-sm text-gray-500 mt-1">Owner: {request.item.owner.fullName}</p>
              </div>
            </div>
          </div>

          {/* Parties */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <h3 className="text-sm font-medium text-gray-700 mb-2">Borrower</h3>
              <div className="bg-gray-50 p-3 rounded-lg">
                <p className="font-medium">{request.borrower.fullName}</p>
                <p className="text-sm text-gray-600">{request.borrower.username}</p>
                {request.status === 'APPROVED' && !isSentView && request.borrower.email && (
                  <p className="text-sm text-gray-600 mt-1">{request.borrower.email}</p>
                )}
                {request.status === 'APPROVED' && !isSentView && request.borrower.phone && (
                  <p className="text-sm text-gray-600">{request.borrower.phone}</p>
                )}
              </div>
            </div>
            <div>
              <h3 className="text-sm font-medium text-gray-700 mb-2">Lender</h3>
              <div className="bg-gray-50 p-3 rounded-lg">
                <p className="font-medium">{request.lender.fullName}</p>
                <p className="text-sm text-gray-600">{request.lender.username}</p>
                {request.status === 'APPROVED' && isSentView && request.lender.email && (
                  <p className="text-sm text-gray-600 mt-1">{request.lender.email}</p>
                )}
                {request.status === 'APPROVED' && isSentView && request.lender.phone && (
                  <p className="text-sm text-gray-600">{request.lender.phone}</p>
                )}
              </div>
            </div>
          </div>

          {/* Dates */}
          <div>
            <h3 className="text-sm font-medium text-gray-700 mb-2">Borrow Period</h3>
            <div className="bg-gray-50 p-3 rounded-lg">
              <div className="flex justify-between">
                <span className="text-sm text-gray-600">Borrow Date:</span>
                <span className="text-sm font-medium">{formatDate(request.borrowDate)}</span>
              </div>
              <div className="flex justify-between mt-1">
                <span className="text-sm text-gray-600">Return Date:</span>
                <span className="text-sm font-medium">{formatDate(request.returnDate)}</span>
              </div>
            </div>
          </div>

          {/* Messages */}
          {request.requestMessage && (
            <div>
              <h3 className="text-sm font-medium text-gray-700 mb-2">Request Message</h3>
              <div className="bg-blue-50 p-3 rounded-lg">
                <p className="text-sm text-gray-700">{request.requestMessage}</p>
              </div>
            </div>
          )}

          {request.responseMessage && (
            <div>
              <h3 className="text-sm font-medium text-gray-700 mb-2">Response Message</h3>
              <div className="bg-green-50 p-3 rounded-lg">
                <p className="text-sm text-gray-700">{request.responseMessage}</p>
              </div>
            </div>
          )}

          {/* Status History */}
          <div>
            <h3 className="text-sm font-medium text-gray-700 mb-2">Status History</h3>
            <div className="space-y-2">
              <div className="flex items-center text-sm">
                <div className="w-2 h-2 bg-gray-400 rounded-full mr-2"></div>
                <span className="text-gray-600">Created:</span>
                <span className="ml-2 font-medium">{formatDateTime(request.createdAt)}</span>
              </div>
              {request.status !== 'PENDING' && (
                <div className="flex items-center text-sm">
                  <div className="w-2 h-2 bg-gray-400 rounded-full mr-2"></div>
                  <span className="text-gray-600">Last Updated:</span>
                  <span className="ml-2 font-medium">{formatDateTime(request.updatedAt)}</span>
                </div>
              )}
              {request.returnedAt && (
                <div className="flex items-center text-sm">
                  <div className="w-2 h-2 bg-blue-400 rounded-full mr-2"></div>
                  <span className="text-gray-600">Returned:</span>
                  <span className="ml-2 font-medium">{formatDateTime(request.returnedAt)}</span>
                </div>
              )}
              {request.completedAt && (
                <div className="flex items-center text-sm">
                  <div className="w-2 h-2 bg-green-400 rounded-full mr-2"></div>
                  <span className="text-gray-600">Completed:</span>
                  <span className="ml-2 font-medium">{formatDateTime(request.completedAt)}</span>
                </div>
              )}
            </div>
          </div>

          {/* Action Buttons */}
          {getActionButtons()}
        </div>
      </div>
    </div>
  );
};

export default RequestDetailModal;
