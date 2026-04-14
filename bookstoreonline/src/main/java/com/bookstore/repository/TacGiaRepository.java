package com.bookstore.repository;

import com.bookstore.entity.TacGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TacGiaRepository extends JpaRepository<TacGia, Integer> {
}
