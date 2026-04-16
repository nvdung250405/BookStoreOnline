package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.AuthorDTO;
import com.bookstore.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@Tag(name = "Authors", description = "Quản lý dữ liệu tác giả")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách tác giả", description = "Lấy danh sách tất cả tác giả hiện có trong hệ thống phục vụ tạo nhanh sách hoặc hiển thị cho khách xem.")
    public ResponseEntity<ApiResponse<List<AuthorDTO>>> getAllAuthors() {
        List<AuthorDTO> authors = authorService.getAllAuthors();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tác giả thành công", authors));
    }

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thêm tác giả mới (ADMIN)", description = "Admin thêm tác giả mới vào hệ thống.")
    public ResponseEntity<ApiResponse<AuthorDTO>> createAuthor(@RequestBody AuthorDTO dto) {
        return ResponseEntity.status(201).body(ApiResponse.created(authorService.saveAuthor(dto)));
    }

    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật tác giả (ADMIN)", description = "Admin cập nhật thông tin tác giả dựa trên mã ID.")
    public ResponseEntity<ApiResponse<AuthorDTO>> updateAuthor(@PathVariable Integer id, @RequestBody AuthorDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tác giả thành công", authorService.updateAuthor(id, dto)));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa tác giả (ADMIN)", description = "Admin xóa tác giả khỏi hệ thống nếu không có sách liên kết.")
    public ResponseEntity<ApiResponse<Void>> deleteAuthor(@PathVariable Integer id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa tác giả thành công", null));
    }
}
