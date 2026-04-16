package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.entity.Order;
import com.bookstore.entity.Payment;
import com.bookstore.repository.OrderRepository;
import com.bookstore.repository.PaymentRepository;
import com.bookstore.security.VNPayUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin("*")
@Tag(name = "Payment Management", description = "Xử lý thanh toán VNPay và Momo")
@SuppressWarnings("null")
public class PaymentController {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public PaymentController(OrderRepository orderRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/admin/all")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy tất cả giao dịch thanh toán (ADMIN)", description = "Admin xem lịch sử toàn bộ các giao dịch thanh toán trong hệ thống.")
    public ApiResponse<List<Payment>> getAllPayments() {
        return ApiResponse.success(paymentRepository.findAll());
    }

    @Value("${vnpay.tmn-code}")
    private String vnp_TmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnp_HashSecret;

    @Value("${vnpay.pay-url}")
    private String vnp_PayUrl;

    @Value("${vnpay.return-url}")
    private String vnp_ReturnUrl;

    @GetMapping("/vnpay/create")
    @Operation(summary = "Khởi tạo thanh toán VNPay", description = "Tạo URL thanh toán VNPay cho đơn hàng")
    public ApiResponse<String> createPayment(@RequestParam String orderId, HttpServletRequest request) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"NEW".equals(order.getStatusCode())) {
            throw new RuntimeException("Order is not in pending payment status");
        }

        long amount = order.getTotalPayment().longValue() * 100;
        String vnp_TxnRef = orderId + "_" + System.currentTimeMillis();
        
        // Update transaction reference for Payment record
        Payment payment = paymentRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment information not found"));
        payment.setTransactionReference(vnp_TxnRef);
        paymentRepository.save(payment);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Payment for order " + orderId);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", LocalDateTime.now().format(formatter));
        vnp_Params.put("vnp_ExpireDate", LocalDateTime.now().plusMinutes(15).format(formatter));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayUtils.hmacSHA512(vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnp_PayUrl + "?" + queryUrl;
        return ApiResponse.success("Created payment URL successfully", paymentUrl);
    }

    @GetMapping("/momo/create")
    @Operation(summary = "Khởi tạo thanh toán Momo (Simulated)", description = "Tạo URL thanh toán Momo giả lập cho đơn hàng")
    public ApiResponse<String> createMomoPayment(@RequestParam String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"NEW".equals(order.getStatusCode())) {
            throw new RuntimeException("Order is not in pending payment status");
        }

        String mockMomoUrl = "http://localhost:8080/web/Orders/PaymentResult?orderId=" + orderId + "&status=success&provider=momo";
        
        Payment payment = paymentRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment information not found"));
        payment.setPaymentMethod("MOMO");
        payment.setStatusCode("PENDING");
        paymentRepository.save(payment);

        return ApiResponse.success("Simulated Momo payment initialized", mockMomoUrl);
    }

    @GetMapping("/vnpay-callback")
    @Operation(summary = "Xử lý kết quả VNPay", description = "Nhận và xác thực kết quả thanh toán từ VNPay")
    public ApiResponse<String> vnpayCallback(@RequestParam Map<String, String> params) {
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

            return ApiResponse.success("Payment result processed", "SUCCESS");
        } else {
            return ApiResponse.error(400, "Invalid checksum");
        }
    }
}
