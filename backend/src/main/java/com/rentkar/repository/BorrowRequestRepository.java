package com.rentkar.repository;

import com.rentkar.model.BorrowRequest;
import com.rentkar.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {
    
    // Find all requests sent by a borrower
    List<BorrowRequest> findByBorrowerId(Long borrowerId);
    
    // Find all requests received by a lender
    List<BorrowRequest> findByLenderId(Long lenderId);
    
    // Find requests by borrower and status
    List<BorrowRequest> findByBorrowerIdAndStatus(Long borrowerId, RequestStatus status);
    
    // Find requests by lender and status
    List<BorrowRequest> findByLenderIdAndStatus(Long lenderId, RequestStatus status);
    
    // Find requests by item
    List<BorrowRequest> findByItemId(Long itemId);
    
    // Count pending requests for a lender
    @Query("SELECT COUNT(br) FROM BorrowRequest br WHERE br.lender.id = :lenderId AND br.status = 'PENDING'")
    long countPendingRequestsByLender(@Param("lenderId") Long lenderId);
    
    // Count requests by borrower and status
    @Query("SELECT COUNT(br) FROM BorrowRequest br WHERE br.borrower.id = :borrowerId AND br.status = :status")
    long countByBorrowerAndStatus(@Param("borrowerId") Long borrowerId, @Param("status") RequestStatus status);
    
    // Count requests by lender and status
    @Query("SELECT COUNT(br) FROM BorrowRequest br WHERE br.lender.id = :lenderId AND br.status = :status")
    long countByLenderAndStatus(@Param("lenderId") Long lenderId, @Param("status") RequestStatus status);
}
