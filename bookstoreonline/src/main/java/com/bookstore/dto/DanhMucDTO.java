package com.bookstore.dto;

import java.util.List;

public class DanhMucDTO {
    private Integer maDanhMuc;
    private String tenDanhMuc;
    private Integer maDanhMucCha;
    private List<DanhMucDTO> danhMucCon;

    public DanhMucDTO() {}

    public DanhMucDTO(Integer maDanhMuc, String tenDanhMuc, Integer maDanhMucCha, List<DanhMucDTO> danhMucCon) {
        this.maDanhMuc = maDanhMuc;
        this.tenDanhMuc = tenDanhMuc;
        this.maDanhMucCha = maDanhMucCha;
        this.danhMucCon = danhMucCon;
    }

    public Integer getMaDanhMuc() { return maDanhMuc; }
    public void setMaDanhMuc(Integer maDanhMuc) { this.maDanhMuc = maDanhMuc; }
    public String getTenDanhMuc() { return tenDanhMuc; }
    public void setTenDanhMuc(String tenDanhMuc) { this.tenDanhMuc = tenDanhMuc; }
    public Integer getMaDanhMucCha() { return maDanhMucCha; }
    public void setMaDanhMucCha(Integer maDanhMucCha) { this.maDanhMucCha = maDanhMucCha; }
    public List<DanhMucDTO> getDanhMucCon() { return danhMucCon; }
    public void setDanhMucCon(List<DanhMucDTO> danhMucCon) { this.danhMucCon = danhMucCon; }
}
