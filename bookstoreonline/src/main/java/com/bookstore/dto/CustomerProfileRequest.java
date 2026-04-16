package com.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public class CustomerProfileRequest {
    
    @NotBlank(message = "Họ tên không được để trống")
    @Schema(example = "Trần Thị Khách")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(max = 15, message = "Số điện thoại không quá 15 ký tự")
    @Schema(example = "0901234567")
    private String phone;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @Schema(example = "456 Đường Lê Lợi, Quận 1, TP.HCM")
    private String shippingAddress;

    public CustomerProfileRequest() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
}
