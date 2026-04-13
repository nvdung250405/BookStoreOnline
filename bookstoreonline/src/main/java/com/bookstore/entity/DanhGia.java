package com.bookstore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "danh_gia")
public class DanhGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_dg")
    private Long maDg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_khachhang", nullable = false)
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isbn", nullable = false)
    private Sach sach;

    @Column(name = "diem_dg")
    private Integer diemDg;

    @Column(name = "nhan_xet", columnDefinition = "NVARCHAR(MAX)")
    private String nhanXet;

    @Column(name = "ngay_dg")
    private LocalDateTime ngayDg = LocalDateTime.now();

    public DanhGia() {}

    public Long getMaDg() { return maDg; }
    public void setMaDg(Long maDg) { this.maDg = maDg; }
    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }
    public Sach getSach() { return sach; }
    public void setSach(Sach sach) { this.sach = sach; }
    public Integer getDiemDg() { return diemDg; }
    public void setDiemDg(Integer diemDg) { this.diemDg = diemDg; }
    public String getNhanXet() { return nhanXet; }
    public void setNhanXet(String nhanXet) { this.nhanXet = nhanXet; }
    public LocalDateTime getNgayDg() { return ngayDg; }
    public void setNgayDg(LocalDateTime ngayDg) { this.ngayDg = ngayDg; }
}
