package com.bookstore.dto;

import java.math.BigDecimal;

public class ImportResponseDto {
    private String maPhieuNhap;
    private BigDecimal tongTien;
    private String thongBao;

    public ImportResponseDto() {}

    public ImportResponseDto(String maPhieuNhap, BigDecimal tongTien, String thongBao) {
        this.maPhieuNhap = maPhieuNhap;
        this.tongTien = tongTien;
        this.thongBao = thongBao;
    }

    public String getMaPhieuNhap() { return maPhieuNhap; }
    public void setMaPhieuNhap(String maPhieuNhap) { this.maPhieuNhap = maPhieuNhap; }
    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }
    public String getThongBao() { return thongBao; }
    public void setThongBao(String thongBao) { this.thongBao = thongBao; }
}