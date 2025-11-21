package com.rentkar.repository;

import com.rentkar.model.Item;
import com.rentkar.model.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    
    Page<Item> findByStatus(ItemStatus status, Pageable pageable);
    
    Page<Item> findByOwnerId(Long ownerId, Pageable pageable);
    
    Page<Item> findByCategory(String category, Pageable pageable);
    
    @Query("SELECT i FROM Item i WHERE " +
           "(LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Item> searchItems(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT i FROM Item i WHERE " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:category IS NULL OR i.category = :category) AND " +
           "(:keyword IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Item> findWithFilters(@Param("status") ItemStatus status, 
                                @Param("category") String category, 
                                @Param("keyword") String keyword, 
                                Pageable pageable);
}
