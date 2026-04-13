package com.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public class AdminUserResponseDTO {
    @Schema(example = "nv_kho_01")
    private String username;

    @Schema(example = "STOREKEEPER")
    private String role;

    @Schema(example = "true")
    private Boolean trangThai;

    @Schema(example = "2024-04-13T08:00:00")
    private LocalDateTime ngayTao;

    @Schema(example = "KHO")
    private String boPhan;

    public AdminUserResponseDTO() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime setNgayTao) { this.ngayTao = setNgayTao; }
    public String getBoPhan() { return boPhan; }
    public void setBoPhan(String boPhan) { this.boPhan = boPhan; }
}
