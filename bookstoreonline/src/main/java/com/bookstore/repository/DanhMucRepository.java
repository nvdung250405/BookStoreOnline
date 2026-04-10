package com.bookstore.repository;

import com.bookstore.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DanhMucRepository extends JpaRepository<DanhMuc, Integer> {
    // Tìm các danh mục cha (không có parentId)
    List<DanhMuc> findByDanhMucChaIsNull();
    
    // Tìm kiếm danh mục theo tên (chứa từ khóa)
    List<DanhMuc> findByTenDanhMucContainingIgnoreCase(String ten);
}
