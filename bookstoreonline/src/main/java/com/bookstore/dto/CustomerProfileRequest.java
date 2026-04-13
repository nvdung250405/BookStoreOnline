package com.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public class CustomerProfileRequest {
    
    @NotBlank(message = "Họ tên không được để trống")
    @Schema(example = "Trần Thị Khách")
    private String hoTen;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(max = 10, message = "Số điện thoại không quá 10 ký tự")
    @Schema(example = "0901234567")
    private String sdt;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @Schema(example = "456 Đường Lê Lợi, Quận 1, TP.HCM")
    private String diaChiGiaoHang;

    public CustomerProfileRequest() {}

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public String getDiaChiGiaoHang() { return diaChiGiaoHang; }
    public void setDiaChiGiaoHang(String diaChiGiaoHang) { this.diaChiGiaoHang = diaChiGiaoHang; }
}
