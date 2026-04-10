package com.bookstore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "phieu_nhap")
public class PhieuNhap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_phieunhap")
    private Long maPhieuNhap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_ncc")
    private NhaCungCap nhaCungCap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_kho")
    private KhoHang khoHang;

    @Column(name = "ngay_nhap")
    private LocalDateTime ngayNhap = LocalDateTime.now();

    @Column(name = "nguoi_nhap")
    private String nguoiNhap;

    public PhieuNhap() {}

    public Long getMaPhieuNhap() { return maPhieuNhap; }
    public void setMaPhieuNhap(Long maPhieuNhap) { this.maPhieuNhap = maPhieuNhap; }
    public NhaCungCap getNhaCungCap() { return nhaCungCap; }
    public void setNhaCungCap(NhaCungCap nhaCungCap) { this.nhaCungCap = nhaCungCap; }
    public KhoHang getKhoHang() { return khoHang; }
    public void setKhoHang(KhoHang khoHang) { this.khoHang = khoHang; }
    public LocalDateTime getNgayNhap() { return ngayNhap; }
    public void setNgayNhap(LocalDateTime ngayNhap) { this.ngayNhap = ngayNhap; }
    public String getNguoiNhap() { return nguoiNhap; }
    public void setNguoiNhap(String nguoiNhap) { this.nguoiNhap = nguoiNhap; }
}
