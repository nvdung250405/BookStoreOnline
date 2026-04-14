package com.bookstore.dto;

public class NhaCungCapDto {
    private Integer maNcc;
    private String tenNcc;
    private String thongTinLienHe;

    public NhaCungCapDto() {}

    public NhaCungCapDto(Integer maNcc, String tenNcc, String thongTinLienHe) {
        this.maNcc = maNcc;
        this.tenNcc = tenNcc;
        this.thongTinLienHe = thongTinLienHe;
    }

    public Integer getMaNcc() { return maNcc; }
    public void setMaNcc(Integer maNcc) { this.maNcc = maNcc; }
    public String getTenNcc() { return tenNcc; }
    public void setTenNcc(String tenNcc) { this.tenNcc = tenNcc; }
    public String getThongTinLienHe() { return thongTinLienHe; }
    public void setThongTinLienHe(String thongTinLienHe) { this.thongTinLienHe = thongTinLienHe; }
}