-- ==============================================================================
-- DỮ LIỆU MẪU (SAMPLE DATA) CHO HỆ THỐNG NHÀ SÁCH ONLINE
-- Database: nha_sach_online (SQL Server)
-- Số lượng: >5 bản ghi mỗi bảng, bao phủ các trường hợp đa dạng
-- ==============================================================================

USE [nha_sach_online];
GO

-- 1. TAI_KHOAN (Mật khẩu mẫu ở đây là 'password123' đã băm hoặc chuỗi giả lập)
-- Chú ý: Roles: 'ADMIN', 'STAFF', 'STOREKEEPER', 'CUSTOMER'
INSERT INTO [dbo].[tai_khoan] ([username], [password], [role], [trang_thai], [ngay_tao]) VALUES
('admin', '$2a$10$hn.nR/A4Jw0/sgMHZfu4UeIv5owT5mTcpLVHgnHj4xMK4CT1z0YOu', 'ADMIN', 1, GETDATE()),
('staff1', '$2a$10$hn.nR/A4Jw0/sgMHZfu4UeIv5owT5mTcpLVHgnHj4xMK4CT1z0YOu', 'STAFF', 1, GETDATE()),
('staff2', '$2a$10$hn.nR/A4Jw0/sgMHZfu4UeIv5owT5mTcpLVHgnHj4xMK4CT1z0YOu', 'STAFF', 1, GETDATE()),
('kho1', '$2a$10$hn.nR/A4Jw0/sgMHZfu4UeIv5owT5mTcpLVHgnHj4xMK4CT1z0YOu', 'STOREKEEPER', 1, GETDATE()),
('customer1', '$2a$10$hn.nR/A4Jw0/sgMHZfu4UeIv5owT5mTcpLVHgnHj4xMK4CT1z0YOu', 'CUSTOMER', 1, GETDATE()),
('customer2', '$2a$10$hn.nR/A4Jw0/sgMHZfu4UeIv5owT5mTcpLVHgnHj4xMK4CT1z0YOu', 'CUSTOMER', 1, GETDATE()),
('customer3', '$2a$10$hn.nR/A4Jw0/sgMHZfu4UeIv5owT5mTcpLVHgnHj4xMK4CT1z0YOu', 'CUSTOMER', 0, GETDATE()); -- Tài khoản bị khóa
GO

-- 2. NHAN_VIEN (Phần nhân sự)
-- Bo phan: 'QUAN_LY', 'BAN_HANG', 'KHO'
INSERT INTO [dbo].[nhan_vien] ([username], [ho_ten], [sdt], [bo_phan]) VALUES
('admin', N'Nguyễn Quản Trị', '0901234567', 'QUAN_LY'),
('staff1', N'Trần Thị Bán Hàng', '0912345678', 'BAN_HANG'),
('staff2', N'Lê Văn Sales', '0923456789', 'BAN_HANG'),
('kho1', N'Phạm Thủ Kho', '0934567890', 'KHO');
GO

-- 3. KHACH_HANG (Phần khách hàng)
INSERT INTO [dbo].[khach_hang] ([username], [ho_ten], [sdt], [dia_chi_giao_hang], [diem_tich_luy]) VALUES
('customer1', N'Lê Minh Khôi', '0987123456', N'123 Đường Lê Lợi, Quận 1, TP.HCM', 150),
('customer2', N'Hoàng Thanh Trúc', '0987234567', N'456 Đường Nguyễn Huệ, Quận 3, TP.HCM', 50),
('customer3', N'Đặng Quốc Bảo', '0987345678', N'789 Đường CMT8, Quận Tân Bình, TP.HCM', 0);
GO

-- 4. DANH_MUC (Phân cấp cha-con)
INSERT INTO [dbo].[danh_muc] ([ten_danhmuc], [danh_muc_cha_id]) VALUES
(N'Sách Kinh Tế', NULL),      -- ID 1
(N'Sách Văn Học', NULL),      -- ID 2
(N'Sách Kỹ Năng', NULL),      -- ID 3
(N'Kinh Tế Học Cơ Bản', 1),   -- ID 4
(N'Tiểu Thuyết Trinh Thám', 2), -- ID 5
(N'Phát Triển Bản Thân', 3);  -- ID 6
GO

