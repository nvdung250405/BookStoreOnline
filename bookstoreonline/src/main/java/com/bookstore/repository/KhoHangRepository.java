package com.bookstore.repository;

import com.bookstore.entity.KhoHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KhoHangRepository extends JpaRepository<KhoHang, Integer> {

    @Query("SELECT k FROM KhoHang k JOIN FETCH k.sach WHERE k.sach.isbn = :isbn")
    Optional<KhoHang> findByIsbn(@Param("isbn") String isbn);

    // Thêm vào dưới hàm findByIsbn trong KhoHangRepository.java

    // Lọc danh sách hàng sắp hết (API 38)
    @Query("SELECT k FROM KhoHang k JOIN FETCH k.sach WHERE k.soLuongTon <= k.nguongBaoDong")
    List<KhoHang> findLowStockItems();
}