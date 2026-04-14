package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.SachDTO;
import com.bookstore.service.SachService;
import com.bookstore.service.AiSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.bookstore.dto.SachCreateRequest;
import com.bookstore.dto.SachUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Books", description = "Quản lý thông tin sách")
public class SachController {

    private final SachService sachService;
    private final AiSearchService aiSearchService;

    public SachController(SachService sachService, AiSearchService aiSearchService) {
        this.sachService = sachService;
        this.aiSearchService = aiSearchService;
    }

    @GetMapping("/books")
    @Operation(summary = "Lấy danh sách và tìm kiếm sách", description = "Lấy danh sách tất cả sách công khai hoặc tìm kiếm, lọc theo tên, mô tả, khoảng giá, danh mục, NXB, số lượng trang.")
    public ResponseEntity<ApiResponse<List<SachDTO>>> getAllBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String nxbName,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minSoTrang,
            @RequestParam(required = false) Integer maxSoTrang) {
        List<SachDTO> books = sachService.searchAndFilterBooks(keyword, categoryName, nxbName, minPrice, maxPrice, minSoTrang, maxSoTrang);
        return ResponseEntity.ok(ApiResponse.success("Lọc và tìm kiếm sách thành công", books));
    }

    @PostMapping("/books/ai-search")
    @Operation(summary = "Tìm kiếm sách bằng AI (NLP)", description = "Phân tích câu nói tự nhiên (Ví dụ: 'tìm sách thiếu nhi giá dưới 50k') để tự bóc tách mức giá và danh mục.")
    public ResponseEntity<ApiResponse<List<SachDTO>>> aiSearchBooks(
            @Valid @RequestBody com.bookstore.dto.AiSearchRequest request) {
        List<SachDTO> books = aiSearchService.searchByNaturalLanguage(request.getQuery());
        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm AI thành công", books));
    }

    @GetMapping("/books/{isbn}")
    @Operation(summary = "Xem chi tiết sách", description = "Lấy thông tin chi tiết của một cuốn sách bao gồm các quyền lợi và thông tin tác giả, NXB cấu thành dựa trên mã ISBN.")
    public ResponseEntity<ApiResponse<SachDTO>> getBookDetail(
            @org.springframework.web.bind.annotation.PathVariable String isbn) {
        SachDTO book = sachService.getBookByIsbn(isbn);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi tiết sách thành công", book));
    }

    @PostMapping("/admin/books")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thêm sách mới (ADMIN)", description = "Admin thêm sách mới kèm theo danh mục, nhà xuất bản và danh sách tác giả.")
    public ResponseEntity<ApiResponse<SachDTO>> createBook(
            @Valid @RequestBody SachCreateRequest request) {
        SachDTO created = sachService.createBook(request);
        return ResponseEntity.status(201).body(ApiResponse.created(created));
    }

    @PutMapping("/admin/books/{isbn}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sửa thông tin sách (ADMIN)", description = "Cập nhật thông tin chi tiết của sách (tên, giá, mô tả, NXB, danh mục, tác giả) dựa trên mã ISBN.")
    public ResponseEntity<ApiResponse<SachDTO>> updateBook(
            @org.springframework.web.bind.annotation.PathVariable String isbn,
            @Valid @RequestBody SachUpdateRequest request) {
        SachDTO updated = sachService.updateBook(isbn, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin sách thành công", updated));
    }

    @DeleteMapping("/admin/books/{isbn}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa mềm sách (ADMIN)", description = "Đánh dấu sách là đã xóa (xóa mềm) trên hệ thống mà không làm mất dữ liệu lịch sử.")
    public ResponseEntity<ApiResponse<String>> deleteBook(
            @org.springframework.web.bind.annotation.PathVariable String isbn) {
        sachService.softDeleteBook(isbn);
        return ResponseEntity.ok(ApiResponse.success("Xóa mềm sách thành công", null));
    }
}
