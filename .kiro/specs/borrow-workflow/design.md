# Design Document - Borrow Workflow

## Overview

The Borrow Workflow feature implements the core peer-to-peer sharing functionality of RentKar. It enables users to request items, manage those requests through their lifecycle, and track the complete borrowing process from initial request to final completion. The system uses a state machine approach with five distinct states (PENDING, APPROVED, REJECTED, RETURNED, COMPLETED) and enforces proper authorization and validation at each transition.

The implementation consists of a BorrowRequest entity with status tracking, REST endpoints for all workflow actions, and frontend components for both borrower and lender perspectives.

## Architecture

### Backend Architecture

```
BorrowRequestController (REST API Layer)
    ↓
BorrowRequestService (Business Logic Layer)
    ↓
BorrowRequestRepository (Data Access Layer)
    ↓
MySQL Database
```

**Key Components:**
- **BorrowRequest Entity**: Core entity with status, dates, messages, and relationships
- **BorrowRequestService**: Business logic for workflow transitions and validation
- **BorrowRequestRepository**: JPA repository with custom queries
- **BorrowRequestController**: REST endpoints for all workflow actions
- **Authorization**: Role-based checks (borrower vs lender actions)

### Frontend Architecture

```
MyRequestsPage / IncomingRequestsPage
    ↓
RequestCard / RequestDetailModal Components
    ↓
borrowRequestService API client
    ↓
Backend API
```

**Key Components:**
- **MyRequestsPage**: Borrower view of sent requests
- **IncomingRequestsPage**: Lender view of received requests
- **RequestCard**: Display component for request summary
- **RequestDetailModal**: Full request details with actions
- **StatusBadge**: Visual status indicators
- **borrowRequestService**: API client for all request operations

## Components and Interfaces

### Backend Components

#### BorrowRequest Entity
```java
@Entity
@Table(name = "borrow_requests")
public class BorrowRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
    
    @ManyToOne
    @JoinColumn(name = "borrower_id", nullable = false)
    private User borrower;
    
    @ManyToOne
    @JoinColumn(name = "lender_id", nullable = false)
    private User lender;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status; // PENDING, APPROVED, REJECTED, RETURNED, COMPLETED
    
    @Column(columnDefinition = "TEXT")
    private String requestMessage;
    
    @Column(columnDefinition = "TEXT")
    private String responseMessage;
    
    @Column(nullable = false)
    private LocalDate borrowDate;
    
    @Column(nullable = false)
    private LocalDate returnDate;
    
    private LocalDateTime returnedAt;
    private LocalDateTime completedAt;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Getters and setters
}
```

#### BorrowRequestService Interface
```java
public interface BorrowRequestService {
    /**
     * Create a new borrow request
     */
    BorrowRequest createRequest(Long itemId, CreateBorrowRequestDTO dto, User borrower);
    
    /**
     * Get all requests sent by a user (borrower view)
     */
    List<BorrowRequest> getSentRequests(User borrower, RequestStatus status);
    
    /**
     * Get all requests received by a user (lender view)
     */
    List<BorrowRequest> getReceivedRequests(User lender, RequestStatus status);
    
    /**
     * Get request by ID with authorization check
     */
    BorrowRequest getRequestById(Long id, User user);
    
    /**
     * Approve a borrow request (lender only)
     */
    BorrowRequest approveRequest(Long id, String responseMessage, User lender);
    
    /**
     * Reject a borrow request (lender only)
     */
    BorrowRequest rejectRequest(Long id, String responseMessage, User lender);
    
    /**
     * Mark item as returned (lender only)
     */
    BorrowRequest markAsReturned(Long id, User lender);
    
    /**
     * Confirm return and complete transaction (borrower only)
     */
    BorrowRequest confirmReturn(Long id, User borrower);
    
    /**
     * Cancel a pending request (borrower only)
     */
    void cancelRequest(Long id, User borrower);
    
    /**
     * Get request statistics for a user
     */
    RequestStatistics getStatistics(User user);
}
```

