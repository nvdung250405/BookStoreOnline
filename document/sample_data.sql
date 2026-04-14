USE bookstore_db;
GO

-- ==============================================================================
-- BƯỚC 1: XÓA DỮ LIỆU CŨ (Thứ tự chuẩn để không lỗi khóa ngoại)
-- ==============================================================================
DELETE FROM audit_log;
DELETE FROM ho_tro;
DELETE FROM danh_gia;
DELETE FROM gio_hang;
DELETE FROM chi_tiet_don_hang;
DELETE FROM van_chuyen;
DELETE FROM thanh_toan;
DELETE FROM don_hang;
DELETE FROM chi_tiet_phieu_nhap;
DELETE FROM phieu_nhap;
DELETE FROM phieu_xuat;
DELETE FROM kho_hang;
DELETE FROM sach_tac_gia;
DELETE FROM sach_vat_ly;
DELETE FROM sach_dien_tu;
DELETE FROM sach;
DELETE FROM tac_gia;
DELETE FROM nxb;
DELETE FROM danh_muc;
DELETE FROM nhan_vien;
DELETE FROM khach_hang;
DELETE FROM tai_khoan;
DELETE FROM voucher;
DELETE FROM nha_cung_cap;

-- BƯỚC 2: RESET IDENTITY (Để ID bắt đầu lại từ 1)
DBCC CHECKIDENT ('nhan_vien', RESEED, 0);
DBCC CHECKIDENT ('khach_hang', RESEED, 0);
DBCC CHECKIDENT ('danh_muc', RESEED, 0);
DBCC CHECKIDENT ('nxb', RESEED, 0);
DBCC CHECKIDENT ('tac_gia', RESEED, 0);
DBCC CHECKIDENT ('nha_cung_cap', RESEED, 0);
DBCC CHECKIDENT ('kho_hang', RESEED, 0);
DBCC CHECKIDENT ('danh_gia', RESEED, 0);
DBCC CHECKIDENT ('ho_tro', RESEED, 0);

-- ==============================================================================
-- BƯỚC 3: NẠP DỮ LIỆU ĐÃ HỢP NHẤT 
-- ==============================================================================

-- 1. TÀI KHOẢN (Password mặc định: 123456)
INSERT INTO tai_khoan (username, password, role, trang_thai) VALUES 
('admin', '$2a$10$hn.nR/A4Jw0/sgMHZfu4UeIv5owT5mTcpLVHgnHj4xMK4CT1z0YOu', 'ADMIN', 1),
('nhanvien1', '$2a$10$hn.nR/A4Jw0/sgMHZfu4UeIv5owT5mTcpLVHgnHj4xMK4CT1z0YOu', 'STAFF', 1),
('khachhang1', '$2a$10$hn.nR/A4Jw0/sgMHZfu4UeIv5owT5mTcpLVHgnHj4xMK4CT1z0YOu', 'CUSTOMER', 1),
('khachhang2', '$2a$10$hn.nR/A4Jw0/sgMHZfu4UeIv5owT5mTcpLVHgnHj4xMK4CT1z0YOu', 'CUSTOMER', 1),
('kho.nhanvien1', '123456', 'STOREKEEPER', 1),
('khach.vip', '123456', 'CUSTOMER', 1);

-- 2. NHÂN VIÊN & KHÁCH HÀNG (ÉP ID 1, 2, 3)
SET IDENTITY_INSERT nhan_vien ON;
INSERT INTO nhan_vien (ma_nhanvien, username, ho_ten, sdt, bo_phan) VALUES 
(1, 'nhanvien1', N'Nguyễn Văn Thành', '0987654321', 'BAN_HANG'),
(2, 'kho.nhanvien1', N'Trần Thủ Kho', '0901234567', 'KHO');
SET IDENTITY_INSERT nhan_vien OFF;

SET IDENTITY_INSERT khach_hang ON;
INSERT INTO khach_hang (ma_khachhang, username, ho_ten, sdt, dia_chi_giao_hang, diem_tich_luy) VALUES 
(1, 'khachhang1', N'Trần Thị Lan', '0123456789', N'123 Cầu Giấy, Hà Nội', 100),
(2, 'khachhang2', N'Lê Minh Hoàng', '0912345678', N'456 Quận 1, TP.HCM', 50),
(3, 'khach.vip', N'Nguyễn Khách VIP', '0987654321', N'Biệt thự ABC, Quận 7, TP.HCM', 500);
SET IDENTITY_INSERT khach_hang OFF;

-- 3. DANH MỤC, NXB, TÁC GIẢ (ÉP ID)
SET IDENTITY_INSERT danh_muc ON;
INSERT INTO danh_muc (ma_danhmuc, ten_danhmuc) VALUES 
(1, N'Văn học'), (2, N'Kinh tế'), (3, N'Công nghệ'), (4, N'Tâm Lý Kỹ Năng');
SET IDENTITY_INSERT danh_muc OFF;

