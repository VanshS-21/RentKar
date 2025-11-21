import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navigation from '../components/Navigation';
import RequestCard from '../components/RequestCard';
import RequestDetailModal from '../components/RequestDetailModal';
import DeleteConfirmation from '../components/DeleteConfirmation';
import ErrorBoundary from '../components/ErrorBoundary';
import ErrorDisplay from '../components/ErrorDisplay';
import LoadingSkeleton from '../components/LoadingSkeleton';
import { Button } from '../components/ui/button';
import borrowRequestService from '../services/borrowRequestService';
import toast from 'react-hot-toast';

/**
 * @typedef {import('../types/borrowRequest').BorrowRequest} BorrowRequest
 * @typedef {import('../types/borrowRequest').RequestStatistics} RequestStatistics
 * @typedef {import('../types/borrowRequest').RequestStatus} RequestStatus
 */

/**
 * MyRequestsPage component - Borrower view of sent borrow requests
 * Requirements: 2.1, 2.2, 2.3, 2.4
 */
const MyRequestsPage = () => {
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
  const [showCancelModal, setShowCancelModal] = useState(false);
  
  /** @type {[number | null, Function]} */
  const [requestToCancel, setRequestToCancel] = useState(null);
  
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
    const result = await borrowRequestService.getSentRequests(statusFilter);

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
    if (action === 'cancel') {
      // Show confirmation modal instead of directly canceling
      setRequestToCancel(requestId);
      setShowCancelModal(true);
      return;
    }

    setIsProcessing(true);

    // Store previous state for rollback
    const previousRequests = [...requests];
    const previousSelectedRequest = selectedRequest;
    const previousStatistics = statistics;

    try {
      let result;
      
      // Optimistic update: Update UI immediately
      if (action === 'confirm') {
        const optimisticRequest = requests.find(req => req.id === requestId);
        if (optimisticRequest) {
          const updatedRequest = {
            ...optimisticRequest,
            status: 'COMPLETED',
            completedAt: new Date().toISOString(),
          };
          
          setRequests(prevRequests =>
            prevRequests.map(req =>
              req.id === requestId ? updatedRequest : req
            )
          );
          
          if (selectedRequest && selectedRequest.id === requestId) {
            setSelectedRequest(updatedRequest);
          }

          // Update statistics optimistically
          if (statistics) {
            setStatistics({
              ...statistics,
              returnedCount: statistics.returnedCount - 1,
              completedCount: statistics.completedCount + 1,
            });
          }
        }

        result = await borrowRequestService.confirmReturn(requestId);
      }

      if (result && result.success) {
        // Show specific toast notification based on action (Requirements: 14.2, 14.4)
        if (action === 'confirm') {
          toast.success(`✅ Return confirmed! Transaction completed successfully.`);
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

  const handleCancelConfirm = async () => {
    if (!requestToCancel) return;

    setIsProcessing(true);

    // Store previous state for rollback
    const previousRequests = [...requests];
    const previousStatistics = statistics;
    const requestToDelete = requests.find(req => req.id === requestToCancel);

    // Optimistic update: Remove immediately
    setRequests(prevRequests =>
      prevRequests.filter(req => req.id !== requestToCancel)
    );

    // Update statistics optimistically
    if (statistics && requestToDelete) {
      const statusKey = `${requestToDelete.status.toLowerCase()}Count`;
      setStatistics({
        ...statistics,
        [statusKey]: Math.max(0, (statistics[statusKey] || 0) - 1),
        totalSent: Math.max(0, statistics.totalSent - 1),
      });
    }

    const result = await borrowRequestService.cancelRequest(requestToCancel);

    if (result.success) {
      toast.success('🚫 Request canceled successfully');
      
      // Close modals
      setShowCancelModal(false);
      setShowDetailModal(false);
      setRequestToCancel(null);
      setSelectedRequest(null);
      
      // Refresh statistics with actual data
      fetchStatistics();
    } else {
      // Rollback on error
      setRequests(previousRequests);
      setStatistics(previousStatistics);
      toast.error(result.error || 'Failed to cancel request');
    }
    
    setIsProcessing(false);
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
        return statistics.totalSent;
    }
  };

  return (
    <ErrorBoundary onReset={() => window.location.reload()}>
      <div className="min-h-screen bg-background">
        <Navigation />
      
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold mb-2">My Requests</h1>
          <p className="text-muted-foreground">
            Track the status of items you've requested to borrow
          </p>
        </div>

        {/* Statistics */}
        {statistics && (
          <div className="grid grid-cols-2 md:grid-cols-6 gap-4 mb-8">
            <div className="bg-white p-4 rounded-lg shadow-sm border transition-all duration-200 hover:shadow-md">
              <div className="text-2xl font-bold text-gray-900">{statistics.totalSent}</div>
              <div className="text-sm text-gray-600">Total Sent</div>
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
          
          <Button onClick={() => navigate('/items')} variant="outline">
            Browse Items
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
                viewType="sent"
                onAction={handleAction}
                onClick={() => handleCardClick(request)}
              />
            ))}
          </div>
        ) : (
          <div className="text-center py-12 bg-white rounded-lg shadow-sm">
            <div className="text-6xl mb-4">📭</div>
            <h3 className="text-xl font-semibold mb-2">No requests found</h3>
            <p className="text-muted-foreground mb-6">
              {filter === 'ALL'
                ? "You haven't made any borrow requests yet. Start by browsing available items!"
                : `You don't have any ${filter.toLowerCase()} requests.`}
            </p>
            <Button onClick={() => navigate('/items')} size="lg">
              Browse Available Items
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
          viewType="sent"
          onAction={handleAction}
          isProcessing={isProcessing}
        />
      )}

      {/* Cancel Confirmation Modal */}
      <DeleteConfirmation
        isOpen={showCancelModal}
        onClose={() => {
          setShowCancelModal(false);
          setRequestToCancel(null);
        }}
        onConfirm={handleCancelConfirm}
        itemTitle={
          requestToCancel
            ? requests.find(r => r.id === requestToCancel)?.item.title || 'this request'
            : 'this request'
        }
        isDeleting={isProcessing}
        title="Cancel Request"
        message="Are you sure you want to cancel your request for"
        actionText="Cancel Request"
      />
      </div>
    </ErrorBoundary>
  );
};

export default MyRequestsPage;
