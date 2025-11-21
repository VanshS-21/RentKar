package com.rentkar.service;

import com.rentkar.dto.CreateBorrowRequestDTO;
import com.rentkar.model.*;
import com.rentkar.repository.BorrowRequestRepository;
import com.rentkar.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Edge case tests for borrow request validation
 * Requirements: 12.1, 12.2, 12.3, 12.5
 */
public class BorrowRequestValidationTest {
    
    private BorrowRequestService service;
    private ItemRepository mockItemRepo;
    private BorrowRequestRepository mockRequestRepo;
    private BorrowRequestMapper mapper;
    
    @BeforeEach
    void setUp() {
        mockItemRepo = Mockito.mock(ItemRepository.class);
        mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        mapper = new BorrowRequestMapper();
        service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
    }
    
    /**
     * Test past borrow dates are rejected
     * Requirements: 12.1
     */
    @Test
    void testPastBorrowDateIsRejected() {
        // Arrange
        Long itemId = 1L;
        LocalDate pastDate = LocalDate.now().minusDays(1);
        LocalDate futureDate = LocalDate.now().plusDays(7);
        CreateBorrowRequestDTO dto = new CreateBorrowRequestDTO(pastDate, futureDate, "Test message");
        
        User borrower = createUser(1L, "borrower", "borrower@test.com");
        User lender = createUser(2L, "lender", "lender@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.AVAILABLE);
        
        when(mockItemRepo.findById(itemId)).thenReturn(Optional.of(item));
        
        // Act & Assert
        assertThatThrownBy(() -> service.createRequest(itemId, dto, borrower))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Borrow date cannot be in the past");
    }
    
    /**
     * Test invalid date ranges are rejected (return date before borrow date)
     * Requirements: 12.2
     */
    @Test
    void testInvalidDateRangeIsRejected() {
        // Arrange
        Long itemId = 1L;
        LocalDate borrowDate = LocalDate.now().plusDays(7);
        LocalDate returnDate = LocalDate.now().plusDays(3); // Before borrow date
        CreateBorrowRequestDTO dto = new CreateBorrowRequestDTO(borrowDate, returnDate, "Test message");
        
        User borrower = createUser(1L, "borrower", "borrower@test.com");
        User lender = createUser(2L, "lender", "lender@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.AVAILABLE);
        
        when(mockItemRepo.findById(itemId)).thenReturn(Optional.of(item));
        
        // Act & Assert
        assertThatThrownBy(() -> service.createRequest(itemId, dto, borrower))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Return date must be after borrow date");
    }
    
    /**
     * Test invalid date ranges are rejected (return date equals borrow date)
     * Requirements: 12.2
     */
    @Test
    void testSameDateRangeIsRejected() {
        // Arrange
        Long itemId = 1L;
        LocalDate sameDate = LocalDate.now().plusDays(7);
        CreateBorrowRequestDTO dto = new CreateBorrowRequestDTO(sameDate, sameDate, "Test message");
        
        User borrower = createUser(1L, "borrower", "borrower@test.com");
        User lender = createUser(2L, "lender", "lender@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.AVAILABLE);
        
        when(mockItemRepo.findById(itemId)).thenReturn(Optional.of(item));
        
        // Act & Assert
        assertThatThrownBy(() -> service.createRequest(itemId, dto, borrower))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Return date must be after borrow date");
    }
    
    /**
     * Test self-borrowing is rejected
     * Requirements: 12.5
     */
    @Test
    void testSelfBorrowingIsRejected() {
        // Arrange
        Long itemId = 1L;
        Long userId = 1L;
        LocalDate borrowDate = LocalDate.now();
        LocalDate returnDate = LocalDate.now().plusDays(7);
        CreateBorrowRequestDTO dto = new CreateBorrowRequestDTO(borrowDate, returnDate, "Test message");
        
        User user = createUser(userId, "user", "user@test.com");
        Item item = createItem(itemId, "Test Item", user, ItemStatus.AVAILABLE); // Same user owns the item
        
        when(mockItemRepo.findById(itemId)).thenReturn(Optional.of(item));
        
        // Act & Assert
        assertThatThrownBy(() -> service.createRequest(itemId, dto, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot borrow your own item");
    }
    
    /**
     * Test unavailable items are rejected (BORROWED status)
     * Requirements: 12.3
     */
    @Test
    void testBorrowedItemIsRejected() {
        // Arrange
        Long itemId = 1L;
        LocalDate borrowDate = LocalDate.now();
        LocalDate returnDate = LocalDate.now().plusDays(7);
        CreateBorrowRequestDTO dto = new CreateBorrowRequestDTO(borrowDate, returnDate, "Test message");
        
        User borrower = createUser(1L, "borrower", "borrower@test.com");
        User lender = createUser(2L, "lender", "lender@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.BORROWED); // Item is borrowed
        
        when(mockItemRepo.findById(itemId)).thenReturn(Optional.of(item));
        
        // Act & Assert
        assertThatThrownBy(() -> service.createRequest(itemId, dto, borrower))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Item is not available for borrowing");
    }
    
    /**
     * Test unavailable items are rejected (UNAVAILABLE status)
     * Requirements: 12.3
     */
    @Test
    void testUnavailableItemIsRejected() {
        // Arrange
        Long itemId = 1L;
        LocalDate borrowDate = LocalDate.now();
        LocalDate returnDate = LocalDate.now().plusDays(7);
        CreateBorrowRequestDTO dto = new CreateBorrowRequestDTO(borrowDate, returnDate, "Test message");
        
        User borrower = createUser(1L, "borrower", "borrower@test.com");
        User lender = createUser(2L, "lender", "lender@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.UNAVAILABLE); // Item is unavailable
        
        when(mockItemRepo.findById(itemId)).thenReturn(Optional.of(item));
        
        // Act & Assert
        assertThatThrownBy(() -> service.createRequest(itemId, dto, borrower))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Item is not available for borrowing");
    }
    
    /**
     * Test non-existent item is rejected
     * Requirements: 12.3
     */
    @Test
    void testNonExistentItemIsRejected() {
        // Arrange
        Long itemId = 999L;
        LocalDate borrowDate = LocalDate.now();
        LocalDate returnDate = LocalDate.now().plusDays(7);
        CreateBorrowRequestDTO dto = new CreateBorrowRequestDTO(borrowDate, returnDate, "Test message");
        
        User borrower = createUser(1L, "borrower", "borrower@test.com");
        
        when(mockItemRepo.findById(itemId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> service.createRequest(itemId, dto, borrower))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Item not found");
    }
    
    /**
     * Test approving non-pending request fails
     * Requirements: 4.5
     */
    @Test
    void testApprovingNonPendingRequestFails() {
        // Arrange
        Long requestId = 1L;
        User borrower = createUser(1L, "borrower", "borrower@test.com");
        User lender = createUser(2L, "lender", "lender@test.com");
        Item item = createItem(1L, "Test Item", lender, ItemStatus.AVAILABLE);
        
        BorrowRequest request = createBorrowRequest(requestId, item, borrower, lender, RequestStatus.APPROVED);
        
        when(mockRequestRepo.findById(requestId)).thenReturn(Optional.of(request));
        
        // Act & Assert
        assertThatThrownBy(() -> service.approveRequest(requestId, "Approved", lender))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only pending requests can be approved");
    }
    
    /**
     * Test non-owner cannot approve
     * Requirements: 13.1
     */
    @Test
    void testNonOwnerCannotApprove() {
        // Arrange
        Long requestId = 1L;
        User borrower = createUser(1L, "borrower", "borrower@test.com");
        User lender = createUser(2L, "lender", "lender@test.com");
        User otherUser = createUser(3L, "other", "other@test.com");
        Item item = createItem(1L, "Test Item", lender, ItemStatus.AVAILABLE);
        
        BorrowRequest request = createBorrowRequest(requestId, item, borrower, lender, RequestStatus.PENDING);
        
        when(mockRequestRepo.findById(requestId)).thenReturn(Optional.of(request));
        
        // Act & Assert
        assertThatThrownBy(() -> service.approveRequest(requestId, "Approved", otherUser))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Only the item owner can approve this request");
    }
    
    /**
     * Test approving unavailable item fails
     * Requirements: 4.5
     */
    @Test
    void testApprovingUnavailableItemFails() {
        // Arrange
        Long requestId = 1L;
        User borrower = createUser(1L, "borrower", "borrower@test.com");
        User lender = createUser(2L, "lender", "lender@test.com");
        Item item = createItem(1L, "Test Item", lender, ItemStatus.BORROWED); // Item is already borrowed
        
        BorrowRequest request = createBorrowRequest(requestId, item, borrower, lender, RequestStatus.PENDING);
        
        when(mockRequestRepo.findById(requestId)).thenReturn(Optional.of(request));
        
        // Act & Assert
        assertThatThrownBy(() -> service.approveRequest(requestId, "Approved", lender))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Item is no longer available");
    }
    
    /**
     * Test rejecting non-pending request fails
     * Requirements: 5.5
     */
    @Test
    void testRejectingNonPendingRequestFails() {
        // Arrange
        Long requestId = 1L;
        User borrower = createUser(1L, "borrower", "borrower@test.com");
        User lender = createUser(2L, "lender", "lender@test.com");
        Item item = createItem(1L, "Test Item", lender, ItemStatus.AVAILABLE);
        
        BorrowRequest request = createBorrowRequest(requestId, item, borrower, lender, RequestStatus.APPROVED);
        
        when(mockRequestRepo.findById(requestId)).thenReturn(Optional.of(request));
        
        // Act & Assert
        assertThatThrownBy(() -> service.rejectRequest(requestId, "Not available", lender))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only pending requests can be rejected");
    }
    
    /**
     * Test non-owner cannot reject
     * Requirements: 13.2
     */
    @Test
    void testNonOwnerCannotReject() {
        // Arrange
        Long requestId = 1L;
        User borrower = createUser(1L, "borrower", "borrower@test.com");
        User lender = createUser(2L, "lender", "lender@test.com");
        User otherUser = createUser(3L, "other", "other@test.com");
        Item item = createItem(1L, "Test Item", lender, ItemStatus.AVAILABLE);
        
        BorrowRequest request = createBorrowRequest(requestId, item, borrower, lender, RequestStatus.PENDING);
        
        when(mockRequestRepo.findById(requestId)).thenReturn(Optional.of(request));
        
        // Act & Assert
        assertThatThrownBy(() -> service.rejectRequest(requestId, "Not available", otherUser))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Only the item owner can reject this request");
    }
    
    /**
     * Test marking non-approved request as returned fails
     * Requirements: 6.3
     */
    @Test
    void testMarkingNonApprovedRequestAsReturnedFails() {
        // Arrange
        Long requestId = 1L;
        User borrower = createUser(1L, "borrower", "borrower@test.com");
        User lender = createUser(2L, "lender", "lender@test.com");
        Item item = createItem(1L, "Test Item", lender, ItemStatus.AVAILABLE);
        
        BorrowRequest request = createBorrowRequest(requestId, item, borrower, lender, RequestStatus.PENDING);
        
        when(mockRequestRepo.findById(requestId)).thenReturn(Optional.of(request));
        
        // Act & Assert
        assertThatThrownBy(() -> service.markAsReturned(requestId, lender))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only approved requests can be marked as returned");
    }
    
    /**
     * Test non-owner cannot mark as returned
     * Requirements: 13.3
     */
    @Test
    void testNonOwnerCannotMarkAsReturned() {
        // Arrange
        Long requestId = 1L;
        User borrower = createUser(1L, "borrower", "borrower@test.com");
        User lender = createUser(2L, "lender", "lender@test.com");
        User otherUser = createUser(3L, "other", "other@test.com");
        Item item = createItem(1L, "Test Item", lender, ItemStatus.BORROWED);
        
        BorrowRequest request = createBorrowRequest(requestId, item, borrower, lender, RequestStatus.APPROVED);
        
        when(mockRequestRepo.findById(requestId)).thenReturn(Optional.of(request));
        
        // Act & Assert
        assertThatThrownBy(() -> service.markAsReturned(requestId, otherUser))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Only the item owner can mark the item as returned");
    }
    
    /**
     * Test confirming non-returned request fails
     * Requirements: 7.3
     */
    @Test
    void testConfirmingNonReturnedRequestFails() {
        // Arrange
        Long requestId = 1L;
        User borrower = createUser(1L, "borrower", "borrower@test.com");
        User lender = createUser(2L, "lender", "lender@test.com");
        Item item = createItem(1L, "Test Item", lender, ItemStatus.BORROWED);
        
        BorrowRequest request = createBorrowRequest(requestId, item, borrower, lender, RequestStatus.APPROVED);
        
        when(mockRequestRepo.findById(requestId)).thenReturn(Optional.of(request));
        
        // Act & Assert
        assertThatThrownBy(() -> service.confirmReturn(requestId, borrower))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only returned requests can be confirmed");
    }
    
    /**
     * Test non-borrower cannot confirm return
     * Requirements: 13.4
     */
    @Test
    void testNonBorrowerCannotConfirmReturn() {
        // Arrange
        Long requestId = 1L;
        User borrower = createUser(1L, "borrower", "borrower@test.com");
        User lender = createUser(2L, "lender", "lender@test.com");
        User otherUser = createUser(3L, "other", "other@test.com");
        Item item = createItem(1L, "Test Item", lender, ItemStatus.AVAILABLE);
        
        BorrowRequest request = createBorrowRequest(requestId, item, borrower, lender, RequestStatus.RETURNED);
        setBorrowRequestField(request, "returnedAt", LocalDateTime.now());
        
        when(mockRequestRepo.findById(requestId)).thenReturn(Optional.of(request));
        
        // Act & Assert
        assertThatThrownBy(() -> service.confirmReturn(requestId, otherUser))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Only the borrower can confirm the return");
    }
    
    /**
     * Test canceling non-pending request fails
     * Requirements: 9.2
     */
    @Test
    void testCancelingNonPendingRequestFails() {
        // Arrange
        Long requestId = 1L;
        User borrower = createUser(1L, "borrower", "borrower@test.com");
        User lender = createUser(2L, "lender", "lender@test.com");
        Item item = createItem(1L, "Test Item", lender, ItemStatus.BORROWED);
        
        BorrowRequest request = createBorrowRequest(requestId, item, borrower, lender, RequestStatus.APPROVED);
        
        when(mockRequestRepo.findById(requestId)).thenReturn(Optional.of(request));
        
        // Act & Assert
        assertThatThrownBy(() -> service.cancelRequest(requestId, borrower))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only pending requests can be canceled");
    }
    
    /**
     * Test non-borrower cannot cancel
     * Requirements: 13.5
     */
    @Test
    void testNonBorrowerCannotCancel() {
        // Arrange
        Long requestId = 1L;
        User borrower = createUser(1L, "borrower", "borrower@test.com");
        User lender = createUser(2L, "lender", "lender@test.com");
        User otherUser = createUser(3L, "other", "other@test.com");
        Item item = createItem(1L, "Test Item", lender, ItemStatus.AVAILABLE);
        
        BorrowRequest request = createBorrowRequest(requestId, item, borrower, lender, RequestStatus.PENDING);
        
        when(mockRequestRepo.findById(requestId)).thenReturn(Optional.of(request));
        
        // Act & Assert
        assertThatThrownBy(() -> service.cancelRequest(requestId, otherUser))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Only the borrower can cancel this request");
    }
    
    private BorrowRequest createBorrowRequest(Long id, Item item, User borrower, User lender, RequestStatus status) {
        BorrowRequest request = new BorrowRequest();
        setBorrowRequestField(request, "id", id);
        setBorrowRequestField(request, "item", item);
        setBorrowRequestField(request, "borrower", borrower);
        setBorrowRequestField(request, "lender", lender);
        setBorrowRequestField(request, "status", status);
        setBorrowRequestField(request, "borrowDate", LocalDate.now().plusDays(1));
        setBorrowRequestField(request, "returnDate", LocalDate.now().plusDays(8));
        setBorrowRequestField(request, "createdAt", LocalDateTime.now());
        setBorrowRequestField(request, "updatedAt", LocalDateTime.now());
        return request;
    }
    
    private void setBorrowRequestField(BorrowRequest request, String fieldName, Object value) {
        try {
            var field = BorrowRequest.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(request, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    // Helper methods
    private User createUser(Long id, String username, String email) {
        User user = new User();
        setUserField(user, "id", id);
        setUserField(user, "username", username);
        setUserField(user, "email", email);
        setUserField(user, "password", "hashedPassword");
        setUserField(user, "fullName", "Test User");
        setUserField(user, "role", Role.USER);
        setUserField(user, "createdAt", LocalDateTime.now());
        setUserField(user, "updatedAt", LocalDateTime.now());
        return user;
    }
    
    private Item createItem(Long id, String title, User owner, ItemStatus status) {
        Item item = new Item();
        setItemField(item, "id", id);
        setItemField(item, "title", title);
        setItemField(item, "description", "Test description");
        setItemField(item, "category", "Electronics");
        setItemField(item, "imageUrl", "https://test.com/image.jpg");
        setItemField(item, "status", status);
        setItemField(item, "owner", owner);
        setItemField(item, "createdAt", LocalDateTime.now());
        setItemField(item, "updatedAt", LocalDateTime.now());
        return item;
    }
    
    private void setUserField(User user, String fieldName, Object value) {
        try {
            var field = User.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(user, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void setItemField(Item item, String fieldName, Object value) {
        try {
            var field = Item.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(item, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