-- 5. NXB (Nhà xuất bản)
INSERT INTO [dbo].[nxb] ([ten_nxb]) VALUES
(N'NXB Trẻ'),
(N'NXB Kim Đồng'),
(N'NXB Giáo Dục'),
(N'NXB Tổng Hợp TP.HCM'),
(N'NXB Nhã Nam');
GO

-- 6. TAC_GIA (Tác giả)
INSERT INTO [dbo].[tac_gia] ([ten_tacgia], [tieu_su]) VALUES
(N'Nguyễn Nhật Ánh', N'Nhà văn nổi tiếng với các tác phẩm dành cho tuổi thanh thiếu niên.'),
(N'Dale Carnegie', N'Tác giả cuốn Đắc Nhân Tâm.'),
(N'Conan Doyle', N'Cha đẻ của nhân vật Sherlock Holmes.'),
(N'Haruki Murakami', N'Nhà văn đương đại nổi tiếng của Nhật Bản.'),
(N'Paulo Coelho', N'Tác giả cuốn Nhà Giả Kim.');
GO

-- 7. SACH (Thông tin sách)
INSERT INTO [dbo].[sach] ([isbn], [ten_sach], [gia_niem_yet], [so_trang], [ma_danhmuc], [ma_nxb], [mo_ta_ngu_nghia], [anh_bia], [da_xoa]) VALUES
('9786041123456', N'Cho Tôi Xin Một Vé Đi Tuổi Thơ', 85000, 210, 2, 1, N'Câu chuyện về tuổi thơ hồn nhiên.', 'chotoixinmotvedituoitho.jpg', 0),
('9780062315007', N'Nhà Giả Kim', 79000, 180, 2, 5, N'Hành trình đi tìm vận mệnh.', 'nhagiakim.jpg', 0),
('9786045612345', N'Đắc Nhân Tâm', 95000, 320, 6, 4, N'Nghệ thuật thu phục lòng người.', 'dacnhantam.jpg', 0),
('9781234567890', N'Kinh Tế Học Cơ Bản', 150000, 450, 4, 3, N'Kiến thức kinh tế cơ bản.', 'kinhtehoccoban.jpg', 0),
('9780987654321', N'Sherlock Holmes Toàn Tập', 250000, 1200, 5, 5, N'Tuyển tập truyện trinh thám.', 'sherlockholmestoantap.jpg', 0),
('9781111111111', N'Sách Cũ Lỗi Thời', 50000, 100, 1, 1, N'Sách này đã ngừng bán.', 'sachculoithoi.jpg', 1);
GO

-- 8. SACH_VAT_LY & SACH_DIEN_TU
INSERT INTO [dbo].[sach_vat_ly] ([isbn], [can_nang]) VALUES
('9786041123456', 0.35),
('9780062315007', 0.25),
('9786045612345', 0.45);

INSERT INTO [dbo].[sach_dien_tu] ([isbn], [dung_luong_file], [duong_dan_tai]) VALUES
('9781234567890', 5.5, 'https://cdn.bookstore.com/download/9781234567890.pdf'),
('9780987654321', 12.0, 'https://cdn.bookstore.com/download/9780987654321.epub');
GO

-- 9. SACH_TAC_GIA (Liên kết nhiều-nhiều)
INSERT INTO [dbo].[sach_tac_gia] ([isbn], [ma_tacgia]) VALUES
('9786041123456', 1), -- Cho Tôi Xin Một Vé... - Nguyễn Nhật Ánh
('9780062315007', 5), -- Nhà Giả Kim - Paulo Coelho
('9786045612345', 2), -- Đắc Nhân Tâm - Dale Carnegie
('9781234567890', 2), -- Kinh Tế Học - Dale Carnegie (Giả định)
('9780987654321', 3); -- Sherlock Holmes - Conan Doyle
GO

