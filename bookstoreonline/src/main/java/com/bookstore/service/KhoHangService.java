package com.bookstore.service;

import com.bookstore.dto.*;
import com.bookstore.entity.*;
import com.bookstore.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class KhoHangService {

    private final KhoHangRepository khoHangRepository;
    private final PhieuNhapRepository phieuNhapRepository;
    private final ChiTietPhieuNhapRepository chiTietPhieuNhapRepository;
    private final NhaCungCapRepository nhaCungCapRepository;
    private final NhanVienRepository nhanVienRepository;
    private final SachRepository sachRepository;
    private final PhieuXuatRepository phieuXuatRepository;
    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;

    public KhoHangService(KhoHangRepository khoHangRepository, PhieuNhapRepository phieuNhapRepository,
                         ChiTietPhieuNhapRepository chiTietPhieuNhapRepository, NhaCungCapRepository nhaCungCapRepository,
                         NhanVienRepository nhanVienRepository, SachRepository sachRepository,
                         PhieuXuatRepository phieuXuatRepository, DonHangRepository donHangRepository,
                         ChiTietDonHangRepository chiTietDonHangRepository) {
        this.khoHangRepository = khoHangRepository;
        this.phieuNhapRepository = phieuNhapRepository;
        this.chiTietPhieuNhapRepository = chiTietPhieuNhapRepository;
        this.nhaCungCapRepository = nhaCungCapRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.sachRepository = sachRepository;
        this.phieuXuatRepository = phieuXuatRepository;
        this.donHangRepository = donHangRepository;
        this.chiTietDonHangRepository = chiTietDonHangRepository;
    }

    @Transactional(readOnly = true)
    public InventoryDetailDTO scanBarcode(String isbn) {
        // 1. Tìm thông tin trong kho
        KhoHang kho = khoHangRepository.findByIsbn(isbn)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hàng trong kho với mã Barcode (ISBN): " + isbn));

        // 2. Kích hoạt màng lọc bảo vệ: Kiểm tra xem đây có phải là Sách Vật Lý không
        if (!(kho.getSach() instanceof SachVatLy)) {
            throw new RuntimeException("Cảnh báo: Mã Barcode " + isbn + " thuộc về Sách Điện Tử (E-book). Sách điện tử không tồn tại trong kho vật lý!");
        }

        SachVatLy sachVatLy = (SachVatLy) kho.getSach();

        // 3. Map sang DTO trả về (Sử dụng đúng hàm getTenSach() từ file Entity của bạn)
        return new InventoryDetailDTO(
                sachVatLy.getIsbn(),
                sachVatLy.getTenSach(),
                kho.getSoLuongTon(),
                kho.getViTriKe()
        );
    }
    @Transactional(readOnly = true)
    public List<LowStockAlertDTO> getLowStockAlerts() {
        // Lấy danh sách từ DB
        List<KhoHang> lowStockItems = khoHangRepository.findLowStockItems();

        // Map danh sách Entity sang danh sách DTO
        return lowStockItems.stream()
                .map(kho -> new LowStockAlertDTO(
                        kho.getSach().getIsbn(),
                        kho.getSach().getTenSach(), // Lấy chuẩn tenSach từ Entity Sach
                        kho.getSoLuongTon(),
                        kho.getNguongBaoDong()
                ))
                .collect(Collectors.toList());
    }
    @Transactional // Rất quan trọng: Có lỗi là tự động Rollback không lưu gì cả!
    public ImportResponseDto nhapKhoHieuQua(ImportRequestDto request) {

        // 1. Kiểm tra đầu vào: Nhà cung cấp và Nhân viên
        NhaCungCap ncc = nhaCungCapRepository.findById(request.maNcc())
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy Nhà Cung Cấp với ID: " + request.maNcc()));

        NhanVien nv = nhanVienRepository.findById(request.maNhanVien())
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy Nhân Viên với ID: " + request.maNhanVien()));

        // 2. Tính toán tổng tiền trước
        BigDecimal tongTien = request.chiTietList().stream()
                .map(item -> item.donGiaNhap().multiply(BigDecimal.valueOf(item.soLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Khởi tạo Phiếu Nhập
        String maPhieuNhapMoi = "PN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        PhieuNhap phieuNhap = new PhieuNhap();
        phieuNhap.setMaPhieuNhap(maPhieuNhapMoi);
        phieuNhap.setNhaCungCap(ncc);
        phieuNhap.setNhanVien(nv);
        phieuNhap.setNgayNhap(LocalDateTime.now());
        phieuNhap.setTongTien(tongTien);

        // Lưu bảng cha trước để lấy khóa ngoại
        phieuNhapRepository.save(phieuNhap);

        // 4. Duyệt qua từng cuốn sách được quét để lưu chi tiết và cộng kho
        for (ChiTietImportRequest item : request.chiTietList()) {

            // 4.1. Lấy thông tin sách & Chặn sách điện tử
            Sach sach = sachRepository.findById(item.isbn())
                    .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy sách với mã ISBN: " + item.isbn()));

            if (!(sach instanceof SachVatLy)) {
                throw new RuntimeException("Lỗi logic: Sách " + item.isbn() + " (" + sach.getTenSach() + ") là E-Book, không thể nhập vào kho vật lý!");
            }

            // 4.2. Lưu chi tiết phiếu nhập với Khóa phức hợp
            ChiTietPhieuNhapId idChiTiet = new ChiTietPhieuNhapId(maPhieuNhapMoi, item.isbn());

            ChiTietPhieuNhap chiTiet = new ChiTietPhieuNhap();
            chiTiet.setId(idChiTiet);
            chiTiet.setPhieuNhap(phieuNhap);
            chiTiet.setSach(sach);
            chiTiet.setSoLuong(item.soLuong());
            chiTiet.setDonGiaNhap(item.donGiaNhap());

            chiTietPhieuNhapRepository.save(chiTiet);

            // 4.3. Cộng dồn số lượng tồn vào kho
            KhoHang kho = khoHangRepository.findByIsbn(item.isbn())
                    .orElseThrow(() -> new RuntimeException("Lỗi: Sách " + item.isbn() + " chưa được khởi tạo không gian trong kho hàng!"));

            kho.setSoLuongTon(kho.getSoLuongTon() + item.soLuong());
            khoHangRepository.save(kho);
        }

        // 5. Trả về DTO kết quả
        return new ImportResponseDto(maPhieuNhapMoi, tongTien, "Nhập kho thành công! Đã cộng tồn kho.");
    }
    @Transactional
    public String xuatKhoTuDong(ExportRequestDto request) {
        // 1. Lấy mã đơn hàng từ DTO (DTO của bạn đã là String nên không cần convert nữa)
        String maDHStr = request.maDonHang();

        // 2. Tìm đơn hàng
        DonHang donHang = donHangRepository.findById(maDHStr)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + maDHStr));

        // 3. Kiểm tra nếu đã xuất kho rồi (Dùng đúng hàm trong Repository)
        if (phieuXuatRepository.existsByDonHang(donHang)) {
            throw new RuntimeException("Đơn hàng này đã được xuất kho trước đó!");
        }

        // 4. LẤY DANH SÁCH CHI TIẾT ĐƠN HÀNG (Đây là bước bạn bị thiếu)
        List<ChiTietDonHang> danhSachChiTiet = chiTietDonHangRepository.findByDonHang(donHang);
        if (danhSachChiTiet.isEmpty()) {
            throw new RuntimeException("Lỗi: Đơn hàng không có sản phẩm nào!");
        }

        // 5. Quét từng mặt hàng trong đơn để trừ kho
        for (ChiTietDonHang chiTiet : danhSachChiTiet) {
            Sach sach = chiTiet.getSach();

            // Bỏ qua Sách Điện Tử
            if (sach instanceof SachDienTu) {
                continue;
            }

            // Tìm sách vật lý trong kho
            KhoHang kho = khoHangRepository.findByIsbn(sach.getIsbn())
                    .orElseThrow(() -> new RuntimeException("Lỗi: Sách " + sach.getIsbn() + " chưa có cấu hình trong kho!"));

            // Kiểm tra tồn kho
            if (kho.getSoLuongTon() < chiTiet.getSoLuong()) {
                throw new RuntimeException("Lỗi: Sách [" + sach.getTenSach() + "] không đủ tồn kho để xuất!");
            }

            // Tiến hành trừ kho
            kho.setSoLuongTon(kho.getSoLuongTon() - chiTiet.getSoLuong());
            khoHangRepository.save(kho);
        }

        // 6. Tạo Phiếu Xuất và TỰ SINH MÃ (Fix lỗi NULL ma_phieuxuat)
        PhieuXuat phieuXuat = new PhieuXuat();

        // Sinh mã ngẫu nhiên (Ví dụ: PX-171306... )
        String maPX = "PX-" + (System.currentTimeMillis() % 10000000);
        phieuXuat.setMaPhieuXuat(maPX);
        phieuXuat.setDonHang(donHang);
        phieuXuat.setNgayXuat(LocalDateTime.now());

        // Nếu Entity PhieuXuat của bạn có trường nguoiXuat thì uncomment dòng dưới:
        // phieuXuat.setNguoiXuat(request.nguoiXuat());

        phieuXuatRepository.save(phieuXuat);

        // 7. Cập nhật trạng thái đơn hàng sang 'CHO_LAY_HANG'
        donHang.setTrangThai("CHO_LAY_HANG");
        donHangRepository.save(donHang);

        return "Xuất kho thành công cho đơn hàng " + maDHStr + ". Mã phiếu: " + maPX;
    }
}