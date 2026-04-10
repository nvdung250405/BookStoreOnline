package com.bookstore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "sach")
@Inheritance(strategy = InheritanceType.JOINED)
public class Sach {
    @Id
    @Column(length = 20)
    private String isbn;

    @Column(nullable = false, length = 200)
    private String tieuDe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_danhmuc")
    private DanhMuc danhMuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_tacgia")
    private TacGia tacGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nxb")
    private Nxb nxb;

    @Column(name = "gia_ban", precision = 18, scale = 2)
    private BigDecimal giaBan;

    @Column(name = "nam_xuat_ban")
    private Integer namXuatBan;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "hinh_anh")
    private String hinhAnh;

    public Sach() {}

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }
    public DanhMuc getDanhMuc() { return danhMuc; }
    public void setDanhMuc(DanhMuc danhMuc) { this.danhMuc = danhMuc; }
    public TacGia getTacGia() { return tacGia; }
    public void setTacGia(TacGia tacGia) { this.tacGia = tacGia; }
    public Nxb getNxb() { return nxb; }
    public void setNxb(Nxb nxb) { this.nxb = nxb; }
    public BigDecimal getGiaBan() { return giaBan; }
    public void setGiaBan(BigDecimal giaBan) { this.giaBan = giaBan; }
    public Integer getNamXuatBan() { return namXuatBan; }
    public void setNamXuatBan(Integer namXuatBan) { this.namXuatBan = namXuatBan; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }
}