-- 10. NHA_CUNG_CAP
INSERT INTO [dbo].[nha_cung_cap] ([ten_ncc], [thong_tin_lien_he]) VALUES
(N'Công ty Sách Phương Nam', N'123 Phan Xích Long, Phú Nhuận'),
(N'Fahasa Head Office', N'60-62 Lê Lợi, Quận 1'),
(N'Nhà Sách Tiền Phong', N'Quận Cầu Giấy, Hà Nội'),
(N'Đại lý Sách Miền Nam', N'Đường 3/2, Quận 10'),
(N'Công ty In ấn Thăng Long', N'Quận Hải Châu, Đà Nẵng');
GO

-- 11. KHO_HANG (Tồn kho)
INSERT INTO [dbo].[kho_hang] ([isbn], [so_luong_ton], [vi_tri_ke], [nguong_bao_dong]) VALUES
('9786041123456', 100, 'A-12', 10),
('9780062315007', 50, 'B-05', 5),
('9786045612345', 3, 'A-01', 5), -- Tình trạng sắp hết hàng
('9781234567890', 200, 'C-01', 20),
('9780987654321', 30, 'B-10', 5);
GO

-- 12. PHIEU_NHAP & CHI_TIET_PHIEU_NHAP
INSERT INTO [dbo].[phieu_nhap] ([ma_phieunhap], [ma_ncc], [ma_nhanvien], [ngay_nhap], [tong_tien]) VALUES
('PN20240414-01', 1, 4, GETDATE(), 5000000),
('PN20240414-02', 2, 4, GETDATE(), 3000000);

INSERT INTO [dbo].[chi_tiet_phieu_nhap] ([ma_phieunhap], [isbn], [so_luong], [don_gia_nhap]) VALUES
('PN20240414-01', '9786041123456', 50, 60000),
('PN20240414-01', '9780062315007', 20, 55000),
('PN20240414-02', '9781234567890', 30, 100000);
GO

-- 13. VOUCHER
INSERT INTO [dbo].[voucher] ([ma_voucher], [gia_tri_giam], [dieu_kien_toi_thieu], [thoi_han]) VALUES
('GIAM20K', 20000, 200000, '2026-12-31'),
('FREESHIP', 30000, 500000, '2026-06-30'),
('WELCOME10', 10000, 0, '2026-12-31'),
('BLACKFRIDAY', 50000, 1000000, '2026-11-30'),
('MUAHEXANH', 15000, 150000, '2026-08-30');
GO

-- 14. DON_HANG & CHI_TIET_DON_HANG
-- Trang thai: 'MOI', 'DA_XAC_NHAN', 'CHO_LAY_HANG', 'DANG_GIAO', 'HOAN_TAT', 'DA_HUY'
INSERT INTO [dbo].[don_hang] ([ma_donhang], [ma_khachhang], [ma_voucher], [ngay_tao], [tong_tien_hang], [phi_vanchuyen], [tong_thanh_toan], [trang_thai], [dia_chi_giao_cu_the]) VALUES
('ORD-001', 1, 'GIAM20K', GETDATE(), 250000, 30000, 260000, 'HOAN_TAT', N'Phòng 502, Chung cư B'),
('ORD-002', 2, NULL, GETDATE(), 85000, 20000, 105000, 'DANG_GIAO', N'Kiệt 12/4, Đường Xuân Thủy'),
('ORD-003', 1, 'FREESHIP', GETDATE(), 520000, 0, 520000, 'MOI', N'Văn phòng công ty Green'),
('ORD-004', 3, NULL, GETDATE(), 150000, 30000, 180000, 'DA_HUY', N'Số 1 Trần Phú'),
('ORD-005', 2, 'WELCOME10', GETDATE(), 95000, 20000, 105000, 'CHO_LAY_HANG', N'Cổng trường THPT Lê Quý Đôn');

INSERT INTO [dbo].[chi_tiet_don_hang] ([ma_donhang], [isbn], [so_luong], [gia_ban_chot]) VALUES
('ORD-001', '9786041123456', 2, 85000),
('ORD-001', '9780062315007', 1, 80000),
('ORD-002', '9786041123456', 1, 85000),
('ORD-003', '9780987654321', 2, 250000),
('ORD-003', '9786041123456', 1, 85000);
GO

