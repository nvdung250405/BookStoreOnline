package com.bookstore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sach_vat_ly")
@PrimaryKeyJoinColumn(name = "isbn")
public class SachVatLy extends Sach {
    @Column(name = "so_trang")
    private Integer soTrang;

    @Column(length = 50)
    private String kichThuoc;

    @Column(length = 20)
    private String khoiLuong;

    public SachVatLy() {}

    public Integer getSoTrang() { return soTrang; }
    public void setSoTrang(Integer soTrang) { this.soTrang = soTrang; }
    public String getKichThuoc() { return kichThuoc; }
    public void setKichThuoc(String kichThuoc) { this.kichThuoc = kichThuoc; }
    public String getKhoiLuong() { return khoiLuong; }
    public void setKhoiLuong(String khoiLuong) { this.khoiLuong = khoiLuong; }
}