#### DTOs
```java
public class CreateBorrowRequestDTO {
    @NotNull
    private LocalDate borrowDate;
    
    @NotNull
    private LocalDate returnDate;
    
    @Size(max = 500)
    private String requestMessage;
}

public class BorrowRequestDTO {
    private Long id;
    private ItemDTO item;
    private UserDTO borrower;
    private UserDTO lender;
    private RequestStatus status;
    private String requestMessage;
    private String responseMessage;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private LocalDateTime returnedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public class RequestStatistics {
    private int pendingCount;
    private int approvedCount;
    private int rejectedCount;
    private int returnedCount;
    private int completedCount;
    private int totalSent;
    private int totalReceived;
}
```

### Frontend Components

#### MyRequestsPage Component
```typescript
const MyRequestsPage: React.FC = () => {
  const [requests, setRequests] = useState<BorrowRequest[]>([]);
  const [filter, setFilter] = useState<RequestStatus | 'ALL'>('ALL');
  const [loading, setLoading] = useState(true);
  const [statistics, setStatistics] = useState<RequestStatistics | null>(null);
  
  // Fetch sent requests
  // Filter by status
  // Display request cards
  // Handle cancel action
}
```

#### IncomingRequestsPage Component
```typescript
const IncomingRequestsPage: React.FC = () => {
  const [requests, setRequests] = useState<BorrowRequest[]>([]);
  const [filter, setFilter] = useState<RequestStatus | 'ALL'>('ALL');
  const [loading, setLoading] = useState(true);
  const [statistics, setStatistics] = useState<RequestStatistics | null>(null);
  
  // Fetch received requests
  // Filter by status
  // Display request cards
  // Handle approve/reject/return actions
}
```

#### RequestCard Component
```typescript
interface RequestCardProps {
  request: BorrowRequest;
  viewType: 'sent' | 'received';
  onAction: (action: string, requestId: number) => void;
}

const RequestCard: React.FC<RequestCardProps> = ({ request, viewType, onAction }) => {
  // Display request summary
  // Show status badge
  // Show appropriate actions based on status and viewType
}
```

## Data Models

### Request Status Flow

```
PENDING → APPROVED → RETURNED → COMPLETED
   ↓
REJECTED
```

**Status Transitions:**
- PENDING → APPROVED (lender approves)
- PENDING → REJECTED (lender rejects)
- APPROVED → RETURNED (lender marks as returned)
- RETURNED → COMPLETED (borrower confirms return)
- PENDING → DELETED (borrower cancels)

### Database Schema

