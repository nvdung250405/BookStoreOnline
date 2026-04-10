package com.bookstore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "kho_hang")
public class KhoHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_kho")
    private Integer maKho;

    @Column(name = "ten_kho", nullable = false, length = 100)
    private String tenKho;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String viTri;

    public KhoHang() {}

    public Integer getMaKho() { return maKho; }
    public void setMaKho(Integer maKho) { this.maKho = maKho; }
    public String getTenKho() { return tenKho; }
    public void setTenKho(String tenKho) { this.tenKho = tenKho; }
    public String getViTri() { return viTri; }
    public void setViTri(String viTri) { this.viTri = viTri; }
}
