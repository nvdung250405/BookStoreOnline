package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.service.ThanhToanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Xử lý thanh toán trực tuyến")
public class ThanhToanController {

    private final ThanhToanService thanhToanService;

    public ThanhToanController(ThanhToanService thanhToanService) {
        this.thanhToanService = thanhToanService;
    }

    @PostMapping("/vnpay-create")
    @Operation(summary = "Khởi tạo thanh toán VNPay", description = "Tạo URL thanh toán VNPay cho đơn hàng.")
    public ResponseEntity<ApiResponse<String>> createVNPayPayment(@RequestParam String maDonHang, HttpServletRequest request) throws UnsupportedEncodingException {
        String paymentUrl = thanhToanService.createVNPayPayment(maDonHang, request);
        return ResponseEntity.ok(ApiResponse.success("Tạo URL thanh toán thành công", paymentUrl));
    }

    @GetMapping("/vnpay-callback")
    @Operation(summary = "Xử lý kết quả VNPay (Callback)", description = "VNPay sẽ gọi API này để thông báo kết quả giao dịch.")
    public ResponseEntity<ApiResponse<String>> vnpayCallback(@RequestParam Map<String, String> params) {
        thanhToanService.processVNPayCallback(params);
        String responseCode = params.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            return ResponseEntity.ok(ApiResponse.success("Thanh toán thành công", null));
        } else {
            return ResponseEntity.status(400).body(ApiResponse.error(400, "Thanh toán thất bại. Mã lỗi: " + responseCode));
        }
    }
}