```sql
CREATE TABLE borrow_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_id BIGINT NOT NULL,
    borrower_id BIGINT NOT NULL,
    lender_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    request_message TEXT,
    response_message TEXT,
    borrow_date DATE NOT NULL,
    return_date DATE NOT NULL,
    returned_at DATETIME,
    completed_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (borrower_id) REFERENCES users(id),
    FOREIGN KEY (lender_id) REFERENCES users(id),
    INDEX idx_borrower (borrower_id),
    INDEX idx_lender (lender_id),
    INDEX idx_item (item_id),
    INDEX idx_status (status)
);
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Request creation with PENDING status
*For any* valid borrow request creation, the created request should have status PENDING.
**Validates: Requirements 1.3**

### Property 2: Request associations
*For any* created borrow request, it should be correctly associated with the borrower, lender, and item.
**Validates: Requirements 1.4**

### Property 3: Sent requests filtering
*For any* user, the sent requests list should contain only requests where that user is the borrower.
**Validates: Requirements 2.1**

### Property 4: Sent request information completeness
*For any* displayed sent request, it should contain item details, lender information, dates, and current status.
**Validates: Requirements 2.2**

### Property 5: Status filtering accuracy
*For any* status filter applied to requests, all returned requests should have exactly that status.
**Validates: Requirements 2.3, 3.3**

### Property 6: Received requests filtering
*For any* user, the received requests list should contain only requests for items owned by that user.
**Validates: Requirements 3.1**

### Property 7: Received request information completeness
*For any* displayed received request, it should contain borrower information, item details, dates, and request message.
**Validates: Requirements 3.2**

### Property 8: Contact information visibility
*For any* approved request, contact details should be visible to the lender; for non-approved requests, they should not be visible.
**Validates: Requirements 3.5**

### Property 9: Approval status transition
*For any* pending request that is approved, the request status should change to APPROVED.
**Validates: Requirements 4.1**

### Property 10: Item status on approval
*For any* approved request, the associated item status should change to BORROWED.
**Validates: Requirements 4.2**

### Property 11: Contact information on approval
*For any* approved request, borrower contact information should be displayed to the lender.
**Validates: Requirements 4.4**

### Property 12: Rejection status transition
*For any* pending request that is rejected, the request status should change to REJECTED.
**Validates: Requirements 5.1**

### Property 13: Item status on rejection
*For any* rejected request, the associated item status should remain AVAILABLE.
**Validates: Requirements 5.2**

### Property 14: Contact information on rejection
*For any* rejected request, contact information should not be displayed.
**Validates: Requirements 5.4**

### Property 15: Return status transition
*For any* approved request that is marked as returned, the request status should change to RETURNED.
**Validates: Requirements 6.1**

### Property 16: Item status on return
*For any* request marked as returned, the associated item status should change back to AVAILABLE.
**Validates: Requirements 6.2**

### Property 17: Return timestamp recording
*For any* request marked as returned, a return timestamp should be recorded.
**Validates: Requirements 6.4**

### Property 18: Item availability after return
*For any* item that has been returned, it should be available for new borrow requests.
**Validates: Requirements 6.5**

### Property 19: Completion status transition
*For any* returned request that is confirmed, the request status should change to COMPLETED.
**Validates: Requirements 7.1**

### Property 20: Completion timestamp recording
*For any* completed request, a completion timestamp should be recorded.
**Validates: Requirements 7.4**

### Property 21: Completed request in history
*For any* completed request, it should appear in the request history.
**Validates: Requirements 7.5**

### Property 22: Status badge rendering
*For any* request status, the correct badge color and text should be displayed (PENDING=yellow, APPROVED=green, REJECTED=red, RETURNED=blue, COMPLETED=gray).
**Validates: Requirements 8.1, 8.2, 8.3, 8.4, 8.5**

### Property 23: Request cancellation
*For any* pending request that is canceled, it should be deleted from the system.
**Validates: Requirements 9.1**

### Property 24: Cancellation visibility
*For any* canceled request, it should not appear in either borrower or lender views.
**Validates: Requirements 9.3**

### Property 25: Item status on cancellation
*For any* canceled request, the associated item status should remain unchanged.
**Validates: Requirements 9.4**

### Property 26: Request detail completeness
*For any* request detail view, it should display all status transitions, messages, item information, and appropriate actions.
**Validates: Requirements 10.1, 10.2, 10.3, 10.4, 10.5**

### Property 27: Borrowed item display
*For any* item with status BORROWED, the UI should display "Currently Borrowed" instead of "Request to Borrow".
**Validates: Requirements 11.1**

### Property 28: Return date display
*For any* borrowed item, the expected return date should be displayed.
**Validates: Requirements 11.2**

### Property 29: Borrowed item request prevention
*For any* item with status BORROWED, new borrow requests should not be allowed.
**Validates: Requirements 11.3**

### Property 30: Immediate availability after return
*For any* item that is marked as returned, it should immediately show as available.
**Validates: Requirements 11.4**

### Property 31: Borrower information privacy
*For any* borrowed item, borrower information should only be visible to the lender, not to other users.
**Validates: Requirements 11.5**

### Property 32: Statistics accuracy
*For any* user, the displayed request statistics should accurately reflect the count of requests in each status.
**Validates: Requirements 15.1, 15.2, 15.3, 15.4, 15.5**

## Error Handling

### Backend Error Handling

**Validation Errors:**
- Past borrow dates (400 Bad Request)
- Return date before borrow date (400 Bad Request)
- Item not available (400 Bad Request)
- Self-borrowing attempt (400 Bad Request)
- Invalid status transitions (400 Bad Request)

**Authorization Errors:**
- Non-owner trying to approve/reject/return (403 Forbidden)
- Non-borrower trying to confirm return/cancel (403 Forbidden)
- Unauthenticated requests (401 Unauthorized)

**Not Found Errors:**
- Request not found (404 Not Found)
- Item not found (404 Not Found)

**Conflict Errors:**
- Item already borrowed (409 Conflict)
- Request already processed (409 Conflict)

### Frontend Error Handling

**API Error Responses:**
- Display user-friendly error messages
- Show validation errors inline on forms
- Handle authorization errors with appropriate messages
- Retry failed requests with user confirmation

**Network Errors:**
- Handle connection failures gracefully
- Display offline message
- Suggest checking internet connection

**State Management:**
- Optimistic updates with rollback on error
- Refresh data after successful actions
- Handle concurrent modifications

## Testing Strategy

### Unit Testing

**Backend Unit Tests:**
- Test BorrowRequestService business logic
- Test status transition validation
- Test authorization checks
- Test date validation
- Mock repositories and external dependencies

**Frontend Unit Tests:**
- Test component rendering
- Test status badge display
- Test action button visibility
- Test form validation
- Mock API calls

### Property-Based Testing

The system will use property-based testing to verify correctness properties across a wide range of inputs. For Java backend testing, we will use **jqwik**. For JavaScript/TypeScript frontend testing, we will use **fast-check**.

**Property Test Configuration:**
- Each property test should run a minimum of 100 iterations
- Use smart generators that create valid requests with various statuses
- Each property test must include a comment tag referencing the design document property
- Tag format: `// Feature: borrow-workflow, Property X: <property description>`

