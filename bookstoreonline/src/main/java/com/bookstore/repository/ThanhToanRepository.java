package com.bookstore.repository;

import com.bookstore.entity.ThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThanhToanRepository extends JpaRepository<ThanhToan, String> {
    Optional<ThanhToan> findByDonHang_MaDonHang(String maDonHang);
    Optional<ThanhToan> findByMaThamChieuCong(String maThamChieuCong);
}
