package com.bookstore.dto;

import java.time.LocalDateTime;

public class HoTroDTO {
    private Long maHoTro;
    private String tenKhachHang;
    private String tieuDe;
    private String noiDung;
    private String trangThai;
    private LocalDateTime thoiGian;

    public HoTroDTO() {}

    public Long getMaHoTro() { return maHoTro; }
    public void setMaHoTro(Long maHoTro) { this.maHoTro = maHoTro; }
    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }
    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }
}
