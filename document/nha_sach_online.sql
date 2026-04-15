USE [master]
GO

IF DB_ID('nha_sach_online') IS NOT NULL 
    DROP DATABASE nha_sach_online;
GO

CREATE DATABASE nha_sach_online;
GO

USE nha_sach_online;
GO

/* =============================================================================
   1. LOOKUP TABLES (DANH MỤC TRẠNG THÁI)
============================================================================= */
CREATE TABLE trang_thai_don_hang (ma_trang_thai NVARCHAR(50) PRIMARY KEY);
INSERT INTO trang_thai_don_hang VALUES 
(N'MOI'), (N'DA_XAC_NHAN'), (N'CHO_LAY_HANG'), (N'DANG_GIAO'), (N'HOAN_TAT'), (N'DA_HUY');

CREATE TABLE trang_thai_thanh_toan (ma_trang_thai NVARCHAR(50) PRIMARY KEY);
INSERT INTO trang_thai_thanh_toan VALUES 
(N'PENDING'), (N'SUCCESS'), (N'FAILED');

CREATE TABLE phuong_thuc_thanh_toan (ma_pt NVARCHAR(50) PRIMARY KEY);
INSERT INTO phuong_thuc_thanh_toan VALUES 
(N'COD'), (N'MOMO'), (N'VNPAY');

CREATE TABLE trang_thai_ho_tro (ma_trang_thai NVARCHAR(50) PRIMARY KEY);
INSERT INTO trang_thai_ho_tro VALUES 
(N'OPEN'), (N'PROCESSING'), (N'CLOSED');

/* =============================================================================
   2. TÀI KHOẢN (PHÂN QUYỀN RBAC)
============================================================================= */
CREATE TABLE tai_khoan (
    username NVARCHAR(50) PRIMARY KEY,
    password NVARCHAR(255) NOT NULL,
    role NVARCHAR(50) NOT NULL 
        CHECK (role IN (N'ADMIN', N'STAFF', N'STOREKEEPER', N'CUSTOMER')),
    trang_thai BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME NULL,
    deleted_at DATETIME NULL,
    CONSTRAINT CK_password CHECK (LEN(password) >= 6)
);

/* =============================================================================
   3. NGƯỜI DÙNG (THÔNG TIN ĐỊNH DANH)
============================================================================= */
CREATE TABLE khach_hang (
    ma_khachhang BIGINT IDENTITY PRIMARY KEY,
    username NVARCHAR(50) UNIQUE,
    ho_ten NVARCHAR(100) NOT NULL,
    sdt NVARCHAR(15),
    diem_tich_luy INT DEFAULT 0 CHECK (diem_tich_luy >= 0),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME NULL,
    deleted_at DATETIME NULL,
    FOREIGN KEY (username) REFERENCES tai_khoan(username) ON DELETE CASCADE,
    CONSTRAINT UQ_kh_sdt UNIQUE(sdt) -- Đảm bảo SĐT duy nhất
);

CREATE TABLE nhan_vien (
    ma_nhanvien INT IDENTITY PRIMARY KEY,
    username NVARCHAR(50) UNIQUE,
    ho_ten NVARCHAR(100) NOT NULL,
    sdt NVARCHAR(15),
    bo_phan NVARCHAR(50) CHECK (bo_phan IN (N'QUAN_LY', N'BAN_HANG', N'KHO')),
    created_at DATETIME DEFAULT GETDATE(),
    deleted_at DATETIME NULL,
    FOREIGN KEY (username) REFERENCES tai_khoan(username) ON DELETE CASCADE,
    CONSTRAINT UQ_nv_sdt UNIQUE(sdt) -- Đảm bảo SĐT duy nhất
);

