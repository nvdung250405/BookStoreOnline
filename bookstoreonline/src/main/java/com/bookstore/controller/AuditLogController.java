package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.AuditLogDTO;
import com.bookstore.dto.AuditLogStatsDTO;
import com.bookstore.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/audit-logs")
@Tag(name = "Audit Logs", description = "Nhật ký hệ thống (Admin only)")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Lấy thống kê nhật ký hệ thống", description = "Các thông số về số lượng log, người dùng hoạt động...")
    public ResponseEntity<ApiResponse<AuditLogStatsDTO>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê thành công", auditLogService.getAuditStats()));
    }

    @GetMapping
    @Operation(summary = "Lấy tất cả nhật ký hệ thống", description = "Lấy danh sách log hoạt động có hỗ trợ lọc theo user, hành động và thời gian.")
    public ResponseEntity<ApiResponse<List<AuditLogDTO>>> getLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<AuditLogDTO> logs;
        if (username != null || action != null || startDate != null || endDate != null) {
            logs = auditLogService.filterLogs(username, action, startDate, endDate);
        } else {
            logs = auditLogService.getAllLogs();
        }
        
        return ResponseEntity.ok(ApiResponse.success("Lấy nhật ký thành công", logs));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết nhật ký", description = "Tìm kiếm chi tiết một bản ghi log cụ thể theo ID.")
    public ResponseEntity<ApiResponse<AuditLogDTO>> getLogDetail(@PathVariable Long id) {
        return auditLogService.getLogById(id)
                .map(log -> ResponseEntity.ok(ApiResponse.success("Lấy chi tiết nhật ký thành công", log)))
                .orElse(ResponseEntity.notFound().build());
    }
}
