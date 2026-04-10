package com.bookstore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "thanh_toan")
public class ThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_thanhtoan")
    private Long maThanhToan;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_donhang", unique = true, nullable = false)
    private DonHang donHang;

    @Column(name = "ngay_tt")
    private LocalDateTime ngayThanhToan = LocalDateTime.now();

    @Column(name = "so_tien", precision = 18, scale = 2)
    private BigDecimal soTien;

    @Column(name = "phuong_thuc", length = 50)
    private String phuongThuc;

    @Column(name = "trang_thai", length = 50)
    private String trangThai;

    public ThanhToan() {}

    public Long getMaThanhToan() { return maThanhToan; }
    public void setMaThanhToan(Long maThanhToan) { this.maThanhToan = maThanhToan; }
    public DonHang getDonHang() { return donHang; }
    public void setDonHang(DonHang donHang) { this.donHang = donHang; }
    public LocalDateTime getNgayThanhToan() { return ngayThanhToan; }
    public void setNgayThanhToan(LocalDateTime ngayThanhToan) { this.ngayThanhToan = ngayThanhToan; }
    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }
    public String getPhuongThuc() { return phuongThuc; }
    public void setPhuongThuc(String phuongThuc) { this.phuongThuc = phuongThuc; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
