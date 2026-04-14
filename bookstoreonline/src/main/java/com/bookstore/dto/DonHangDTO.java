package com.bookstore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DonHangDTO {
    private String maDonHang;
    private LocalDateTime ngayTao;
    private BigDecimal tongTienHang;
    private BigDecimal phiVanChuyen;
    private BigDecimal tongThanhToan;
    private String trangThai;
    private String diaChiGiaoCuThe;
    private String hoTenKhachHang;
    private List<ChiTietDonHangDTO> chiTiet;

    public DonHangDTO() {}

    public String getMaDonHang() { return maDonHang; }
    public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
    public BigDecimal getTongTienHang() { return tongTienHang; }
    public void setTongTienHang(BigDecimal tongTienHang) { this.tongTienHang = tongTienHang; }
    public BigDecimal getPhiVanChuyen() { return phiVanChuyen; }
    public void setPhiVanChuyen(BigDecimal phiVanChuyen) { this.phiVanChuyen = phiVanChuyen; }
    public BigDecimal getTongThanhToan() { return tongThanhToan; }
    public void setTongThanhToan(BigDecimal tongThanhToan) { this.tongThanhToan = tongThanhToan; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getDiaChiGiaoCuThe() { return diaChiGiaoCuThe; }
    public void setDiaChiGiaoCuThe(String diaChiGiaoCuThe) { this.diaChiGiaoCuThe = diaChiGiaoCuThe; }
    public String getHoTenKhachHang() { return hoTenKhachHang; }
    public void setHoTenKhachHang(String hoTenKhachHang) { this.hoTenKhachHang = hoTenKhachHang; }
    public List<ChiTietDonHangDTO> getChiTiet() { return chiTiet; }
    public void setChiTiet(List<ChiTietDonHangDTO> chiTiet) { this.chiTiet = chiTiet; }
}