/* =============================================================================
   4. DANH MỤC & SÁCH (HỖ TRỢ AI SEARCH & LISKOV OOP)
============================================================================= */
CREATE TABLE danh_muc (
    ma_danhmuc INT IDENTITY PRIMARY KEY,
    ten_danhmuc NVARCHAR(100) NOT NULL,
    danh_muc_cha_id INT NULL,
    FOREIGN KEY (danh_muc_cha_id) REFERENCES danh_muc(ma_danhmuc)
);

CREATE TABLE nxb (
    ma_nxb INT IDENTITY PRIMARY KEY,
    ten_nxb NVARCHAR(150) NOT NULL
);

CREATE TABLE sach (
    isbn NVARCHAR(13) PRIMARY KEY,
    ten_sach NVARCHAR(255) NOT NULL,
    gia_niem_yet DECIMAL(10,2) NOT NULL CHECK (gia_niem_yet >= 0),
    so_trang INT,
    ma_danhmuc INT,
    ma_nxb INT,
    mo_ta_ngu_nghia NVARCHAR(MAX),
    anh_bia NVARCHAR(255),
    da_xoa BIT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME NULL,
    deleted_at DATETIME NULL,
    FOREIGN KEY (ma_danhmuc) REFERENCES danh_muc(ma_danhmuc),
    FOREIGN KEY (ma_nxb) REFERENCES nxb(ma_nxb)
);

CREATE TABLE tac_gia (
    ma_tacgia INT IDENTITY PRIMARY KEY,
    ten_tacgia NVARCHAR(100) NOT NULL,
    tieu_su NVARCHAR(MAX)
);

CREATE TABLE sach_tac_gia (
    isbn NVARCHAR(13),
    ma_tacgia INT,
    PRIMARY KEY (isbn, ma_tacgia),
    FOREIGN KEY (isbn) REFERENCES sach(isbn),
    FOREIGN KEY (ma_tacgia) REFERENCES tac_gia(ma_tacgia)
);

CREATE TABLE sach_dien_tu (
    isbn NVARCHAR(13) PRIMARY KEY,
    dung_luong_file DECIMAL(10,2),
    duong_dan_tai NVARCHAR(255),
    FOREIGN KEY (isbn) REFERENCES sach(isbn) ON DELETE CASCADE
);

CREATE TABLE sach_vat_ly (
    isbn NVARCHAR(13) PRIMARY KEY,
    can_nang DECIMAL(5,2),
    FOREIGN KEY (isbn) REFERENCES sach(isbn) ON DELETE CASCADE
);

/* =============================================================================
   5. KHO HÀNG (HỖ TRỢ BARCODE QUÉT MÃ)
============================================================================= */
CREATE TABLE nha_cung_cap (
    ma_ncc INT IDENTITY PRIMARY KEY,
    ten_ncc NVARCHAR(150) NOT NULL,
    thong_tin_lien_he NVARCHAR(MAX)
);

CREATE TABLE kho_hang (
    ma_kho INT IDENTITY PRIMARY KEY,
    isbn NVARCHAR(13) UNIQUE NOT NULL,
    so_luong_ton INT DEFAULT 0 CHECK (so_luong_ton >= 0),
    vi_tri_ke NVARCHAR(50),
    nguong_bao_dong INT DEFAULT 5,
    FOREIGN KEY (isbn) REFERENCES sach(isbn)
);

CREATE TABLE phieu_nhap (
    ma_phieunhap NVARCHAR(20) PRIMARY KEY,
    ma_ncc INT NOT NULL,
    ma_nhanvien INT NOT NULL,
    ngay_nhap DATETIME DEFAULT GETDATE(),
    tong_tien DECIMAL(15,2) DEFAULT 0,
    FOREIGN KEY (ma_ncc) REFERENCES nha_cung_cap(ma_ncc),
    FOREIGN KEY (ma_nhanvien) REFERENCES nhan_vien(ma_nhanvien)
);

