package com.bookstore.repository;

import com.bookstore.entity.DonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;

@Repository
public interface DonHangRepository extends JpaRepository<DonHang, String> {

    // Đã đổi kiểu ID từ Long thành String và sửa d.tongTien thành d.tongThanhToan
    @Query("SELECT SUM(d.tongThanhToan) FROM DonHang d WHERE d.trangThai = :trangThai")
    BigDecimal sumTongTienByTrangThai(@Param("trangThai") String trangThai);

    @Query("SELECT COUNT(d) FROM DonHang d WHERE d.trangThai = :trangThai")
    long countByTrangThai(@Param("trangThai") String trangThai);

    java.util.List<DonHang> findByKhachHang_TaiKhoan_UsernameOrderByNgayTaoDesc(String username);
}
