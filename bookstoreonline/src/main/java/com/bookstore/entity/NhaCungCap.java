package com.bookstore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "nha_cung_cap")
public class NhaCungCap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_ncc")
    private Integer maNcc;

    @Column(name = "ten_ncc", nullable = false, length = 100)
    private String tenNcc;

    @Column(length = 15)
    private String sdt;

    @Column(length = 200)
    private String email;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String diaChi;

    public NhaCungCap() {}

    public Integer getMaNcc() { return maNcc; }
    public void setMaNcc(Integer maNcc) { this.maNcc = maNcc; }
    public String getTenNcc() { return tenNcc; }
    public void setTenNcc(String tenNcc) { this.tenNcc = tenNcc; }
    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
}
