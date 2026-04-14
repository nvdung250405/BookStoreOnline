package com.bookstore.repository;

import com.bookstore.entity.DonHang;
import com.bookstore.entity.PhieuXuat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhieuXuatRepository extends JpaRepository<PhieuXuat, String> {
    boolean existsByDonHang(DonHang donHang);
}