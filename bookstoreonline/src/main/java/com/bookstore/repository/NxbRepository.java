package com.bookstore.repository;

import com.bookstore.entity.Nxb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NxbRepository extends JpaRepository<Nxb, Integer> {
}
