package com.bookstore.controller;

import com.bookstore.dto.NhaCungCapDto;
import com.bookstore.service.NhaCungCapService; // Đã đổi
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final NhaCungCapService nhaCungCapService; // Đã đổi

    @GetMapping
    public ResponseEntity<List<NhaCungCapDto>> getAll() {
        return ResponseEntity.ok(nhaCungCapService.layTatCaNhaCungCap());
    }

    @PostMapping
    public ResponseEntity<NhaCungCapDto> create(@RequestBody NhaCungCapDto request) {
        return ResponseEntity.ok(nhaCungCapService.themNhaCungCap(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NhaCungCapDto> update(@PathVariable Integer id, @RequestBody NhaCungCapDto request) {
        return ResponseEntity.ok(nhaCungCapService.suaNhaCungCap(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        nhaCungCapService.xoaNhaCungCap(id);
        return ResponseEntity.ok("Xóa thành công nhà cung cấp!");
    }
}