**Backend Property Tests (using jqwik):**
- Generate random requests and verify status transitions
- Generate random users and verify authorization
- Generate random dates and verify validation
- Test all status flow paths
- Test filtering and querying logic

**Frontend Property Tests (using fast-check):**
- Generate random request data and verify UI rendering
- Generate random status values and verify badge display
- Test action button visibility across statuses
- Test filtering and sorting logic

### Integration Testing

**Backend Integration Tests:**
- Test complete request lifecycle (create → approve → return → complete)
- Test rejection flow
- Test cancellation flow
- Test authorization with real database
- Test concurrent request handling

**Frontend Integration Tests:**
- Test complete user flows for borrower
- Test complete user flows for lender
- Test request creation and submission
- Test status updates and notifications
- Mock backend API responses

### End-to-End Testing

- Test borrower creates request for available item
- Test lender approves request and item becomes borrowed
- Test lender marks item as returned
- Test borrower confirms return and completes transaction
- Test lender rejects request
- Test borrower cancels pending request
- Test filtering and searching requests

## Security Considerations

**Authorization:**
- Verify user identity for all actions
- Check ownership for lender actions (approve, reject, return)
- Check borrower identity for borrower actions (cancel, confirm return)
- Prevent unauthorized access to request details

**Data Privacy:**
- Hide contact information until request is approved
- Only show borrower info to lender
- Only show lender info to borrower
- Don't expose other users' request data

**Input Validation:**
- Validate all dates (no past dates, valid ranges)
- Validate request messages (length limits, sanitization)
- Validate status transitions (only allowed transitions)
- Validate item availability before approval

**Rate Limiting:**
- Limit request creation per user per hour
- Prevent spam requests to same item
- Throttle status update actions

## Performance Considerations

**Database Optimization:**
- Index on borrower_id, lender_id, item_id, status
- Use pagination for request lists
- Optimize queries with proper joins
- Cache frequently accessed data

**Frontend Performance:**
- Lazy load request details
- Paginate request lists
- Use optimistic updates for better UX
- Cache request data locally
- Debounce filter changes

**API Response Time:**
- Keep request creation under 500ms
- Keep status updates under 300ms
- Keep list queries under 1s
- Use database connection pooling

## Deployment Considerations

**Database Migration:**
- Create borrow_requests table
- Add foreign key constraints
- Create indexes for performance
- Add status enum values

**Environment Variables:**
- Configure request limits
- Configure notification settings
- Configure date validation rules

**Monitoring:**
- Track request creation rate
- Monitor status transition patterns
- Alert on high rejection rates
- Track completion rates

**Graceful Degradation:**
- Handle database connection failures
- Queue failed notifications for retry
- Provide manual refresh options
- Display cached data when offline

---

*Last Updated: November 21, 2025*
