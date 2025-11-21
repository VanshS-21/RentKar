import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navigation from '../components/Navigation';
import RequestCard from '../components/RequestCard';
import RequestDetailModal from '../components/RequestDetailModal';
import ErrorBoundary from '../components/ErrorBoundary';
import ErrorDisplay from '../components/ErrorDisplay';
import LoadingSkeleton from '../components/LoadingSkeleton';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import borrowRequestService from '../services/borrowRequestService';
import toast from 'react-hot-toast';

/**
 * @typedef {import('../types/borrowRequest').BorrowRequest} BorrowRequest
 * @typedef {import('../types/borrowRequest').RequestStatistics} RequestStatistics
 * @typedef {import('../types/borrowRequest').RequestStatus} RequestStatus
 */

/**
 * IncomingRequestsPage component - Lender view of received borrow requests
 * Requirements: 3.1, 3.2, 3.3, 3.4
 */
const IncomingRequestsPage = () => {
  const navigate = useNavigate();
  
  /** @type {[BorrowRequest[], Function]} */
  const [requests, setRequests] = useState([]);
  
  /** @type {[RequestStatus | 'ALL', Function]} */
  const [filter, setFilter] = useState('ALL');
  
  const [loading, setLoading] = useState(true);
  
  /** @type {[RequestStatistics | null, Function]} */
  const [statistics, setStatistics] = useState(null);
  
  /** @type {[BorrowRequest | null, Function]} */
  const [selectedRequest, setSelectedRequest] = useState(null);
  
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [showActionDialog, setShowActionDialog] = useState(false);
  const [currentAction, setCurrentAction] = useState(null);
  const [responseMessage, setResponseMessage] = useState('');
  
  const [isProcessing, setIsProcessing] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchRequests();
    fetchStatistics();
  }, [filter]);

  const fetchRequests = async () => {
    setLoading(true);
    setError(null);
    const statusFilter = filter === 'ALL' ? null : filter;
    const result = await borrowRequestService.getReceivedRequests(statusFilter);

    if (result.success) {
      setRequests(result.data || []);
    } else {
      setError(result.error || 'Failed to load requests');
    }
    setLoading(false);
  };

  const fetchStatistics = async () => {
    const result = await borrowRequestService.getStatistics();
    if (result.success) {
      setStatistics(result.data);
    }
  };

  const handleCardClick = (request) => {
    setSelectedRequest(request);
    setShowDetailModal(true);
  };

  const handleAction = async (action, requestId, data = null) => {
    // If action requires a dialog (approve/reject), show it
    if ((action === 'approve' || action === 'reject') && !data) {
      const request = requests.find(r => r.id === requestId);
      setSelectedRequest(request);
      setCurrentAction(action);
      setShowActionDialog(true);
      return;
    }

    setIsProcessing(true);

    // Store previous state for rollback
    const previousRequests = [...requests];
    const previousSelectedRequest = selectedRequest;
    const previousStatistics = statistics;

    try {
      let result;
      const requestToUpdate = requests.find(req => req.id === requestId);
      
      // Optimistic update: Update UI immediately
      if (requestToUpdate) {
        let optimisticRequest = { ...requestToUpdate };
        
        if (action === 'approve') {
          optimisticRequest.status = 'APPROVED';
          optimisticRequest.responseMessage = data?.responseMessage || null;
          optimisticRequest.updatedAt = new Date().toISOString();
        } else if (action === 'reject') {
          optimisticRequest.status = 'REJECTED';
          optimisticRequest.responseMessage = data?.responseMessage || null;
          optimisticRequest.updatedAt = new Date().toISOString();
        } else if (action === 'return') {
          optimisticRequest.status = 'RETURNED';
          optimisticRequest.returnedAt = new Date().toISOString();
          optimisticRequest.updatedAt = new Date().toISOString();
        }
        
        setRequests(prevRequests =>
          prevRequests.map(req =>
            req.id === requestId ? optimisticRequest : req
          )
        );
        
        if (selectedRequest && selectedRequest.id === requestId) {
          setSelectedRequest(optimisticRequest);
        }

        // Update statistics optimistically
        if (statistics) {
          const oldStatusKey = `${requestToUpdate.status.toLowerCase()}Count`;
          const newStatusKey = `${optimisticRequest.status.toLowerCase()}Count`;
          setStatistics({
            ...statistics,
            [oldStatusKey]: Math.max(0, (statistics[oldStatusKey] || 0) - 1),
            [newStatusKey]: (statistics[newStatusKey] || 0) + 1,
          });
        }
      }
      
      if (action === 'approve') {
        result = await borrowRequestService.approveRequest(requestId, data?.responseMessage);
      } else if (action === 'reject') {
        result = await borrowRequestService.rejectRequest(requestId, data?.responseMessage);
      } else if (action === 'return') {
        result = await borrowRequestService.markAsReturned(requestId);
      }

      if (result && result.success) {
        // Show specific toast notification based on action (Requirements: 14.2, 14.3, 14.4)
        if (action === 'approve') {
          toast.success(`✅ Request approved! ${result.data.borrower.fullName} can now borrow your item.`);
        } else if (action === 'reject') {
          toast.success(`❌ Request rejected. The item remains available.`);
        } else if (action === 'return') {
          toast.success(`📦 Item marked as returned. Waiting for borrower confirmation.`);
        } else {
          toast.success(result.message || 'Action completed successfully');
        }
        
        // Update with actual server data
        setRequests(prevRequests =>
          prevRequests.map(req =>
            req.id === requestId ? result.data : req
          )
        );
        
        if (selectedRequest && selectedRequest.id === requestId) {
          setSelectedRequest(result.data);
        }
        
        // Close action dialog
        setShowActionDialog(false);
        setCurrentAction(null);
        setResponseMessage('');
        
        // Refresh statistics with actual data
        fetchStatistics();
      } else if (result) {
        // Rollback on error
        setRequests(previousRequests);
        setSelectedRequest(previousSelectedRequest);
        setStatistics(previousStatistics);
        toast.error(result.error || 'Action failed');
      }
    } catch (error) {
      // Rollback on error
      setRequests(previousRequests);
      setSelectedRequest(previousSelectedRequest);
      setStatistics(previousStatistics);
      toast.error('An unexpected error occurred');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleDialogSubmit = () => {
    if (selectedRequest && currentAction) {
      handleAction(currentAction, selectedRequest.id, { responseMessage: responseMessage || null });
    }
  };

  const getFilteredCount = (status) => {
    if (!statistics) return 0;
    
    switch (status) {
      case 'PENDING':
        return statistics.pendingCount;
      case 'APPROVED':
        return statistics.approvedCount;
      case 'REJECTED':
        return statistics.rejectedCount;
      case 'RETURNED':
        return statistics.returnedCount;
      case 'COMPLETED':
        return statistics.completedCount;
      default:
        return statistics.totalReceived;
    }
  };

  return (
    <ErrorBoundary onReset={() => window.location.reload()}>
      <div className="min-h-screen bg-background">
        <Navigation />
      
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold mb-2">Incoming Requests</h1>
          <p className="text-muted-foreground">
            Manage borrow requests for your items
          </p>
        </div>

        {/* Statistics */}
        {statistics && (
          <div className="grid grid-cols-2 md:grid-cols-6 gap-4 mb-8">
            <div className="bg-white p-4 rounded-lg shadow-sm border transition-all duration-200 hover:shadow-md">
              <div className="text-2xl font-bold text-gray-900">{statistics.totalReceived}</div>
              <div className="text-sm text-gray-600">Total Received</div>
            </div>
            <div className="bg-yellow-50 p-4 rounded-lg shadow-sm border border-yellow-200 transition-all duration-200 hover:shadow-md">
              <div className="text-2xl font-bold text-yellow-800">{statistics.pendingCount}</div>
              <div className="text-sm text-yellow-700">Pending</div>
            </div>
            <div className="bg-green-50 p-4 rounded-lg shadow-sm border border-green-200 transition-all duration-200 hover:shadow-md">
              <div className="text-2xl font-bold text-green-800">{statistics.approvedCount}</div>
              <div className="text-sm text-green-700">Approved</div>
            </div>
            <div className="bg-red-50 p-4 rounded-lg shadow-sm border border-red-200 transition-all duration-200 hover:shadow-md">
              <div className="text-2xl font-bold text-red-800">{statistics.rejectedCount}</div>
              <div className="text-sm text-red-700">Rejected</div>
            </div>
            <div className="bg-blue-50 p-4 rounded-lg shadow-sm border border-blue-200 transition-all duration-200 hover:shadow-md">
              <div className="text-2xl font-bold text-blue-800">{statistics.returnedCount}</div>
              <div className="text-sm text-blue-700">Returned</div>
            </div>
            <div className="bg-gray-50 p-4 rounded-lg shadow-sm border border-gray-200 transition-all duration-200 hover:shadow-md">
              <div className="text-2xl font-bold text-gray-800">{statistics.completedCount}</div>
              <div className="text-sm text-gray-700">Completed</div>
            </div>
          </div>
        )}

        {/* Filter Dropdown */}
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
          <div className="flex items-center gap-2">
            <label htmlFor="status-filter" className="text-sm font-medium text-gray-700">
              Filter by status:
            </label>
            <select
              id="status-filter"
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors duration-200"
              aria-label="Filter requests by status"
            >
              <option value="ALL">All ({getFilteredCount('ALL')})</option>
              <option value="PENDING">Pending ({getFilteredCount('PENDING')})</option>
              <option value="APPROVED">Approved ({getFilteredCount('APPROVED')})</option>
              <option value="REJECTED">Rejected ({getFilteredCount('REJECTED')})</option>
              <option value="RETURNED">Returned ({getFilteredCount('RETURNED')})</option>
              <option value="COMPLETED">Completed ({getFilteredCount('COMPLETED')})</option>
            </select>
          </div>
          
          <Button onClick={() => navigate('/my-items')} variant="outline">
            My Items
          </Button>
        </div>

        {/* Request List */}
        {error ? (
          <ErrorDisplay
            message={error}
            onRetry={fetchRequests}
            title="Failed to Load Requests"
          />
        ) : loading ? (
          <LoadingSkeleton type="card" count={3} />
        ) : requests.length > 0 ? (
          <div className="grid grid-cols-1 gap-4">
            {requests.map((request) => (
              <RequestCard
                key={request.id}
                request={request}
                viewType="received"
                onAction={handleAction}
                onClick={() => handleCardClick(request)}
              />
            ))}
          </div>
        ) : (
          <div className="text-center py-12 bg-white rounded-lg shadow-sm">
            <div className="text-6xl mb-4">📬</div>
            <h3 className="text-xl font-semibold mb-2">No requests found</h3>
            <p className="text-muted-foreground mb-6">
              {filter === 'ALL'
                ? "You haven't received any borrow requests yet."
                : `You don't have any ${filter.toLowerCase()} requests.`}
            </p>
            <Button onClick={() => navigate('/my-items')} size="lg">
              View My Items
            </Button>
          </div>
        )}
      </div>

      {/* Request Detail Modal */}
      {selectedRequest && (
        <RequestDetailModal
          isOpen={showDetailModal}
          onClose={() => {
            setShowDetailModal(false);
            setSelectedRequest(null);
          }}
          request={selectedRequest}
          viewType="received"
          onAction={handleAction}
          isProcessing={isProcessing}
        />
      )}

      {/* Action Dialog (Approve/Reject) */}
      {showActionDialog && selectedRequest && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4 transition-opacity duration-200"
          onClick={(e) => {
            if (e.target === e.currentTarget && !isProcessing) {
              setShowActionDialog(false);
              setCurrentAction(null);
              setResponseMessage('');
            }
          }}
          role="dialog"
          aria-modal="true"
          aria-labelledby="action-dialog-title"
        >
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-6 transform transition-all duration-200 ease-out scale-100 opacity-100">
            <h3 id="action-dialog-title" className="text-xl font-semibold mb-4">
              {currentAction === 'approve' ? 'Approve Request' : 'Reject Request'}
            </h3>
            
            <div className="mb-4">
              <p className="text-sm text-gray-600 mb-4">
                {currentAction === 'approve'
                  ? `Approve ${selectedRequest.borrower.fullName}'s request for "${selectedRequest.item.title}"?`
                  : `Reject ${selectedRequest.borrower.fullName}'s request for "${selectedRequest.item.title}"?`}
              </p>
              
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
                  className="mt-1"
                />
              </div>
            </div>
            
            <div className="flex gap-2 justify-end">
              <Button
                variant="outline"
                onClick={() => {
                  setShowActionDialog(false);
                  setCurrentAction(null);
                  setResponseMessage('');
                }}
                disabled={isProcessing}
              >
                Cancel
              </Button>
              <Button
                variant={currentAction === 'approve' ? 'default' : 'destructive'}
                onClick={handleDialogSubmit}
                disabled={isProcessing}
              >
                {isProcessing ? 'Processing...' : currentAction === 'approve' ? 'Approve' : 'Reject'}
              </Button>
            </div>
          </div>
        </div>
      )}
      </div>
    </ErrorBoundary>
  );
};

export default IncomingRequestsPage;
