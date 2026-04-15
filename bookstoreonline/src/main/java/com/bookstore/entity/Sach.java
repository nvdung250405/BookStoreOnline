package com.bookstore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "sach")
@Inheritance(strategy = InheritanceType.JOINED)
public class Sach {
    @Id
    @Column(length = 13)
    private String isbn;

    @Column(name = "ten_sach", nullable = false, length = 255)
    private String tenSach;

    @Column(name = "gia_niem_yet", precision = 10, scale = 2, nullable = false)
    private BigDecimal giaNiemYet;

    @Column(name = "so_trang")
    private Integer soTrang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_danhmuc")
    private DanhMuc danhMuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nxb")
    private Nxb nxb;

    @Column(name = "mo_ta_ngu_nghia", columnDefinition = "NVARCHAR(MAX)")
    private String moTaNguNghia;

    @Column(name = "anh_bia", length = 255)
    private String anhBia;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "sach_tac_gia",
        joinColumns = @JoinColumn(name = "isbn"),
        inverseJoinColumns = @JoinColumn(name = "ma_tacgia")
    )
    private Set<TacGia> danhSachTacGia;

    @Column(name = "da_xoa")
    private Boolean daXoa = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Sach() {}

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTenSach() { return tenSach; }
    public void setTenSach(String tenSach) { this.tenSach = tenSach; }
    public BigDecimal getGiaNiemYet() { return giaNiemYet; }
    public void setGiaNiemYet(BigDecimal giaNiemYet) { this.giaNiemYet = giaNiemYet; }
    public Integer getSoTrang() { return soTrang; }
    public void setSoTrang(Integer soTrang) { this.soTrang = soTrang; }
    public DanhMuc getDanhMuc() { return danhMuc; }
    public void setDanhMuc(DanhMuc danhMuc) { this.danhMuc = danhMuc; }
    public Nxb getNxb() { return nxb; }
    public void setNxb(Nxb nxb) { this.nxb = nxb; }
    public String getMoTaNguNghia() { return moTaNguNghia; }
    public void setMoTaNguNghia(String moTaNguNghia) { this.moTaNguNghia = moTaNguNghia; }
    public String getAnhBia() { return anhBia; }
    public void setAnhBia(String anhBia) { this.anhBia = anhBia; }
    public Set<TacGia> getDanhSachTacGia() { return danhSachTacGia; }
    public void setDanhSachTacGia(Set<TacGia> danhSachTacGia) { this.danhSachTacGia = danhSachTacGia; }
    public Boolean getDaXoa() { return daXoa; }
    public void setDaXoa(Boolean daXoa) { this.daXoa = daXoa; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
