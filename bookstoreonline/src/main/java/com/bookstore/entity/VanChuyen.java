package com.bookstore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "van_chuyen")
public class VanChuyen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_vanchuyen")
    private Long maVanChuyen;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_donhang", unique = true, nullable = false)
    private DonHang donHang;

    @Column(name = "don_vi_vc", length = 100)
    private String donViVanChuyen;

    @Column(name = "ma_van_don", length = 50)
    private String maVanDon;

    @Column(length = 50)
    private String trangThai;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat = LocalDateTime.now();

    public VanChuyen() {}

    public Long getMaVanChuyen() { return maVanChuyen; }
    public void setMaVanChuyen(Long maVanChuyen) { this.maVanChuyen = maVanChuyen; }
    public DonHang getDonHang() { return donHang; }
    public void setDonHang(DonHang donHang) { this.donHang = donHang; }
    public String getDonViVanChuyen() { return donViVanChuyen; }
    public void setDonViVanChuyen(String donViVanChuyen) { this.donViVanChuyen = donViVanChuyen; }
    public String getMaVanDon() { return maVanDon; }
    public void setMaVanDon(String maVanDon) { this.maVanDon = maVanDon; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public LocalDateTime getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(LocalDateTime ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }
}
