package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.DanhMucDTO;
import com.bookstore.service.DanhMucService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Books & Catalog", description = "Quản lý danh mục sách (Dành cho Member B)")
public class DanhMucController {

    private final DanhMucService danhMucService;

    // Constructor thủ công thay cho @RequiredArgsConstructor
    public DanhMucController(DanhMucService danhMucService) {
        this.danhMucService = danhMucService;
    }

    @GetMapping
    @Operation(summary = "Lấy cây danh mục đa cấp", description = "Trả về cấu trúc cây danh mục phục vụ Menu của Frontend")
    public ResponseEntity<ApiResponse<List<DanhMucDTO>>> getAllCategories() {
        List<DanhMucDTO> categories = danhMucService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @PostMapping("/admin")
    @Operation(summary = "Thêm mới danh mục", description = "Tạo danh mục mới (quyền ADMIN)")
    public ResponseEntity<ApiResponse<DanhMucDTO>> createCategory(@RequestBody DanhMucDTO dto) {
        DanhMucDTO created = danhMucService.createCategory(dto);
        return ResponseEntity.status(201).body(ApiResponse.created(created));
    }
}
