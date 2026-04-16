package com.bookstore.repository;

import com.bookstore.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    
    List<Category> findByParentIsNull();
    
    List<Category> findByCategoryNameContainingIgnoreCase(String name);

    Optional<Category> findByCategoryName(String name);
}