CREATE TABLE chi_tiet_phieu_nhap (
    ma_phieunhap NVARCHAR(20),
    isbn NVARCHAR(13),
    so_luong INT NOT NULL CHECK (so_luong > 0),
    don_gia_nhap DECIMAL(12,2) NOT NULL CHECK (don_gia_nhap >= 0),
    PRIMARY KEY (ma_phieunhap, isbn),
    FOREIGN KEY (ma_phieunhap) REFERENCES phieu_nhap(ma_phieunhap),
    FOREIGN KEY (isbn) REFERENCES sach(isbn)
);

/* =============================================================================
   6. VOUCHER & ĐƠN HÀNG (CORE E-COMMERCE)
============================================================================= */
CREATE TABLE voucher (
    ma_voucher NVARCHAR(20) PRIMARY KEY,
    gia_tri_giam DECIMAL(10,2) NOT NULL CHECK (gia_tri_giam >= 0),
    dieu_kien_toi_thieu DECIMAL(12,2) DEFAULT 0,
    thoi_han DATETIME NOT NULL
);

CREATE TABLE don_hang (
    ma_donhang NVARCHAR(20) PRIMARY KEY,
    ma_khachhang BIGINT NOT NULL,
    ma_voucher NVARCHAR(20),
    ngay_tao DATETIME DEFAULT GETDATE(),
    tong_tien_hang DECIMAL(12,2) DEFAULT 0,
    phi_vanchuyen DECIMAL(10,2) DEFAULT 0 CHECK (phi_vanchuyen >= 0), -- Vá lỗi phi_vanchuyen âm
    tong_thanh_toan DECIMAL(15,2) DEFAULT 0,
    trang_thai NVARCHAR(50) DEFAULT N'MOI',
    dia_chi_giao_cu_the NVARCHAR(MAX),
    FOREIGN KEY (ma_khachhang) REFERENCES khach_hang(ma_khachhang),
    FOREIGN KEY (ma_voucher) REFERENCES voucher(ma_voucher),
    FOREIGN KEY (trang_thai) REFERENCES trang_thai_don_hang(ma_trang_thai)
);

CREATE TABLE chi_tiet_don_hang (
    ma_donhang NVARCHAR(20),
    isbn NVARCHAR(13),
    so_luong INT NOT NULL CHECK (so_luong > 0),
    gia_ban_chot DECIMAL(12,2) NOT NULL CHECK (gia_ban_chot >= 0),
    PRIMARY KEY (ma_donhang, isbn),
    FOREIGN KEY (ma_donhang) REFERENCES don_hang(ma_donhang),
    FOREIGN KEY (isbn) REFERENCES sach(isbn)
);

/* =============================================================================
   7. PHIẾU XUẤT KHO
============================================================================= */
CREATE TABLE phieu_xuat (
    ma_phieuxuat NVARCHAR(20) PRIMARY KEY,
    ma_donhang NVARCHAR(20) UNIQUE NOT NULL,
    ngay_xuat DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (ma_donhang) REFERENCES don_hang(ma_donhang)
);

CREATE TABLE chi_tiet_phieu_xuat (
    ma_phieuxuat NVARCHAR(20),
    isbn NVARCHAR(13),
    so_luong INT NOT NULL CHECK (so_luong > 0),
    PRIMARY KEY (ma_phieuxuat, isbn),
    FOREIGN KEY (ma_phieuxuat) REFERENCES phieu_xuat(ma_phieuxuat),
    FOREIGN KEY (isbn) REFERENCES sach(isbn)
);

/* =============================================================================
   8. THANH TOÁN - VẬN CHUYỂN (LIVE TRACKING)
============================================================================= */
CREATE TABLE thanh_toan (
    ma_thanhtoan NVARCHAR(50) PRIMARY KEY,
    ma_donhang NVARCHAR(20) NOT NULL,
    phuong_thuc NVARCHAR(50) NOT NULL,
    trang_thai NVARCHAR(50) DEFAULT N'PENDING',
    ngay_thanh_toan DATETIME,
    ma_tham_chieu_cong NVARCHAR(100),
    FOREIGN KEY (ma_donhang) REFERENCES don_hang(ma_donhang),
    FOREIGN KEY (phuong_thuc) REFERENCES phuong_thuc_thanh_toan(ma_pt),
    FOREIGN KEY (trang_thai) REFERENCES trang_thai_thanh_toan(ma_trang_thai)
);

