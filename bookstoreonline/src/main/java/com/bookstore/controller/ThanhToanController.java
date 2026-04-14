package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.entity.DonHang;
import com.bookstore.entity.ThanhToan;
import com.bookstore.repository.DonHangRepository;
import com.bookstore.repository.ThanhToanRepository;
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
@Tag(name = "Payment Management", description = "Xử lý thanh toán VNPay")
@SuppressWarnings("null")
public class ThanhToanController {

    private final DonHangRepository donHangRepository;
    private final ThanhToanRepository thanhToanRepository;

    public ThanhToanController(DonHangRepository donHangRepository, ThanhToanRepository thanhToanRepository) {
        this.donHangRepository = donHangRepository;
        this.thanhToanRepository = thanhToanRepository;
    }

    @Value("${vnpay.tmn-code}")
    private String vnp_TmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnp_HashSecret;

    @Value("${vnpay.pay-url}")
    private String vnp_PayUrl;

    @Value("${vnpay.return-url}")
    private String vnp_ReturnUrl;

    @PostMapping("/vnpay-create")
    @Operation(summary = "Khởi tạo thanh toán VNPay", description = "Tạo URL thanh toán VNPay cho đơn hàng")
    public ApiResponse<String> createPayment(@RequestParam String maDonHang, HttpServletRequest request) throws Exception {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        if (!"CHO_THANH_TOAN".equals(donHang.getTrangThai())) {
            throw new RuntimeException("Đơn hàng không ở trạng thái chờ thanh toán");
        }

        long amount = donHang.getTongThanhToan().longValue() * 100;
        String vnp_TxnRef = maDonHang + "_" + System.currentTimeMillis();
        
        // Cập nhật mã tham chiếu cho bản ghi ThanhToan
        ThanhToan thanhToan = thanhToanRepository.findByDonHang_MaDonHang(maDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin thanh toán"));
        thanhToan.setMaThamChieuCong(vnp_TxnRef);
        thanhToanRepository.save(thanhToan);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + maDonHang);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", LocalDateTime.now().format(formatter));

        cld.add(Calendar.MINUTE, 15);
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
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                //Build query
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

        return ApiResponse.success("Tạo URL thanh toán thành công", paymentUrl);
    }

    @GetMapping("/vnpay-callback")
    @Operation(summary = "Xử lý kết quả VNPay", description = "Nhận và xác thực kết quả thanh toán từ VNPay")
    public ApiResponse<String> vnpayCallback(@RequestParam Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHashType");
        params.remove("vnp_SecureHash");

        // Sắp xếp các tham số để tính mã hash
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

            ThanhToan tt = thanhToanRepository.findByMaThamChieuCong(txnRef)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin giao dịch"));

            DonHang dh = tt.getDonHang();

            if ("00".equals(responseCode)) {
                tt.setTrangThai("SUCCESS");
                tt.setNgayThanhToan(LocalDateTime.now());
                dh.setTrangThai("DA_THANH_TOAN");
            } else {
                tt.setTrangThai("FAILED");
                dh.setTrangThai("THANH_TOAN_THAT_BAI");
            }
            thanhToanRepository.save(tt);
            donHangRepository.save(dh);

            return ApiResponse.success("Xử lý kết quả thành công", "SUCCESS");
        } else {
            return ApiResponse.error(400, "Sai checksum");
        }
    }
}
