package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.BookRankingDTO;
import com.bookstore.dto.RevenueReportDTO;
import com.bookstore.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
@Tag(name = "Admin Dashboard", description = "Các API báo cáo, thống kê và nhật ký hệ thống dành cho Quản trị viên")
@CrossOrigin("*")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    public AdminDashboardController(AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Thống kê tổng hợp", description = "Lấy các con số tổng quát: khách hàng, tồn kho, đơn hàng mới...")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getQuickStats() {
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê thành công", dashboardService.getQuickStats()));
    }

    @GetMapping("/dashboard/revenue")
    @Operation(summary = "Báo cáo doanh thu", description = "Tính tổng doanh thu từ các đơn hàng đã hoàn tất (COMPLETED)")
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

    @GetMapping("/audit-stats")
    @Operation(summary = "Thống kê audit", description = "Lấy dữ liệu thống kê hoạt động hệ thống cho dashboard")
    public ResponseEntity<ApiResponse<Object>> getAuditStats() {
        Object stats = dashboardService.getAuditStats();
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê hệ thống thành công", stats));
    }
}
