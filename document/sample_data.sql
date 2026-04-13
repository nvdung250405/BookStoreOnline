-- ==============================================================================
-- DỮ LIỆU MẪU ĐÃ ĐỒNG BỘ THEO NHÓM ENTITY (ten_sach, ma_voucher)
-- ==============================================================================

-- 1. TÀI KHOẢN
INSERT INTO tai_khoan (username, password, role, trang_thai) VALUES 
('admin', '123456', 'ADMIN', 1),
('nhanvien1', '123456', 'STAFF', 1),
('khachhang1', '123456', 'CUSTOMER', 1),
('khachhang2', '123456', 'CUSTOMER', 1);

-- 2. NHÂN VIÊN & KHÁCH HÀNG
INSERT INTO nhan_vien (username, ho_ten, sdt, bo_phan) VALUES 
('nhanvien1', N'Nguyễn Văn Thành', '0987654321', 'BAN_HANG');

INSERT INTO khach_hang (username, ho_ten, sdt, dia_chi_giao_hang, diem_tich_luy) VALUES 
('khachhang1', N'Trần Thị Lan', '0123456789', N'123 Cầu Giấy, Hà Nội', 100),
('khachhang2', N'Lê Minh Hoàng', '0912345678', N'456 Quận 1, TP.HCM', 50);

-- 3. DANH MỤC, NXB, TÁC GIẢ
INSERT INTO danh_muc (ten_danhmuc, danh_muc_cha_id) VALUES 
(N'Văn học trong nước', NULL),
(N'Kỹ năng sống', NULL);

INSERT INTO nxb (ten_nxb) VALUES (N'NXB Trẻ'), (N'NXB Kim Đồng');

INSERT INTO tac_gia (ten_tacgia, tieu_su) VALUES 
(N'Nguyễn Nhật Ánh', N'Tác giả nổi tiếng với các tác phẩm dành cho tuổi trẻ'),
(N'Dale Carnegie', N'Tác giả cuốn Đắc Nhân Tâm');

-- 4. SÁCH (ten_sach khớp Entity)
INSERT INTO sach (isbn, ten_sach, gia_niem_yet, so_trang, ma_danhmuc, ma_nxb, mo_ta_ngu_nghia, anh_bia) VALUES 
('9781234567890', N'Cho tôi xin một vé đi tuổi thơ', 85000, 200, 1, 1, N'Câu chuyện về thời thơ ấu đầy kỷ niệm', 've-tuoi-tho.jpg');
INSERT INTO sach_vat_ly (isbn, can_nang) VALUES ('9781234567890', 0.35);

INSERT INTO sach (isbn, ten_sach, gia_niem_yet, so_trang, ma_danhmuc, ma_nxb, mo_ta_ngu_nghia, anh_bia) VALUES 
('9780123456789', N'Đắc Nhân Tâm (Ebook)', 50000, 350, 2, 2, N'Cẩm nang giao tiếp kinh điển', 'dac-nhan-tam.jpg');
INSERT INTO sach_dien_tu (isbn, dung_luong_file, duong_dan_tai) VALUES ('9780123456789', 5.5, 'download/dac-nhan-tam.pdf');

INSERT INTO sach_tac_gia (isbn, ma_tacgia) VALUES ('9781234567890', 1), ('9780123456789', 2);

-- 5. VOUCHER (ma_voucher khớp Entity)
INSERT INTO voucher (ma_voucher, gia_tri_giam, dieu_kien_toi_thieu, thoi_han) VALUES 
('GIAM20K', 20000, 100000, '2026-12-31'),
('FREESHIP', 15000, 50000, '2026-12-31');

-- 6. ĐÁNH GIÁ & HỖ TRỢ
INSERT INTO danh_gia (ma_khachhang, isbn, diem_dg, nhan_xet) VALUES 
(1, '9781234567890', 5, N'Sách đóng gói rất đẹp, giao hàng nhanh'),
(2, '9780123456789', 4, N'Nội dung rất ý nghĩa, ebook đọc mượt');

INSERT INTO ho_tro (ma_khachhang, tieu_de, noi_dung, trang_thai) VALUES 
(1, N'Vấn đề thanh toán', N'Tôi đã thanh toán nhưng chưa thấy xác nhận đơn hàng', 'OPEN');
