package com.bookstore.dto;

import java.util.List;

public class ImportRequestDto {
    private Integer maNcc;
    private Integer maNhanVien;
    private List<ChiTietImportRequest> chiTietList;

    public ImportRequestDto() {}

    public ImportRequestDto(Integer maNcc, Integer maNhanVien, List<ChiTietImportRequest> chiTietList) {
        this.maNcc = maNcc;
        this.maNhanVien = maNhanVien;
        this.chiTietList = chiTietList;
    }

    public Integer getMaNcc() { return maNcc; }
    public void setMaNcc(Integer maNcc) { this.maNcc = maNcc; }
    public Integer getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(Integer maNhanVien) { this.maNhanVien = maNhanVien; }
    public List<ChiTietImportRequest> getChiTietList() { return chiTietList; }
    public void setChiTietList(List<ChiTietImportRequest> chiTietList) { this.chiTietList = chiTietList; }
}