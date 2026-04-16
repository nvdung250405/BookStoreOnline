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

    @GetMapping("/scan/{isbn}")
    @Operation(summary = "Tra cứu tồn kho", description = "Tìm kiếm vị trí kệ và số lượng tồn bằng mã ISBN/Barcode")
    public ResponseEntity<InventoryDetailDTO> scanBarcode(@PathVariable String isbn) {
        InventoryDetailDTO result = inventoryService.scanBarcode(isbn);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Cảnh báo tồn kho thấp", description = "Lấy danh sách các mặt hàng có số lượng tồn kho dưới ngưỡng an toàn")
    public ResponseEntity<List<LowStockAlertDTO>> getLowStockItems() {
        List<LowStockAlertDTO> alerts = inventoryService.getLowStockAlerts();
        return ResponseEntity.ok(alerts);
    }

    @PostMapping("/import")
    @Operation(summary = "Tạo phiếu nhập hàng", description = "Lập phiếu nhập hàng mới và tăng số lượng tồn kho thực tế")
    public ResponseEntity<PurchaseOrderResponseDTO> importInventory(@RequestBody PurchaseOrderRequestDTO request) {
        PurchaseOrderResponseDTO response = inventoryService.importStock(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/export")
    @Operation(summary = "Xuất kho", description = "Trừ tồn kho khi đơn hàng được đóng gói, vận chuyển")
    public ResponseEntity<String> exportInventory(@RequestBody ExportOrderRequestDTO request) {
        String message = inventoryService.exportStock(request);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/adjust")
    @Operation(summary = "Điều chỉnh tồn kho", description = "Cập nhật số lượng tồn kho thủ phòng lý do cụ thể")
    public ResponseEntity<String> adjustInventory(@RequestBody InventoryAdjustmentRequest request) {
        inventoryService.adjustStock(request.getIsbn(), request.getNewQuantity(), request.getReason(), request.getStaffId());
        return ResponseEntity.ok("Stock adjusted successfully!");
    }
}
