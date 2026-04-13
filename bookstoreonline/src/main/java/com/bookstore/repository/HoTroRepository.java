package com.bookstore.repository;

import com.bookstore.entity.HoTro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoTroRepository extends JpaRepository<HoTro, Long> {
    List<HoTro> findByKhachHang_TaiKhoan_Username(String username);
}
