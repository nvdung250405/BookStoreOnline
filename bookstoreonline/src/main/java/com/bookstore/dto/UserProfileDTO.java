package com.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileDTO {
    @Schema(example = "anhnv")
    private String username;

    @Schema(example = "Nguyễn Văn Anh")
    private String hoTen;

    @Schema(example = "0987654321")
    private String sdt;

    @Schema(example = "STAFF")
    private String role;
    
    // KhachHang fields
    @Schema(example = "Số 123, Đường ABC, Quận XYZ, TP.HCM")
    private String diaChiGiaoHang;

    @Schema(example = "150")
    private Integer diemTichLuy;
    
    // NhanVien fields
    @Schema(example = "BAN_HANG")
    private String boPhan;

    public UserProfileDTO() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getDiaChiGiaoHang() { return diaChiGiaoHang; }
    public void setDiaChiGiaoHang(String diaChiGiaoHang) { this.diaChiGiaoHang = diaChiGiaoHang; }
    public Integer getDiemTichLuy() { return diemTichLuy; }
    public void setDiemTichLuy(Integer diemTichLuy) { this.diemTichLuy = diemTichLuy; }
    public String getBoPhan() { return boPhan; }
    public void setBoPhan(String boPhan) { this.boPhan = boPhan; }
}