CREATE TABLE van_chuyen (
    ma_vanchuyen NVARCHAR(30) PRIMARY KEY,
    ma_donhang NVARCHAR(20) UNIQUE NOT NULL,
    doi_tac NVARCHAR(50) DEFAULT N'GHN',
    tracking_id NVARCHAR(50),
    trang_thai_tracking NVARCHAR(100),
    thoi_gian_cap_nhat DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (ma_donhang) REFERENCES don_hang(ma_donhang)
);

/* =============================================================================
   9. PHỤ TRỢ (GIỎ HÀNG, ĐÁNH GIÁ, AUDIT LOG)
============================================================================= */
CREATE TABLE gio_hang (
    ma_khachhang BIGINT,
    isbn NVARCHAR(13),
    so_luong INT DEFAULT 1 CHECK (so_luong > 0),
    PRIMARY KEY (ma_khachhang, isbn),
    FOREIGN KEY (ma_khachhang) REFERENCES khach_hang(ma_khachhang),
    FOREIGN KEY (isbn) REFERENCES sach(isbn)
);

CREATE TABLE danh_gia (
    ma_dg BIGINT IDENTITY PRIMARY KEY,
    ma_khachhang BIGINT NOT NULL,
    isbn NVARCHAR(13) NOT NULL,
    diem_dg INT CHECK (diem_dg BETWEEN 1 AND 5),
    nhan_xet NVARCHAR(MAX),
    ngay_dg DATETIME DEFAULT GETDATE(),
    UNIQUE(ma_khachhang, isbn),
    FOREIGN KEY (ma_khachhang) REFERENCES khach_hang(ma_khachhang),
    FOREIGN KEY (isbn) REFERENCES sach(isbn)
);

CREATE TABLE ho_tro (
    ma_hotro BIGINT IDENTITY PRIMARY KEY,
    ma_khachhang BIGINT NOT NULL,
    tieu_de NVARCHAR(200) NOT NULL,
    noi_dung NVARCHAR(MAX),
    trang_thai NVARCHAR(50) DEFAULT N'OPEN',
    thoi_gian DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (ma_khachhang) REFERENCES khach_hang(ma_khachhang),
    FOREIGN KEY (trang_thai) REFERENCES trang_thai_ho_tro(ma_trang_thai)
);

CREATE TABLE audit_log (
    log_id BIGINT IDENTITY PRIMARY KEY,
    username NVARCHAR(50) NOT NULL,
    hanh_dong NVARCHAR(255) NOT NULL,
    chi_tiet NVARCHAR(MAX),
    thoi_diem DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (username) REFERENCES tai_khoan(username)
);

/* =============================================================================
   10. INDEX (TỐI ƯU HIỆU NĂNG TRUY VẤN - BỔ SUNG FULL)
============================================================================= */
CREATE INDEX IX_donhang_khachhang ON don_hang(ma_khachhang);
CREATE INDEX IX_ctdh_isbn ON chi_tiet_don_hang(isbn);
CREATE INDEX IX_sach_danhmuc ON sach(ma_danhmuc);
CREATE INDEX IX_thanhtoan_donhang ON thanh_toan(ma_donhang);

-- Các Index bổ sung tối ưu cho Trigger và Tracking
CREATE INDEX IX_kho_isbn ON kho_hang(isbn);
CREATE INDEX IX_ctpn_isbn ON chi_tiet_phieu_nhap(isbn);
CREATE INDEX IX_ctxuat_isbn ON chi_tiet_phieu_xuat(isbn);
CREATE INDEX IX_audit_username ON audit_log(username);

