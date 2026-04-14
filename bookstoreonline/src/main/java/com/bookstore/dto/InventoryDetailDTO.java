package com.bookstore.dto;

public class InventoryDetailDTO {
    private String isbn;
    private String tenSach;
    private int soLuongTon;
    private String viTriKe;

    public InventoryDetailDTO() {}

    public InventoryDetailDTO(String isbn, String tenSach, int soLuongTon, String viTriKe) {
        this.isbn = isbn;
        this.tenSach = tenSach;
        this.soLuongTon = soLuongTon;
        this.viTriKe = viTriKe;
    }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTenSach() { return tenSach; }
    public void setTenSach(String tenSach) { this.tenSach = tenSach; }
    public int getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(int soLuongTon) { this.soLuongTon = soLuongTon; }
    public String getViTriKe() { return viTriKe; }
    public void setViTriKe(String viTriKe) { this.viTriKe = viTriKe; }
}