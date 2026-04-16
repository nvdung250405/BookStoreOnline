package com.bookstore.repository;

import com.bookstore.entity.Account;
import com.bookstore.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByUsername(String username);
    long countByStatus(AccountStatus status);
    List<Account> findByUsernameStartingWith(String prefix);
    long countByIsActiveTrue();
}
