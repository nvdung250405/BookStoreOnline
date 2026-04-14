package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.CheckoutRequest;
import com.bookstore.dto.DonHangDTO;
import com.bookstore.service.DonHangService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Orders", description = "Quản lý đơn hàng và thanh toán")
public class DonHangController {

    private final DonHangService donHangService;

    public DonHangController(DonHangService donHangService) {
        this.donHangService = donHangService;
    }

    @PostMapping("/orders/checkout")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Xử lý đặt hàng", description = "Tạo đơn hàng mới từ giỏ hàng hiện tại của khách hàng.")
    public ResponseEntity<ApiResponse<DonHangDTO>> checkout(Principal principal, @RequestBody CheckoutRequest request) {
        DonHangDTO order = donHangService.checkout(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Đặt hàng thành công", order));
    }

    @GetMapping("/orders/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Xem lịch sử đơn hàng", description = "Lấy danh sách các đơn hàng đã đặt của khách hàng hiện tại.")
    public ResponseEntity<ApiResponse<List<DonHangDTO>>> getHistory(Principal principal) {
        List<DonHangDTO> history = donHangService.getHistory(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử đơn hàng thành công", history));
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "Xem chi tiết đơn hàng", description = "Lấy thông tin chi tiết của một đơn hàng bao gồm danh sách sản phẩm.")
    public ResponseEntity<ApiResponse<DonHangDTO>> getDetail(@PathVariable String id) {
        DonHangDTO order = donHangService.getDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết đơn hàng thành công", order));
    }

    @PutMapping("/orders/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Hủy đơn hàng", description = "Khách hàng tự hủy đơn hàng khi đơn còn ở trạng thái chờ xác nhận.")
    public ResponseEntity<ApiResponse<String>> cancelOrder(Principal principal, @PathVariable String id) {
        donHangService.cancelOrder(principal.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("Hủy đơn hàng thành công", null));
    }

    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách tất cả đơn hàng (ADMIN/STAFF)", description = "Quản lý toàn bộ đơn hàng trong hệ thống.")
    public ResponseEntity<ApiResponse<List<DonHangDTO>>> getAllOrders() {
        List<DonHangDTO> orders = donHangService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn hàng thành công", orders));
    }

    @PutMapping("/admin/orders/{id}/status")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    @Operation(summary = "Cập nhật trạng thái đơn hàng (ADMIN/STAFF)", description = "Thay đổi trạng thái đơn hàng (Xác nhận, Đang giao, Đã giao...)")
    public ResponseEntity<ApiResponse<String>> updateStatus(@PathVariable String id, @RequestParam String status) {
        donHangService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái đơn hàng thành công", null));
    }
}
