package com.bookstore.dto;

import java.time.LocalDateTime;

public class DanhGiaDTO {
    private Long maDg;
    private String tenKhachHang;
    private String isbn;
    private Integer diemDg;
    private String nhanXet;
    private LocalDateTime ngayDg;

    public DanhGiaDTO() {}

    public Long getMaDg() { return maDg; }
    public void setMaDg(Long maDg) { this.maDg = maDg; }
    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Integer getDiemDg() { return diemDg; }
    public void setDiemDg(Integer diemDg) { this.diemDg = diemDg; }
    public String getNhanXet() { return nhanXet; }
    public void setNhanXet(String nhanXet) { this.nhanXet = nhanXet; }
    public LocalDateTime getNgayDg() { return ngayDg; }
    public void setNgayDg(LocalDateTime ngayDg) { this.ngayDg = ngayDg; }
}
