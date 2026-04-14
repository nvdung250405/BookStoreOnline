package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.DanhMucDTO;
import com.bookstore.dto.DanhMucRequest;
import com.bookstore.service.DanhMucService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Books & Catalog", description = "Quản lý danh mục sách")
public class DanhMucController {

    private final DanhMucService danhMucService;

    // Constructor thủ công thay cho @RequiredArgsConstructor
    public DanhMucController(DanhMucService danhMucService) {
        this.danhMucService = danhMucService;
    }

    @GetMapping("/categories")
    @Operation(summary = "Lấy cây danh mục đa cấp", description = "Trả về cấu trúc cây danh mục phục vụ Menu của Frontend")
    public ResponseEntity<ApiResponse<List<DanhMucDTO>>> getAllCategories() {
        List<DanhMucDTO> categories = danhMucService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @PostMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thêm mới danh mục", description = "Tạo danh mục mới (quyền ADMIN)")
    public ResponseEntity<ApiResponse<DanhMucDTO>> createCategory(@Valid @RequestBody DanhMucRequest request) {
        DanhMucDTO created = danhMucService.createCategory(request);
        return ResponseEntity.status(201).body(ApiResponse.created(created));
    }

    @PutMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sửa danh mục", description = "Cập nhật tên danh mục hoặc chuyển danh mục cha (quyền ADMIN)")
    public ResponseEntity<ApiResponse<DanhMucDTO>> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody DanhMucRequest request) {
        DanhMucDTO updated = danhMucService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật danh mục thành công", updated));
    }

    @DeleteMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa danh mục", description = "Xóa vĩnh viễn danh mục (quyền ADMIN). Sẽ bị chặn nếu danh mục vẫn còn chứa sách bên trong.")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable Integer id) {
        danhMucService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa danh mục thành công", null));
    }
}
