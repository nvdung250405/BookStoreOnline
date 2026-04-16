package com.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountProfileDTO {
    @Schema(example = "anhnv")
    private String username;

    @Schema(example = "Nguyễn Văn Anh")
    private String fullName;

    @Schema(example = "0987654321")
    private String phone;

    @Schema(example = "STAFF")
    private String role;
    
    // KhachHang fields
    @Schema(example = "Số 123, Đường ABC, Quận XYZ, TP.HCM")
    private String shippingAddress;

    @Schema(example = "150")
    private Integer loyaltyPoints;
    
    // NhanVien fields
    @Schema(example = "BAN_HANG")
    private String department;

    @Schema(example = "true")
    private Boolean isActive;

    @Schema(example = "2024-03-20T10:00:00")
    private LocalDateTime createdAt;

    public AccountProfileDTO() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public Integer getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(Integer loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
