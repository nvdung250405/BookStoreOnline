package com.bookstore.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "danh_muc")
public class DanhMuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_danhmuc")
    private Integer maDanhMuc;

    @Column(name = "ten_danhmuc", nullable = false, length = 100)
    private String tenDanhMuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "danh_muc_cha_id")
    private DanhMuc danhMucCha;

    @OneToMany(mappedBy = "danhMucCha", cascade = CascadeType.ALL)
    private List<DanhMuc> danhMucCon;

    public DanhMuc() {}

    public Integer getMaDanhMuc() { return maDanhMuc; }
    public void setMaDanhMuc(Integer maDanhMuc) { this.maDanhMuc = maDanhMuc; }
    public String getTenDanhMuc() { return tenDanhMuc; }
    public void setTenDanhMuc(String tenDanhMuc) { this.tenDanhMuc = tenDanhMuc; }
    public DanhMuc getDanhMucCha() { return danhMucCha; }
    public void setDanhMucCha(DanhMuc danhMucCha) { this.danhMucCha = danhMucCha; }
    public List<DanhMuc> getDanhMucCon() { return danhMucCon; }
    public void setDanhMucCon(List<DanhMuc> danhMucCon) { this.danhMucCon = danhMucCon; }
}
