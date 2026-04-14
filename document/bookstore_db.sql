-- ==============================================================================
-- CHUYỂN ĐỔI SANG CHUẨN SQL SERVER (T-SQL) - ĐÃ ĐỒNG BỘ VỚI ENTITY JAVA
-- ==============================================================================
create database bookstore_db;
use bookstore_db;
-- 1. NHÓM ĐỊNH DANH & NHÂN SỰ
CREATE TABLE tai_khoan (
    username NVARCHAR(50) PRIMARY KEY,
    password NVARCHAR(255) NOT NULL, 
    role NVARCHAR(50) NOT NULL CONSTRAINT CHK_Role CHECK (role IN ('ADMIN', 'STAFF', 'STOREKEEPER', 'CUSTOMER')),
    trang_thai BIT DEFAULT 1,
    ngay_tao DATETIME DEFAULT GETDATE()
);

CREATE TABLE nhan_vien (
    ma_nhanvien INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) UNIQUE NOT NULL,
    ho_ten NVARCHAR(100) NOT NULL,
    sdt NVARCHAR(15),
    bo_phan NVARCHAR(50) NOT NULL CONSTRAINT CHK_BoPhan CHECK (bo_phan IN ('QUAN_LY', 'BAN_HANG', 'KHO')),
    FOREIGN KEY (username) REFERENCES tai_khoan(username) ON DELETE CASCADE
);

CREATE TABLE khach_hang (
    ma_khachhang BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) UNIQUE NOT NULL,
    ho_ten NVARCHAR(100) NOT NULL,
    sdt NVARCHAR(15),
    dia_chi_giao_hang NVARCHAR(MAX),
    diem_tich_luy INT DEFAULT 0,
    FOREIGN KEY (username) REFERENCES tai_khoan(username) ON DELETE CASCADE
);

CREATE TABLE audit_log (
    log_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL,
    hanh_dong NVARCHAR(255) NOT NULL,
    thoi_diem DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (username) REFERENCES tai_khoan(username)
);

-- 2. NHÓM SẢN PHẨM & DANH MỤC
CREATE TABLE danh_muc (
    ma_danhmuc INT IDENTITY(1,1) PRIMARY KEY,
    ten_danhmuc NVARCHAR(100) NOT NULL,
    danh_muc_cha_id INT NULL,
    FOREIGN KEY (danh_muc_cha_id) REFERENCES danh_muc(ma_danhmuc)
);

CREATE TABLE nxb (
    ma_nxb INT IDENTITY(1,1) PRIMARY KEY,
    ten_nxb NVARCHAR(150) NOT NULL
);

CREATE TABLE tac_gia (
    ma_tacgia INT IDENTITY(1,1) PRIMARY KEY,
    ten_tacgia NVARCHAR(100) NOT NULL,
    tieu_su NVARCHAR(MAX)
);

CREATE TABLE sach (
    isbn NVARCHAR(13) PRIMARY KEY,
    ten_sach NVARCHAR(255) NOT NULL, -- Đồng bộ với tenSach trong Java
    gia_niem_yet DECIMAL(10,2) NOT NULL,
    so_trang INT,
    ma_danhmuc INT,
    ma_nxb INT,
    mo_ta_ngu_nghia NVARCHAR(MAX), 
    anh_bia NVARCHAR(255),
    FOREIGN KEY (ma_danhmuc) REFERENCES danh_muc(ma_danhmuc),
    FOREIGN KEY (ma_nxb) REFERENCES nxb(ma_nxb)
);

CREATE TABLE sach_vat_ly (
    isbn NVARCHAR(13) PRIMARY KEY,
    can_nang DECIMAL(5,2),
    FOREIGN KEY (isbn) REFERENCES sach(isbn) ON DELETE CASCADE
);

CREATE TABLE sach_dien_tu (
    isbn NVARCHAR(13) PRIMARY KEY,
    dung_luong_file DECIMAL(10,2),
    duong_dan_tai NVARCHAR(255),
    FOREIGN KEY (isbn) REFERENCES sach(isbn) ON DELETE CASCADE
);

CREATE TABLE sach_tac_gia (
    isbn NVARCHAR(13) NOT NULL,
    ma_tacgia INT NOT NULL,
    PRIMARY KEY (isbn, ma_tacgia),
    FOREIGN KEY (isbn) REFERENCES sach(isbn),
    FOREIGN KEY (ma_tacgia) REFERENCES tac_gia(ma_tacgia)
);

-- 3. NHÓM KHO VÀ NHÀ CUNG CẤP
CREATE TABLE nha_cung_cap (
    ma_ncc INT IDENTITY(1,1) PRIMARY KEY,
    ten_ncc NVARCHAR(150) NOT NULL,
    thong_tin_lien_he NVARCHAR(MAX)
);

CREATE TABLE kho_hang (
    ma_kho INT IDENTITY(1,1) PRIMARY KEY,
    isbn NVARCHAR(13) UNIQUE NOT NULL,
    so_luong_ton INT DEFAULT 0,
    vi_tri_ke NVARCHAR(50), 
    nguong_bao_dong INT DEFAULT 5,
    FOREIGN KEY (isbn) REFERENCES sach(isbn)
);

CREATE TABLE phieu_nhap (
    ma_phieunhap NVARCHAR(20) PRIMARY KEY,
    ma_ncc INT NOT NULL,
    ma_nhanvien INT NOT NULL,
    ngay_nhap DATETIME DEFAULT GETDATE(),
    tong_tien DECIMAL(15,2),
    FOREIGN KEY (ma_ncc) REFERENCES nha_cung_cap(ma_ncc),
    FOREIGN KEY (ma_nhanvien) REFERENCES nhan_vien(ma_nhanvien)
);

