# Implementation Plan - Borrow Workflow

- [x] 1. Set up backend infrastructure





  - Create BorrowRequest entity with JPA annotations
  - Create RequestStatus enum (PENDING, APPROVED, REJECTED, RETURNED, COMPLETED)
  - Create BorrowRequestRepository interface
  - Add database migration for borrow_requests table
  - _Requirements: 1.3, 1.4_

- [x] 2. Implement core service layer




  - [x] 2.1 Create BorrowRequestService interface and implementation


    - Implement createRequest method with validation
    - Implement getSentRequests method
    - Implement getReceivedRequests method
    - Implement getRequestById with authorization
    - Add error handling for all methods
    - _Requirements: 1.3, 2.1, 3.1_
  
  - [x] 2.2 Write property test for request creation


    - **Property 1: Request creation with PENDING status**
    - **Validates: Requirements 1.3**
  
  - [x] 2.3 Write property test for request associations


    - **Property 2: Request associations**
    - **Validates: Requirements 1.4**


  
  - [x] 2.4 Write property test for sent requests filtering


    - **Property 3: Sent requests filtering**
    - **Validates: Requirements 2.1**
  
  - [X] 2.5 Write property test for received requests filtering
    - **Property 6: Received requests filtering**
    - **Validates: Requirements 3.1**

- [X] 3. Implement request creation and validation


  - [x] 3.1 Create DTOs for request operations


    - Create CreateBorrowRequestDTO with validation annotations
    - Create BorrowRequestDTO for responses
    - Create RequestStatistics DTO
    - Add mapper methods for entity-DTO conversion
    - _Requirements: 1.2, 1.3_
  
  - [x] 3.2 Implement date validation


    - Validate borrow date is not in the past
    - Validate return date is after borrow date
    - Add custom validation annotations if needed
    - _Requirements: 12.1, 12.2_
  
  - [x] 3.3 Implement item availability validation


    - Check item exists and status is AVAILABLE
    - Prevent self-borrowing (borrower != owner)
    - Validate borrower is authenticated
    - _Requirements: 12.3, 12.4, 12.5_
  
  - [x] 3.4 Write edge case tests for validation



    - Test past borrow dates are rejected
    - Test invalid date ranges are rejected
    - Test self-borrowing is rejected
    - Test unavailable items are rejected
    - _Requirements: 12.1, 12.2, 12.3, 12.5_

- [x] 4. Implement approval workflow





  - [x] 4.1 Implement approveRequest method


    - Validate request is PENDING
    - Validate user is the lender
    - Change request status to APPROVED
    - Change item status to BORROWED
    - Save optional response message
    - _Requirements: 4.1, 4.2, 4.3_
  
  - [x] 4.2 Write property test for approval status transition


    - **Property 9: Approval status transition**
    - **Validates: Requirements 4.1**
  
  - [x] 4.3 Write property test for item status on approval


    - **Property 10: Item status on approval**
    - **Validates: Requirements 4.2**
  
  - [x] 4.4 Write property test for contact information on approval


    - **Property 11: Contact information on approval**
    - **Validates: Requirements 4.4**
  
  - [x] 4.5 Write edge case test for approval validation


    - Test approving non-pending request fails
    - Test non-owner cannot approve
    - Test approving unavailable item fails
    - _Requirements: 4.5, 13.1_

- [X] 5. Implement rejection workflow






  - [x] 5.1 Implement rejectRequest method



    - Validate request is PENDING
    - Validate user is the lender
    - Change request status to REJECTED
    - Keep item status as AVAILABLE
    - Save optional reason message
    - _Requirements: 5.1, 5.2, 5.3_
  
  - [x] 5.2 Write property test for rejection status transition



    - **Property 12: Rejection status transition**
    - **Validates: Requirements 5.1**
  

  - [x] 5.3 Write property test for item status on rejection


    - **Property 13: Item status on rejection**
    - **Validates: Requirements 5.2**
  

  - [x] 5.4 Write property test for contact information on rejection


    - **Property 14: Contact information on rejection**
    - **Validates: Requirements 5.4**
  
  - [ ] 5.5 Write edge case test for rejection validation




    - Test rejecting non-pending request fails
    - Test non-owner cannot reject
    - _Requirements: 5.5, 13.2_



