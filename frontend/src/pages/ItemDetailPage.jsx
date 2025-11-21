import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import itemService from '../services/itemService';
import borrowRequestService from '../services/borrowRequestService';
import Navigation from '../components/Navigation';
import toast from 'react-hot-toast';

const ItemDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [item, setItem] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [showBorrowModal, setShowBorrowModal] = useState(false);
  const [borrowDate, setBorrowDate] = useState('');
  const [returnDate, setReturnDate] = useState('');
  const [requestMessage, setRequestMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [activeRequest, setActiveRequest] = useState(null);

  useEffect(() => {
    const fetchItem = async () => {
      setLoading(true);
      setError(null);

      const result = await itemService.getItemById(id);

      if (result.success) {
        setItem(result.data);
        // Fetch active request for this item if user is logged in and not the owner
        if (user && result.data.owner?.id !== user.id) {
          await fetchActiveRequest(result.data.id);
        }
      } else {
        setError(result.error);
        if (result.status === 404) {
          toast.error('Item not found');
        }
      }

      setLoading(false);
    };

    fetchItem();
  }, [id, user]);

  const fetchActiveRequest = async (itemId) => {
    const result = await borrowRequestService.getSentRequests();
    if (result.success) {
      // Find active request for this item (PENDING or APPROVED)
      const active = result.data.find(
        req => req.item.id === itemId && 
        (req.status === 'PENDING' || req.status === 'APPROVED')
      );
      setActiveRequest(active || null);
    }
  };

  const handleEdit = () => {
    navigate(`/items/${id}/edit`);
  };

  const handleDelete = async () => {
    if (!window.confirm(`Are you sure you want to delete "${item.title}"?`)) {
      return;
    }

    setDeleting(true);
    const result = await itemService.deleteItem(id);

    if (result.success) {
      toast.success('Item deleted successfully');
      navigate('/my-items');
    } else {
      toast.error(result.error || 'Failed to delete item');
      setDeleting(false);
    }
  };

  const handleBorrow = () => {
    setShowBorrowModal(true);
    // Set default dates
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const nextWeek = new Date(today);
    nextWeek.setDate(nextWeek.getDate() + 7);
    
    setBorrowDate(tomorrow.toISOString().split('T')[0]);
    setReturnDate(nextWeek.toISOString().split('T')[0]);
    setRequestMessage('');
  };

  const handleCloseBorrowModal = () => {
    setShowBorrowModal(false);
    setBorrowDate('');
    setReturnDate('');
    setRequestMessage('');
  };

  const handleSubmitRequest = async (e) => {
    e.preventDefault();
    
    // Validate dates
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const borrow = new Date(borrowDate);
    const returnD = new Date(returnDate);
    
    if (borrow < today) {
      toast.error('Borrow date cannot be in the past');
      return;
    }
    
    if (returnD <= borrow) {
      toast.error('Return date must be after borrow date');
      return;
    }
    
    setSubmitting(true);
    
    const requestData = {
      borrowDate,
      returnDate,
      requestMessage: requestMessage.trim() || undefined,
    };
    
    const result = await borrowRequestService.createRequest(item.id, requestData);
    
    if (result.success) {
      toast.success('Borrow request sent successfully!');
      handleCloseBorrowModal();
      // Refresh to show the active request
      await fetchActiveRequest(item.id);
    } else {
      toast.error(result.error || 'Failed to create request');
    }
    
    setSubmitting(false);
  };

  const statusColors = {
    AVAILABLE: 'bg-green-100 text-green-800 border-green-200',
    BORROWED: 'bg-yellow-100 text-yellow-800 border-yellow-200',
    UNAVAILABLE: 'bg-gray-100 text-gray-800 border-gray-200',
  };

  const statusText = {
    AVAILABLE: 'Available',
    BORROWED: 'Borrowed',
    UNAVAILABLE: 'Unavailable',
  };

  const isOwner = user && item && user.id === item.owner?.id;
  const canBorrow = user && item && !isOwner && item.status === 'AVAILABLE' && !activeRequest;
  const isBorrowed = item && item.status === 'BORROWED';

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navigation />
        <div className="flex justify-center items-center py-20">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        </div>
      </div>
    );
  }

  if (error || !item) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navigation />
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
            <svg
              className="mx-auto h-12 w-12 text-red-400 mb-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
              />
            </svg>
            <h3 className="text-lg font-medium text-red-900 mb-2">
              {error || 'Item not found'}
            </h3>
            <button
              onClick={() => navigate('/items')}
              className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              Browse Items
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navigation />
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <button
          onClick={() => navigate(-1)}
          className="mb-6 flex items-center text-gray-600 hover:text-gray-900 transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 rounded px-2 py-1"
          aria-label="Go back to previous page"
        >
          <svg
            className="w-5 h-5 mr-2"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M15 19l-7-7 7-7"
            />
          </svg>
          Back
        </button>

        <div className="bg-white rounded-lg shadow-lg overflow-hidden">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 p-8">
            {/* Image Section */}
            <div className="aspect-square bg-gray-200 rounded-lg overflow-hidden">
              {item.imageUrl ? (
                <img
                  src={item.imageUrl}
                  alt={item.title}
                  className="w-full h-full object-cover"
                />
              ) : (
                <div className="w-full h-full flex items-center justify-center text-gray-400">
                  <svg
                    className="w-24 h-24"
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

            {/* Details Section */}
            <div className="flex flex-col">
              <div className="flex-1">
                <div className="flex items-start justify-between mb-4">
                  <h1 className="text-3xl font-bold text-gray-900">
                    {item.title}
                  </h1>
                  <span
                    className={`px-3 py-1 rounded-full text-sm font-medium border ${
                      statusColors[item.status] || statusColors.UNAVAILABLE
                    }`}
                  >
                    {statusText[item.status] || 'Unknown'}
                  </span>
                </div>

                <div className="mb-6">
                  <span className="inline-block px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm font-medium">
                    {item.category}
                  </span>
                </div>

                <div className="mb-6">
                  <h2 className="text-lg font-semibold text-gray-900 mb-2">
                    Description
                  </h2>
                  <p className="text-gray-700 whitespace-pre-wrap">
                    {item.description || 'No description provided.'}
                  </p>
                </div>

                {/* Owner Information */}
                <div className="border-t pt-6 mb-6">
                  <h2 className="text-lg font-semibold text-gray-900 mb-3">
                    Owner Information
                  </h2>
                  <div className="flex items-center space-x-3">
                    <div className="w-12 h-12 bg-blue-600 rounded-full flex items-center justify-center text-white font-semibold text-lg">
                      {item.owner?.username?.charAt(0).toUpperCase() || 'U'}
                    </div>
                    <div>
                      <p className="font-medium text-gray-900">
                        {item.owner?.fullName || 'Unknown User'}
                      </p>
                      <p className="text-sm text-gray-600">
                        @{item.owner?.username || 'unknown'}
                      </p>
                      {item.owner?.email && (
                        <p className="text-sm text-gray-600">
                          {item.owner.email}
                        </p>
                      )}
                      {item.owner?.phone && (
                        <p className="text-sm text-gray-600">
                          {item.owner.phone}
                        </p>
                      )}
                    </div>
                  </div>
                </div>

                {/* Timestamps */}
                <div className="text-sm text-gray-500 space-y-1">
                  <p>
                    Listed: {new Date(item.createdAt).toLocaleDateString()}
                  </p>
                  {item.updatedAt && item.updatedAt !== item.createdAt && (
                    <p>
                      Updated: {new Date(item.updatedAt).toLocaleDateString()}
                    </p>
                  )}
                </div>
              </div>

              {/* Action Buttons */}
              <div className="mt-6 space-y-3">
                {canBorrow && (
                  <button
                    onClick={handleBorrow}
                    className="w-full px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium transition-colors"
                  >
                    Request to Borrow
                  </button>
                )}

                {activeRequest && (
                  <div className="w-full px-6 py-3 bg-yellow-50 border border-yellow-200 rounded-lg">
                    <p className="text-sm font-medium text-yellow-800">
                      You have a {activeRequest.status.toLowerCase()} request for this item
                    </p>
                    <button
                      onClick={() => navigate('/requests/sent')}
                      className="mt-2 text-sm text-blue-600 hover:text-blue-700 font-medium"
                    >
                      View Request →
                    </button>
                  </div>
                )}

                {isBorrowed && !isOwner && !activeRequest && (
                  <div className="w-full px-6 py-3 bg-gray-100 border border-gray-300 rounded-lg text-center">
                    <p className="font-medium text-gray-700">Currently Borrowed</p>
                    {item.borrowDate && item.returnDate && (
                      <p className="text-sm text-gray-600 mt-1">
                        Expected return: {new Date(item.returnDate).toLocaleDateString()}
                      </p>
                    )}
                  </div>
                )}

                {isOwner && (
                  <div className="flex space-x-3">
                    <button
                      onClick={handleEdit}
                      className="flex-1 px-6 py-3 bg-gray-600 text-white rounded-lg hover:bg-gray-700 font-medium transition-colors"
                    >
                      Edit Item
                    </button>
                    <button
                      onClick={handleDelete}
                      disabled={deleting}
                      className="flex-1 px-6 py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {deleting ? 'Deleting...' : 'Delete Item'}
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Borrow Request Modal */}
      {showBorrowModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-md w-full p-6">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-bold text-gray-900">Request to Borrow</h2>
              <button
                onClick={handleCloseBorrowModal}
                className="text-gray-400 hover:text-gray-600"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <form onSubmit={handleSubmitRequest} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Borrow Date *
                </label>
                <input
                  type="date"
                  value={borrowDate}
                  onChange={(e) => setBorrowDate(e.target.value)}
                  required
                  min={new Date().toISOString().split('T')[0]}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Return Date *
                </label>
                <input
                  type="date"
                  value={returnDate}
                  onChange={(e) => setReturnDate(e.target.value)}
                  required
                  min={borrowDate || new Date().toISOString().split('T')[0]}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Message to Owner (Optional)
                </label>
                <textarea
                  value={requestMessage}
                  onChange={(e) => setRequestMessage(e.target.value)}
                  maxLength={500}
                  rows={4}
                  placeholder="Tell the owner why you need this item..."
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                />
                <p className="text-xs text-gray-500 mt-1">
                  {requestMessage.length}/500 characters
                </p>
              </div>

              <div className="flex space-x-3 pt-2">
                <button
                  type="button"
                  onClick={handleCloseBorrowModal}
                  className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 font-medium transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {submitting ? 'Sending...' : 'Send Request'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ItemDetailPage;
