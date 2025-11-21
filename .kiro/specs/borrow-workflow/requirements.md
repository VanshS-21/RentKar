# Requirements Document - Borrow Workflow

## Introduction

The Borrow Workflow feature enables users to request to borrow items from other users, manage those requests, and track the complete lifecycle from request to return. This feature implements the core peer-to-peer sharing functionality of RentKar, allowing lenders to approve or reject requests, and both parties to track the status of borrowed items through completion.

## Glossary

- **Borrower**: The user requesting to borrow an item
- **Lender**: The item owner who receives and responds to borrow requests
- **BorrowRequest**: An entity representing a request to borrow an item
- **Request Status**: The current state of a borrow request (PENDING, APPROVED, REJECTED, RETURNED, COMPLETED)
- **Borrow Period**: The time frame during which an item is borrowed (borrowDate to returnDate)
- **Request Message**: Optional message from borrower explaining why they need the item
- **Response Message**: Optional message from lender when approving/rejecting
- **Sent Requests**: Requests created by the current user (borrower view)
- **Received Requests**: Requests for items owned by the current user (lender view)

## Requirements

### Requirement 1

**User Story:** As a borrower, I want to request to borrow an item, so that I can use items I need without purchasing them.

#### Acceptance Criteria

1. WHEN a borrower views an available item THEN the Borrow Workflow System SHALL display a "Request to Borrow" button
2. WHEN a borrower clicks the borrow button THEN the Borrow Workflow System SHALL display a request form with borrow dates and optional message
3. WHEN a borrower submits a valid request THEN the Borrow Workflow System SHALL create a borrow request with status PENDING
4. WHEN a borrow request is created THEN the Borrow Workflow System SHALL associate it with the borrower, lender, and item
5. WHEN a borrower tries to borrow their own item THEN the Borrow Workflow System SHALL prevent the request and display an error message

### Requirement 2

**User Story:** As a borrower, I want to view all my sent borrow requests, so that I can track the status of items I've requested.

#### Acceptance Criteria

1. WHEN a borrower navigates to "My Requests" THEN the Borrow Workflow System SHALL display all requests created by that user
2. WHEN displaying sent requests THEN the Borrow Workflow System SHALL show item details, lender information, dates, and current status
3. WHEN a borrower filters by status THEN the Borrow Workflow System SHALL display only requests matching that status
4. WHEN a sent request list is empty THEN the Borrow Workflow System SHALL display a helpful message encouraging the user to browse items
5. WHEN displaying request dates THEN the Borrow Workflow System SHALL format them in a user-friendly way

### Requirement 3

**User Story:** As a lender, I want to view all incoming borrow requests for my items, so that I can decide whether to approve them.

#### Acceptance Criteria

1. WHEN a lender navigates to "Incoming Requests" THEN the Borrow Workflow System SHALL display all requests for items they own
2. WHEN displaying received requests THEN the Borrow Workflow System SHALL show borrower information, item details, dates, and request message
3. WHEN a lender filters by status THEN the Borrow Workflow System SHALL display only requests matching that status
4. WHEN a received request list is empty THEN the Borrow Workflow System SHALL display a message indicating no pending requests
5. WHEN displaying borrower information THEN the Borrow Workflow System SHALL include contact details for approved requests

### Requirement 4

**User Story:** As a lender, I want to approve borrow requests, so that I can lend my items to trusted borrowers.

#### Acceptance Criteria

1. WHEN a lender clicks "Approve" on a pending request THEN the Borrow Workflow System SHALL change the request status to APPROVED
2. WHEN a request is approved THEN the Borrow Workflow System SHALL change the item status to BORROWED
3. WHEN a request is approved THEN the Borrow Workflow System SHALL allow the lender to add an optional response message
4. WHEN a request is approved THEN the Borrow Workflow System SHALL display borrower contact information to the lender
5. WHEN approving a request THEN the Borrow Workflow System SHALL validate that the item is still available

### Requirement 5

**User Story:** As a lender, I want to reject borrow requests, so that I can decline requests that don't work for me.

#### Acceptance Criteria

1. WHEN a lender clicks "Reject" on a pending request THEN the Borrow Workflow System SHALL change the request status to REJECTED
2. WHEN a request is rejected THEN the Borrow Workflow System SHALL keep the item status as AVAILABLE
3. WHEN a request is rejected THEN the Borrow Workflow System SHALL allow the lender to add an optional reason message
4. WHEN a request is rejected THEN the Borrow Workflow System SHALL not display contact information
5. WHEN rejecting a request THEN the Borrow Workflow System SHALL validate that the request is still pending

### Requirement 6

**User Story:** As a lender, I want to mark items as returned, so that I can confirm when a borrower has given back my item.

#### Acceptance Criteria

1. WHEN a lender clicks "Mark as Returned" on an approved request THEN the Borrow Workflow System SHALL change the request status to RETURNED
2. WHEN a request is marked as returned THEN the Borrow Workflow System SHALL change the item status back to AVAILABLE
3. WHEN marking as returned THEN the Borrow Workflow System SHALL validate that the request is currently approved
4. WHEN a request is marked as returned THEN the Borrow Workflow System SHALL record the return timestamp
5. WHEN an item is returned THEN the Borrow Workflow System SHALL allow the item to be borrowed by others again

### Requirement 7

**User Story:** As a borrower, I want to confirm that I've returned an item, so that the transaction can be completed.

#### Acceptance Criteria