- [x] 6. Implement return workflow





  - [x] 6.1 Implement markAsReturned method


    - Validate request is APPROVED
    - Validate user is the lender
    - Change request status to RETURNED
    - Change item status to AVAILABLE
    - Record return timestamp
    - _Requirements: 6.1, 6.2, 6.4_
  
  - [x] 6.2 Implement confirmReturn method

    - Validate request is RETURNED
    - Validate user is the borrower
    - Change request status to COMPLETED
    - Record completion timestamp
    - _Requirements: 7.1, 7.4_
  
  - [x] 6.3 Write property test for return status transition


    - **Property 15: Return status transition**
    - **Validates: Requirements 6.1**
  
  - [x] 6.4 Write property test for item status on return

    - **Property 16: Item status on return**
    - **Validates: Requirements 6.2**
  
  - [x] 6.5 Write property test for return timestamp

    - **Property 17: Return timestamp recording**
    - **Validates: Requirements 6.4**
  
  - [x] 6.6 Write property test for item availability after return

    - **Property 18: Item availability after return**
    - **Validates: Requirements 6.5**
  
  - [x] 6.7 Write property test for completion status transition

    - **Property 19: Completion status transition**
    - **Validates: Requirements 7.1**
  
  - [x] 6.8 Write property test for completion timestamp

    - **Property 20: Completion timestamp recording**
    - **Validates: Requirements 7.4**
  
  - [x] 6.9 Write edge case tests for return validation


    - Test marking non-approved request as returned fails
    - Test non-owner cannot mark as returned
    - Test confirming non-returned request fails
    - Test non-borrower cannot confirm return
    - _Requirements: 6.3, 7.3, 13.3, 13.4_

- [x] 7. Implement cancellation workflow




  - [x] 7.1 Implement cancelRequest method


    - Validate request is PENDING
    - Validate user is the borrower
    - Delete the request
    - Do not affect item status
    - _Requirements: 9.1, 9.2, 9.4_
  
  - [x] 7.2 Write property test for request cancellation


    - **Property 23: Request cancellation**
    - **Validates: Requirements 9.1**
  
  - [x] 7.3 Write property test for cancellation visibility

    - **Property 24: Cancellation visibility**
    - **Validates: Requirements 9.3**
  
  - [x] 7.4 Write property test for item status on cancellation

    - **Property 25: Item status on cancellation**
    - **Validates: Requirements 9.4**
  
  - [x] 7.5 Write edge case test for cancellation validation


    - Test canceling non-pending request fails
    - Test non-borrower cannot cancel
    - _Requirements: 9.2, 13.5_

- [x] 8. Implement statistics and filtering





  - [x] 8.1 Implement getStatistics method


    - Count requests by status for borrower
    - Count requests by status for lender
    - Calculate total sent and received
    - _Requirements: 15.1, 15.2, 15.3, 15.4_
  
  - [x] 8.2 Add filtering to getSentRequests and getReceivedRequests

    - Filter by status (optional parameter)
    - Return all if no filter specified
    - _Requirements: 2.3, 3.3_
  
  - [x] 8.3 Write property test for status filtering


    - **Property 5: Status filtering accuracy**
    - **Validates: Requirements 2.3, 3.3**
  
  - [x] 8.4 Write property test for statistics accuracy


    - **Property 32: Statistics accuracy**
    - **Validates: Requirements 15.1, 15.2, 15.3, 15.4, 15.5**

- [x] 9. Create REST API endpoints




  - [x] 9.1 Create BorrowRequestController


    - POST /api/requests - Create new request
    - GET /api/requests/sent - Get sent requests
    - GET /api/requests/received - Get received requests
    - GET /api/requests/{id} - Get request details
    - POST /api/requests/{id}/approve - Approve request
    - POST /api/requests/{id}/reject - Reject request
    - POST /api/requests/{id}/return - Mark as returned
    - POST /api/requests/{id}/confirm - Confirm return
    - DELETE /api/requests/{id} - Cancel request
    - GET /api/requests/statistics - Get statistics
    - _Requirements: All_
  
  - [x] 9.2 Add request/response validation


    - Validate all DTOs with @Valid
    - Add proper error responses
    - Include validation messages
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5_
  
  - [x] 9.3 Add authorization checks


    - Verify authentication for all endpoints
    - Check ownership for lender actions
    - Check borrower identity for borrower actions
    - Return 403 for unauthorized actions
    - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5_
  
  - [x] 9.4 Write integration tests for all endpoints


    - Test create request endpoint
    - Test approve/reject endpoints
    - Test return/confirm endpoints
    - Test cancel endpoint
    - Test filtering and statistics
    - Test authorization checks
    - _Requirements: All_

- [x] 10. Checkpoint - Ensure all backend tests pass





  - Ensure all tests pass, ask the user if questions arise.

