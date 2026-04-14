package com.bookstore.dto;

import com.bookstore.entity.Sach;
import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

public class SachDTO {
    private String isbn;
    private String tenSach;
    private BigDecimal giaNiemYet;
    private Integer soTrang;
    private Integer maDanhMuc;
    private String tenDanhMuc;
    private Integer maNxb;
    private String tenNxb;
    private String anhBia;
    private Set<String> tenTacGia;
    private String moTaNguNghia;

    public SachDTO() {}

    public static SachDTO fromEntity(Sach sach) {
        SachDTO dto = new SachDTO();
        dto.setIsbn(sach.getIsbn());
        dto.setTenSach(sach.getTenSach());
        dto.setGiaNiemYet(sach.getGiaNiemYet());
        dto.setSoTrang(sach.getSoTrang());
        dto.setMoTaNguNghia(sach.getMoTaNguNghia());
        if (sach.getDanhMuc() != null) {
            dto.setMaDanhMuc(sach.getDanhMuc().getMaDanhMuc());
            dto.setTenDanhMuc(sach.getDanhMuc().getTenDanhMuc());
        }
        if (sach.getNxb() != null) {
            dto.setMaNxb(sach.getNxb().getMaNxb());
            dto.setTenNxb(sach.getNxb().getTenNxb());
        }
        dto.setAnhBia(sach.getAnhBia());
        if (sach.getDanhSachTacGia() != null) {
            dto.setTenTacGia(sach.getDanhSachTacGia().stream()
                .map(tg -> tg.getTenTacGia())
                .collect(Collectors.toSet()));
        }
        return dto;
    }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTenSach() { return tenSach; }
    public void setTenSach(String tenSach) { this.tenSach = tenSach; }
    public BigDecimal getGiaNiemYet() { return giaNiemYet; }
    public void setGiaNiemYet(BigDecimal giaNiemYet) { this.giaNiemYet = giaNiemYet; }
    public Integer getSoTrang() { return soTrang; }
    public void setSoTrang(Integer soTrang) { this.soTrang = soTrang; }
    public Integer getMaDanhMuc() { return maDanhMuc; }
    public void setMaDanhMuc(Integer maDanhMuc) { this.maDanhMuc = maDanhMuc; }
    public String getTenDanhMuc() { return tenDanhMuc; }
    public void setTenDanhMuc(String tenDanhMuc) { this.tenDanhMuc = tenDanhMuc; }
    public Integer getMaNxb() { return maNxb; }
    public void setMaNxb(Integer maNxb) { this.maNxb = maNxb; }
    public String getTenNxb() { return tenNxb; }
    public void setTenNxb(String tenNxb) { this.tenNxb = tenNxb; }
    public String getAnhBia() { return anhBia; }
    public void setAnhBia(String anhBia) { this.anhBia = anhBia; }
    public Set<String> getTenTacGia() { return tenTacGia; }
    public void setTenTacGia(Set<String> tenTacGia) { this.tenTacGia = tenTacGia; }
    public String getMoTaNguNghia() { return moTaNguNghia; }
    public void setMoTaNguNghia(String moTaNguNghia) { this.moTaNguNghia = moTaNguNghia; }
}
