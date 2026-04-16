package com.bookstore.repository;

import com.bookstore.dto.InventoryDetailDTO;
import com.bookstore.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    @Query("SELECT i FROM Inventory i JOIN FETCH i.book WHERE i.book.isbn = :isbn")
    Optional<Inventory> findByBook_Isbn(@Param("isbn") String isbn);

    @Query("SELECT i FROM Inventory i JOIN FETCH i.book WHERE i.stockQuantity <= i.alertThreshold")
    List<Inventory> findLowStockItems();
    @Query("SELECT new com.bookstore.dto.InventoryDetailDTO(i.book.isbn, i.book.title, i.stockQuantity, i.shelfLocation) FROM Inventory i JOIN i.book")
    List<InventoryDetailDTO> findAllInventoryDetails();
}