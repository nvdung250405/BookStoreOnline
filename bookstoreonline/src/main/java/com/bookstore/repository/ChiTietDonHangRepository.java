package com.bookstore.repository;

import com.bookstore.entity.ChiTietDonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChiTietDonHangRepository extends JpaRepository<ChiTietDonHang, Long> {

    @Query("SELECT ct.isbn, SUM(ct.soLuong) as totalSold FROM ChiTietDonHang ct GROUP BY ct.isbn ORDER BY totalSold DESC")
    List<Object[]> findTopSellingProjected();
}