/* =============================================================================
   11. TRIGGER (NGHIỆP VỤ TỰ ĐỘNG - XỬ LÝ BULK INSERT HOÀN HẢO)
============================================================================= */
GO

-- 11.1 Trigger Nhập Kho (GROUP BY + MERGE)
CREATE TRIGGER trg_nhap_kho
ON chi_tiet_phieu_nhap
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    -- Cộng kho bằng MERGE (Tự động tính tổng bulk insert)
    MERGE kho_hang AS k
    USING (
        SELECT isbn, SUM(so_luong) AS tong_nhap 
        FROM inserted 
        GROUP BY isbn
    ) AS i
    ON k.isbn = i.isbn
    WHEN MATCHED THEN
        UPDATE SET so_luong_ton = k.so_luong_ton + i.tong_nhap
    WHEN NOT MATCHED THEN
        INSERT (isbn, so_luong_ton) VALUES (i.isbn, i.tong_nhap);
        
    -- Tính tổng tiền phiếu nhập
    UPDATE pn
    SET tong_tien = (
        SELECT SUM(so_luong * don_gia_nhap) 
        FROM chi_tiet_phieu_nhap 
        WHERE ma_phieunhap = pn.ma_phieunhap
    )
    FROM phieu_nhap pn
    JOIN (SELECT DISTINCT ma_phieunhap FROM inserted) i ON pn.ma_phieunhap = i.ma_phieunhap;
END
GO

-- 11.2 Trigger Xuất Kho (GROUP BY + THROW LỖI ÂM KHO)
CREATE TRIGGER trg_xuat_kho
ON chi_tiet_phieu_xuat
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    
    UPDATE k
    SET so_luong_ton = k.so_luong_ton - x.tong_xuat
    FROM kho_hang k
    JOIN (
        SELECT isbn, SUM(so_luong) AS tong_xuat
        FROM inserted
        GROUP BY isbn
    ) x ON k.isbn = x.isbn;

    -- Dùng THROW thay cho RAISERROR & ROLLBACK
    IF EXISTS (SELECT 1 FROM kho_hang WHERE so_luong_ton < 0)
    BEGIN
        THROW 50001, N'Lỗi Transaction: Số lượng tồn kho không đủ để xuất hàng!', 1;
    END
END
GO

-- 11.3 Trigger Tính Tiền Đơn Hàng (XỬ LÝ INSERT, UPDATE, DELETE & CHỐNG LẶP SUBQUERY)
CREATE TRIGGER trg_tinh_tien
ON chi_tiet_don_hang
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @AffectedOrders TABLE (ma_donhang NVARCHAR(20));
    
    INSERT INTO @AffectedOrders (ma_donhang)
    SELECT ma_donhang FROM inserted 
    UNION 
    SELECT ma_donhang FROM deleted;

    UPDATE dh
    SET 
        tong_tien_hang = ISNULL(t.tong, 0),
        
        tong_thanh_toan = CASE 
            WHEN (ISNULL(t.tong, 0) + ISNULL(dh.phi_vanchuyen, 0) - ISNULL(v.gia_tri_giam, 0)) < 0 
            THEN 0
            ELSE (ISNULL(t.tong, 0) + ISNULL(dh.phi_vanchuyen, 0) - ISNULL(v.gia_tri_giam, 0))
        END
    FROM don_hang dh
    LEFT JOIN (
        SELECT ma_donhang, SUM(so_luong * gia_ban_chot) AS tong
        FROM chi_tiet_don_hang
        GROUP BY ma_donhang
    ) t ON dh.ma_donhang = t.ma_donhang
    LEFT JOIN voucher v ON dh.ma_voucher = v.ma_voucher
    WHERE dh.ma_donhang IN (SELECT ma_donhang FROM @AffectedOrders);
END
GO
