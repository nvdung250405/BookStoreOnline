package com.bookstore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sach_dien_tu")
@PrimaryKeyJoinColumn(name = "isbn")
public class SachDienTu extends Sach {
    @Column(name = "dung_luong")
    private String dungLuong;

    @Column(name = "dinh_dang")
    private String dinhDang;

    @Column(name = "duong_dan")
    private String duongDan;

    public SachDienTu() {}

    public String getDungLuong() { return dungLuong; }
    public void setDungLuong(String dungLuong) { this.dungLuong = dungLuong; }
    public String getDinhDang() { return dinhDang; }
    public void setDinhDang(String dinhDang) { this.dinhDang = dinhDang; }
    public String getDuongDan() { return duongDan; }
    public void setDuongDan(String duongDan) { this.duongDan = duongDan; }
}
