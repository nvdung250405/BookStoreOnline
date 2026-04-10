package com.bookstore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "nxb")
public class Nxb {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_nxb")
    private Integer maNxb;

    @Column(name = "ten_nxb", nullable = false, length = 100)
    private String tenNxb;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String thongTin;

    public Nxb() {}

    public Integer getMaNxb() { return maNxb; }
    public void setMaNxb(Integer maNxb) { this.maNxb = maNxb; }
    public String getTenNxb() { return tenNxb; }
    public void setTenNxb(String tenNxb) { this.tenNxb = tenNxb; }
    public String getThongTin() { return thongTin; }
    public void setThongTin(String thongTin) { this.thongTin = thongTin; }
}
