package com.bookstore.dto;

import java.util.List;

public class CheckoutRequest {
    private String diaChiGiaoHang;
    private String phuongThucThanhToan; // "COD", "VNPAY"
    private String maVoucher;

    public CheckoutRequest() {}

    public String getDiaChiGiaoHang() { return diaChiGiaoHang; }
    public void setDiaChiGiaoHang(String diaChiGiaoHang) { this.diaChiGiaoHang = diaChiGiaoHang; }
    public String getPhuongThucThanhToan() { return phuongThucThanhToan; }
    public void setPhuongThucThanhToan(String phuongThucThanhToan) { this.phuongThucThanhToan = phuongThucThanhToan; }
    public String getMaVoucher() { return maVoucher; }
    public void setMaVoucher(String maVoucher) { this.maVoucher = maVoucher; }
}
