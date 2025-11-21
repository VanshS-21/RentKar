package com.rentkar.service;

import com.rentkar.dto.BorrowRequestDTO;
import com.rentkar.dto.CreateBorrowRequestDTO;
import com.rentkar.dto.RequestStatistics;
import com.rentkar.model.BorrowRequest;
import com.rentkar.model.Item;
import com.rentkar.model.ItemStatus;
import com.rentkar.model.RequestStatus;
import com.rentkar.model.User;
import com.rentkar.repository.BorrowRequestRepository;
import com.rentkar.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BorrowRequestServiceImpl implements BorrowRequestService {
    
    private final BorrowRequestRepository borrowRequestRepository;
    private final ItemRepository itemRepository;
    private final BorrowRequestMapper mapper;
    
    @Autowired
    public BorrowRequestServiceImpl(BorrowRequestRepository borrowRequestRepository,
                                   ItemRepository itemRepository,
                                   BorrowRequestMapper mapper) {
        this.borrowRequestRepository = borrowRequestRepository;
        this.itemRepository = itemRepository;
        this.mapper = mapper;
    }
    
    @Override
    @Transactional
    public BorrowRequestDTO createRequest(Long itemId, CreateBorrowRequestDTO dto, User borrower) {
        // Validate dates
        if (dto.getBorrowDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Borrow date cannot be in the past");
        }
        
        if (dto.getReturnDate().isBefore(dto.getBorrowDate()) || dto.getReturnDate().isEqual(dto.getBorrowDate())) {
            throw new IllegalArgumentException("Return date must be after borrow date");
        }
        
        // Validate item exists and is available
        Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        
        if (item.getStatus() != ItemStatus.AVAILABLE) {
            throw new IllegalArgumentException("Item is not available for borrowing");
        }
        
        // Prevent self-borrowing
        if (item.getOwner().getId().equals(borrower.getId())) {
            throw new IllegalArgumentException("Cannot borrow your own item");
        }
        
        // Create the request
        BorrowRequest request = new BorrowRequest();
        request.setItem(item);
        request.setBorrower(borrower);
        request.setLender(item.getOwner());
        request.setStatus(RequestStatus.PENDING);
        request.setBorrowDate(dto.getBorrowDate());
        request.setReturnDate(dto.getReturnDate());
        request.setRequestMessage(dto.getRequestMessage());
        
        BorrowRequest savedRequest = borrowRequestRepository.save(request);
        return mapper.toDTO(savedRequest);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BorrowRequest> getSentRequests(User borrower, RequestStatus status) {
        if (status == null) {
            return borrowRequestRepository.findByBorrowerId(borrower.getId());
        } else {
            return borrowRequestRepository.findByBorrowerIdAndStatus(borrower.getId(), status);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BorrowRequest> getReceivedRequests(User lender, RequestStatus status) {
        if (status == null) {
            return borrowRequestRepository.findByLenderId(lender.getId());
        } else {
            return borrowRequestRepository.findByLenderIdAndStatus(lender.getId(), status);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public BorrowRequest getRequestById(Long id, User user) {
        BorrowRequest request = borrowRequestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        // Check authorization - user must be either borrower or lender
        boolean isAuthorized = request.getBorrower().getId().equals(user.getId()) ||
                              request.getLender().getId().equals(user.getId());
        
        if (!isAuthorized) {
            throw new SecurityException("Not authorized to view this request");
        }
        
        return request;
    }
    
    @Override
    @Transactional
    public BorrowRequest approveRequest(Long id, String responseMessage, User lender) {
        // Get the request
        BorrowRequest request = borrowRequestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        // Validate user is the lender
        if (!request.getLender().getId().equals(lender.getId())) {
            throw new SecurityException("Only the item owner can approve this request");
        }
        
        // Validate request is PENDING
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be approved");
        }
        
        // Validate item is still available
        Item item = request.getItem();
        if (item.getStatus() != ItemStatus.AVAILABLE) {
            throw new IllegalStateException("Item is no longer available");
        }
        
        // Change request status to APPROVED
        request.setStatus(RequestStatus.APPROVED);
        
        // Save optional response message
        if (responseMessage != null && !responseMessage.trim().isEmpty()) {
            request.setResponseMessage(responseMessage);
        }
        
        // Change item status to BORROWED
        item.setStatus(ItemStatus.BORROWED);
        itemRepository.save(item);
        
        return borrowRequestRepository.save(request);
    }
    
    @Override
    @Transactional
    public BorrowRequest rejectRequest(Long id, String responseMessage, User lender) {
        // Get the request
        BorrowRequest request = borrowRequestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        // Validate user is the lender
        if (!request.getLender().getId().equals(lender.getId())) {
            throw new SecurityException("Only the item owner can reject this request");
        }
        
        // Validate request is PENDING
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be rejected");
        }
        
        // Change request status to REJECTED
        request.setStatus(RequestStatus.REJECTED);
        
        // Save optional reason message
        if (responseMessage != null && !responseMessage.trim().isEmpty()) {
            request.setResponseMessage(responseMessage);
        }
        
        // Keep item status as AVAILABLE (no change needed)
        
        return borrowRequestRepository.save(request);
    }
    
    @Override
    @Transactional
    public BorrowRequest markAsReturned(Long id, User lender) {
        // Get the request
        BorrowRequest request = borrowRequestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        // Validate user is the lender
        if (!request.getLender().getId().equals(lender.getId())) {
            throw new SecurityException("Only the item owner can mark the item as returned");
        }
        
        // Validate request is APPROVED
        if (request.getStatus() != RequestStatus.APPROVED) {
            throw new IllegalStateException("Only approved requests can be marked as returned");
        }
        
        // Change request status to RETURNED
        request.setStatus(RequestStatus.RETURNED);
        
        // Record return timestamp
        request.setReturnedAt(java.time.LocalDateTime.now());
        
        // Change item status back to AVAILABLE
        Item item = request.getItem();
        item.setStatus(ItemStatus.AVAILABLE);
        itemRepository.save(item);
        
        return borrowRequestRepository.save(request);
    }
    
    @Override
    @Transactional
    public BorrowRequest confirmReturn(Long id, User borrower) {
        // Get the request
        BorrowRequest request = borrowRequestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        // Validate user is the borrower
        if (!request.getBorrower().getId().equals(borrower.getId())) {
            throw new SecurityException("Only the borrower can confirm the return");
        }
        
        // Validate request is RETURNED
        if (request.getStatus() != RequestStatus.RETURNED) {
            throw new IllegalStateException("Only returned requests can be confirmed");
        }
        
        // Change request status to COMPLETED
        request.setStatus(RequestStatus.COMPLETED);
        
        // Record completion timestamp
        request.setCompletedAt(java.time.LocalDateTime.now());
        
        return borrowRequestRepository.save(request);
    }
    
    @Override
    @Transactional
    public void cancelRequest(Long id, User borrower) {
        // Get the request
        BorrowRequest request = borrowRequestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        // Validate user is the borrower
        if (!request.getBorrower().getId().equals(borrower.getId())) {
            throw new SecurityException("Only the borrower can cancel this request");
        }
        
        // Validate request is PENDING
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be canceled");
        }
        
        // Delete the request (do not affect item status)
        borrowRequestRepository.delete(request);
    }
    
    @Override
    @Transactional(readOnly = true)
    public RequestStatistics getStatistics(User user) {
        Long userId = user.getId();
        
        // Count requests by status for borrower (sent requests)
        int sentPending = (int) borrowRequestRepository.countByBorrowerAndStatus(userId, RequestStatus.PENDING);
        int sentApproved = (int) borrowRequestRepository.countByBorrowerAndStatus(userId, RequestStatus.APPROVED);
        int sentRejected = (int) borrowRequestRepository.countByBorrowerAndStatus(userId, RequestStatus.REJECTED);
        int sentReturned = (int) borrowRequestRepository.countByBorrowerAndStatus(userId, RequestStatus.RETURNED);
        int sentCompleted = (int) borrowRequestRepository.countByBorrowerAndStatus(userId, RequestStatus.COMPLETED);
        
        // Count requests by status for lender (received requests)
        int receivedPending = (int) borrowRequestRepository.countByLenderAndStatus(userId, RequestStatus.PENDING);
        int receivedApproved = (int) borrowRequestRepository.countByLenderAndStatus(userId, RequestStatus.APPROVED);
        int receivedRejected = (int) borrowRequestRepository.countByLenderAndStatus(userId, RequestStatus.REJECTED);
        int receivedReturned = (int) borrowRequestRepository.countByLenderAndStatus(userId, RequestStatus.RETURNED);
        int receivedCompleted = (int) borrowRequestRepository.countByLenderAndStatus(userId, RequestStatus.COMPLETED);
        
        // Calculate totals
        int totalSent = sentPending + sentApproved + sentRejected + sentReturned + sentCompleted;
        int totalReceived = receivedPending + receivedApproved + receivedRejected + receivedReturned + receivedCompleted;
        
        // Create statistics object with combined counts
        RequestStatistics stats = new RequestStatistics();
        stats.setPendingCount(sentPending + receivedPending);
        stats.setApprovedCount(sentApproved + receivedApproved);
        stats.setRejectedCount(sentRejected + receivedRejected);
        stats.setReturnedCount(sentReturned + receivedReturned);
        stats.setCompletedCount(sentCompleted + receivedCompleted);
        stats.setTotalSent(totalSent);
        stats.setTotalReceived(totalReceived);
        
        return stats;
    }
}
