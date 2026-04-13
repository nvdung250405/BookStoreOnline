package com.bookstore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", nullable = false)
    private TaiKhoan taiKhoan;

    @Column(name = "hanh_dong", nullable = false, length = 255)
    private String hanhDong;

    @Column(name = "chi_tiet", columnDefinition = "NVARCHAR(MAX)")
    private String chiTiet;

    @Column(name = "thoi_diem")
    private LocalDateTime thoiDiem = LocalDateTime.now();

    public AuditLog() {}

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public void setTaiKhoan(TaiKhoan taiKhoan) { this.taiKhoan = taiKhoan; }
    public String getHanhDong() { return hanhDong; }
    public void setHanhDong(String hanhDong) { this.hanhDong = hanhDong; }
    public String getChiTiet() { return chiTiet; }
    public void setChiTiet(String chiTiet) { this.chiTiet = chiTiet; }
    public LocalDateTime getThoiDiem() { return thoiDiem; }
    public void setThoiDiem(LocalDateTime thoiDiem) { this.thoiDiem = thoiDiem; }
}
