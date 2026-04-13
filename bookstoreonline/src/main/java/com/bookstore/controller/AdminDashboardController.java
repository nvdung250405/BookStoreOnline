package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.AuditLogDTO;
import com.bookstore.dto.BookRankingDTO;
import com.bookstore.dto.RevenueReportDTO;
import com.bookstore.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Dashboard", description = "Các API báo cáo, thống kê và nhật ký hệ thống dành cho Quản trị viên")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    public AdminDashboardController(AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard/revenue")
    @Operation(summary = "Báo cáo doanh thu", description = "Tính tổng doanh thu từ các đơn hàng đã hoàn tất (HOAN_TAT)")
    public ResponseEntity<ApiResponse<RevenueReportDTO>> getRevenueReport() {
        RevenueReportDTO report = dashboardService.getRevenueReport();
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo doanh thu thành công", report));
    }

    @GetMapping("/dashboard/ranking")
    @Operation(summary = "Thống kê Top bán chạy", description = "Lấy danh sách các đầu sách có doanh số bán ra cao nhất")
    public ResponseEntity<ApiResponse<List<BookRankingDTO>>> getBookRanking() {
        List<BookRankingDTO> ranking = dashboardService.getBookRanking();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách xếp hạng thành công", ranking));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Nhật ký hệ thống", description = "Xem danh sách các hoạt động audit trên toàn hệ thống")
    public ResponseEntity<ApiResponse<List<AuditLogDTO>>> getAuditLogs() {
        List<AuditLogDTO> logs = dashboardService.getAuditLogs();
        return ResponseEntity.ok(ApiResponse.success("Lấy nhật ký hệ thống thành công", logs));
    }
}
