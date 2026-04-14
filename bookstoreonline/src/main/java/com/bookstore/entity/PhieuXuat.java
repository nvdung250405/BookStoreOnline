package com.bookstore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "phieu_xuat")
public class PhieuXuat {
    @Id
    @Column(name = "ma_phieuxuat")
    private String maPhieuXuat;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_donhang", unique = true, nullable = false)
    private DonHang donHang;

    @Column(name = "ngay_xuat")
    private LocalDateTime ngayXuat = LocalDateTime.now();

    @Column(name = "nguoi_xuat")
    private String nguoiXuat;

    public PhieuXuat() {}

    public String getMaPhieuXuat() { return maPhieuXuat; }
    public void setMaPhieuXuat(String maPhieuXuat) { this.maPhieuXuat = maPhieuXuat; }
    public DonHang getDonHang() { return donHang; }
    public void setDonHang(DonHang donHang) { this.donHang = donHang; }
    public LocalDateTime getNgayXuat() { return ngayXuat; }
    public void setNgayXuat(LocalDateTime ngayXuat) { this.ngayXuat = ngayXuat; }
    public String getNguoiXuat() { return nguoiXuat; }
    public void setNguoiXuat(String nguoiXuat) { this.nguoiXuat = nguoiXuat; }
}
