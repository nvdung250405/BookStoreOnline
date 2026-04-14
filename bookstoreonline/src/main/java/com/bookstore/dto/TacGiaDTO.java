package com.bookstore.dto;

public class TacGiaDTO {
    private Integer maTacGia;
    private String tenTacGia;
    private String tieuSu;

    public TacGiaDTO() {}

    public TacGiaDTO(Integer maTacGia, String tenTacGia, String tieuSu) {
        this.maTacGia = maTacGia;
        this.tenTacGia = tenTacGia;
        this.tieuSu = tieuSu;
    }

    public Integer getMaTacGia() { return maTacGia; }
    public void setMaTacGia(Integer maTacGia) { this.maTacGia = maTacGia; }
    public String getTenTacGia() { return tenTacGia; }
    public void setTenTacGia(String tenTacGia) { this.tenTacGia = tenTacGia; }
    public String getTieuSu() { return tieuSu; }
    public void setTieuSu(String tieuSu) { this.tieuSu = tieuSu; }
}
