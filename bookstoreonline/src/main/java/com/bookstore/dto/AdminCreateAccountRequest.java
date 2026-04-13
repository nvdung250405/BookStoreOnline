package com.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AdminCreateAccountRequest {
    
    @NotBlank(message = "Username không được để trống")
    @Size(min = 4, max = 50, message = "Username từ 4-50 ký tự")
    @Schema(example = "staff_sales_01")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
    @Schema(example = "123456")
    private String password;

    @NotBlank(message = "Vai trò không được để trống")
    @Pattern(regexp = "STAFF|ADMIN|STOREKEEPER", message = "Vai trò phải là STAFF, ADMIN hoặc STOREKEEPER")
    @Schema(example = "STAFF")
    private String role;

    public AdminCreateAccountRequest() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