- [x] 11. Set up frontend infrastructure





  - [x] 11.1 Create borrowRequestService API client


    - Implement createRequest function
    - Implement getSentRequests function
    - Implement getReceivedRequests function
    - Implement getRequestById function
    - Implement approveRequest function
    - Implement rejectRequest function
    - Implement markAsReturned function
    - Implement confirmReturn function
    - Implement cancelRequest function
    - Implement getStatistics function
    - Add error handling for all functions
    - _Requirements: All_
  
  - [x] 11.2 Create TypeScript interfaces


    - Create BorrowRequest interface
    - Create RequestStatus enum
    - Create CreateBorrowRequestDTO interface
    - Create RequestStatistics interface
    - _Requirements: All_

- [x] 12. Create shared UI components





  - [x] 12.1 Create StatusBadge component


    - Display badge with correct color for each status
    - PENDING = yellow/orange
    - APPROVED = green
    - REJECTED = red
    - RETURNED = blue
    - COMPLETED = gray
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_
  
  - [x] 12.2 Write property test for status badge rendering


    - **Property 22: Status badge rendering**
    - **Validates: Requirements 8.1, 8.2, 8.3, 8.4, 8.5**
  
  - [x] 12.3 Create RequestCard component


    - Display request summary
    - Show item image and title
    - Show other party (borrower or lender) info
    - Show dates and status
    - Show appropriate action buttons
    - _Requirements: 2.2, 3.2_
  
  - [x] 12.4 Create RequestDetailModal component


    - Display full request information
    - Show all messages
    - Show status history
    - Show action buttons based on status and role
    - Handle approve/reject/return/confirm/cancel actions
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_
  
  - [x] 12.5 Write property test for request detail completeness


    - **Property 26: Request detail completeness**
    - **Validates: Requirements 10.1, 10.2, 10.3, 10.4, 10.5**

- [x] 13. Implement MyRequestsPage (Borrower View)





  - [x] 13.1 Create MyRequestsPage component


    - Fetch sent requests on mount
    - Display loading state
    - Display empty state with helpful message
    - Display request cards in grid/list
    - Add status filter dropdown
    - Show statistics at top
    - _Requirements: 2.1, 2.2, 2.3, 2.4_
  
  - [x] 13.2 Implement cancel request functionality


    - Show confirmation dialog
    - Call cancelRequest API
    - Remove from list on success
    - Show error message on failure
    - _Requirements: 9.1, 9.5_
  
  - [x] 13.3 Implement confirm return functionality


    - Show confirm button for RETURNED requests
    - Call confirmReturn API
    - Update status on success
    - Show success message
    - _Requirements: 7.1, 7.4_
  
  - [x] 13.4 Write property test for sent request information


    - **Property 4: Sent request information completeness**
    - **Validates: Requirements 2.2**
  
  - [x] 13.5 Write property test for completed request in history


    - **Property 21: Completed request in history**
    - **Validates: Requirements 7.5**

- [x] 14. Implement IncomingRequestsPage (Lender View)




  - [x] 14.1 Create IncomingRequestsPage component


    - Fetch received requests on mount
    - Display loading state
    - Display empty state message
    - Display request cards in grid/list
    - Add status filter dropdown
    - Show statistics at top
    - _Requirements: 3.1, 3.2, 3.3, 3.4_
  
  - [x] 14.2 Implement approve request functionality

    - Show approve dialog with optional message field
    - Call approveRequest API
    - Update status and show contact info on success
    - Show error message on failure
    - _Requirements: 4.1, 4.2, 4.3, 4.4_
  
  - [x] 14.3 Implement reject request functionality

    - Show reject dialog with optional reason field
    - Call rejectRequest API
    - Update status on success
    - Show error message on failure
    - _Requirements: 5.1, 5.2, 5.3_
  
  - [x] 14.4 Implement mark as returned functionality

    - Show confirm button for APPROVED requests
    - Call markAsReturned API
    - Update status and item availability on success
    - Show success message
    - _Requirements: 6.1, 6.2, 6.4_
  
  - [x] 14.5 Write property test for received request information


    - **Property 7: Received request information completeness**
    - **Validates: Requirements 3.2**
  
  - [x] 14.6 Write property test for contact information visibility

    - **Property 8: Contact information visibility**
    - **Validates: Requirements 3.5**

