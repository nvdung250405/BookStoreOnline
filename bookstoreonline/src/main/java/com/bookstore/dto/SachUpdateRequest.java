package com.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Set;

public class SachUpdateRequest {

    @NotBlank(message = "Tên sách không được để trống")
    private String tenSach;

    @NotNull(message = "Giá niêm yết không được để trống")
    @PositiveOrZero(message = "Giá niêm yết phải lớn hơn hoặc bằng 0")
    private BigDecimal giaNiemYet;

    private Integer soTrang;

    @NotNull(message = "Mã danh mục không được để trống")
    private Integer maDanhMuc;

    @NotNull(message = "Mã nhà xuất bản không được để trống")
    private Integer maNxb;

    private String moTaNguNghia;

    private String anhBia;

    private Set<Integer> tacGiaIds;

    public String getTenSach() {
        return tenSach;
    }

    public void setTenSach(String tenSach) {
        this.tenSach = tenSach;
    }

    public BigDecimal getGiaNiemYet() {
        return giaNiemYet;
    }

    public void setGiaNiemYet(BigDecimal giaNiemYet) {
        this.giaNiemYet = giaNiemYet;
    }

    public Integer getSoTrang() {
        return soTrang;
    }

    public void setSoTrang(Integer soTrang) {
        this.soTrang = soTrang;
    }

    public Integer getMaDanhMuc() {
        return maDanhMuc;
    }

    public void setMaDanhMuc(Integer maDanhMuc) {
        this.maDanhMuc = maDanhMuc;
    }

    public Integer getMaNxb() {
        return maNxb;
    }

    public void setMaNxb(Integer maNxb) {
        this.maNxb = maNxb;
    }

    public String getMoTaNguNghia() {
        return moTaNguNghia;
    }

    public void setMoTaNguNghia(String moTaNguNghia) {
        this.moTaNguNghia = moTaNguNghia;
    }

    public String getAnhBia() {
        return anhBia;
    }

    public void setAnhBia(String anhBia) {
        this.anhBia = anhBia;
    }

    public Set<Integer> getTacGiaIds() {
        return tacGiaIds;
    }

    public void setTacGiaIds(Set<Integer> tacGiaIds) {
        this.tacGiaIds = tacGiaIds;
    }
}
