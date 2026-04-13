package com.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.bookstore.entity.AuditLog;
import java.time.format.DateTimeFormatter;

public class AuditLogDTO {
    @Schema(example = "1")
    private Long id;

    @Schema(example = "admin")
    private String username;

    @Schema(example = "UPDATE_ROLE")
    private String hanhDong;

    @Schema(example = "Thay đổi quyền user 'nv01' thành STOREKEEPER")
    private String chiTiet;

    @Schema(example = "13/04/2024 08:30:45")
    private String thoiDiem;

    public AuditLogDTO(AuditLog log) {
        this.id = log.getLogId();
        if (log.getTaiKhoan() != null) {
            this.username = log.getTaiKhoan().getUsername();
        }
        this.hanhDong = log.getHanhDong();
        this.chiTiet = log.getChiTiet();
        if (log.getThoiDiem() != null) {
            this.thoiDiem = log.getThoiDiem().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        }
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getHanhDong() { return hanhDong; }
    public String getChiTiet() { return chiTiet; }
    public String getThoiDiem() { return thoiDiem; }
}
