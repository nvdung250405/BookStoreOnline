package com.bookstore.repository;

import com.bookstore.entity.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {
    List<SupportMessage> findByTicket_TicketIdOrderByCreatedAtAsc(Long ticketId);
    List<SupportMessage> findByCreatedAtAfterAndIsStaffFalse(java.time.LocalDateTime timestamp);
    List<SupportMessage> findByCreatedAtAfterAndIsStaffTrueAndTicket_Customer_Account_Username(java.time.LocalDateTime timestamp, String username);
    
    long countByTicket_Customer_Account_UsernameAndIsStaffTrueAndIsReadFalse(String username);
    long countByIsStaffFalseAndIsReadFalse();
    List<SupportMessage> findByTicket_TicketIdAndIsStaffAndIsReadFalse(Long ticketId, boolean isStaff);
}
