package com.bookstore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tac_gia")
public class TacGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_tacgia")
    private Integer maTacGia;

    @Column(name = "ten_tacgia", nullable = false, length = 100)
    private String tenTacGia;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String tieuSu;

    public TacGia() {}

    public Integer getMaTacGia() { return maTacGia; }
    public void setMaTacGia(Integer maTacGia) { this.maTacGia = maTacGia; }
    public String getTenTacGia() { return tenTacGia; }
    public void setTenTacGia(String tenTacGia) { this.tenTacGia = tenTacGia; }
    public String getTieuSu() { return tieuSu; }
    public void setTieuSu(String tieuSu) { this.tieuSu = tieuSu; }
}
