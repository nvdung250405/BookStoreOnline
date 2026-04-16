package com.bookstore.repository;

import com.bookstore.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {

    @Query("SELECT DISTINCT b FROM Book b " +
           "LEFT JOIN b.category c " +
           "LEFT JOIN b.publisher p " +
           "LEFT JOIN b.authors a " +
           "WHERE (b.isDeleted = false OR b.isDeleted IS NULL) AND " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryNames IS NULL OR c.categoryName IN :categoryNames) AND " +
           "(:publisherName IS NULL OR :publisherName = '' OR p.publisherName = :publisherName) AND " +
           "(:minPrice IS NULL OR b.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR b.price <= :maxPrice)")
    List<Book> searchAndFilterBooks(@Param("keyword") String keyword,
                                    @Param("categoryNames") java.util.List<String> categoryNames,
                                    @Param("publisherName") String publisherName,
                                    @Param("minPrice") BigDecimal minPrice,
                                    @Param("maxPrice") BigDecimal maxPrice);
}
