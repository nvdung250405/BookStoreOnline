package com.bookstore.dto;

public class LowStockAlertDTO {
    private String isbn;
    private String tenSach;
    private int soLuongTon;
    private int nguongBaoDong;

    public LowStockAlertDTO() {}

    public LowStockAlertDTO(String isbn, String tenSach, int soLuongTon, int nguongBaoDong) {
        this.isbn = isbn;
        this.tenSach = tenSach;
        this.soLuongTon = soLuongTon;
        this.nguongBaoDong = nguongBaoDong;
    }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTenSach() { return tenSach; }
    public void setTenSach(String tenSach) { this.tenSach = tenSach; }
    public int getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(int soLuongTon) { this.soLuongTon = soLuongTon; }
    public int getNguongBaoDong() { return nguongBaoDong; }
    public void setNguongBaoDong(int nguongBaoDong) { this.nguongBaoDong = nguongBaoDong; }
}