CREATE TABLE chi_tiet_phieu_nhap (
    ma_phieunhap NVARCHAR(20) NOT NULL,
    isbn NVARCHAR(13) NOT NULL,
    so_luong INT NOT NULL,
    don_gia_nhap DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (ma_phieunhap, isbn),
    FOREIGN KEY (ma_phieunhap) REFERENCES phieu_nhap(ma_phieunhap),
    FOREIGN KEY (isbn) REFERENCES sach(isbn)
);

-- 4. NHÓM GIAO DỊCH & GIỎ HÀNG
CREATE TABLE gio_hang (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_khachhang BIGINT NOT NULL,
    isbn NVARCHAR(13) NOT NULL,
    so_luong INT DEFAULT 1,
    FOREIGN KEY (ma_khachhang) REFERENCES khach_hang(ma_khachhang),
    FOREIGN KEY (isbn) REFERENCES sach(isbn)
);

CREATE TABLE voucher (
    ma_voucher NVARCHAR(20) PRIMARY KEY, -- Đồng bộ với maVoucher trong Java
    gia_tri_giam DECIMAL(10,2) NOT NULL,
    dieu_kien_toi_thieu DECIMAL(12,2) DEFAULT 0,
    thoi_han DATETIME NOT NULL
);

CREATE TABLE don_hang (
    ma_donhang NVARCHAR(20) PRIMARY KEY,
    ma_khachhang BIGINT NOT NULL,
    ma_voucher NVARCHAR(20),
    ngay_tao DATETIME DEFAULT GETDATE(),
    tong_tien_hang DECIMAL(12,2) NOT NULL,
    phi_vanchuyen DECIMAL(10,2) DEFAULT 0,
    tong_thanh_toan DECIMAL(15,2) NOT NULL,
    trang_thai NVARCHAR(50) DEFAULT 'MOI' CONSTRAINT CHK_DH_TrangThai CHECK (trang_thai IN ('MOI', 'DA_XAC_NHAN', 'CHO_LAY_HANG', 'DANG_GIAO', 'HOAN_TAT', 'DA_HUY')),
    dia_chi_giao_cu_the NVARCHAR(MAX),
    FOREIGN KEY (ma_khachhang) REFERENCES khach_hang(ma_khachhang),
    FOREIGN KEY (ma_voucher) REFERENCES voucher(ma_voucher)
);

CREATE TABLE chi_tiet_don_hang (
    ma_donhang NVARCHAR(20) NOT NULL,
    isbn NVARCHAR(13) NOT NULL,
    so_luong INT NOT NULL,
    gia_ban_chot DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (ma_donhang, isbn),
    FOREIGN KEY (ma_donhang) REFERENCES don_hang(ma_donhang),
    FOREIGN KEY (isbn) REFERENCES sach(isbn)
);

CREATE TABLE thanh_toan (
    ma_thanhtoan NVARCHAR(50) PRIMARY KEY,
    ma_donhang NVARCHAR(20) NOT NULL,
    phuong_thuc NVARCHAR(50) NOT NULL CONSTRAINT CHK_TT_PhuongThuc CHECK (phuong_thuc IN ('COD', 'VNPAY', 'MOMO')),
    trang_thai NVARCHAR(50) DEFAULT 'PENDING' CONSTRAINT CHK_TT_TrangThai CHECK (trang_thai IN ('PENDING', 'SUCCESS', 'FAILED')),
    ngay_thanh_toan DATETIME,
    ma_tham_chieu_cong NVARCHAR(100), 
    FOREIGN KEY (ma_donhang) REFERENCES don_hang(ma_donhang)
);

-- 5. NHÓM LOGISTICS & XUẤT KHO
CREATE TABLE phieu_xuat (
    ma_phieuxuat NVARCHAR(20) PRIMARY KEY,
    ma_donhang NVARCHAR(20) UNIQUE NOT NULL,
    ngay_xuat DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (ma_donhang) REFERENCES don_hang(ma_donhang)
);

CREATE TABLE van_chuyen (
    ma_vanchuyen NVARCHAR(30) PRIMARY KEY,
    ma_donhang NVARCHAR(20) UNIQUE NOT NULL,
    doi_tac NVARCHAR(50) DEFAULT 'GHN',
    tracking_id NVARCHAR(50), 
    trang_thai_tracking NVARCHAR(100),
    thoi_gian_cap_nhat DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (ma_donhang) REFERENCES don_hang(ma_donhang)
);

-- 6. NHÓM ĐÁNH GIÁ & HỖ TRỢ
CREATE TABLE danh_gia (
    ma_dg BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_khachhang BIGINT NOT NULL,
    isbn NVARCHAR(13) NOT NULL,
    diem_dg INT CONSTRAINT CHK_Diem CHECK (diem_dg BETWEEN 1 AND 5),
    nhan_xet NVARCHAR(MAX),
    ngay_dg DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (ma_khachhang) REFERENCES khach_hang(ma_khachhang),
    FOREIGN KEY (isbn) REFERENCES sach(isbn)
);

CREATE TABLE ho_tro (
    ma_hotro BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_khachhang BIGINT NOT NULL,
    tieu_de NVARCHAR(200) NOT NULL,
    noi_dung NVARCHAR(MAX),
    trang_thai NVARCHAR(50) DEFAULT 'OPEN' CONSTRAINT CHK_HT_TrangThai CHECK (trang_thai IN ('OPEN', 'PROCESSING', 'CLOSED')),
    thoi_gian DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (ma_khachhang) REFERENCES khach_hang(ma_khachhang)
);
