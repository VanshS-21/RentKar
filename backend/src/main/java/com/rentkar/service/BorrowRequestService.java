package com.rentkar.service;

import com.rentkar.dto.BorrowRequestDTO;
import com.rentkar.dto.CreateBorrowRequestDTO;
import com.rentkar.model.BorrowRequest;
import com.rentkar.model.RequestStatus;
import com.rentkar.model.User;

import java.time.LocalDate;
import java.util.List;

public interface BorrowRequestService {
    
    /**
     * Create a new borrow request
     * @param itemId The ID of the item to borrow
     * @param dto The request data
     * @param borrower The user creating the request
     * @return The created borrow request DTO
     */
    BorrowRequestDTO createRequest(Long itemId, CreateBorrowRequestDTO dto, User borrower);
    
    /**
     * Get all requests sent by a user (borrower view)
     * @param borrower The user who sent the requests
     * @param status Optional status filter (null for all)
     * @return List of borrow requests
     */
    List<BorrowRequest> getSentRequests(User borrower, RequestStatus status);
    
    /**
     * Get all requests received by a user (lender view)
     * @param lender The user who owns the items
     * @param status Optional status filter (null for all)
     * @return List of borrow requests
     */
    List<BorrowRequest> getReceivedRequests(User lender, RequestStatus status);
    
    /**
     * Get request by ID with authorization check
     * @param id The request ID
     * @param user The user requesting access
     * @return The borrow request
     * @throws RuntimeException if request not found or user not authorized
     */
    BorrowRequest getRequestById(Long id, User user);
    
    /**
     * Approve a borrow request (lender only)
     * @param id The request ID
     * @param responseMessage Optional response message from lender
     * @param lender The user approving the request
     * @return The approved borrow request
     * @throws RuntimeException if request not found, not pending, or user not authorized
     */
    BorrowRequest approveRequest(Long id, String responseMessage, User lender);
    
    /**
     * Reject a borrow request (lender only)
     * @param id The request ID
     * @param responseMessage Optional reason message from lender
     * @param lender The user rejecting the request
     * @return The rejected borrow request
     * @throws RuntimeException if request not found, not pending, or user not authorized
     */
    BorrowRequest rejectRequest(Long id, String responseMessage, User lender);
    
    /**
     * Mark item as returned (lender only)
     * @param id The request ID
     * @param lender The user marking the item as returned
     * @return The updated borrow request
     * @throws RuntimeException if request not found, not approved, or user not authorized
     */
    BorrowRequest markAsReturned(Long id, User lender);
    
    /**
     * Confirm return and complete transaction (borrower only)
     * @param id The request ID
     * @param borrower The user confirming the return
     * @return The completed borrow request
     * @throws RuntimeException if request not found, not returned, or user not authorized
     */
    BorrowRequest confirmReturn(Long id, User borrower);
    
    /**
     * Cancel a pending borrow request (borrower only)
     * @param id The request ID
     * @param borrower The user canceling the request
     * @throws RuntimeException if request not found, not pending, or user not authorized
     */
    void cancelRequest(Long id, User borrower);
    
    /**
     * Get request statistics for a user
     * @param user The user to get statistics for
     * @return Request statistics including counts by status for sent and received requests
     */
    com.rentkar.dto.RequestStatistics getStatistics(User user);
}
