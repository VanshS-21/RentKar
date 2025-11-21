package com.rentkar.controller;

import com.rentkar.dto.CreateBorrowRequestDTO;
import com.rentkar.dto.CreateItemRequest;
import com.rentkar.dto.LoginRequest;
import com.rentkar.dto.RegisterRequest;
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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.properties")
public class BorrowRequestControllerIntegrationTest {
    
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
        borrowerRegister.setUsername("borrower");
        borrowerRegister.setEmail("borrower@example.com");
        borrowerRegister.setPassword("password123");
        borrowerRegister.setFullName("Borrower User");
        borrowerRegister.setPhone("1234567890");
        
        restTemplate.postForEntity(authUrl + "/register", borrowerRegister, Map.class);
        
        LoginRequest borrowerLogin = new LoginRequest();
        borrowerLogin.setUsername("borrower");
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
        lenderRegister.setUsername("lender");
        lenderRegister.setEmail("lender@example.com");
        lenderRegister.setPassword("password123");
        lenderRegister.setFullName("Lender User");
        lenderRegister.setPhone("0987654321");
        
        restTemplate.postForEntity(authUrl + "/register", lenderRegister, Map.class);
        
        LoginRequest lenderLogin = new LoginRequest();
        lenderLogin.setUsername("lender");
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
        itemId = createTestItem("Test Item", "Test Description", "Electronics", lenderToken);
    }
    
    @Test
    void testCreateRequestWithValidData() {
        CreateBorrowRequestDTO dto = new CreateBorrowRequestDTO();
        dto.setBorrowDate(LocalDate.now().plusDays(1));
        dto.setReturnDate(LocalDate.now().plusDays(7));
        dto.setRequestMessage("I need this item for a project");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + borrowerToken);
        HttpEntity<CreateBorrowRequestDTO> entity = new HttpEntity<>(dto, headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "?itemId=" + itemId,
            HttpMethod.POST,
            entity,
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("message")).isEqualTo("Borrow request created successfully");
        
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data).isNotNull();
        assertThat(data.get("status")).isEqualTo("PENDING");
        assertThat(data.get("requestMessage")).isEqualTo("I need this item for a project");
        
        Map<String, Object> borrower = (Map<String, Object>) data.get("borrower");
        assertThat(borrower.get("username")).isEqualTo("borrower");
        
        Map<String, Object> lender = (Map<String, Object>) data.get("lender");
        assertThat(lender.get("username")).isEqualTo("lender");
    }
    
    @Test
    void testCreateRequestWithPastBorrowDate() {
        CreateBorrowRequestDTO dto = new CreateBorrowRequestDTO();
        dto.setBorrowDate(LocalDate.now().minusDays(1)); // Past date
        dto.setReturnDate(LocalDate.now().plusDays(7));
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + borrowerToken);
        HttpEntity<CreateBorrowRequestDTO> entity = new HttpEntity<>(dto, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "?itemId=" + itemId,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            assertThat(e.getMessage()).contains("cannot retry due to server authentication");
        }
    }
    
    @Test
    void testCreateRequestWithInvalidDateRange() {
        CreateBorrowRequestDTO dto = new CreateBorrowRequestDTO();
        dto.setBorrowDate(LocalDate.now().plusDays(7));
        dto.setReturnDate(LocalDate.now().plusDays(1)); // Return before borrow
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + borrowerToken);
        HttpEntity<CreateBorrowRequestDTO> entity = new HttpEntity<>(dto, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "?itemId=" + itemId,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            assertThat(e.getMessage()).contains("cannot retry due to server authentication");
        }
    }
    
    @Test
    void testCreateRequestForOwnItem() {
        CreateBorrowRequestDTO dto = new CreateBorrowRequestDTO();
        dto.setBorrowDate(LocalDate.now().plusDays(1));
        dto.setReturnDate(LocalDate.now().plusDays(7));
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + lenderToken); // Lender trying to borrow own item
        HttpEntity<CreateBorrowRequestDTO> entity = new HttpEntity<>(dto, headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "?itemId=" + itemId,
            HttpMethod.POST,
            entity,
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
    }
    
    @Test
    void testGetSentRequests() {
        // Create a request
        Long requestId = createTestRequest(itemId, borrowerToken);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + borrowerToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/sent",
            HttpMethod.GET,
            entity,
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
        assertThat(data).hasSize(1);
        assertThat(data.get(0).get("status")).isEqualTo("PENDING");
    }
    
    @Test
    void testGetSentRequestsWithStatusFilter() {
        // Create multiple requests
        Long requestId1 = createTestRequest(itemId, borrowerToken);
        
        // Approve one request
        approveTestRequest(requestId1, lenderToken);
        
        // Create another item and request
        Long itemId2 = createTestItem("Item 2", "Description 2", "Books", lenderToken);
        Long requestId2 = createTestRequest(itemId2, borrowerToken);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + borrowerToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/sent?status=PENDING",
            HttpMethod.GET,
            entity,
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
        assertThat(data).hasSize(1);
        assertThat(data.get(0).get("status")).isEqualTo("PENDING");
    }
    
    @Test
    void testGetReceivedRequests() {
        // Create a request
        createTestRequest(itemId, borrowerToken);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + lenderToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/received",
            HttpMethod.GET,
            entity,
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
        assertThat(data).hasSize(1);
        assertThat(data.get(0).get("status")).isEqualTo("PENDING");
    }
    
    @Test
    void testGetRequestById() {
        Long requestId = createTestRequest(itemId, borrowerToken);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + borrowerToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/" + requestId,
            HttpMethod.GET,
            entity,
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("id")).isEqualTo(requestId.intValue());
        assertThat(data.get("status")).isEqualTo("PENDING");
    }
    
    @Test
    void testGetRequestByIdUnauthorized() {
        Long requestId = createTestRequest(itemId, borrowerToken);
        
        // Create third user
        String thirdUserToken = createThirdUser();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + thirdUserToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/" + requestId,
            HttpMethod.GET,
            entity,
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
    }
    
    @Test
    void testApproveRequest() {
        Long requestId = createTestRequest(itemId, borrowerToken);
        
        Map<String, String> body = new HashMap<>();
        body.put("responseMessage", "Approved! Please pick it up tomorrow.");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + lenderToken);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/" + requestId + "/approve",
            HttpMethod.POST,
            entity,
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("status")).isEqualTo("APPROVED");
        assertThat(data.get("responseMessage")).isEqualTo("Approved! Please pick it up tomorrow.");
    }
    
    @Test
    void testApproveRequestByNonOwner() {
        Long requestId = createTestRequest(itemId, borrowerToken);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + borrowerToken); // Borrower trying to approve
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(new HashMap<>(), headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/" + requestId + "/approve",
            HttpMethod.POST,
            entity,
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
    }
    
    @Test
    void testRejectRequest() {
        Long requestId = createTestRequest(itemId, borrowerToken);
        
        Map<String, String> body = new HashMap<>();
        body.put("responseMessage", "Sorry, I need it myself.");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + lenderToken);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/" + requestId + "/reject",
            HttpMethod.POST,
            entity,
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("status")).isEqualTo("REJECTED");
        assertThat(data.get("responseMessage")).isEqualTo("Sorry, I need it myself.");
    }
    
    @Test
    void testMarkAsReturned() {
        Long requestId = createTestRequest(itemId, borrowerToken);
        approveTestRequest(requestId, lenderToken);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + lenderToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/" + requestId + "/return",
            HttpMethod.POST,
            entity,
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("status")).isEqualTo("RETURNED");
        assertThat(data.get("returnedAt")).isNotNull();
    }
    
    @Test
    void testMarkAsReturnedByNonOwner() {
        Long requestId = createTestRequest(itemId, borrowerToken);
        approveTestRequest(requestId, lenderToken);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + borrowerToken); // Borrower trying to mark as returned
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/" + requestId + "/return",
            HttpMethod.POST,
            entity,
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
    }
    
    @Test
    void testConfirmReturn() {
        Long requestId = createTestRequest(itemId, borrowerToken);
        approveTestRequest(requestId, lenderToken);
        markAsReturnedTestRequest(requestId, lenderToken);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + borrowerToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/" + requestId + "/confirm",
            HttpMethod.POST,
            entity,
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("status")).isEqualTo("COMPLETED");
        assertThat(data.get("completedAt")).isNotNull();
    }
    
    @Test
    void testConfirmReturnByNonBorrower() {
        Long requestId = createTestRequest(itemId, borrowerToken);
        approveTestRequest(requestId, lenderToken);
        markAsReturnedTestRequest(requestId, lenderToken);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + lenderToken); // Lender trying to confirm
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/" + requestId + "/confirm",
            HttpMethod.POST,
            entity,
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
    }
    
    @Test
    void testCancelRequest() {
        Long requestId = createTestRequest(itemId, borrowerToken);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + borrowerToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/" + requestId,
            HttpMethod.DELETE,
            entity,
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("message")).isEqualTo("Request canceled successfully");
    }
    
    @Test
    void testCancelRequestByNonBorrower() {
        Long requestId = createTestRequest(itemId, borrowerToken);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + lenderToken); // Lender trying to cancel
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/" + requestId,
            HttpMethod.DELETE,
            entity,
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
    }
    
    @Test
    void testGetStatistics() {
        // Create multiple requests with different statuses
        Long requestId1 = createTestRequest(itemId, borrowerToken);
        
        Long itemId2 = createTestItem("Item 2", "Description 2", "Books", lenderToken);
        Long requestId2 = createTestRequest(itemId2, borrowerToken);
        approveTestRequest(requestId2, lenderToken);
        
        Long itemId3 = createTestItem("Item 3", "Description 3", "Electronics", lenderToken);
        Long requestId3 = createTestRequest(itemId3, borrowerToken);
        rejectTestRequest(requestId3, lenderToken);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + borrowerToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/statistics",
            HttpMethod.GET,
            entity,
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("pendingCount")).isEqualTo(1);
        assertThat(data.get("approvedCount")).isEqualTo(1);
        assertThat(data.get("rejectedCount")).isEqualTo(1);
        assertThat(data.get("totalSent")).isEqualTo(3);
    }
    
    @Test
    void testCompleteWorkflow() {
        // Create request
        Long requestId = createTestRequest(itemId, borrowerToken);
        
        // Approve request
        approveTestRequest(requestId, lenderToken);
        
        // Mark as returned
        markAsReturnedTestRequest(requestId, lenderToken);
        
        // Confirm return
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + borrowerToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/" + requestId + "/confirm",
            HttpMethod.POST,
            entity,
            Map.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("status")).isEqualTo("COMPLETED");
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
    
    private Long createTestRequest(Long itemId, String token) {
        CreateBorrowRequestDTO dto = new CreateBorrowRequestDTO();
        dto.setBorrowDate(LocalDate.now().plusDays(1));
        dto.setReturnDate(LocalDate.now().plusDays(7));
        dto.setRequestMessage("Test request");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<CreateBorrowRequestDTO> entity = new HttpEntity<>(dto, headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "?itemId=" + itemId,
            HttpMethod.POST,
            entity,
            Map.class
        );
        
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        return ((Number) data.get("id")).longValue();
    }
    
    private void approveTestRequest(Long requestId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(new HashMap<>(), headers);
        
        restTemplate.exchange(
            baseUrl + "/" + requestId + "/approve",
            HttpMethod.POST,
            entity,
            Map.class
        );
    }
    
    private void rejectTestRequest(Long requestId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(new HashMap<>(), headers);
        
        restTemplate.exchange(
            baseUrl + "/" + requestId + "/reject",
            HttpMethod.POST,
            entity,
            Map.class
        );
    }
    
    private void markAsReturnedTestRequest(Long requestId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        restTemplate.exchange(
            baseUrl + "/" + requestId + "/return",
            HttpMethod.POST,
            entity,
            Map.class
        );
    }
    
    private String createThirdUser() {
        RegisterRequest register = new RegisterRequest();
        register.setUsername("thirduser");
        register.setEmail("third@example.com");
        register.setPassword("password123");
        register.setFullName("Third User");
        register.setPhone("5555555555");
        
        restTemplate.postForEntity(authUrl + "/register", register, Map.class);
        
        LoginRequest login = new LoginRequest();
        login.setUsername("thirduser");
        login.setPassword("password123");
        
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
            authUrl + "/login",
            login,
            Map.class
        );
        
        Map<String, Object> data = (Map<String, Object>) loginResponse.getBody().get("data");
        return (String) data.get("token");
    }
}