- [x] 15. Integrate with ItemDetailPage





  - [x] 15.1 Add "Request to Borrow" button


    - Show button only for AVAILABLE items
    - Hide button if user is the owner
    - Show "Currently Borrowed" for BORROWED items
    - Show expected return date for borrowed items
    - _Requirements: 1.1, 11.1, 11.2_
  
  - [x] 15.2 Create borrow request form modal


    - Add date pickers for borrow and return dates
    - Add optional message textarea
    - Validate dates (no past dates, return after borrow)
    - Submit to createRequest API
    - Show success message and redirect
    - _Requirements: 1.2, 1.3, 12.1, 12.2_
  
  - [x] 15.3 Write property test for borrowed item display


    - **Property 27: Borrowed item display**
    - **Validates: Requirements 11.1**
  
  - [x] 15.4 Write property test for return date display


    - **Property 28: Return date display**
    - **Validates: Requirements 11.2**
  
  - [x] 15.5 Write property test for borrowed item request prevention


    - **Property 29: Borrowed item request prevention**
    - **Validates: Requirements 11.3**
  
  - [x] 15.6 Write property test for immediate availability after return


    - **Property 30: Immediate availability after return**
    - **Validates: Requirements 11.4**
  
  - [x] 15.7 Write property test for borrower information privacy


    - **Property 31: Borrower information privacy**
    - **Validates: Requirements 11.5**

- [x] 16. Add navigation and routing







  - [x] 16.1 Add routes for request pages

    - Add /requests/sent route for MyRequestsPage
    - Add /requests/received route for IncomingRequestsPage
    - Make routes protected (require authentication)
    - _Requirements: 2.1, 3.1_
  
  - [x] 16.2 Update navigation component

    - Add "My Requests" link
    - Add "Incoming Requests" link
    - Show notification badge for pending incoming requests
    - _Requirements: 14.1_

- [x] 17. Implement notifications




  - [x] 17.1 Add notification badge to navigation


    - Show count of pending incoming requests
    - Update count in real-time
    - Clear on viewing incoming requests page
    - _Requirements: 14.1, 14.2_
  
  - [x] 17.2 Add toast notifications for status changes


    - Show notification when request is approved
    - Show notification when request is rejected
    - Show notification when item is returned
    - Show notification when return is confirmed
    - _Requirements: 14.2, 14.3, 14.4_

- [x] 18. Add error handling and loading states





  - [x] 18.1 Implement error boundaries


    - Catch and display API errors
    - Show user-friendly error messages
    - Provide retry options
    - _Requirements: All_
  
  - [x] 18.2 Add loading skeletons


    - Show skeleton for request cards while loading
    - Show spinner for action buttons during API calls
    - Disable buttons during processing
    - _Requirements: All_
  
  - [x] 18.3 Implement optimistic updates


    - Update UI immediately on actions
    - Rollback on error
    - Show success feedback
    - _Requirements: All_

- [x] 19. Testing and validation





  - [x] 19.1 Write end-to-end test for complete borrow flow





    - Test borrower creates request
    - Test lender approves request
    - Test lender marks as returned
    - Test borrower confirms return
    - Verify item status changes throughout
    - _Requirements: 1.3, 4.1, 6.1, 7.1_
  
  - [x] 19.2 Write end-to-end test for rejection flow

    - Test borrower creates request
    - Test lender rejects request
    - Verify item remains available
    - _Requirements: 1.3, 5.1, 5.2_
  
  - [x] 19.3 Write end-to-end test for cancellation flow

    - Test borrower creates request
    - Test borrower cancels request
    - Verify request is deleted
    - _Requirements: 1.3, 9.1, 9.3_
  
  - [x] 19.4 Write property tests for authorization


    - Test non-owner cannot approve/reject/return
    - Test non-borrower cannot cancel/confirm
    - Test unauthenticated users cannot access endpoints
    - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5_

- [x] 20. Final checkpoint - Ensure all tests pass





  - Ensure all tests pass, ask the user if questions arise.

- [x] 21. Documentation and polish




  - [x] 21.1 Update API documentation


    - Document all borrow request endpoints
    - Add request/response examples
    - Document error responses
    - Document authorization requirements
    - _Requirements: All_
  
  - [x] 21.2 Update user documentation


    - Create user guide for borrowing items
    - Create user guide for lending items
    - Document request statuses and workflow
    - Add FAQ section
    - _Requirements: All_
  
  - [x] 21.3 Add UI polish


    - Ensure responsive design on all pages
    - Add smooth transitions and animations
    - Improve accessibility (ARIA labels, keyboard navigation)
    - Test on different screen sizes
    - _Requirements: All_

