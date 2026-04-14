package com.bookstore.dto;

import lombok.Data;

@Data
public class CheckoutRequest {
    private String username;
    private String maVoucher;
    private String diaChiGiaoHang;
    private String phuongThucThanhToan; // E.g., "VNPAY", "COD"
}
