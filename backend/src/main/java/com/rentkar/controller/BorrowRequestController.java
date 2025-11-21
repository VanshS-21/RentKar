package com.rentkar.controller;

import com.rentkar.dto.BorrowRequestDTO;
import com.rentkar.dto.CreateBorrowRequestDTO;
import com.rentkar.dto.RequestStatistics;
import com.rentkar.model.BorrowRequest;
import com.rentkar.model.RequestStatus;
import com.rentkar.model.User;
import com.rentkar.repository.UserRepository;
import com.rentkar.service.BorrowRequestMapper;
import com.rentkar.service.BorrowRequestService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "*")
public class BorrowRequestController {
    
    private final BorrowRequestService borrowRequestService;
    private final BorrowRequestMapper borrowRequestMapper;
    private final UserRepository userRepository;
    
    public BorrowRequestController(BorrowRequestService borrowRequestService,
                                  BorrowRequestMapper borrowRequestMapper,
                                  UserRepository userRepository) {
        this.borrowRequestService = borrowRequestService;
        this.borrowRequestMapper = borrowRequestMapper;
        this.userRepository = userRepository;
    }
    
    /**
     * Create a new borrow request
     * POST /api/requests?itemId={itemId}
     */
    @PostMapping
    public ResponseEntity<?> createRequest(
            @RequestParam Long itemId,
            @Valid @RequestBody CreateBorrowRequestDTO dto) {
        try {
            User borrower = getCurrentUser();
            BorrowRequestDTO request = borrowRequestService.createRequest(itemId, dto, borrower);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createSuccessResponse(request, "Borrow request created successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to create borrow request"));
        }
    }
    
    /**
     * Get all requests sent by the current user (borrower view)
     * GET /api/requests/sent?status={status}
     */
    @GetMapping("/sent")
    public ResponseEntity<?> getSentRequests(
            @RequestParam(required = false) RequestStatus status) {
        try {
            User borrower = getCurrentUser();
            List<BorrowRequest> requests = borrowRequestService.getSentRequests(borrower, status);
            List<BorrowRequestDTO> requestDTOs = requests.stream()
                    .map(borrowRequestMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(createSuccessResponse(requestDTOs, "Sent requests retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve sent requests"));
        }
    }
    
    /**
     * Get all requests received by the current user (lender view)
     * GET /api/requests/received?status={status}
     */
    @GetMapping("/received")
    public ResponseEntity<?> getReceivedRequests(
            @RequestParam(required = false) RequestStatus status) {
        try {
            User lender = getCurrentUser();
            List<BorrowRequest> requests = borrowRequestService.getReceivedRequests(lender, status);
            List<BorrowRequestDTO> requestDTOs = requests.stream()
                    .map(borrowRequestMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(createSuccessResponse(requestDTOs, "Received requests retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve received requests"));
        }
    }
    
    /**
     * Get request by ID
     * GET /api/requests/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRequestById(@PathVariable Long id) {
        try {
            User user = getCurrentUser();
            BorrowRequest request = borrowRequestService.getRequestById(id, user);
            BorrowRequestDTO requestDTO = borrowRequestMapper.toDTO(request);
            return ResponseEntity.ok(createSuccessResponse(requestDTO, "Request retrieved successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (SecurityException | AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve request"));
        }
    }
    
    /**
     * Approve a borrow request (lender only)
     * POST /api/requests/{id}/approve
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveRequest(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            User lender = getCurrentUser();
            String responseMessage = body != null ? body.get("responseMessage") : null;
            BorrowRequest request = borrowRequestService.approveRequest(id, responseMessage, lender);
            BorrowRequestDTO requestDTO = borrowRequestMapper.toDTO(request);
            return ResponseEntity.ok(createSuccessResponse(requestDTO, "Request approved successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (SecurityException | AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to approve request"));
        }
    }
    
    /**
     * Reject a borrow request (lender only)
     * POST /api/requests/{id}/reject
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectRequest(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            User lender = getCurrentUser();
            String responseMessage = body != null ? body.get("responseMessage") : null;
            BorrowRequest request = borrowRequestService.rejectRequest(id, responseMessage, lender);
            BorrowRequestDTO requestDTO = borrowRequestMapper.toDTO(request);
            return ResponseEntity.ok(createSuccessResponse(requestDTO, "Request rejected successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (SecurityException | AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to reject request"));
        }
    }
    
    /**
     * Mark item as returned (lender only)
     * POST /api/requests/{id}/return
     */
    @PostMapping("/{id}/return")
    public ResponseEntity<?> markAsReturned(@PathVariable Long id) {
        try {
            User lender = getCurrentUser();
            BorrowRequest request = borrowRequestService.markAsReturned(id, lender);
            BorrowRequestDTO requestDTO = borrowRequestMapper.toDTO(request);
            return ResponseEntity.ok(createSuccessResponse(requestDTO, "Item marked as returned successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (SecurityException | AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to mark item as returned"));
        }
    }
    
    /**
     * Confirm return and complete transaction (borrower only)
     * POST /api/requests/{id}/confirm
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmReturn(@PathVariable Long id) {
        try {
            User borrower = getCurrentUser();
            BorrowRequest request = borrowRequestService.confirmReturn(id, borrower);
            BorrowRequestDTO requestDTO = borrowRequestMapper.toDTO(request);
            return ResponseEntity.ok(createSuccessResponse(requestDTO, "Return confirmed successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (SecurityException | AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to confirm return"));
        }
    }
    
    /**
     * Cancel a pending borrow request (borrower only)
     * DELETE /api/requests/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelRequest(@PathVariable Long id) {
        try {
            User borrower = getCurrentUser();
            borrowRequestService.cancelRequest(id, borrower);
            return ResponseEntity.ok(createSuccessResponse(null, "Request canceled successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (SecurityException | AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to cancel request"));
        }
    }
    
    /**
     * Get request statistics for the current user
     * GET /api/requests/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        try {
            User user = getCurrentUser();
            RequestStatistics statistics = borrowRequestService.getStatistics(user);
            return ResponseEntity.ok(createSuccessResponse(statistics, "Statistics retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve statistics"));
        }
    }
    
    /**
     * Get the currently authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("User not authenticated");
        }
        
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
    
    /**
     * Create a success response
     */
    private Map<String, Object> createSuccessResponse(Object data, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }
    
    /**
     * Create an error response
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}
