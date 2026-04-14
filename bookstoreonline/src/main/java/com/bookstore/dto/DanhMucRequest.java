package com.bookstore.dto;

import jakarta.validation.constraints.NotBlank;

public class DanhMucRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    private String tenDanhMuc;

    private Integer maDanhMucCha;

    public String getTenDanhMuc() {
        return tenDanhMuc;
    }

    public void setTenDanhMuc(String tenDanhMuc) {
        this.tenDanhMuc = tenDanhMuc;
    }

    public Integer getMaDanhMucCha() {
        return maDanhMucCha;
    }

    public void setMaDanhMucCha(Integer maDanhMucCha) {
        this.maDanhMucCha = maDanhMucCha;
    }
}
