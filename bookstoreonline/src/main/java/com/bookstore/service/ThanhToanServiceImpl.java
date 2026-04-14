package com.bookstore.service;

import com.bookstore.config.VNPAYConfig;
import com.bookstore.entity.DonHang;
import com.bookstore.entity.ThanhToan;
import com.bookstore.repository.DonHangRepository;
import com.bookstore.repository.ThanhToanRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class ThanhToanServiceImpl implements ThanhToanService {

    private final VNPAYConfig vnpayConfig;
    private final DonHangRepository donHangRepository;
    private final ThanhToanRepository thanhToanRepository;

    public ThanhToanServiceImpl(VNPAYConfig vnpayConfig, 
                                DonHangRepository donHangRepository, 
                                ThanhToanRepository thanhToanRepository) {
        this.vnpayConfig = vnpayConfig;
        this.donHangRepository = donHangRepository;
        this.thanhToanRepository = thanhToanRepository;
    }

    @Override
    public String createVNPayPayment(String maDonHang, HttpServletRequest request) throws UnsupportedEncodingException {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        long amount = donHang.getTongThanhToan().longValue() * 100; // VNPay uses cents (x100)

        String vnp_TxnRef = maDonHang;
        String vnp_TmnCode = vnpayConfig.vnp_TmnCode;
        
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnpayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnpayConfig.getIpAddress(request));

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPAYConfig.hmacSHA512(vnpayConfig.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        
        return vnpayConfig.vnp_PayUrl + "?" + queryUrl;
    }

    @Override
    public void processVNPayCallback(Map<String, String> params) {
        String vnp_ResponseCode = params.get("vnp_ResponseCode");
        String vnp_TxnRef = params.get("vnp_TxnRef");
        String vnp_TransactionNo = params.get("vnp_TransactionNo");

        DonHang donHang = donHangRepository.findById(vnp_TxnRef)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng cho vnp_TxnRef: " + vnp_TxnRef));

        if ("00".equals(vnp_ResponseCode)) {
            // Success
            donHang.setTrangThai("DA_THANH_TOAN");
            
            ThanhToan thanhToan = new ThanhToan();
            thanhToan.setMaThanhToan("VNPay-" + vnp_TransactionNo);
            thanhToan.setDonHang(donHang);
            thanhToan.setPhuongThuc("VNPAY");
            thanhToan.setTrangThai("SUCCESS");
            thanhToan.setNgayThanhToan(LocalDateTime.now());
            thanhToan.setMaThamChieuCong(vnp_TransactionNo);
            
            thanhToanRepository.save(thanhToan);
        } else {
            // Failed
            donHang.setTrangThai("THANH_TOAN_THAT_BAI");
        }
        donHangRepository.save(donHang);
    }
}
