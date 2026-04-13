package com.bookstore.repository;

import com.bookstore.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {
    // Đã chuyển sang findByTaiKhoan_Username để thống nhất chuẩn JPA với KhachHangRepository
    Optional<NhanVien> findByTaiKhoan_Username(String username);
}
