package com.bookstore.dto;

import java.util.List;

public class TrackingResponseDto {
    private String maDonHang;
    private String donViVanChuyen;
    private String maVanDon;
    private String trangThaiHienTai;
    private List<ChiTietTracking> lichSuTrangThai;

    public TrackingResponseDto() {}

    public TrackingResponseDto(String maDonHang, String donViVanChuyen, String maVanDon, String trangThaiHienTai, List<ChiTietTracking> lichSuTrangThai) {
        this.maDonHang = maDonHang;
        this.donViVanChuyen = donViVanChuyen;
        this.maVanDon = maVanDon;
        this.trangThaiHienTai = trangThaiHienTai;
        this.lichSuTrangThai = lichSuTrangThai;
    }

    public String getMaDonHang() { return maDonHang; }
    public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }
    public String getDonViVanChuyen() { return donViVanChuyen; }
    public void setDonViVanChuyen(String donViVanChuyen) { this.donViVanChuyen = donViVanChuyen; }
    public String getMaVanDon() { return maVanDon; }
    public void setMaVanDon(String maVanDon) { this.maVanDon = maVanDon; }
    public String getTrangThaiHienTai() { return trangThaiHienTai; }
    public void setTrangThaiHienTai(String trangThaiHienTai) { this.trangThaiHienTai = trangThaiHienTai; }
    public List<ChiTietTracking> getLichSuTrangThai() { return lichSuTrangThai; }
    public void setLichSuTrangThai(List<ChiTietTracking> lichSuTrangThai) { this.lichSuTrangThai = lichSuTrangThai; }

    public static class ChiTietTracking {
        private String thoiGian;
        private String trangThai;
        private String viTri;

        public ChiTietTracking() {}

        public ChiTietTracking(String thoiGian, String trangThai, String viTri) {
            this.thoiGian = thoiGian;
            this.trangThai = trangThai;
            this.viTri = viTri;
        }

        public String getThoiGian() { return thoiGian; }
        public void setThoiGian(String thoiGian) { this.thoiGian = thoiGian; }
        public String getTrangThai() { return trangThai; }
        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
        public String getViTri() { return viTri; }
        public void setViTri(String viTri) { this.viTri = viTri; }
    }
}