package com.bookstore.repository;

import com.bookstore.entity.ChiTietDonHang;
import com.bookstore.entity.ChiTietDonHangId;
import com.bookstore.entity.DonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChiTietDonHangRepository extends JpaRepository<ChiTietDonHang, ChiTietDonHangId> {

    // Sửa ct.isbn thành ct.id.isbn vì isbn nằm trong khóa chính phức hợp
    // @EmbeddedId
    @Query("SELECT ct.id.isbn, SUM(ct.soLuong) as totalSold FROM ChiTietDonHang ct GROUP BY ct.id.isbn ORDER BY totalSold DESC")
    List<Object[]> findTopSellingProjected();
    List<ChiTietDonHang> findByDonHang(DonHang donHang);
}
