package com.bookstore.repository;

import com.bookstore.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {
    
    // Phương thức này bị mất sau khi bạn Pull code từ Member A, tôi thêm lại để các Service hoạt động
    Optional<KhachHang> findByTaiKhoan_Username(String username);
}
