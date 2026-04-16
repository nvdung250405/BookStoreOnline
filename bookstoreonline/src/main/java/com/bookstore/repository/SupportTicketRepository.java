package com.bookstore.repository;

import com.bookstore.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByCustomer_Account_Username(String username);
    long countByStatusCode(String statusCode);
    List<SupportTicket> findByStatusCodeIn(List<String> statusCodes);
}