1. WHEN a borrower clicks "Confirm Return" on a returned request THEN the Borrow Workflow System SHALL change the request status to COMPLETED
2. WHEN a request is completed THEN the Borrow Workflow System SHALL finalize the transaction
3. WHEN confirming return THEN the Borrow Workflow System SHALL validate that the request status is RETURNED
4. WHEN a request is completed THEN the Borrow Workflow System SHALL record the completion timestamp
5. WHEN a transaction is completed THEN the Borrow Workflow System SHALL display it in the request history

### Requirement 8

**User Story:** As a user, I want to see clear status indicators for borrow requests, so that I can understand the current state at a glance.

#### Acceptance Criteria

1. WHEN displaying a pending request THEN the Borrow Workflow System SHALL show a yellow/orange badge with "Pending" text
2. WHEN displaying an approved request THEN the Borrow Workflow System SHALL show a green badge with "Approved" text
3. WHEN displaying a rejected request THEN the Borrow Workflow System SHALL show a red badge with "Rejected" text
4. WHEN displaying a returned request THEN the Borrow Workflow System SHALL show a blue badge with "Returned" text
5. WHEN displaying a completed request THEN the Borrow Workflow System SHALL show a gray badge with "Completed" text

### Requirement 9

**User Story:** As a user, I want to cancel my pending borrow requests, so that I can withdraw requests I no longer need.

#### Acceptance Criteria

1. WHEN a borrower clicks "Cancel" on a pending request THEN the Borrow Workflow System SHALL delete the request
2. WHEN canceling a request THEN the Borrow Workflow System SHALL validate that the request is still pending
3. WHEN a request is canceled THEN the Borrow Workflow System SHALL remove it from both borrower and lender views
4. WHEN canceling a request THEN the Borrow Workflow System SHALL not affect the item status
5. WHEN a request is canceled THEN the Borrow Workflow System SHALL show a confirmation dialog before deletion

### Requirement 10

**User Story:** As a user, I want to view the complete history of a borrow request, so that I can see all status changes and messages.

#### Acceptance Criteria

1. WHEN a user clicks on a request THEN the Borrow Workflow System SHALL display the full request details
2. WHEN displaying request details THEN the Borrow Workflow System SHALL show all status transitions with timestamps
3. WHEN displaying request details THEN the Borrow Workflow System SHALL show all messages exchanged between borrower and lender
4. WHEN displaying request details THEN the Borrow Workflow System SHALL show item information and images
5. WHEN displaying request details THEN the Borrow Workflow System SHALL show appropriate actions based on current status and user role

### Requirement 11

**User Story:** As a borrower, I want to see which items are currently borrowed, so that I know when they'll be available.

#### Acceptance Criteria

1. WHEN viewing an item with status BORROWED THEN the Borrow Workflow System SHALL display "Currently Borrowed" instead of "Request to Borrow"
2. WHEN viewing a borrowed item THEN the Borrow Workflow System SHALL show the expected return date
3. WHEN viewing a borrowed item THEN the Borrow Workflow System SHALL not allow new borrow requests
4. WHEN an item is returned THEN the Borrow Workflow System SHALL immediately show it as available again
5. WHEN viewing borrowed items THEN the Borrow Workflow System SHALL not display borrower information to other users

### Requirement 12

**User Story:** As a developer, I want proper validation for borrow requests, so that the system maintains data integrity.

#### Acceptance Criteria

1. WHEN creating a request THEN the Borrow Workflow System SHALL validate that borrow date is not in the past
2. WHEN creating a request THEN the Borrow Workflow System SHALL validate that return date is after borrow date
3. WHEN creating a request THEN the Borrow Workflow System SHALL validate that the item exists and is available
4. WHEN creating a request THEN the Borrow Workflow System SHALL validate that the borrower is authenticated
5. WHEN creating a request THEN the Borrow Workflow System SHALL validate that the borrower is not the item owner

### Requirement 13

**User Story:** As a developer, I want proper authorization for borrow request actions, so that users can only perform allowed operations.

#### Acceptance Criteria

1. WHEN a user tries to approve a request THEN the Borrow Workflow System SHALL verify they are the item owner
2. WHEN a user tries to reject a request THEN the Borrow Workflow System SHALL verify they are the item owner
3. WHEN a user tries to mark as returned THEN the Borrow Workflow System SHALL verify they are the item owner
4. WHEN a user tries to confirm return THEN the Borrow Workflow System SHALL verify they are the borrower
5. WHEN a user tries to cancel a request THEN the Borrow Workflow System SHALL verify they are the borrower

### Requirement 14

**User Story:** As a user, I want to receive notifications about request status changes, so that I stay informed about my transactions.

#### Acceptance Criteria

1. WHEN a lender receives a new request THEN the Borrow Workflow System SHALL display a notification badge on "Incoming Requests"
2. WHEN a borrower's request is approved THEN the Borrow Workflow System SHALL display a notification
3. WHEN a borrower's request is rejected THEN the Borrow Workflow System SHALL display a notification
4. WHEN an item is marked as returned THEN the Borrow Workflow System SHALL notify the borrower
5. WHEN viewing notifications THEN the Borrow Workflow System SHALL mark them as read

### Requirement 15

**User Story:** As a user, I want to see request statistics, so that I can understand my borrowing and lending activity.

#### Acceptance Criteria

1. WHEN viewing "My Requests" THEN the Borrow Workflow System SHALL display count of pending, approved, and completed requests
2. WHEN viewing "Incoming Requests" THEN the Borrow Workflow System SHALL display count of pending requests requiring action
3. WHEN viewing request statistics THEN the Borrow Workflow System SHALL update counts in real-time
4. WHEN a user has no requests THEN the Borrow Workflow System SHALL display zero counts
5. WHEN displaying statistics THEN the Borrow Workflow System SHALL use clear, readable formatting

