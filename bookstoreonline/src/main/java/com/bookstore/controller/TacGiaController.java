package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.TacGiaDTO;
import com.bookstore.service.TacGiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@Tag(name = "Authors", description = "Quản lý dữ liệu tác giả")
public class TacGiaController {

    private final TacGiaService tacGiaService;

    public TacGiaController(TacGiaService tacGiaService) {
        this.tacGiaService = tacGiaService;
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách tác giả", description = "Lấy danh sách tất cả tác giả hiện có trong hệ thống phục vụ tạo nhanh sách hoặc hiển thị cho khách xem.")
    public ResponseEntity<ApiResponse<List<TacGiaDTO>>> getAllAuthors() {
        List<TacGiaDTO> authors = tacGiaService.getAllAuthors();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tác giả thành công", authors));
    }
}
