package com.bookstore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher")
public class Voucher {
    @Id
    @Column(length = 50)
    private String code;

    @Column(name = "giam_gia", precision = 18, scale = 2)
    private BigDecimal giamGia;

    @Column(name = "giatri_toithieu", precision = 18, scale = 2)
    private BigDecimal giaTriToiThieu;

    @Column(name = "ngay_het_han")
    private LocalDateTime ngayHetHan;

    @Column(name = "so_luong")
    private Integer soLuong;

    public Voucher() {}

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public BigDecimal getGiamGia() { return giamGia; }
    public void setGiamGia(BigDecimal giamGia) { this.giamGia = giamGia; }
    public BigDecimal getGiaTriToiThieu() { return giaTriToiThieu; }
    public void setGiaTriToiThieu(BigDecimal giaTriToiThieu) { this.giaTriToiThieu = giaTriToiThieu; }
    public LocalDateTime getNgayHetHan() { return ngayHetHan; }
    public void setNgayHetHan(LocalDateTime ngayHetHan) { this.ngayHetHan = ngayHetHan; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
}
