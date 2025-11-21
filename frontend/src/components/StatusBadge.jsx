import React from 'react';
import { cn } from '../lib/utils';

/**
 * @typedef {import('../types/borrowRequest').RequestStatus} RequestStatus
 */

/**
 * StatusBadge component displays a colored badge for borrow request status
 * 
 * @param {Object} props
 * @param {RequestStatus} props.status - The request status
 * @param {string} [props.className] - Additional CSS classes
 * @returns {JSX.Element}
 */
const StatusBadge = ({ status, className }) => {
  const getStatusStyles = (status) => {
    switch (status) {
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800 border-yellow-300';
      case 'APPROVED':
        return 'bg-green-100 text-green-800 border-green-300';
      case 'REJECTED':
        return 'bg-red-100 text-red-800 border-red-300';
      case 'RETURNED':
        return 'bg-blue-100 text-blue-800 border-blue-300';
      case 'COMPLETED':
        return 'bg-gray-100 text-gray-800 border-gray-300';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-300';
    }
  };

  const getStatusText = (status) => {
    switch (status) {
      case 'PENDING':
        return 'Pending';
      case 'APPROVED':
        return 'Approved';
      case 'REJECTED':
        return 'Rejected';
      case 'RETURNED':
        return 'Returned';
      case 'COMPLETED':
        return 'Completed';
      default:
        return status;
    }
  };

  return (
    <span
      className={cn(
        'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border',
        getStatusStyles(status),
        className
      )}
    >
      {getStatusText(status)}
    </span>
  );
};

export default StatusBadge;
