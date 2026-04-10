package com.bookstore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "chi_tiet_don_hang")
@IdClass(com.bookstore.entity.ChiTietDonHangId.class)
public class ChiTietDonHang {
    @Id
    private Long maDonHang;

    @Id
    private String isbn;

    @Column(nullable = false)
    private Integer soLuong;

    @Column(name = "gia_ban", precision = 18, scale = 2)
    private BigDecimal giaBan;

    public ChiTietDonHang() {}

    public Long getMaDonHang() { return maDonHang; }
    public void setMaDonHang(Long maDonHang) { this.maDonHang = maDonHang; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
    public BigDecimal getGiaBan() { return giaBan; }
    public void setGiaBan(BigDecimal giaBan) { this.giaBan = giaBan; }
}
