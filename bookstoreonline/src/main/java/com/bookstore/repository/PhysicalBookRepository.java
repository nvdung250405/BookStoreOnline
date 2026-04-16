package com.bookstore.repository;

import com.bookstore.entity.PhysicalBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhysicalBookRepository extends JpaRepository<PhysicalBook, String> {
}
