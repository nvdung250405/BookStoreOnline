package com.bookstore.dto;

import java.math.BigDecimal;

public class ChiTietDonHangDTO {
    private String isbn;
    private String tenSach;
    private Integer soLuong;
    private BigDecimal giaBanChot;

    public ChiTietDonHangDTO() {}

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTenSach() { return tenSach; }
    public void setTenSach(String tenSach) { this.tenSach = tenSach; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
    public BigDecimal getGiaBanChot() { return giaBanChot; }
    public void setGiaBanChot(BigDecimal giaBanChot) { this.giaBanChot = giaBanChot; }
}
