package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.PaymentResponseDTO;
import com.bookstore.repository.PaymentRepository;
import com.bookstore.service.PaymentService;
import com.bookstore.util.RequestUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin("*")
@Tag(name = "Payment Management", description = "Xử lý thanh toán VNPay và Momo thông qua Service Layer")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentService paymentService, PaymentRepository paymentRepository) {
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/admin/all")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy tất cả giao dịch thanh toán (ADMIN)", description = "Admin xem lịch sử toàn bộ các giao dịch thanh toán trong hệ thống.")
    public ApiResponse<List<PaymentResponseDTO>> getAllPayments() {
        List<PaymentResponseDTO> dtos = paymentRepository.findAll().stream().map(p -> {
            PaymentResponseDTO dto = new PaymentResponseDTO();
            dto.setPaymentId(p.getPaymentId());
            dto.setOrderId(p.getOrder().getOrderId());
            dto.setAmount(p.getOrder().getTotalPayment());
            dto.setPaymentMethod(p.getPaymentMethod());
            dto.setStatus(p.getStatus());
            dto.setPaymentDate(p.getPaymentDate());
            dto.setTransactionReference(p.getTransactionReference());
            return dto;
        }).collect(Collectors.toList());
        
        return ApiResponse.success(dtos);
    }

    @GetMapping("/vnpay/create")
    @Operation(summary = "Khởi tạo thanh toán VNPay", description = "Tạo URL thanh toán VNPay cho đơn hàng với IP thực của Client")
    public ApiResponse<String> createPayment(@RequestParam String orderId, HttpServletRequest request) throws Exception {
        String clientIp = RequestUtils.getClientIp(request);
        String paymentUrl = paymentService.createVnpayUrl(orderId, clientIp);
        return ApiResponse.success("Created payment URL successfully", paymentUrl);
    }

    @GetMapping("/momo/create")
    @Operation(summary = "Khởi tạo thanh toán Momo (Simulated)", description = "Tạo URL thanh toán Momo giả lập cho đơn hàng")
    public ApiResponse<String> createMomoPayment(@RequestParam String orderId) {
        String mockUrl = paymentService.createMockMomoUrl(orderId);
        return ApiResponse.success("Simulated Momo payment initialized", mockUrl);
    }

    @GetMapping("/vnpay-callback")
    @Operation(summary = "Xử lý kết quả VNPay", description = "Nhận và xác thực kết quả thanh toán từ VNPay thông qua Service Layer")
    public ApiResponse<String> vnpayCallback(@RequestParam Map<String, String> params) {
        String result = paymentService.processVnpayCallback(params);
        if ("SUCCESS".equals(result)) {
            return ApiResponse.success("Payment result processed", "SUCCESS");
        } else {
            return ApiResponse.error(400, result);
    @Operation(summary = "Xử lý kết quả VNPay", description = "Nhận và xác thực kết quả thanh toán từ VNPay")
    @Transactional
    public RedirectView vnpayCallback(@RequestParam Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHashType");
        params.remove("vnp_SecureHash");

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        String checkSum = VNPayUtils.hmacSHA512(vnp_HashSecret, hashData.toString());
        if (checkSum.equalsIgnoreCase(vnp_SecureHash)) {
            String txnRef = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");

            Payment payment = paymentRepository.findByTransactionReference(txnRef)
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            Order order = payment.getOrder();

            if ("00".equals(responseCode)) {
                payment.setStatusCode("SUCCESS");
                payment.setPaymentDate(LocalDateTime.now());
                order.setStatusCode("CONFIRMED"); 
            } else {
                payment.setStatusCode("FAILED");
                order.setStatusCode("CANCELLED"); 
            }
            paymentRepository.save(payment);
            orderRepository.save(order);

            String redirectUrl = "http://localhost:8080/web/index.html#/Orders/PaymentResult/" + order.getOrderId();
            return new RedirectView(redirectUrl);
        } else {
            return new RedirectView("http://localhost:8080/web/index.html#/Orders/PaymentResult/error");
        }
    }
}
