package com.rentkar.controller;

import com.rentkar.dto.CreateBorrowRequestDTO;
import com.rentkar.dto.CreateItemRequest;
import com.rentkar.dto.LoginRequest;
import com.rentkar.dto.RegisterRequest;
import com.rentkar.model.ItemStatus;
import com.rentkar.model.RequestStatus;
import com.rentkar.repository.BorrowRequestRepository;
import com.rentkar.repository.ItemRepository;
import com.rentkar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end tests for the complete borrow workflow.
 * These tests verify the entire lifecycle of borrow requests from creation to completion.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.properties")
public class BorrowWorkflowEndToEndTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private BorrowRequestRepository borrowRequestRepository;
    
    private String baseUrl;
    private String authUrl;
    private String itemUrl;
    private String borrowerToken;
    private String lenderToken;
    private Long borrowerId;
    private Long lenderId;
    private Long itemId;
    
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/requests";
        authUrl = "http://localhost:" + port + "/api/auth";
        itemUrl = "http://localhost:" + port + "/api/items";
        
        // Clean up
        borrowRequestRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
        
        // Register borrower
        RegisterRequest borrowerRegister = new RegisterRequest();
        borrowerRegister.setUsername("e2e_borrower");
        borrowerRegister.setEmail("e2e_borrower@example.com");
        borrowerRegister.setPassword("password123");
        borrowerRegister.setFullName("E2E Borrower User");
        borrowerRegister.setPhone("1234567890");
        
        restTemplate.postForEntity(authUrl + "/register", borrowerRegister, Map.class);
        
        LoginRequest borrowerLogin = new LoginRequest();
        borrowerLogin.setUsername("e2e_borrower");
        borrowerLogin.setPassword("password123");
        
        ResponseEntity<Map> borrowerLoginResponse = restTemplate.postForEntity(
            authUrl + "/login",
            borrowerLogin,
            Map.class
        );
        
        Map<String, Object> borrowerData = (Map<String, Object>) borrowerLoginResponse.getBody().get("data");
        borrowerToken = (String) borrowerData.get("token");
        Map<String, Object> borrowerUser = (Map<String, Object>) borrowerData.get("user");
        borrowerId = ((Number) borrowerUser.get("id")).longValue();
        
        // Register lender
        RegisterRequest lenderRegister = new RegisterRequest();
        lenderRegister.setUsername("e2e_lender");
        lenderRegister.setEmail("e2e_lender@example.com");
        lenderRegister.setPassword("password123");
        lenderRegister.setFullName("E2E Lender User");
        lenderRegister.setPhone("0987654321");
        
        restTemplate.postForEntity(authUrl + "/register", lenderRegister, Map.class);
        
        LoginRequest lenderLogin = new LoginRequest();
        lenderLogin.setUsername("e2e_lender");
        lenderLogin.setPassword("password123");
        
        ResponseEntity<Map> lenderLoginResponse = restTemplate.postForEntity(
            authUrl + "/login",
            lenderLogin,
            Map.class
        );
        
        Map<String, Object> lenderData = (Map<String, Object>) lenderLoginResponse.getBody().get("data");
        lenderToken = (String) lenderData.get("token");
        Map<String, Object> lenderUser = (Map<String, Object>) lenderData.get("user");
        lenderId = ((Number) lenderUser.get("id")).longValue();
        
        // Create an item owned by lender
        itemId = createTestItem("E2E Test Item", "Test Description", "Electronics", lenderToken);
    }
    
    /**
     * Test Requirements: 1.3, 4.1, 6.1, 7.1
     * Tests the complete borrow workflow:
     * 1. Borrower creates request
     * 2. Lender approves request
     * 3. Lender marks as returned
     * 4. Borrower confirms return
     * Verifies item status changes throughout the workflow
     */
    @Test
    void testCompleteBorrowWorkflow() {
        // Step 1: Borrower creates request (Requirement 1.3)
        CreateBorrowRequestDTO createDto = new CreateBorrowRequestDTO();
        createDto.setBorrowDate(LocalDate.now().plusDays(1));
        createDto.setReturnDate(LocalDate.now().plusDays(7));
        createDto.setRequestMessage("I need this for a project");
        
        HttpHeaders borrowerHeaders = new HttpHeaders();
        borrowerHeaders.set("Authorization", "Bearer " + borrowerToken);
        HttpEntity<CreateBorrowRequestDTO> createEntity = new HttpEntity<>(createDto, borrowerHeaders);
        
        ResponseEntity<Map> createResponse = restTemplate.exchange(
            baseUrl + "?itemId=" + itemId,
            HttpMethod.POST,
            createEntity,
            Map.class
        );
        
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody().get("success")).isEqualTo(true);
        
        Map<String, Object> createdRequest = (Map<String, Object>) createResponse.getBody().get("data");
        Long requestId = ((Number) createdRequest.get("id")).longValue();
        assertThat(createdRequest.get("status")).isEqualTo("PENDING");
        
        // Verify item is still AVAILABLE after request creation
        ResponseEntity<Map> itemResponse = getItem(itemId, borrowerToken);
        Map<String, Object> itemData = (Map<String, Object>) itemResponse.getBody().get("data");
        assertThat(itemData.get("status")).isEqualTo("AVAILABLE");
        
        // Step 2: Lender approves request (Requirement 4.1)
        Map<String, String> approveBody = new HashMap<>();
        approveBody.put("responseMessage", "Approved! You can pick it up tomorrow.");
        
        HttpHeaders lenderHeaders = new HttpHeaders();
        lenderHeaders.set("Authorization", "Bearer " + lenderToken);
        HttpEntity<Map<String, String>> approveEntity = new HttpEntity<>(approveBody, lenderHeaders);
        
        ResponseEntity<Map> approveResponse = restTemplate.exchange(
            baseUrl + "/" + requestId + "/approve",
            HttpMethod.POST,
            approveEntity,
            Map.class
        );
        
        assertThat(approveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(approveResponse.getBody().get("success")).isEqualTo(true);
        
        Map<String, Object> approvedRequest = (Map<String, Object>) approveResponse.getBody().get("data");
        assertThat(approvedRequest.get("status")).isEqualTo("APPROVED");
        assertThat(approvedRequest.get("responseMessage")).isEqualTo("Approved! You can pick it up tomorrow.");
        
        // Verify item status changed to BORROWED after approval
        itemResponse = getItem(itemId, lenderToken);
        itemData = (Map<String, Object>) itemResponse.getBody().get("data");
        assertThat(itemData.get("status")).isEqualTo("BORROWED");
        
        // Step 3: Lender marks as returned (Requirement 6.1)
        HttpEntity<Void> returnEntity = new HttpEntity<>(lenderHeaders);
        
        ResponseEntity<Map> returnResponse = restTemplate.exchange(
            baseUrl + "/" + requestId + "/return",
            HttpMethod.POST,
            returnEntity,
            Map.class
        );
        
        assertThat(returnResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(returnResponse.getBody().get("success")).isEqualTo(true);
        
        Map<String, Object> returnedRequest = (Map<String, Object>) returnResponse.getBody().get("data");
        assertThat(returnedRequest.get("status")).isEqualTo("RETURNED");
        assertThat(returnedRequest.get("returnedAt")).isNotNull();
        
        // Verify item status changed back to AVAILABLE after return
        itemResponse = getItem(itemId, lenderToken);
        itemData = (Map<String, Object>) itemResponse.getBody().get("data");
        assertThat(itemData.get("status")).isEqualTo("AVAILABLE");
        
        // Step 4: Borrower confirms return (Requirement 7.1)
        HttpEntity<Void> confirmEntity = new HttpEntity<>(borrowerHeaders);
        
        ResponseEntity<Map> confirmResponse = restTemplate.exchange(
            baseUrl + "/" + requestId + "/confirm",
            HttpMethod.POST,
            confirmEntity,
            Map.class
        );
        
        assertThat(confirmResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(confirmResponse.getBody().get("success")).isEqualTo(true);
        
        Map<String, Object> completedRequest = (Map<String, Object>) confirmResponse.getBody().get("data");
        assertThat(completedRequest.get("status")).isEqualTo("COMPLETED");
        assertThat(completedRequest.get("completedAt")).isNotNull();
        
        // Verify item remains AVAILABLE after completion
        itemResponse = getItem(itemId, borrowerToken);
        itemData = (Map<String, Object>) itemResponse.getBody().get("data");
        assertThat(itemData.get("status")).isEqualTo("AVAILABLE");
        
        // Verify the complete workflow is reflected in request history
        ResponseEntity<Map> requestDetailsResponse = restTemplate.exchange(
            baseUrl + "/" + requestId,
            HttpMethod.GET,
            new HttpEntity<>(borrowerHeaders),
            Map.class
        );
        
        assertThat(requestDetailsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> finalRequest = (Map<String, Object>) requestDetailsResponse.getBody().get("data");
        assertThat(finalRequest.get("status")).isEqualTo("COMPLETED");
        assertThat(finalRequest.get("returnedAt")).isNotNull();
        assertThat(finalRequest.get("completedAt")).isNotNull();
    }
    
    /**
     * Test Requirements: 1.3, 5.1, 5.2
     * Tests the rejection workflow:
     * 1. Borrower creates request
     * 2. Lender rejects request
     * Verifies item remains available after rejection
     */
    @Test
    void testRejectionWorkflow() {
        // Step 1: Borrower creates request (Requirement 1.3)
        CreateBorrowRequestDTO createDto = new CreateBorrowRequestDTO();
        createDto.setBorrowDate(LocalDate.now().plusDays(1));
        createDto.setReturnDate(LocalDate.now().plusDays(7));
        createDto.setRequestMessage("Can I borrow this?");
        
        HttpHeaders borrowerHeaders = new HttpHeaders();
        borrowerHeaders.set("Authorization", "Bearer " + borrowerToken);
        HttpEntity<CreateBorrowRequestDTO> createEntity = new HttpEntity<>(createDto, borrowerHeaders);
        
        ResponseEntity<Map> createResponse = restTemplate.exchange(
            baseUrl + "?itemId=" + itemId,
            HttpMethod.POST,
            createEntity,
            Map.class
        );
        
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map<String, Object> createdRequest = (Map<String, Object>) createResponse.getBody().get("data");
        Long requestId = ((Number) createdRequest.get("id")).longValue();
        assertThat(createdRequest.get("status")).isEqualTo("PENDING");
        
        // Verify item is AVAILABLE before rejection
        ResponseEntity<Map> itemResponseBefore = getItem(itemId, borrowerToken);
        Map<String, Object> itemDataBefore = (Map<String, Object>) itemResponseBefore.getBody().get("data");
        assertThat(itemDataBefore.get("status")).isEqualTo("AVAILABLE");
        
        // Step 2: Lender rejects request (Requirement 5.1)
        Map<String, String> rejectBody = new HashMap<>();
        rejectBody.put("responseMessage", "Sorry, I need it myself during that time.");
        
        HttpHeaders lenderHeaders = new HttpHeaders();
        lenderHeaders.set("Authorization", "Bearer " + lenderToken);
        HttpEntity<Map<String, String>> rejectEntity = new HttpEntity<>(rejectBody, lenderHeaders);
        
        ResponseEntity<Map> rejectResponse = restTemplate.exchange(
            baseUrl + "/" + requestId + "/reject",
            HttpMethod.POST,
            rejectEntity,
            Map.class
        );
        
        assertThat(rejectResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rejectResponse.getBody().get("success")).isEqualTo(true);
        
        Map<String, Object> rejectedRequest = (Map<String, Object>) rejectResponse.getBody().get("data");
        assertThat(rejectedRequest.get("status")).isEqualTo("REJECTED");
        assertThat(rejectedRequest.get("responseMessage")).isEqualTo("Sorry, I need it myself during that time.");
        
        // Verify item remains AVAILABLE after rejection (Requirement 5.2)
        ResponseEntity<Map> itemResponseAfter = getItem(itemId, lenderToken);
        Map<String, Object> itemDataAfter = (Map<String, Object>) itemResponseAfter.getBody().get("data");
        assertThat(itemDataAfter.get("status")).isEqualTo("AVAILABLE");
        
        // Verify the rejected request is still visible in history
        ResponseEntity<Map> requestDetailsResponse = restTemplate.exchange(
            baseUrl + "/" + requestId,
            HttpMethod.GET,
            new HttpEntity<>(borrowerHeaders),
            Map.class
        );
        
        assertThat(requestDetailsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> finalRequest = (Map<String, Object>) requestDetailsResponse.getBody().get("data");
        assertThat(finalRequest.get("status")).isEqualTo("REJECTED");
    }
    
    /**
     * Test Requirements: 1.3, 9.1, 9.3
     * Tests the cancellation workflow:
     * 1. Borrower creates request
     * 2. Borrower cancels request
     * Verifies request is deleted and not visible in lists
     */
    @Test
    void testCancellationWorkflow() {
        // Step 1: Borrower creates request (Requirement 1.3)
        CreateBorrowRequestDTO createDto = new CreateBorrowRequestDTO();
        createDto.setBorrowDate(LocalDate.now().plusDays(1));
        createDto.setReturnDate(LocalDate.now().plusDays(7));
        createDto.setRequestMessage("I'd like to borrow this");
        
        HttpHeaders borrowerHeaders = new HttpHeaders();
        borrowerHeaders.set("Authorization", "Bearer " + borrowerToken);
        HttpEntity<CreateBorrowRequestDTO> createEntity = new HttpEntity<>(createDto, borrowerHeaders);
        
        ResponseEntity<Map> createResponse = restTemplate.exchange(
            baseUrl + "?itemId=" + itemId,
            HttpMethod.POST,
            createEntity,
            Map.class
        );
        
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map<String, Object> createdRequest = (Map<String, Object>) createResponse.getBody().get("data");
        Long requestId = ((Number) createdRequest.get("id")).longValue();
        assertThat(createdRequest.get("status")).isEqualTo("PENDING");
        
        // Verify request appears in borrower's sent requests
        ResponseEntity<Map> sentRequestsBefore = restTemplate.exchange(
            baseUrl + "/sent",
            HttpMethod.GET,
            new HttpEntity<>(borrowerHeaders),
            Map.class
        );
        assertThat(sentRequestsBefore.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sentRequestsBefore.getBody().get("data")).asList().isNotEmpty();
        
        // Verify request appears in lender's received requests
        HttpHeaders lenderHeaders = new HttpHeaders();
        lenderHeaders.set("Authorization", "Bearer " + lenderToken);
        ResponseEntity<Map> receivedRequestsBefore = restTemplate.exchange(
            baseUrl + "/received",
            HttpMethod.GET,
            new HttpEntity<>(lenderHeaders),
            Map.class
        );
        assertThat(receivedRequestsBefore.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(receivedRequestsBefore.getBody().get("data")).asList().isNotEmpty();
        
        // Step 2: Borrower cancels request (Requirement 9.1)
        HttpEntity<Void> cancelEntity = new HttpEntity<>(borrowerHeaders);
        
        ResponseEntity<Map> cancelResponse = restTemplate.exchange(
            baseUrl + "/" + requestId,
            HttpMethod.DELETE,
            cancelEntity,
            Map.class
        );
        
        assertThat(cancelResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cancelResponse.getBody().get("success")).isEqualTo(true);
        assertThat(cancelResponse.getBody().get("message")).isEqualTo("Request canceled successfully");
        
        // Verify request is deleted - attempting to get it should fail (Requirement 9.3)
        ResponseEntity<Map> getDeletedRequest = restTemplate.exchange(
            baseUrl + "/" + requestId,
            HttpMethod.GET,
            new HttpEntity<>(borrowerHeaders),
            Map.class
        );
        // Request should not be accessible (either 404 or 500 indicates it's not found)
        assertThat(getDeletedRequest.getStatusCode().is4xxClientError() || 
                   getDeletedRequest.getStatusCode().is5xxServerError()).isTrue();
        
        // Verify request no longer appears in borrower's sent requests (Requirement 9.3)
        ResponseEntity<Map> sentRequestsAfter = restTemplate.exchange(
            baseUrl + "/sent",
            HttpMethod.GET,
            new HttpEntity<>(borrowerHeaders),
            Map.class
        );
        assertThat(sentRequestsAfter.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sentRequestsAfter.getBody().get("data")).asList().isEmpty();
        
        // Verify request no longer appears in lender's received requests (Requirement 9.3)
        ResponseEntity<Map> receivedRequestsAfter = restTemplate.exchange(
            baseUrl + "/received",
            HttpMethod.GET,
            new HttpEntity<>(lenderHeaders),
            Map.class
        );
        assertThat(receivedRequestsAfter.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(receivedRequestsAfter.getBody().get("data")).asList().isEmpty();
        
        // Verify item status remains AVAILABLE
        ResponseEntity<Map> itemResponse = getItem(itemId, borrowerToken);
        Map<String, Object> itemData = (Map<String, Object>) itemResponse.getBody().get("data");
        assertThat(itemData.get("status")).isEqualTo("AVAILABLE");
    }
    
    // Helper methods
    
    private Long createTestItem(String title, String description, String category, String token) {
        CreateItemRequest request = new CreateItemRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCategory(category);
        request.setImageUrl("https://example.com/image.jpg");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<CreateItemRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            itemUrl,
            HttpMethod.POST,
            entity,
            Map.class
        );
        
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        return ((Number) data.get("id")).longValue();
    }
    
    private ResponseEntity<Map> getItem(Long itemId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        return restTemplate.exchange(
            itemUrl + "/" + itemId,
            HttpMethod.GET,
            entity,
            Map.class
        );
    }
}
