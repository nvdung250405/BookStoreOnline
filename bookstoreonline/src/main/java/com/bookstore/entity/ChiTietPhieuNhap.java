package com.bookstore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "chi_tiet_phieu_nhap")
@IdClass(com.bookstore.entity.ChiTietPhieuNhapId.class)
public class ChiTietPhieuNhap {
    @Id
    private Long maPhieuNhap;

    @Id
    private String isbn;

    @Column(nullable = false)
    private Integer soLuong;

    @Column(name = "gia_nhap", precision = 18, scale = 2)
    private BigDecimal giaNhap;

    public ChiTietPhieuNhap() {}

    public Long getMaPhieuNhap() { return maPhieuNhap; }
    public void setMaPhieuNhap(Long maPhieuNhap) { this.maPhieuNhap = maPhieuNhap; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
    public BigDecimal getGiaNhap() { return giaNhap; }
    public void setGiaNhap(BigDecimal giaNhap) { this.giaNhap = giaNhap; }
}
