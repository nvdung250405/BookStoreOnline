package com.bookstore.dto;

import java.math.BigDecimal;

public class ChiTietImportRequest {
    private String isbn;
    private Integer soLuong;
    private BigDecimal donGiaNhap;

    public ChiTietImportRequest() {}

    public ChiTietImportRequest(String isbn, Integer soLuong, BigDecimal donGiaNhap) {
        this.isbn = isbn;
        this.soLuong = soLuong;
        this.donGiaNhap = donGiaNhap;
    }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
    public BigDecimal getDonGiaNhap() { return donGiaNhap; }
    public void setDonGiaNhap(BigDecimal donGiaNhap) { this.donGiaNhap = donGiaNhap; }
}