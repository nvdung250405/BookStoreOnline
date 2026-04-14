package com.bookstore.dto;

public class NxbDTO {
    private Integer maNxb;
    private String tenNxb;

    public NxbDTO() {}

    public NxbDTO(Integer maNxb, String tenNxb) {
        this.maNxb = maNxb;
        this.tenNxb = tenNxb;
    }

    public Integer getMaNxb() { return maNxb; }
    public void setMaNxb(Integer maNxb) { this.maNxb = maNxb; }
    public String getTenNxb() { return tenNxb; }
    public void setTenNxb(String tenNxb) { this.tenNxb = tenNxb; }
}
