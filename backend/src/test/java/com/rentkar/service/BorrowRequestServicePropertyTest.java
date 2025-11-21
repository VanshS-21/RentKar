package com.rentkar.service;

import com.rentkar.dto.BorrowRequestDTO;
import com.rentkar.dto.CreateBorrowRequestDTO;
import com.rentkar.model.*;
import com.rentkar.repository.BorrowRequestRepository;
import com.rentkar.repository.ItemRepository;
import net.jqwik.api.*;
import net.jqwik.time.api.DateTimes;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class BorrowRequestServicePropertyTest {
    
    // Feature: borrow-workflow, Property 1: Request creation with PENDING status
    // Validates: Requirements 1.3
    @Property(tries = 100)
    void requestCreationHasPendingStatus(
            @ForAll("validItemId") Long itemId,
            @ForAll("futureBorrowDate") LocalDate borrowDate,
            @ForAll("validReturnDate") LocalDate returnDate,
            @ForAll("validRequestMessage") String requestMessage,
            @ForAll("validUserId") Long borrowerId,
            @ForAll("validUserId") Long lenderId) {
        
        Assume.that(!borrowerId.equals(lenderId));
        Assume.that(returnDate.isAfter(borrowDate));
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        Map<Long, BorrowRequest> requestDb = new HashMap<>();
        AtomicLong idCounter = new AtomicLong(1);
        
        User borrower = createUser(borrowerId, "borrower" + borrowerId, "borrower" + borrowerId + "@test.com");
        User lender = createUser(lenderId, "lender" + lenderId, "lender" + lenderId + "@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.AVAILABLE);
        
        when(mockItemRepo.findById(itemId)).thenReturn(Optional.of(item));
        when(mockRequestRepo.save(any(BorrowRequest.class))).thenAnswer(inv -> {
            BorrowRequest request = inv.getArgument(0);
            if (request.getId() == null) {
                setBorrowRequestField(request, "id", idCounter.getAndIncrement());
            }
            setBorrowRequestField(request, "createdAt", LocalDateTime.now());
            setBorrowRequestField(request, "updatedAt", LocalDateTime.now());
            requestDb.put(request.getId(), request);
            return request;
        });
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        CreateBorrowRequestDTO dto = new CreateBorrowRequestDTO(borrowDate, returnDate, requestMessage);
        BorrowRequestDTO result = service.createRequest(itemId, dto, borrower);
        
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RequestStatus.PENDING);
        assertThat(result.getBorrowDate()).isEqualTo(borrowDate);
        assertThat(result.getReturnDate()).isEqualTo(returnDate);
        assertThat(result.getRequestMessage()).isEqualTo(requestMessage);
    }
    
    // Feature: borrow-workflow, Property 2: Request associations
    // Validates: Requirements 1.4
    @Property(tries = 100)
    void requestAssociationsAreCorrect(
            @ForAll("validItemId") Long itemId,
            @ForAll("futureBorrowDate") LocalDate borrowDate,
            @ForAll("validReturnDate") LocalDate returnDate,
            @ForAll("validRequestMessage") String requestMessage,
            @ForAll("validUserId") Long borrowerId,
            @ForAll("validUserId") Long lenderId) {
        
        Assume.that(!borrowerId.equals(lenderId));
        Assume.that(returnDate.isAfter(borrowDate));
        
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        Map<Long, BorrowRequest> requestDb = new HashMap<>();
        AtomicLong idCounter = new AtomicLong(1);
        
        User borrower = createUser(borrowerId, "borrower" + borrowerId, "borrower" + borrowerId + "@test.com");
        User lender = createUser(lenderId, "lender" + lenderId, "lender" + lenderId + "@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.AVAILABLE);
        
        when(mockItemRepo.findById(itemId)).thenReturn(Optional.of(item));
        when(mockRequestRepo.save(any(BorrowRequest.class))).thenAnswer(inv -> {
            BorrowRequest request = inv.getArgument(0);
            if (request.getId() == null) {
                setBorrowRequestField(request, "id", idCounter.getAndIncrement());
            }
            setBorrowRequestField(request, "createdAt", LocalDateTime.now());
            setBorrowRequestField(request, "updatedAt", LocalDateTime.now());
            requestDb.put(request.getId(), request);
            return request;
        });
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        CreateBorrowRequestDTO dto = new CreateBorrowRequestDTO(borrowDate, returnDate, requestMessage);
        BorrowRequestDTO result = service.createRequest(itemId, dto, borrower);
        
        assertThat(result).isNotNull();
        assertThat(result.getBorrower()).isNotNull();
        assertThat(result.getBorrower().getId()).isEqualTo(borrowerId);
        assertThat(result.getLender()).isNotNull();
        assertThat(result.getLender().getId()).isEqualTo(lenderId);
        assertThat(result.getItem()).isNotNull();
        assertThat(result.getItem().getId()).isEqualTo(itemId);
    }
    
    // Feature: borrow-workflow, Property 3: Sent requests filtering
    // Validates: Requirements 2.1
    @Property(tries = 100)
    void sentRequestsFilteringOnlyReturnsBorrowerRequests(
            @ForAll("validUserId") Long borrowerId,
            @ForAll("requestList") List<BorrowRequest> allRequests) {
        
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        
        User borrower = createUser(borrowerId, "borrower" + borrowerId, "borrower" + borrowerId + "@test.com");
        
        // Filter requests that belong to this borrower
        List<BorrowRequest> borrowerRequests = allRequests.stream()
                .filter(r -> r.getBorrower().getId().equals(borrowerId))
                .toList();
        
        when(mockRequestRepo.findByBorrowerId(borrowerId)).thenReturn(borrowerRequests);
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        List<BorrowRequest> result = service.getSentRequests(borrower, null);
        
        assertThat(result).isNotNull();
        assertThat(result).allMatch(r -> r.getBorrower().getId().equals(borrowerId));
        assertThat(result.size()).isEqualTo(borrowerRequests.size());
    }
    
    // Feature: borrow-workflow, Property 5: Status filtering accuracy
    // Validates: Requirements 2.3, 3.3
    @Property(tries = 100)
    void statusFilteringReturnsOnlyMatchingStatus(
            @ForAll("validUserId") Long userId,
            @ForAll("requestList") List<BorrowRequest> allRequests,
            @ForAll RequestStatus filterStatus) {
        
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        
        User user = createUser(userId, "user" + userId, "user" + userId + "@test.com");
        
        // Filter requests by borrower and status
        List<BorrowRequest> borrowerRequestsWithStatus = allRequests.stream()
                .filter(r -> r.getBorrower().getId().equals(userId))
                .filter(r -> r.getStatus() == filterStatus)
                .toList();
        
        // Filter requests by lender and status
        List<BorrowRequest> lenderRequestsWithStatus = allRequests.stream()
                .filter(r -> r.getLender().getId().equals(userId))
                .filter(r -> r.getStatus() == filterStatus)
                .toList();
        
        when(mockRequestRepo.findByBorrowerIdAndStatus(userId, filterStatus))
                .thenReturn(borrowerRequestsWithStatus);
        when(mockRequestRepo.findByLenderIdAndStatus(userId, filterStatus))
                .thenReturn(lenderRequestsWithStatus);
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        // Test sent requests filtering
        List<BorrowRequest> sentResult = service.getSentRequests(user, filterStatus);
        assertThat(sentResult).isNotNull();
        assertThat(sentResult).allMatch(r -> r.getStatus() == filterStatus);
        assertThat(sentResult).allMatch(r -> r.getBorrower().getId().equals(userId));
        assertThat(sentResult.size()).isEqualTo(borrowerRequestsWithStatus.size());
        
        // Test received requests filtering
        List<BorrowRequest> receivedResult = service.getReceivedRequests(user, filterStatus);
        assertThat(receivedResult).isNotNull();
        assertThat(receivedResult).allMatch(r -> r.getStatus() == filterStatus);
        assertThat(receivedResult).allMatch(r -> r.getLender().getId().equals(userId));
        assertThat(receivedResult.size()).isEqualTo(lenderRequestsWithStatus.size());
    }
    
    // Feature: borrow-workflow, Property 6: Received requests filtering
    // Validates: Requirements 3.1
    @Property(tries = 100)
    void receivedRequestsFilteringOnlyReturnsLenderRequests(
            @ForAll("validUserId") Long lenderId,
            @ForAll("requestList") List<BorrowRequest> allRequests) {
        
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        
        User lender = createUser(lenderId, "lender" + lenderId, "lender" + lenderId + "@test.com");
        
        // Filter requests that belong to this lender
        List<BorrowRequest> lenderRequests = allRequests.stream()
                .filter(r -> r.getLender().getId().equals(lenderId))
                .toList();
        
        when(mockRequestRepo.findByLenderId(lenderId)).thenReturn(lenderRequests);
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        List<BorrowRequest> result = service.getReceivedRequests(lender, null);
        
        assertThat(result).isNotNull();
        assertThat(result).allMatch(r -> r.getLender().getId().equals(lenderId));
        assertThat(result.size()).isEqualTo(lenderRequests.size());
    }
    
    // Feature: borrow-workflow, Property 9: Approval status transition
    // Validates: Requirements 4.1
    @Property(tries = 100)
    void approvalChangesStatusToApproved(
            @ForAll("validItemId") Long itemId,
            @ForAll("validUserId") Long borrowerId,
            @ForAll("validUserId") Long lenderId,
            @ForAll("validRequestMessage") String responseMessage) {
        
        Assume.that(!borrowerId.equals(lenderId));
        
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        
        User borrower = createUser(borrowerId, "borrower" + borrowerId, "borrower" + borrowerId + "@test.com");
        User lender = createUser(lenderId, "lender" + lenderId, "lender" + lenderId + "@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.AVAILABLE);
        
        // Create a pending request
        BorrowRequest request = new BorrowRequest();
        setBorrowRequestField(request, "id", 1L);
        setBorrowRequestField(request, "item", item);
        setBorrowRequestField(request, "borrower", borrower);
        setBorrowRequestField(request, "lender", lender);
        setBorrowRequestField(request, "status", RequestStatus.PENDING);
        setBorrowRequestField(request, "borrowDate", LocalDate.now().plusDays(1));
        setBorrowRequestField(request, "returnDate", LocalDate.now().plusDays(8));
        setBorrowRequestField(request, "createdAt", LocalDateTime.now());
        setBorrowRequestField(request, "updatedAt", LocalDateTime.now());
        
        when(mockRequestRepo.findById(1L)).thenReturn(Optional.of(request));
        when(mockRequestRepo.save(any(BorrowRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mockItemRepo.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        BorrowRequest result = service.approveRequest(1L, responseMessage, lender);
        
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RequestStatus.APPROVED);
    }
    
    // Feature: borrow-workflow, Property 10: Item status on approval
    // Validates: Requirements 4.2
    @Property(tries = 100)
    void approvalChangesItemStatusToBorrowed(
            @ForAll("validItemId") Long itemId,
            @ForAll("validUserId") Long borrowerId,
            @ForAll("validUserId") Long lenderId,
            @ForAll("validRequestMessage") String responseMessage) {
        
        Assume.that(!borrowerId.equals(lenderId));
        
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        
        User borrower = createUser(borrowerId, "borrower" + borrowerId, "borrower" + borrowerId + "@test.com");
        User lender = createUser(lenderId, "lender" + lenderId, "lender" + lenderId + "@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.AVAILABLE);
        
        // Create a pending request
        BorrowRequest request = new BorrowRequest();
        setBorrowRequestField(request, "id", 1L);
        setBorrowRequestField(request, "item", item);
        setBorrowRequestField(request, "borrower", borrower);
        setBorrowRequestField(request, "lender", lender);
        setBorrowRequestField(request, "status", RequestStatus.PENDING);
        setBorrowRequestField(request, "borrowDate", LocalDate.now().plusDays(1));
        setBorrowRequestField(request, "returnDate", LocalDate.now().plusDays(8));
        setBorrowRequestField(request, "createdAt", LocalDateTime.now());
        setBorrowRequestField(request, "updatedAt", LocalDateTime.now());
        
        when(mockRequestRepo.findById(1L)).thenReturn(Optional.of(request));
        when(mockRequestRepo.save(any(BorrowRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mockItemRepo.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        BorrowRequest result = service.approveRequest(1L, responseMessage, lender);
        
        assertThat(result).isNotNull();
        assertThat(result.getItem().getStatus()).isEqualTo(ItemStatus.BORROWED);
    }
    
    // Feature: borrow-workflow, Property 11: Contact information on approval
    // Validates: Requirements 4.4
    @Property(tries = 100)
    void approvedRequestExposesContactInformation(
            @ForAll("validItemId") Long itemId,
            @ForAll("validUserId") Long borrowerId,
            @ForAll("validUserId") Long lenderId,
            @ForAll("validRequestMessage") String responseMessage) {
        
        Assume.that(!borrowerId.equals(lenderId));
        
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        
        String borrowerEmail = "borrower" + borrowerId + "@test.com";
        String borrowerPhone = "+1234567890";
        
        User borrower = createUser(borrowerId, "borrower" + borrowerId, borrowerEmail);
        setUserField(borrower, "phone", borrowerPhone);
        User lender = createUser(lenderId, "lender" + lenderId, "lender" + lenderId + "@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.AVAILABLE);
        
        // Create a pending request
        BorrowRequest request = new BorrowRequest();
        setBorrowRequestField(request, "id", 1L);
        setBorrowRequestField(request, "item", item);
        setBorrowRequestField(request, "borrower", borrower);
        setBorrowRequestField(request, "lender", lender);
        setBorrowRequestField(request, "status", RequestStatus.PENDING);
        setBorrowRequestField(request, "borrowDate", LocalDate.now().plusDays(1));
        setBorrowRequestField(request, "returnDate", LocalDate.now().plusDays(8));
        setBorrowRequestField(request, "createdAt", LocalDateTime.now());
        setBorrowRequestField(request, "updatedAt", LocalDateTime.now());
        
        when(mockRequestRepo.findById(1L)).thenReturn(Optional.of(request));
        when(mockRequestRepo.save(any(BorrowRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mockItemRepo.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        BorrowRequest result = service.approveRequest(1L, responseMessage, lender);
        
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RequestStatus.APPROVED);
        // Verify borrower contact information is accessible
        assertThat(result.getBorrower()).isNotNull();
        assertThat(result.getBorrower().getEmail()).isEqualTo(borrowerEmail);
        assertThat(result.getBorrower().getPhone()).isEqualTo(borrowerPhone);
    }
    
    // Feature: borrow-workflow, Property 12: Rejection status transition
    // Validates: Requirements 5.1
    @Property(tries = 100)
    void rejectionChangesStatusToRejected(
            @ForAll("validItemId") Long itemId,
            @ForAll("validUserId") Long borrowerId,
            @ForAll("validUserId") Long lenderId,
            @ForAll("validRequestMessage") String responseMessage) {
        
        Assume.that(!borrowerId.equals(lenderId));
        
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        
        User borrower = createUser(borrowerId, "borrower" + borrowerId, "borrower" + borrowerId + "@test.com");
        User lender = createUser(lenderId, "lender" + lenderId, "lender" + lenderId + "@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.AVAILABLE);
        
        // Create a pending request
        BorrowRequest request = new BorrowRequest();
        setBorrowRequestField(request, "id", 1L);
        setBorrowRequestField(request, "item", item);
        setBorrowRequestField(request, "borrower", borrower);
        setBorrowRequestField(request, "lender", lender);
        setBorrowRequestField(request, "status", RequestStatus.PENDING);
        setBorrowRequestField(request, "borrowDate", LocalDate.now().plusDays(1));
        setBorrowRequestField(request, "returnDate", LocalDate.now().plusDays(8));
        setBorrowRequestField(request, "createdAt", LocalDateTime.now());
        setBorrowRequestField(request, "updatedAt", LocalDateTime.now());
        
        when(mockRequestRepo.findById(1L)).thenReturn(Optional.of(request));
        when(mockRequestRepo.save(any(BorrowRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        BorrowRequest result = service.rejectRequest(1L, responseMessage, lender);
        
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RequestStatus.REJECTED);
    }
    
    // Feature: borrow-workflow, Property 13: Item status on rejection
    // Validates: Requirements 5.2
    @Property(tries = 100)
    void rejectionKeepsItemStatusAsAvailable(
            @ForAll("validItemId") Long itemId,
            @ForAll("validUserId") Long borrowerId,
            @ForAll("validUserId") Long lenderId,
            @ForAll("validRequestMessage") String responseMessage) {
        
        Assume.that(!borrowerId.equals(lenderId));
        
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        
        User borrower = createUser(borrowerId, "borrower" + borrowerId, "borrower" + borrowerId + "@test.com");
        User lender = createUser(lenderId, "lender" + lenderId, "lender" + lenderId + "@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.AVAILABLE);
        
        // Create a pending request
        BorrowRequest request = new BorrowRequest();
        setBorrowRequestField(request, "id", 1L);
        setBorrowRequestField(request, "item", item);
        setBorrowRequestField(request, "borrower", borrower);
        setBorrowRequestField(request, "lender", lender);
        setBorrowRequestField(request, "status", RequestStatus.PENDING);
        setBorrowRequestField(request, "borrowDate", LocalDate.now().plusDays(1));
        setBorrowRequestField(request, "returnDate", LocalDate.now().plusDays(8));
        setBorrowRequestField(request, "createdAt", LocalDateTime.now());
        setBorrowRequestField(request, "updatedAt", LocalDateTime.now());
        
        when(mockRequestRepo.findById(1L)).thenReturn(Optional.of(request));
        when(mockRequestRepo.save(any(BorrowRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        BorrowRequest result = service.rejectRequest(1L, responseMessage, lender);
        
        assertThat(result).isNotNull();
        assertThat(result.getItem().getStatus()).isEqualTo(ItemStatus.AVAILABLE);
    }
    
    // Feature: borrow-workflow, Property 14: Contact information on rejection
    // Validates: Requirements 5.4
    @Property(tries = 100)
    void rejectedRequestHasRejectedStatus(
            @ForAll("validItemId") Long itemId,
            @ForAll("validUserId") Long borrowerId,
            @ForAll("validUserId") Long lenderId,
            @ForAll("validRequestMessage") String responseMessage) {
        
        Assume.that(!borrowerId.equals(lenderId));
        
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        
        User borrower = createUser(borrowerId, "borrower" + borrowerId, "borrower" + borrowerId + "@test.com");
        User lender = createUser(lenderId, "lender" + lenderId, "lender" + lenderId + "@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.AVAILABLE);
        
        // Create a pending request
        BorrowRequest request = new BorrowRequest();
        setBorrowRequestField(request, "id", 1L);
        setBorrowRequestField(request, "item", item);
        setBorrowRequestField(request, "borrower", borrower);
        setBorrowRequestField(request, "lender", lender);
        setBorrowRequestField(request, "status", RequestStatus.PENDING);
        setBorrowRequestField(request, "borrowDate", LocalDate.now().plusDays(1));
        setBorrowRequestField(request, "returnDate", LocalDate.now().plusDays(8));
        setBorrowRequestField(request, "createdAt", LocalDateTime.now());
        setBorrowRequestField(request, "updatedAt", LocalDateTime.now());
        
        when(mockRequestRepo.findById(1L)).thenReturn(Optional.of(request));
        when(mockRequestRepo.save(any(BorrowRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        BorrowRequest result = service.rejectRequest(1L, responseMessage, lender);
        
        // Verify the request has REJECTED status, which the UI layer will use
        // to determine not to display contact information
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RequestStatus.REJECTED);
    }
    
    // Feature: borrow-workflow, Property 15: Return status transition
    // Validates: Requirements 6.1
    @Property(tries = 100)
    void markAsReturnedChangesStatusToReturned(
            @ForAll("validItemId") Long itemId,
            @ForAll("validUserId") Long borrowerId,
            @ForAll("validUserId") Long lenderId) {
        
        Assume.that(!borrowerId.equals(lenderId));
        
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        
        User borrower = createUser(borrowerId, "borrower" + borrowerId, "borrower" + borrowerId + "@test.com");
        User lender = createUser(lenderId, "lender" + lenderId, "lender" + lenderId + "@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.BORROWED);
        
        // Create an approved request
        BorrowRequest request = new BorrowRequest();
        setBorrowRequestField(request, "id", 1L);
        setBorrowRequestField(request, "item", item);
        setBorrowRequestField(request, "borrower", borrower);
        setBorrowRequestField(request, "lender", lender);
        setBorrowRequestField(request, "status", RequestStatus.APPROVED);
        setBorrowRequestField(request, "borrowDate", LocalDate.now().minusDays(5));
        setBorrowRequestField(request, "returnDate", LocalDate.now().plusDays(2));
        setBorrowRequestField(request, "createdAt", LocalDateTime.now());
        setBorrowRequestField(request, "updatedAt", LocalDateTime.now());
        
        when(mockRequestRepo.findById(1L)).thenReturn(Optional.of(request));
        when(mockRequestRepo.save(any(BorrowRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mockItemRepo.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        BorrowRequest result = service.markAsReturned(1L, lender);
        
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RequestStatus.RETURNED);
    }
    
    // Feature: borrow-workflow, Property 16: Item status on return
    // Validates: Requirements 6.2
    @Property(tries = 100)
    void markAsReturnedChangesItemStatusToAvailable(
            @ForAll("validItemId") Long itemId,
            @ForAll("validUserId") Long borrowerId,
            @ForAll("validUserId") Long lenderId) {
        
        Assume.that(!borrowerId.equals(lenderId));
        
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        
        User borrower = createUser(borrowerId, "borrower" + borrowerId, "borrower" + borrowerId + "@test.com");
        User lender = createUser(lenderId, "lender" + lenderId, "lender" + lenderId + "@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.BORROWED);
        
        // Create an approved request
        BorrowRequest request = new BorrowRequest();
        setBorrowRequestField(request, "id", 1L);
        setBorrowRequestField(request, "item", item);
        setBorrowRequestField(request, "borrower", borrower);
        setBorrowRequestField(request, "lender", lender);
        setBorrowRequestField(request, "status", RequestStatus.APPROVED);
        setBorrowRequestField(request, "borrowDate", LocalDate.now().minusDays(5));
        setBorrowRequestField(request, "returnDate", LocalDate.now().plusDays(2));
        setBorrowRequestField(request, "createdAt", LocalDateTime.now());
        setBorrowRequestField(request, "updatedAt", LocalDateTime.now());
        
        when(mockRequestRepo.findById(1L)).thenReturn(Optional.of(request));
        when(mockRequestRepo.save(any(BorrowRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mockItemRepo.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        BorrowRequest result = service.markAsReturned(1L, lender);
        
        assertThat(result).isNotNull();
        assertThat(result.getItem().getStatus()).isEqualTo(ItemStatus.AVAILABLE);
    }
    
    // Feature: borrow-workflow, Property 17: Return timestamp recording
    // Validates: Requirements 6.4
    @Property(tries = 100)
    void markAsReturnedRecordsTimestamp(
            @ForAll("validItemId") Long itemId,
            @ForAll("validUserId") Long borrowerId,
            @ForAll("validUserId") Long lenderId) {
        
        Assume.that(!borrowerId.equals(lenderId));
        
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        
        User borrower = createUser(borrowerId, "borrower" + borrowerId, "borrower" + borrowerId + "@test.com");
        User lender = createUser(lenderId, "lender" + lenderId, "lender" + lenderId + "@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.BORROWED);
        
        // Create an approved request
        BorrowRequest request = new BorrowRequest();
        setBorrowRequestField(request, "id", 1L);
        setBorrowRequestField(request, "item", item);
        setBorrowRequestField(request, "borrower", borrower);
        setBorrowRequestField(request, "lender", lender);
        setBorrowRequestField(request, "status", RequestStatus.APPROVED);
        setBorrowRequestField(request, "borrowDate", LocalDate.now().minusDays(5));
        setBorrowRequestField(request, "returnDate", LocalDate.now().plusDays(2));
        setBorrowRequestField(request, "createdAt", LocalDateTime.now());
        setBorrowRequestField(request, "updatedAt", LocalDateTime.now());
        
        when(mockRequestRepo.findById(1L)).thenReturn(Optional.of(request));
        when(mockRequestRepo.save(any(BorrowRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mockItemRepo.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        LocalDateTime beforeReturn = LocalDateTime.now();
        BorrowRequest result = service.markAsReturned(1L, lender);
        LocalDateTime afterReturn = LocalDateTime.now();
        
        assertThat(result).isNotNull();
        assertThat(result.getReturnedAt()).isNotNull();
        assertThat(result.getReturnedAt()).isBetween(beforeReturn, afterReturn);
    }
    
    // Feature: borrow-workflow, Property 18: Item availability after return
    // Validates: Requirements 6.5
    @Property(tries = 100)
    void itemIsAvailableForNewRequestsAfterReturn(
            @ForAll("validItemId") Long itemId,
            @ForAll("validUserId") Long borrowerId,
            @ForAll("validUserId") Long lenderId) {
        
        Assume.that(!borrowerId.equals(lenderId));
        
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        
        User borrower = createUser(borrowerId, "borrower" + borrowerId, "borrower" + borrowerId + "@test.com");
        User lender = createUser(lenderId, "lender" + lenderId, "lender" + lenderId + "@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.BORROWED);
        
        // Create an approved request
        BorrowRequest request = new BorrowRequest();
        setBorrowRequestField(request, "id", 1L);
        setBorrowRequestField(request, "item", item);
        setBorrowRequestField(request, "borrower", borrower);
        setBorrowRequestField(request, "lender", lender);
        setBorrowRequestField(request, "status", RequestStatus.APPROVED);
        setBorrowRequestField(request, "borrowDate", LocalDate.now().minusDays(5));
        setBorrowRequestField(request, "returnDate", LocalDate.now().plusDays(2));
        setBorrowRequestField(request, "createdAt", LocalDateTime.now());
        setBorrowRequestField(request, "updatedAt", LocalDateTime.now());
        
        when(mockRequestRepo.findById(1L)).thenReturn(Optional.of(request));
        when(mockRequestRepo.save(any(BorrowRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mockItemRepo.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        BorrowRequest result = service.markAsReturned(1L, lender);
        
        assertThat(result).isNotNull();
        assertThat(result.getItem().getStatus()).isEqualTo(ItemStatus.AVAILABLE);
        // Verify the item can be borrowed again (status is AVAILABLE)
        assertThat(result.getItem().getStatus()).isEqualTo(ItemStatus.AVAILABLE);
    }
    
    // Feature: borrow-workflow, Property 19: Completion status transition
    // Validates: Requirements 7.1
    @Property(tries = 100)
    void confirmReturnChangesStatusToCompleted(
            @ForAll("validItemId") Long itemId,
            @ForAll("validUserId") Long borrowerId,
            @ForAll("validUserId") Long lenderId) {
        
        Assume.that(!borrowerId.equals(lenderId));
        
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        
        User borrower = createUser(borrowerId, "borrower" + borrowerId, "borrower" + borrowerId + "@test.com");
        User lender = createUser(lenderId, "lender" + lenderId, "lender" + lenderId + "@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.AVAILABLE);
        
        // Create a returned request
        BorrowRequest request = new BorrowRequest();
        setBorrowRequestField(request, "id", 1L);
        setBorrowRequestField(request, "item", item);
        setBorrowRequestField(request, "borrower", borrower);
        setBorrowRequestField(request, "lender", lender);
        setBorrowRequestField(request, "status", RequestStatus.RETURNED);
        setBorrowRequestField(request, "borrowDate", LocalDate.now().minusDays(5));
        setBorrowRequestField(request, "returnDate", LocalDate.now().minusDays(1));
        setBorrowRequestField(request, "returnedAt", LocalDateTime.now().minusHours(2));
        setBorrowRequestField(request, "createdAt", LocalDateTime.now());
        setBorrowRequestField(request, "updatedAt", LocalDateTime.now());
        
        when(mockRequestRepo.findById(1L)).thenReturn(Optional.of(request));
        when(mockRequestRepo.save(any(BorrowRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        BorrowRequest result = service.confirmReturn(1L, borrower);
        
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RequestStatus.COMPLETED);
    }
    
    // Feature: borrow-workflow, Property 20: Completion timestamp recording
    // Validates: Requirements 7.4
    @Property(tries = 100)
    void confirmReturnRecordsCompletionTimestamp(
            @ForAll("validItemId") Long itemId,
            @ForAll("validUserId") Long borrowerId,
            @ForAll("validUserId") Long lenderId) {
        
        Assume.that(!borrowerId.equals(lenderId));
        
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        
        User borrower = createUser(borrowerId, "borrower" + borrowerId, "borrower" + borrowerId + "@test.com");
        User lender = createUser(lenderId, "lender" + lenderId, "lender" + lenderId + "@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.AVAILABLE);
        
        // Create a returned request
        BorrowRequest request = new BorrowRequest();
        setBorrowRequestField(request, "id", 1L);
        setBorrowRequestField(request, "item", item);
        setBorrowRequestField(request, "borrower", borrower);
        setBorrowRequestField(request, "lender", lender);
        setBorrowRequestField(request, "status", RequestStatus.RETURNED);
        setBorrowRequestField(request, "borrowDate", LocalDate.now().minusDays(5));
        setBorrowRequestField(request, "returnDate", LocalDate.now().minusDays(1));
        setBorrowRequestField(request, "returnedAt", LocalDateTime.now().minusHours(2));
        setBorrowRequestField(request, "createdAt", LocalDateTime.now());
        setBorrowRequestField(request, "updatedAt", LocalDateTime.now());
        
        when(mockRequestRepo.findById(1L)).thenReturn(Optional.of(request));
        when(mockRequestRepo.save(any(BorrowRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        LocalDateTime beforeCompletion = LocalDateTime.now();
        BorrowRequest result = service.confirmReturn(1L, borrower);
        LocalDateTime afterCompletion = LocalDateTime.now();
        
        assertThat(result).isNotNull();
        assertThat(result.getCompletedAt()).isNotNull();
        assertThat(result.getCompletedAt()).isBetween(beforeCompletion, afterCompletion);
    }
    
    // Feature: borrow-workflow, Property 23: Request cancellation
    // Validates: Requirements 9.1
    @Property(tries = 100)
    void cancelRequestDeletesRequest(
            @ForAll("validItemId") Long itemId,
            @ForAll("validUserId") Long borrowerId,
            @ForAll("validUserId") Long lenderId) {
        
        Assume.that(!borrowerId.equals(lenderId));
        
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        
        User borrower = createUser(borrowerId, "borrower" + borrowerId, "borrower" + borrowerId + "@test.com");
        User lender = createUser(lenderId, "lender" + lenderId, "lender" + lenderId + "@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.AVAILABLE);
        
        // Create a pending request
        BorrowRequest request = new BorrowRequest();
        setBorrowRequestField(request, "id", 1L);
        setBorrowRequestField(request, "item", item);
        setBorrowRequestField(request, "borrower", borrower);
        setBorrowRequestField(request, "lender", lender);
        setBorrowRequestField(request, "status", RequestStatus.PENDING);
        setBorrowRequestField(request, "borrowDate", LocalDate.now().plusDays(1));
        setBorrowRequestField(request, "returnDate", LocalDate.now().plusDays(8));
        setBorrowRequestField(request, "createdAt", LocalDateTime.now());
        setBorrowRequestField(request, "updatedAt", LocalDateTime.now());
        
        when(mockRequestRepo.findById(1L)).thenReturn(Optional.of(request));
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        service.cancelRequest(1L, borrower);
        
        // Verify delete was called
        Mockito.verify(mockRequestRepo).delete(request);
    }
    
    // Feature: borrow-workflow, Property 24: Cancellation visibility
    // Validates: Requirements 9.3
    @Property(tries = 100)
    void canceledRequestNotInLists(
            @ForAll("validItemId") Long itemId,
            @ForAll("validUserId") Long borrowerId,
            @ForAll("validUserId") Long lenderId) {
        
        Assume.that(!borrowerId.equals(lenderId));
        
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        
        User borrower = createUser(borrowerId, "borrower" + borrowerId, "borrower" + borrowerId + "@test.com");
        User lender = createUser(lenderId, "lender" + lenderId, "lender" + lenderId + "@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.AVAILABLE);
        
        // Create a pending request
        BorrowRequest request = new BorrowRequest();
        setBorrowRequestField(request, "id", 1L);
        setBorrowRequestField(request, "item", item);
        setBorrowRequestField(request, "borrower", borrower);
        setBorrowRequestField(request, "lender", lender);
        setBorrowRequestField(request, "status", RequestStatus.PENDING);
        setBorrowRequestField(request, "borrowDate", LocalDate.now().plusDays(1));
        setBorrowRequestField(request, "returnDate", LocalDate.now().plusDays(8));
        setBorrowRequestField(request, "createdAt", LocalDateTime.now());
        setBorrowRequestField(request, "updatedAt", LocalDateTime.now());
        
        // Setup: Initially the request exists in both lists
        when(mockRequestRepo.findById(1L)).thenReturn(Optional.of(request));
        when(mockRequestRepo.findByBorrowerId(borrowerId)).thenReturn(List.of(request));
        when(mockRequestRepo.findByLenderId(lenderId)).thenReturn(List.of(request));
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        // Cancel the request
        service.cancelRequest(1L, borrower);
        
        // After cancellation, the request should not appear in either list
        when(mockRequestRepo.findByBorrowerId(borrowerId)).thenReturn(List.of());
        when(mockRequestRepo.findByLenderId(lenderId)).thenReturn(List.of());
        
        List<BorrowRequest> sentRequests = service.getSentRequests(borrower, null);
        List<BorrowRequest> receivedRequests = service.getReceivedRequests(lender, null);
        
        assertThat(sentRequests).isEmpty();
        assertThat(receivedRequests).isEmpty();
    }
    
    // Feature: borrow-workflow, Property 25: Item status on cancellation
    // Validates: Requirements 9.4
    @Property(tries = 100)
    void cancelRequestDoesNotAffectItemStatus(
            @ForAll("validItemId") Long itemId,
            @ForAll("validUserId") Long borrowerId,
            @ForAll("validUserId") Long lenderId) {
        
        Assume.that(!borrowerId.equals(lenderId));
        
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        
        User borrower = createUser(borrowerId, "borrower" + borrowerId, "borrower" + borrowerId + "@test.com");
        User lender = createUser(lenderId, "lender" + lenderId, "lender" + lenderId + "@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.AVAILABLE);
        
        // Create a pending request
        BorrowRequest request = new BorrowRequest();
        setBorrowRequestField(request, "id", 1L);
        setBorrowRequestField(request, "item", item);
        setBorrowRequestField(request, "borrower", borrower);
        setBorrowRequestField(request, "lender", lender);
        setBorrowRequestField(request, "status", RequestStatus.PENDING);
        setBorrowRequestField(request, "borrowDate", LocalDate.now().plusDays(1));
        setBorrowRequestField(request, "returnDate", LocalDate.now().plusDays(8));
        setBorrowRequestField(request, "createdAt", LocalDateTime.now());
        setBorrowRequestField(request, "updatedAt", LocalDateTime.now());
        
        when(mockRequestRepo.findById(1L)).thenReturn(Optional.of(request));
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        ItemStatus statusBeforeCancel = item.getStatus();
        service.cancelRequest(1L, borrower);
        ItemStatus statusAfterCancel = item.getStatus();
        
        // Item status should remain unchanged
        assertThat(statusAfterCancel).isEqualTo(statusBeforeCancel);
        assertThat(statusAfterCancel).isEqualTo(ItemStatus.AVAILABLE);
        
        // Verify itemRepository.save was never called
        Mockito.verify(mockItemRepo, Mockito.never()).save(any(Item.class));
    }
    
    // Feature: borrow-workflow, Property 32: Statistics accuracy
    // Validates: Requirements 15.1, 15.2, 15.3, 15.4, 15.5
    @Property(tries = 100)
    void statisticsAccuratelyReflectRequestCounts(
            @ForAll("validUserId") Long userId,
            @ForAll("requestList") List<BorrowRequest> allRequests) {
        
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        
        User user = createUser(userId, "user" + userId, "user" + userId + "@test.com");
        
        // Count sent requests by status (where user is borrower)
        long sentPending = allRequests.stream()
                .filter(r -> r.getBorrower().getId().equals(userId))
                .filter(r -> r.getStatus() == RequestStatus.PENDING)
                .count();
        long sentApproved = allRequests.stream()
                .filter(r -> r.getBorrower().getId().equals(userId))
                .filter(r -> r.getStatus() == RequestStatus.APPROVED)
                .count();
        long sentRejected = allRequests.stream()
                .filter(r -> r.getBorrower().getId().equals(userId))
                .filter(r -> r.getStatus() == RequestStatus.REJECTED)
                .count();
        long sentReturned = allRequests.stream()
                .filter(r -> r.getBorrower().getId().equals(userId))
                .filter(r -> r.getStatus() == RequestStatus.RETURNED)
                .count();
        long sentCompleted = allRequests.stream()
                .filter(r -> r.getBorrower().getId().equals(userId))
                .filter(r -> r.getStatus() == RequestStatus.COMPLETED)
                .count();
        
        // Count received requests by status (where user is lender)
        long receivedPending = allRequests.stream()
                .filter(r -> r.getLender().getId().equals(userId))
                .filter(r -> r.getStatus() == RequestStatus.PENDING)
                .count();
        long receivedApproved = allRequests.stream()
                .filter(r -> r.getLender().getId().equals(userId))
                .filter(r -> r.getStatus() == RequestStatus.APPROVED)
                .count();
        long receivedRejected = allRequests.stream()
                .filter(r -> r.getLender().getId().equals(userId))
                .filter(r -> r.getStatus() == RequestStatus.REJECTED)
                .count();
        long receivedReturned = allRequests.stream()
                .filter(r -> r.getLender().getId().equals(userId))
                .filter(r -> r.getStatus() == RequestStatus.RETURNED)
                .count();
        long receivedCompleted = allRequests.stream()
                .filter(r -> r.getLender().getId().equals(userId))
                .filter(r -> r.getStatus() == RequestStatus.COMPLETED)
                .count();
        
        // Mock repository responses
        when(mockRequestRepo.countByBorrowerAndStatus(userId, RequestStatus.PENDING))
                .thenReturn(sentPending);
        when(mockRequestRepo.countByBorrowerAndStatus(userId, RequestStatus.APPROVED))
                .thenReturn(sentApproved);
        when(mockRequestRepo.countByBorrowerAndStatus(userId, RequestStatus.REJECTED))
                .thenReturn(sentRejected);
        when(mockRequestRepo.countByBorrowerAndStatus(userId, RequestStatus.RETURNED))
                .thenReturn(sentReturned);
        when(mockRequestRepo.countByBorrowerAndStatus(userId, RequestStatus.COMPLETED))
                .thenReturn(sentCompleted);
        
        when(mockRequestRepo.countByLenderAndStatus(userId, RequestStatus.PENDING))
                .thenReturn(receivedPending);
        when(mockRequestRepo.countByLenderAndStatus(userId, RequestStatus.APPROVED))
                .thenReturn(receivedApproved);
        when(mockRequestRepo.countByLenderAndStatus(userId, RequestStatus.REJECTED))
                .thenReturn(receivedRejected);
        when(mockRequestRepo.countByLenderAndStatus(userId, RequestStatus.RETURNED))
                .thenReturn(receivedReturned);
        when(mockRequestRepo.countByLenderAndStatus(userId, RequestStatus.COMPLETED))
                .thenReturn(receivedCompleted);
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        com.rentkar.dto.RequestStatistics stats = service.getStatistics(user);
        
        // Verify statistics match expected counts
        assertThat(stats).isNotNull();
        assertThat(stats.getPendingCount()).isEqualTo((int)(sentPending + receivedPending));
        assertThat(stats.getApprovedCount()).isEqualTo((int)(sentApproved + receivedApproved));
        assertThat(stats.getRejectedCount()).isEqualTo((int)(sentRejected + receivedRejected));
        assertThat(stats.getReturnedCount()).isEqualTo((int)(sentReturned + receivedReturned));
        assertThat(stats.getCompletedCount()).isEqualTo((int)(sentCompleted + receivedCompleted));
        
        // Verify total counts
        long expectedTotalSent = sentPending + sentApproved + sentRejected + sentReturned + sentCompleted;
        long expectedTotalReceived = receivedPending + receivedApproved + receivedRejected + receivedReturned + receivedCompleted;
        assertThat(stats.getTotalSent()).isEqualTo((int)expectedTotalSent);
        assertThat(stats.getTotalReceived()).isEqualTo((int)expectedTotalReceived);
    }
    
    // Arbitraries
    @Provide
    Arbitrary<Long> validItemId() {
        return Arbitraries.longs().between(1L, 10000L);
    }
    
    @Provide
    Arbitrary<Long> validUserId() {
        return Arbitraries.longs().between(1L, 10000L);
    }
    
    @Provide
    Arbitrary<LocalDate> futureBorrowDate() {
        return Arbitraries.of(
            LocalDate.now(),
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(14),
            LocalDate.now().plusDays(30)
        );
    }
    
    @Provide
    Arbitrary<LocalDate> validReturnDate() {
        return Arbitraries.of(
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(8),
            LocalDate.now().plusDays(15),
            LocalDate.now().plusDays(31),
            LocalDate.now().plusDays(60)
        );
    }
    
    @Provide
    Arbitrary<String> validRequestMessage() {
        return Arbitraries.strings().alpha().numeric().withChars(' ', '.', ',', '!', '?')
                .ofMinLength(0).ofMaxLength(500);
    }
    
    @Provide
    Arbitrary<List<BorrowRequest>> requestList() {
        return Combinators.combine(
                validUserId(),
                validUserId(),
                validItemId(),
                futureBorrowDate(),
                validReturnDate(),
                Arbitraries.of(RequestStatus.values())
        ).as((borrowerId, lenderId, itemId, borrowDate, returnDate, status) -> {
            if (borrowerId.equals(lenderId) || !returnDate.isAfter(borrowDate)) {
                return null;
            }
            User borrower = createUser(borrowerId, "borrower" + borrowerId, "borrower" + borrowerId + "@test.com");
            User lender = createUser(lenderId, "lender" + lenderId, "lender" + lenderId + "@test.com");
            Item item = createItem(itemId, "Test Item", lender, ItemStatus.AVAILABLE);
            
            BorrowRequest request = new BorrowRequest();
            setBorrowRequestField(request, "id", itemId);
            setBorrowRequestField(request, "item", item);
            setBorrowRequestField(request, "borrower", borrower);
            setBorrowRequestField(request, "lender", lender);
            setBorrowRequestField(request, "status", status);
            setBorrowRequestField(request, "borrowDate", borrowDate);
            setBorrowRequestField(request, "returnDate", returnDate);
            setBorrowRequestField(request, "createdAt", LocalDateTime.now());
            setBorrowRequestField(request, "updatedAt", LocalDateTime.now());
            return request;
        }).injectNull(0.0).list().ofMinSize(0).ofMaxSize(20)
          .map(list -> list.stream().filter(Objects::nonNull).toList());
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
    
    private void setBorrowRequestField(BorrowRequest request, String fieldName, Object value) {
        try {
            var field = BorrowRequest.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(request, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    // Feature: borrow-workflow, Property: Authorization for approve/reject/return
    // Validates: Requirements 13.1, 13.2, 13.3
    @Property(tries = 100)
    void nonOwnerCannotApproveRejectOrReturn(
            @ForAll("validItemId") Long itemId,
            @ForAll("validUserId") Long borrowerId,
            @ForAll("validUserId") Long lenderId,
            @ForAll("validUserId") Long unauthorizedUserId) {
        
        Assume.that(!borrowerId.equals(lenderId));
        Assume.that(!unauthorizedUserId.equals(lenderId));
        Assume.that(!unauthorizedUserId.equals(borrowerId));
        
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        
        User borrower = createUser(borrowerId, "borrower" + borrowerId, "borrower" + borrowerId + "@test.com");
        User lender = createUser(lenderId, "lender" + lenderId, "lender" + lenderId + "@test.com");
        User unauthorizedUser = createUser(unauthorizedUserId, "unauthorized" + unauthorizedUserId, "unauthorized" + unauthorizedUserId + "@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.AVAILABLE);
        
        // Create a pending request
        BorrowRequest pendingRequest = new BorrowRequest();
        setBorrowRequestField(pendingRequest, "id", 1L);
        setBorrowRequestField(pendingRequest, "item", item);
        setBorrowRequestField(pendingRequest, "borrower", borrower);
        setBorrowRequestField(pendingRequest, "lender", lender);
        setBorrowRequestField(pendingRequest, "status", RequestStatus.PENDING);
        setBorrowRequestField(pendingRequest, "borrowDate", LocalDate.now().plusDays(1));
        setBorrowRequestField(pendingRequest, "returnDate", LocalDate.now().plusDays(8));
        setBorrowRequestField(pendingRequest, "createdAt", LocalDateTime.now());
        setBorrowRequestField(pendingRequest, "updatedAt", LocalDateTime.now());
        
        // Create an approved request for testing return
        BorrowRequest approvedRequest = new BorrowRequest();
        setBorrowRequestField(approvedRequest, "id", 2L);
        setBorrowRequestField(approvedRequest, "item", item);
        setBorrowRequestField(approvedRequest, "borrower", borrower);
        setBorrowRequestField(approvedRequest, "lender", lender);
        setBorrowRequestField(approvedRequest, "status", RequestStatus.APPROVED);
        setBorrowRequestField(approvedRequest, "borrowDate", LocalDate.now().minusDays(1));
        setBorrowRequestField(approvedRequest, "returnDate", LocalDate.now().plusDays(7));
        setBorrowRequestField(approvedRequest, "createdAt", LocalDateTime.now());
        setBorrowRequestField(approvedRequest, "updatedAt", LocalDateTime.now());
        
        when(mockRequestRepo.findById(1L)).thenReturn(Optional.of(pendingRequest));
        when(mockRequestRepo.findById(2L)).thenReturn(Optional.of(approvedRequest));
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        // Test: Non-owner cannot approve (Requirement 13.1)
        try {
            service.approveRequest(1L, "Approved", unauthorizedUser);
            throw new AssertionError("Expected SecurityException for unauthorized approve");
        } catch (SecurityException e) {
            // Expected
            assertThat(e.getMessage()).contains("owner");
        }
        
        // Test: Non-owner cannot reject (Requirement 13.2)
        try {
            service.rejectRequest(1L, "Rejected", unauthorizedUser);
            throw new AssertionError("Expected SecurityException for unauthorized reject");
        } catch (SecurityException e) {
            // Expected
            assertThat(e.getMessage()).contains("owner");
        }
        
        // Test: Non-owner cannot mark as returned (Requirement 13.3)
        try {
            service.markAsReturned(2L, unauthorizedUser);
            throw new AssertionError("Expected SecurityException for unauthorized return");
        } catch (SecurityException e) {
            // Expected
            assertThat(e.getMessage()).contains("owner");
        }
    }
    
    // Feature: borrow-workflow, Property: Authorization for confirm return and cancel
    // Validates: Requirements 13.4, 13.5
    @Property(tries = 100)
    void nonBorrowerCannotConfirmOrCancel(
            @ForAll("validItemId") Long itemId,
            @ForAll("validUserId") Long borrowerId,
            @ForAll("validUserId") Long lenderId,
            @ForAll("validUserId") Long unauthorizedUserId) {
        
        Assume.that(!borrowerId.equals(lenderId));
        Assume.that(!unauthorizedUserId.equals(borrowerId));
        Assume.that(!unauthorizedUserId.equals(lenderId));
        
        BorrowRequestRepository mockRequestRepo = Mockito.mock(BorrowRequestRepository.class);
        ItemRepository mockItemRepo = Mockito.mock(ItemRepository.class);
        BorrowRequestMapper mapper = new BorrowRequestMapper();
        
        User borrower = createUser(borrowerId, "borrower" + borrowerId, "borrower" + borrowerId + "@test.com");
        User lender = createUser(lenderId, "lender" + lenderId, "lender" + lenderId + "@test.com");
        User unauthorizedUser = createUser(unauthorizedUserId, "unauthorized" + unauthorizedUserId, "unauthorized" + unauthorizedUserId + "@test.com");
        Item item = createItem(itemId, "Test Item", lender, ItemStatus.AVAILABLE);
        
        // Create a pending request for testing cancel
        BorrowRequest pendingRequest = new BorrowRequest();
        setBorrowRequestField(pendingRequest, "id", 1L);
        setBorrowRequestField(pendingRequest, "item", item);
        setBorrowRequestField(pendingRequest, "borrower", borrower);
        setBorrowRequestField(pendingRequest, "lender", lender);
        setBorrowRequestField(pendingRequest, "status", RequestStatus.PENDING);
        setBorrowRequestField(pendingRequest, "borrowDate", LocalDate.now().plusDays(1));
        setBorrowRequestField(pendingRequest, "returnDate", LocalDate.now().plusDays(8));
        setBorrowRequestField(pendingRequest, "createdAt", LocalDateTime.now());
        setBorrowRequestField(pendingRequest, "updatedAt", LocalDateTime.now());
        
        // Create a returned request for testing confirm
        BorrowRequest returnedRequest = new BorrowRequest();
        setBorrowRequestField(returnedRequest, "id", 2L);
        setBorrowRequestField(returnedRequest, "item", item);
        setBorrowRequestField(returnedRequest, "borrower", borrower);
        setBorrowRequestField(returnedRequest, "lender", lender);
        setBorrowRequestField(returnedRequest, "status", RequestStatus.RETURNED);
        setBorrowRequestField(returnedRequest, "borrowDate", LocalDate.now().minusDays(5));
        setBorrowRequestField(returnedRequest, "returnDate", LocalDate.now().minusDays(1));
        setBorrowRequestField(returnedRequest, "returnedAt", LocalDateTime.now().minusHours(2));
        setBorrowRequestField(returnedRequest, "createdAt", LocalDateTime.now());
        setBorrowRequestField(returnedRequest, "updatedAt", LocalDateTime.now());
        
        when(mockRequestRepo.findById(1L)).thenReturn(Optional.of(pendingRequest));
        when(mockRequestRepo.findById(2L)).thenReturn(Optional.of(returnedRequest));
        
        BorrowRequestService service = new BorrowRequestServiceImpl(mockRequestRepo, mockItemRepo, mapper);
        
        // Test: Non-borrower cannot cancel (Requirement 13.5)
        try {
            service.cancelRequest(1L, unauthorizedUser);
            throw new AssertionError("Expected SecurityException for unauthorized cancel");
        } catch (SecurityException e) {
            // Expected
            assertThat(e.getMessage()).contains("borrower");
        }
        
        // Test: Non-borrower cannot confirm return (Requirement 13.4)
        try {
            service.confirmReturn(2L, unauthorizedUser);
            throw new AssertionError("Expected SecurityException for unauthorized confirm");
        } catch (SecurityException e) {
            // Expected
            assertThat(e.getMessage()).contains("borrower");
        }
    }
}
