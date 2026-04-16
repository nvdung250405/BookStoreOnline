package com.bookstore.controller;

import com.bookstore.dto.*;
import com.bookstore.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory Management", description = "Quản lý tồn kho, nhập xuất hàng hóa")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Operation(summary = "Tra cứu tồn kho", description = "Tìm kiếm vị trí kệ và số lượng tồn bằng mã ISBN/Barcode")
    @GetMapping("/scan/{isbn}")
    public ResponseEntity<ApiResponse<InventoryDetailDTO>> scanBarcode(@PathVariable String isbn) {
        InventoryDetailDTO result = inventoryService.scanBarcode(isbn);

        ApiResponse<InventoryDetailDTO> response = new ApiResponse<>(200, "Quét mã thành công", result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Cảnh báo tồn kho thấp", description = "Lấy danh sách các mặt hàng có số lượng tồn kho dưới ngưỡng an toàn")
    public ResponseEntity<ApiResponse<List<LowStockAlertDTO>>> getLowStockItems() {
        List<LowStockAlertDTO> alerts = inventoryService.getLowStockAlerts();

        // Đã bọc ApiResponse
        ApiResponse<List<LowStockAlertDTO>> response = new ApiResponse<>(200, "Lấy danh sách cảnh báo thành công", alerts);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/import")
    @Operation(summary = "Tạo phiếu nhập hàng", description = "Lập phiếu nhập hàng mới và tăng số lượng tồn kho thực tế")
    public ResponseEntity<ApiResponse<PurchaseOrderResponseDTO>> importInventory(@RequestBody PurchaseOrderRequestDTO request) {
        PurchaseOrderResponseDTO result = inventoryService.importStock(request);

        // Đã bọc ApiResponse
        ApiResponse<PurchaseOrderResponseDTO> response = new ApiResponse<>(200, "Lập phiếu nhập thành công", result);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/export")
    @Operation(summary = "Xuất kho", description = "Trừ tồn kho khi đơn hàng được đóng gói, vận chuyển")
    public ResponseEntity<ApiResponse<String>> exportInventory(@RequestBody ExportOrderRequestDTO request) {
        String message = inventoryService.exportStock(request);

        // Đã bọc ApiResponse
        ApiResponse<String> response = new ApiResponse<>(200, "Xuất kho thành công", message);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/all")
    @Operation(summary = "Lấy toàn bộ danh sách tồn kho")
    public ApiResponse<List<InventoryDetailDTO>> getAllInventory() {
        // Hàm này trong Service bạn viết query lấy toàn bộ kho ra map vào DTO nhé
        return ApiResponse.success(inventoryService.getAllInventory());
    }
}