-- 15. THANH_TOAN
-- Phuong thuc: 'COD', 'VNPAY', 'MOMO'. Trang thai: 'PENDING', 'SUCCESS', 'FAILED'
INSERT INTO [dbo].[thanh_toan] ([ma_thanhtoan], [ma_donhang], [phuong_thuc], [trang_thai], [ngay_thanh_toan], [ma_tham_chieu_cong]) VALUES
('PAY001', 'ORD-001', 'COD', 'SUCCESS', GETDATE(), NULL),
('PAY002', 'ORD-002', 'MOMO', 'SUCCESS', GETDATE(), 'MOMO20240414_TRX01'),
('PAY003', 'ORD-003', 'VNPAY', 'PENDING', NULL, NULL),
('PAY004', 'ORD-004', 'COD', 'FAILED', NULL, NULL),
('PAY005', 'ORD-005', 'VNPAY', 'SUCCESS', GETDATE(), 'VNPAY_123456789');
GO

-- 16. PHIEU_XUAT & VAN_CHUYEN
INSERT INTO [dbo].[phieu_xuat] ([ma_phieuxuat], [ma_donhang], [ngay_xuat], [nguoi_xuat]) VALUES
('PX-001', 'ORD-001', GETDATE(), 'staff1'),
('PX-002', 'ORD-002', GETDATE(), 'staff1');

INSERT INTO [dbo].[van_chuyen] ([ma_vanchuyen], [ma_donhang], [doi_tac], [tracking_id], [trang_thai_tracking], [thoi_gian_cap_nhat]) VALUES
('GHN_10001', 'ORD-001', 'GHN', 'TRK001', N'Giao hàng thành công', GETDATE()),
('GHN_10002', 'ORD-002', 'GHN', 'TRK002', N'Đang luân chuyển qua kho HCM', GETDATE()),
('JNT_20001', 'ORD-005', 'GHN', NULL, N'Chờ bưu tá lấy hàng', GETDATE());
GO

-- 17. AUDIT_LOG
INSERT INTO [dbo].[audit_log] ([username], [hanh_dong], [thoi_diem], [chi_tiet]) VALUES
('admin', N'Cập nhật tồn kho', GETDATE(), N'Mã sách 9786041123456 cập nhật +50'),
('staff1', N'Xác nhận đơn hàng', GETDATE(), N'Đơn hàng ORD-001 khách Nguyễn Khôi'),
('kho1', N'Nhập kho phiếu PN01', GETDATE(), N'Nhập 100 cuốn tiểu thuyết'),
('admin', N'Khóa tài khoản khách', GETDATE(), N'Khóa tài khoản customer3 do vi phạm'),
('staff2', N'Hủy đơn hàng', GETDATE(), N'Hủy đơn ORD-004 theo yêu cầu khách');
GO

-- 18. GIO_HANG (Dữ liệu tạm)
INSERT INTO [dbo].[gio_hang] ([ma_khachhang], [isbn], [so_luong]) VALUES
(1, '9780987654321', 1),
(1, '9786045612345', 2),
(2, '9786041123456', 1);
GO

-- 19. DANH_GIA
INSERT INTO [dbo].[danh_gia] ([ma_khachhang], [isbn], [diem_dg], [nhan_xet], [ngay_dg]) VALUES
(1, '9786041123456', 5, N'Sách rất hay, đóng gói đẹp!', GETDATE()),
(2, '9780062315007', 4, N'Nội dung sâu sắc, đáng đọc.', GETDATE()),
(1, '9786045612345', 5, N'Giao hàng nhanh, sách chuẩn.', GETDATE()),
(3, '9781234567890', 3, N'Kiến thức ổn nhưng hơi khó hiểu.', GETDATE());
GO

-- 20. HO_TRO
INSERT INTO [dbo].[ho_tro] ([ma_khachhang], [tieu_de], [noi_dung], [trang_thai], [thoi_gian]) VALUES
(1, N'Hỏi về đơn hàng', N'Đơn ORD-003 bao giờ giao?', 'OPEN', GETDATE()),
(2, N'Đổi trả sách', N'Sách bị rách bìa, muốn đổi.', 'PROCESSING', GETDATE()),
(1, N'Tư vấn sách kinh tế', N'Có cuốn nào về chứng khoán không?', 'CLOSED', GETDATE());
GO

-- ==============================================================================
-- HOÀN TẤT NHẬP DỮ LIỆU MẪU
-- ==============================================================================