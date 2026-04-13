package com.bookstore.service;

import com.bookstore.dto.AuditLogDTO;
import com.bookstore.dto.BookRankingDTO;
import com.bookstore.dto.RevenueReportDTO;
import com.bookstore.entity.Sach;
import com.bookstore.repository.AuditLogRepository;
import com.bookstore.repository.ChiTietDonHangRepository;
import com.bookstore.repository.DonHangRepository;
import com.bookstore.repository.SachRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {

    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;
    private final AuditLogRepository auditLogRepository;
    private final SachRepository sachRepository;

    public AdminDashboardService(DonHangRepository donHangRepository,
                                 ChiTietDonHangRepository chiTietDonHangRepository,
                                 AuditLogRepository auditLogRepository,
                                 SachRepository sachRepository) {
        this.donHangRepository = donHangRepository;
        this.chiTietDonHangRepository = chiTietDonHangRepository;
        this.auditLogRepository = auditLogRepository;
        this.sachRepository = sachRepository;
    }

    @Transactional(readOnly = true)
    public RevenueReportDTO getRevenueReport() {
        BigDecimal revenue = donHangRepository.sumTongTienByTrangThai("HOAN_TAT");
        long count = donHangRepository.countByTrangThai("HOAN_TAT");
        return new RevenueReportDTO(revenue, count, "HOAN_TAT");
    }

    @Transactional(readOnly = true)
    public List<BookRankingDTO> getBookRanking() {
        List<Object[]> results = chiTietDonHangRepository.findTopSellingProjected();
        
        return results.stream().map(row -> {
            String isbn = (String) row[0];
            long totalSold = (long) row[1];
            
            String bookName = sachRepository.findById(isbn)
                    .map(Sach::getTieuDe)
                    .orElse("Sách bí ẩn (Không tìm thấy thông tin)");
            
            return new BookRankingDTO(isbn, bookName, totalSold);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogDTO> getAuditLogs() {
        return auditLogRepository.findAllByOrderByThoiDiemDesc().stream()
                .map(AuditLogDTO::new)
                .collect(Collectors.toList());
    }
}
