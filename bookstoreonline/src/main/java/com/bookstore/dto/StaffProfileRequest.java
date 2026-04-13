package com.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class StaffProfileRequest {
    
    @NotBlank(message = "Họ tên không được để trống")
    @Schema(example = "Nguyễn Văn Nhân Viên")
    private String hoTen;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(max = 10, message = "Số điện thoại không quá 10 ký tự")
    @Schema(example = "0945667788")
    private String sdt;

    public StaffProfileRequest() {}

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }
}
