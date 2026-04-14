package com.bookstore.dto;

public class ExportRequestDto {
    private String maDonHang;
    private String nguoiXuat;

    public ExportRequestDto() {}

    public ExportRequestDto(String maDonHang, String nguoiXuat) {
        this.maDonHang = maDonHang;
        this.nguoiXuat = nguoiXuat;
    }

    public String getMaDonHang() { return maDonHang; }
    public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }
    public String getNguoiXuat() { return nguoiXuat; }
    public void setNguoiXuat(String nguoiXuat) { this.nguoiXuat = nguoiXuat; }
}