SET IDENTITY_INSERT nxb ON;
INSERT INTO nxb (ma_nxb, ten_nxb) VALUES 
(1, N'NXB Trẻ'), (2, N'NXB Kim Đồng'), (3, N'NXB Giáo Dục'), (4, N'NXB Tổng Hợp');
SET IDENTITY_INSERT nxb OFF;

SET IDENTITY_INSERT tac_gia ON;
INSERT INTO tac_gia (ma_tacgia, ten_tacgia) VALUES 
(1, N'Nguyễn Nhật Ánh'), (2, N'Dale Carnegie'), (3, N'Robert Kiyosaki');
SET IDENTITY_INSERT tac_gia OFF;

-- 4. NHÀ CUNG CẤP
SET IDENTITY_INSERT nha_cung_cap ON;
INSERT INTO nha_cung_cap (ma_ncc, ten_ncc, thong_tin_lien_he) VALUES
(1, N'Fahasa Tổng', N'1900636467 - cskh@fahasa.com - Quận 1, HCM'),
(2, N'Tiki Trading', N'19006035 - partner@tiki.vn - Tân Bình, HCM');
SET IDENTITY_INSERT nha_cung_cap OFF;

-- 5. SÁCH (Bao gồm dữ liệu cũ + dữ liệu Nhóm V)
INSERT INTO sach (isbn, ten_sach, gia_niem_yet, so_trang, ma_danhmuc, ma_nxb) VALUES 
('9781111111111', N'Mắt Biếc', 100000, 300, 1, 1),
('9782222222222', N'Dạy Con Làm Giàu', 120000, 500, 2, 3),
('9786043653194', N'Đắc Nhân Tâm', 86000, 320, 4, 4), -- Test Quét mã
('9781234567890', N'Cây Cam Ngọt Của Tôi', 75000, 250, 1, 1), -- Test Hết hàng
('9780000000000', N'Sách Ebook Lập Trình (PDF)', 150000, 500, 3, 3); -- Test chặn Ebook

INSERT INTO sach_vat_ly (isbn, can_nang) VALUES 
('9781111111111', 0.5), ('9782222222222', 0.7), ('9786043653194', 0.5), ('9781234567890', 0.3);

INSERT INTO sach_dien_tu (isbn, dung_luong_file, duong_dan_tai) VALUES 
('9780000000000', 15.5, 'link_tai_pdf');

-- 6. KHO HÀNG (Dữ liệu nền cho API 37, 38)
SET IDENTITY_INSERT kho_hang ON;
INSERT INTO kho_hang (ma_kho, isbn, so_luong_ton, vi_tri_ke, nguong_bao_dong) VALUES
(1, '9781111111111', 20, N'Kệ B - Tầng 1', 10),
(2, '9786043653194', 150, N'Kệ A - Tầng 2', 15), -- Dồi dào
(3, '9781234567890', 3, N'Kệ C - Tầng 1', 10);   -- Cảnh báo hết hàng
SET IDENTITY_INSERT kho_hang OFF;

-- 7. ĐƠN HÀNG (Cũ + Mới Nhóm V)
-- Đơn hàng cũ
INSERT INTO don_hang (ma_donhang, ma_khachhang, tong_tien_hang, tong_thanh_toan, trang_thai, ngay_tao) VALUES 
('DH001', 1, 250000, 250000, 'HOAN_TAT', '2024-04-10'),
('DH002', 2, 500000, 500000, 'HOAN_TAT', '2024-04-12');

INSERT INTO chi_tiet_don_hang (ma_donhang, isbn, so_luong, gia_ban_chot) VALUES 
('DH001', '9781111111111', 2, 100000), ('DH002', '9781111111111', 5, 100000);

-- Đơn hàng test cho API 40 (Của KHÁCH VIP - ID 3)
INSERT INTO don_hang (ma_donhang, ma_khachhang, tong_tien_hang, tong_thanh_toan, trang_thai, ngay_tao) 
VALUES ('DH-100234', 3, 247000, 260000, 'MOI', GETDATE());

INSERT INTO chi_tiet_don_hang (ma_donhang, isbn, so_luong, gia_ban_chot) VALUES 
('DH-100234', '9786043653194', 2, 86000), 
('DH-100234', '9781234567890', 1, 75000); 

-- 8. DỮ LIỆU KHÁC (Audit, Voucher, Đánh giá)
INSERT INTO audit_log (username, hanh_dong, thoi_diem) VALUES 
('admin', N'UPDATE_ROLE: Thay đổi quyền user khachhang1 thành STAFF', GETDATE()),
('admin', N'LOCK_ACCOUNT: Khóa tài khoản khachhang2 do vi phạm', GETDATE());

INSERT INTO voucher (ma_voucher, gia_tri_giam, thoi_han) VALUES ('GIAM20K', 20000, '2025-12-31');
INSERT INTO danh_gia (ma_khachhang, isbn, diem_dg, nhan_xet) VALUES (1, '9781111111111', 5, N'Sách tuyệt vời');
INSERT INTO ho_tro (ma_khachhang, tieu_de, noi_dung, trang_thai) VALUES (1, N'Cần giúp đỡ', N'Giao hàng hơi chậm', 'OPEN');
GO