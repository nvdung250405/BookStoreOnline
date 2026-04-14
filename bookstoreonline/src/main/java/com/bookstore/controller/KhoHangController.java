package com.bookstore.controller;

import com.bookstore.dto.*;
import com.bookstore.service.KhoHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class KhoHangController {

    private final KhoHangService khoHangService;

    // API 37: Tra cứu vị trí kệ và số lượng bằng Barcode
    // Method: GET
    // URL: http://localhost:8080/api/inventory/scan/{isbn}
    @GetMapping("/scan/{isbn}")
    public ResponseEntity<InventoryDetailDTO> scanBarcode(@PathVariable String isbn) {
        InventoryDetailDTO result = khoHangService.scanBarcode(isbn);
        return ResponseEntity.ok(result);
    }

    // API 38: Lọc danh sách hàng sắp hết (Cảnh báo tồn kho)
    // Method: GET
    // URL: http://localhost:8080/api/inventory/low-stock
    @GetMapping("/low-stock")
    public ResponseEntity<List<LowStockAlertDTO>> getLowStockItems() {
        List<LowStockAlertDTO> alerts = khoHangService.getLowStockAlerts();
        return ResponseEntity.ok(alerts);
    }

    // API 39: Tạo phiếu nhập hàng (Tăng tồn kho)
    // Method: POST
    // URL: http://localhost:8080/api/inventory/import
    @PostMapping("/import")
    public ResponseEntity<ImportResponseDto> importInventory(@RequestBody ImportRequestDto request) {
        ImportResponseDto response = khoHangService.nhapKhoHieuQua(request);
        return ResponseEntity.ok(response);
    }
    // API 40: Xuất hàng cho đơn (Tự động đồng bộ và trừ kho)
    // Method: POST
    // URL: http://localhost:8080/api/inventory/export
    @PostMapping("/export")
    public ResponseEntity<String> exportInventory(@RequestBody ExportRequestDto request) {
        String message = khoHangService.xuatKhoTuDong(request);
        return ResponseEntity.ok(message);
    }
}