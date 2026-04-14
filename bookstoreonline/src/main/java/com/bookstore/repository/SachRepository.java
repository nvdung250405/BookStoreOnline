package com.bookstore.repository;

import com.bookstore.entity.Sach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SachRepository extends JpaRepository<Sach, String> {

    @Query("SELECT DISTINCT s FROM Sach s " +
           "LEFT JOIN s.danhMuc d " +
           "LEFT JOIN s.nxb n " +
           "LEFT JOIN s.danhSachTacGia t " +
           "WHERE (s.daXoa = false OR s.daXoa IS NULL) AND " +
           "(:keyword IS NULL OR LOWER(s.tenSach) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.moTaNguNghia) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.tenTacGia) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:tenDanhMuc IS NULL OR LOWER(d.tenDanhMuc) LIKE LOWER(CONCAT('%', :tenDanhMuc, '%'))) AND " +
           "(:tenNxb IS NULL OR LOWER(n.tenNxb) LIKE LOWER(CONCAT('%', :tenNxb, '%'))) AND " +
           "(:minPrice IS NULL OR s.giaNiemYet >= :minPrice) AND " +
           "(:maxPrice IS NULL OR s.giaNiemYet <= :maxPrice) AND " +
           "(:minSoTrang IS NULL OR s.soTrang >= :minSoTrang) AND " +
           "(:maxSoTrang IS NULL OR s.soTrang <= :maxSoTrang)")
    List<Sach> searchAndFilterBooks(@Param("keyword") String keyword,
                                    @Param("tenDanhMuc") String tenDanhMuc,
                                    @Param("tenNxb") String tenNxb,
                                    @Param("minPrice") BigDecimal minPrice,
                                    @Param("maxPrice") BigDecimal maxPrice,
                                    @Param("minSoTrang") Integer minSoTrang,
                                    @Param("maxSoTrang") Integer maxSoTrang);
}
