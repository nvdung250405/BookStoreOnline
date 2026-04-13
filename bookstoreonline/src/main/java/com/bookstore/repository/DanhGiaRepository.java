package com.bookstore.repository;

import com.bookstore.entity.DanhGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DanhGiaRepository extends JpaRepository<DanhGia, Long> {
    
    // Lấy tất cả đánh giá của một cuốn sách
    List<DanhGia> findBySach_Isbn(String isbn);
}
