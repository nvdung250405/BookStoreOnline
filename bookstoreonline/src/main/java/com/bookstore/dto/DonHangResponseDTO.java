package com.bookstore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
 
public class DonHangResponseDTO {
    private String maDonHang;
    private String username;
    private String hoTenKhachHang;
    private LocalDateTime ngayTao;
    private BigDecimal tongTienHang;
    private BigDecimal phiVanChuyen;
    private BigDecimal tongThanhToan;
    private String trangThai;
    private String diaChiGiaoHang;
    private String maVoucher;
    private List<ChiTietDonHangDTO> chiTietDonHangs;

    public DonHangResponseDTO() {}

    public String getMaDonHang() { return maDonHang; }
    public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getHoTenKhachHang() { return hoTenKhachHang; }
    public void setHoTenKhachHang(String hoTenKhachHang) { this.hoTenKhachHang = hoTenKhachHang; }
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
    public String getDiaChiGiaoHang() { return diaChiGiaoHang; }
    public void setDiaChiGiaoHang(String diaChiGiaoHang) { this.diaChiGiaoHang = diaChiGiaoHang; }
    public String getMaVoucher() { return maVoucher; }
    public void setMaVoucher(String maVoucher) { this.maVoucher = maVoucher; }
    public List<ChiTietDonHangDTO> getChiTietDonHangs() { return chiTietDonHangs; }
    public void setChiTietDonHangs(List<ChiTietDonHangDTO> chiTietDonHangs) { this.chiTietDonHangs = chiTietDonHangs; }
}
