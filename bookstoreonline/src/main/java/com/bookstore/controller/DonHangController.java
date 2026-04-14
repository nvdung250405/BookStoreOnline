package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.CheckoutRequest;
import com.bookstore.dto.DonHangResponseDTO;
import com.bookstore.service.DonHangService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin("*")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Quản lý đơn hàng và thanh toán")
public class DonHangController {

    private final DonHangService donHangService;

    @PostMapping("/checkout")
    @Operation(summary = "Xử lý giao dịch đặt hàng", description = "Tạo đơn hàng mới từ giỏ hàng hiện tại")
    public ApiResponse<DonHangResponseDTO> checkout(@RequestBody CheckoutRequest request) {
        return ApiResponse.success("Đặt hàng thành công", donHangService.checkout(request));
    }

    @GetMapping("/history")
    @Operation(summary = "Xem lịch sử đơn của khách", description = "Lấy danh sách đơn hàng đã đặt của người dùng hiện tại")
    public ApiResponse<List<DonHangResponseDTO>> getHistory(@RequestParam String username) {
        return ApiResponse.success(donHangService.layLichSuDonHang(username));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết hóa đơn", description = "Lấy thông tin chi tiết của một đơn hàng cụ thể")
    public ApiResponse<DonHangResponseDTO> getOrderDetail(@PathVariable String id) {
        return ApiResponse.success(donHangService.layChiTietDonHang(id));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Khách hàng tự hủy đơn", description = "Hủy đơn hàng nếu đơn vẫn ở trạng thái chờ xác nhận hoặc chờ thanh toán")
    public ApiResponse<String> cancelOrder(@PathVariable String id) {
        donHangService.huyDonHang(id);
        return ApiResponse.success("Đã hủy đơn hàng thành công", null);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "[STAFF] Quản lý đơn hàng", description = "Admin xem toàn bộ danh sách đơn hàng trong hệ thống")
    public ApiResponse<List<DonHangResponseDTO>> getAllOrders() {
        return ApiResponse.success(donHangService.layTatCaDonHang());
    }

    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "[STAFF] Cập nhật trạng thái đơn", description = "Admin cập nhật trạng thái mới cho đơn hàng (Xác nhận, Đã giao, v.v)")
    public ApiResponse<String> updateStatus(@PathVariable String id, @RequestParam String trangThai) {
        donHangService.capNhatTrangThai(id, trangThai);
        return ApiResponse.success("Cập nhật trạng thái thành công", null);
    }
}
