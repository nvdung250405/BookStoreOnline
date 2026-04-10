package com.bookstore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "nhan_vien")
public class NhanVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_nhanvien")
    private Integer maNhanVien;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", unique = true, nullable = false)
    private TaiKhoan taiKhoan;

    @Column(name = "ho_ten", nullable = false, length = 100)
    private String hoTen;

    @Column(length = 15)
    private String sdt;

    @Column(name = "bo_phan", nullable = false)
    private String boPhan;

    public NhanVien() {}

    public Integer getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(Integer maNhanVien) { this.maNhanVien = maNhanVien; }
    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public void setTaiKhoan(TaiKhoan taiKhoan) { this.taiKhoan = taiKhoan; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public String getBoPhan() { return boPhan; }
    public void setBoPhan(String boPhan) { this.boPhan = boPhan; }
}
