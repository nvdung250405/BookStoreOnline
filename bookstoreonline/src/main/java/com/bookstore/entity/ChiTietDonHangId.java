package com.bookstore.entity;

import java.io.Serializable;
import java.util.Objects;

public class ChiTietDonHangId implements Serializable {
    private Long maDonHang;
    private String isbn;

    public ChiTietDonHangId() {}

    public ChiTietDonHangId(Long maDonHang, String isbn) {
        this.maDonHang = maDonHang;
        this.isbn = isbn;
    }

    public Long getMaDonHang() { return maDonHang; }
    public void setMaDonHang(Long maDonHang) { this.maDonHang = maDonHang; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietDonHangId that = (ChiTietDonHangId) o;
        return Objects.equals(maDonHang, that.maDonHang) && Objects.equals(isbn, that.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maDonHang, isbn);
    }
}
