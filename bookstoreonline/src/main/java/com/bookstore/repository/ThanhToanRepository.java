package com.bookstore.repository;

import com.bookstore.entity.ThanhToan;
import com.bookstore.entity.DonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ThanhToanRepository extends JpaRepository<ThanhToan, String> {
    Optional<ThanhToan> findByDonHang(DonHang donHang);
    Optional<ThanhToan> findByMaThamChieuCong(String maThamChieuCong);
}
