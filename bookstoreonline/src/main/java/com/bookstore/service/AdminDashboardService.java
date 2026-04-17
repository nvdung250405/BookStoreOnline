package com.bookstore.service;

import com.bookstore.dto.AuditLogDTO;
import com.bookstore.dto.BookRankingDTO;
import com.bookstore.dto.RevenueReportDTO;
import com.bookstore.entity.Book;
import com.bookstore.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final AuditLogRepository auditLogRepository;
    private final BookRepository bookRepository;
    private final AccountRepository accountRepository;
    private final InventoryRepository inventoryRepository;
    private final SupportTicketRepository supportTicketRepository;

    public AdminDashboardService(OrderRepository orderRepository,
                                 OrderDetailRepository orderDetailRepository,
                                 AuditLogRepository auditLogRepository,
                                 BookRepository bookRepository,
                                 AccountRepository accountRepository,
                                 InventoryRepository inventoryRepository,
                                 SupportTicketRepository supportTicketRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.auditLogRepository = auditLogRepository;
        this.bookRepository = bookRepository;
        this.accountRepository = accountRepository;
        this.inventoryRepository = inventoryRepository;
        this.supportTicketRepository = supportTicketRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getQuickStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Requirement: Active Customer Count (account.is_active = 1)
        stats.put("customerCount", accountRepository.countByIsActiveTrue());
        
        // Total Books
        stats.put("totalBooks", bookRepository.count());
        
        // Low Stock Count (stock_quantity <= alert_threshold)
        long lowStock = inventoryRepository.findAll().stream()
                .filter(inv -> {
                    Integer qty = inv.getStockQuantity();
                    Integer threshold = inv.getAlertThreshold();
                    return qty != null && threshold != null && qty <= threshold;
                })
                .count();
        stats.put("lowStockCount", lowStock);
        
        // Total Revenue (status = COMPLETED)
        stats.put("totalRevenue", orderRepository.sumTotalAmountByStatusCode("COMPLETED"));
        
        // Open Tickets (status = OPEN)
        stats.put("openTickets", supportTicketRepository.countByStatusCode("OPEN"));
        
        return stats;
    }

    @Transactional(readOnly = true)
    public RevenueReportDTO getRevenueReport() {
        BigDecimal revenue = orderRepository.sumTotalAmountByStatusCode("COMPLETED");
        long count = orderRepository.countByStatusCode("COMPLETED");
        return new RevenueReportDTO(revenue, count, "COMPLETED");
    }

    @Transactional(readOnly = true)
    public List<BookRankingDTO> getBookRanking() {
        List<Object[]> results = orderDetailRepository.findTopSellingProjected();
        
        return results.stream().map(row -> {
            String isbn = (row[0] != null) ? (String) row[0] : "";
            long totalSold = (row[1] != null) ? (long) row[1] : 0L;
            
            String title = "";
            if (!isbn.isEmpty()) {
                title = bookRepository.findById(isbn)
                    .map(Book::getTitle)
                    .orElse("Unknown Book");
            }
            
            return new BookRankingDTO(isbn, title, totalSold);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogDTO> getAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc().stream()
                .map(AuditLogDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AuditLogDTO getAuditLogDetail(Long id) {
        if (id == null) throw new IllegalArgumentException("ID cannot be null");
        return auditLogRepository.findById(id)
                .map(AuditLogDTO::new)
                .orElseThrow(() -> new RuntimeException("Audit log not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Object getAuditStats() {
        long totalLogs = auditLogRepository.count();
        long loginCount = auditLogRepository.findAll().stream()
                .filter(log -> "LOGIN".equalsIgnoreCase(log.getAction()))
                .count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLogs", totalLogs);
        stats.put("loginCount", loginCount);
        stats.put("lastScan", LocalDateTime.now());
        return stats;
    }
}
