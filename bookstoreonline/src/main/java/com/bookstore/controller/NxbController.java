package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.NxbDTO;
import com.bookstore.service.NxbService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/publishers")
@Tag(name = "Publishers", description = "Quản lý dữ liệu Nhà xuất bản")
public class NxbController {

    private final NxbService nxbService;

    public NxbController(NxbService nxbService) {
        this.nxbService = nxbService;
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách NXB", description = "Lấy danh sách tất cả nhà xuất bản hiện có trong hệ thống phục vụ tạo nhanh sách hoặc bộ lọc tiềm kiếm.")
    public ResponseEntity<ApiResponse<List<NxbDTO>>> getAllPublishers() {
        List<NxbDTO> pubs = nxbService.getAllNxbs();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhà xuất bản thành công", pubs));
    }
}
