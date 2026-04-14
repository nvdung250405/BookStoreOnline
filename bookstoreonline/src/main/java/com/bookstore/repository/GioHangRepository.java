package com.bookstore.repository;

import com.bookstore.entity.GioHang;
//import com.bookstore.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GioHangRepository extends JpaRepository<GioHang, Long> {
    
    // Tìm danh sách giỏ hàng dựa trên username của tài khoản khách hàng
    List<GioHang> findByKhachHang_TaiKhoan_Username(String username);
    
    // Xóa toàn bộ giỏ hàng của một khách hàng
    void deleteByKhachHang_TaiKhoan_Username(String username);
    
    // Tìm một sản phẩm cụ thể trong giỏ hàng để cập nhật số lượng
    Optional<GioHang> findByKhachHang_TaiKhoan_UsernameAndSach_Isbn(String username, String isbn);

    List<GioHang> findByKhachHang(com.bookstore.entity.KhachHang khachHang);
}
