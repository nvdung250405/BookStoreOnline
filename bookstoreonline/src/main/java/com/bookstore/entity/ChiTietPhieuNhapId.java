package com.bookstore.entity;

import java.io.Serializable;
import java.util.Objects;

public class ChiTietPhieuNhapId implements Serializable {
    private Long maPhieuNhap;
    private String isbn;

    public ChiTietPhieuNhapId() {}

    public ChiTietPhieuNhapId(Long maPhieuNhap, String isbn) {
        this.maPhieuNhap = maPhieuNhap;
        this.isbn = isbn;
    }

    public Long getMaPhieuNhap() { return maPhieuNhap; }
    public void setMaPhieuNhap(Long maPhieuNhap) { this.maPhieuNhap = maPhieuNhap; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietPhieuNhapId that = (ChiTietPhieuNhapId) o;
        return Objects.equals(maPhieuNhap, that.maPhieuNhap) && Objects.equals(isbn, that.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maPhieuNhap, isbn);
    }
}
