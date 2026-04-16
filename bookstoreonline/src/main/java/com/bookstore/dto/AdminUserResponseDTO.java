package com.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.bookstore.enums.AccountStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

public class AdminUserResponseDTO {
    @Schema(example = "stock_manager_01")
    private String username;

    @Schema(example = "STOREKEEPER")
    private String role;

    @Schema(example = "true")
    private AccountStatus status;

    @Schema(example = "2024-04-13T08:00:00")
    private LocalDateTime createdAt;

    @Schema(example = "WAREHOUSE")
    private String department;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;

    public AdminUserResponseDTO() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
