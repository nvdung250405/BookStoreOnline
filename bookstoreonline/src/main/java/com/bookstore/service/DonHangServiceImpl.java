package com.bookstore.service;

import com.bookstore.dto.CheckoutRequest;
import com.bookstore.dto.ChiTietDonHangDTO;
import com.bookstore.dto.DonHangResponseDTO;
import com.bookstore.entity.*;
import com.bookstore.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class DonHangServiceImpl implements DonHangService {

    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;
    private final KhachHangRepository khachHangRepository;
    private final GioHangRepository gioHangRepository;
    private final VoucherRepository voucherRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final AuditLogRepository auditLogRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    public DonHangServiceImpl(DonHangRepository donHangRepository,
                             ChiTietDonHangRepository chiTietDonHangRepository,
                             KhachHangRepository khachHangRepository,
                             GioHangRepository gioHangRepository,
                             VoucherRepository voucherRepository,
                             ThanhToanRepository thanhToanRepository,
                             AuditLogRepository auditLogRepository,
                             TaiKhoanRepository taiKhoanRepository) {
        this.donHangRepository = donHangRepository;
        this.chiTietDonHangRepository = chiTietDonHangRepository;
        this.khachHangRepository = khachHangRepository;
        this.gioHangRepository = gioHangRepository;
        this.voucherRepository = voucherRepository;
        this.thanhToanRepository = thanhToanRepository;
        this.auditLogRepository = auditLogRepository;
        this.taiKhoanRepository = taiKhoanRepository;
    }

    @Override
    @Transactional
    public DonHangResponseDTO checkout(CheckoutRequest request) {
        // 1. Tìm khách hàng
        KhachHang khachHang = khachHangRepository.findByTaiKhoan_Username(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại: " + request.getUsername()));

        // 2. Lấy các item trong giỏ hàng
        List<GioHang> gioHangList = gioHangRepository.findByKhachHang(khachHang);
        if (gioHangList.isEmpty()) {
            throw new RuntimeException("Giỏ hàng của bạn đang trống!");
        }

        // 3. Tính toán tổng tiền hàng
        BigDecimal tongTienHang = gioHangList.stream()
                .map(item -> item.getSach().getGiaNiemYet().multiply(BigDecimal.valueOf(item.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Xử lý Voucher nếu có
        Voucher voucher = null;
        BigDecimal reduction = BigDecimal.ZERO;
        if (request.getMaVoucher() != null && !request.getMaVoucher().isEmpty()) {
            voucher = voucherRepository.findById(request.getMaVoucher())
                    .orElseThrow(() -> new RuntimeException("Mã voucher không hợp lệ"));
            
            // Kiểm tra điều kiện và thời hạn
            if (voucher.getThoiHan().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Voucher đã hết hạn");
            }
            if (tongTienHang.compareTo(voucher.getDieuKienToiThieu()) < 0) {
                throw new RuntimeException("Đơn hàng chưa đạt điều kiện tối thiểu để dùng voucher");
            }
            reduction = voucher.getGiaTriGiam();
        }

        // 5. Tính phí vận chuyển (Ví dụ mặc định)
        BigDecimal phiVanChuyen = (tongTienHang.compareTo(new BigDecimal("200000")) >= 0) ? BigDecimal.ZERO : new BigDecimal("30000");

        // 6. Tính tổng thanh toán cuối cùng
        BigDecimal tongThanhToan = tongTienHang.add(phiVanChuyen).subtract(reduction);
        if (tongThanhToan.compareTo(BigDecimal.ZERO) < 0) tongThanhToan = BigDecimal.ZERO;

        // 7. Khởi tạo đơn hàng
        String maDonHang = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        DonHang donHang = new DonHang();
        donHang.setMaDonHang(maDonHang);
        donHang.setKhachHang(khachHang);
        donHang.setVoucher(voucher);
        donHang.setNgayTao(LocalDateTime.now());
        donHang.setTongTienHang(tongTienHang);
        donHang.setPhiVanChuyen(phiVanChuyen);
        donHang.setTongThanhToan(tongThanhToan);
        donHang.setTrangThai("CHO_XAC_NHAN");
        donHang.setDiaChiGiaoCuThe(request.getDiaChiGiaoHang());

        donHangRepository.save(donHang);

        // 8. Lưu Chi tiết đơn hàng và Clear giỏ hàng
        List<ChiTietDonHangDTO> chiTietDTOs = new ArrayList<>();
        for (GioHang gh : gioHangList) {
            ChiTietDonHang chiTiet = new ChiTietDonHang();
            chiTiet.setId(new ChiTietDonHangId(maDonHang, gh.getSach().getIsbn()));
            chiTiet.setDonHang(donHang);
            chiTiet.setSach(gh.getSach());
            chiTiet.setSoLuong(gh.getSoLuong());
            chiTiet.setGiaBanChot(gh.getSach().getGiaNiemYet());
            
            chiTietDonHangRepository.save(chiTiet);
            
            // Map sang DTO
            ChiTietDonHangDTO dto = new ChiTietDonHangDTO();
            dto.setIsbn(gh.getSach().getIsbn());
            dto.setTenSach(gh.getSach().getTenSach());
            dto.setSoLuong(gh.getSoLuong());
            dto.setGiaBanChot(gh.getSach().getGiaNiemYet());
            chiTietDTOs.add(dto);
        }

        // Dọn sạch giỏ hàng
        gioHangRepository.deleteAll(gioHangList);

        // 9. Nếu chọn VNPay thì khởi tạo bản ghi ThanhToan tạm (PENDING)
        if ("VNPAY".equalsIgnoreCase(request.getPhuongThucThanhToan())) {
            ThanhToan tt = new ThanhToan();
            tt.setMaThanhToan("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            tt.setDonHang(donHang);
            tt.setPhuongThuc("VNPAY");
            tt.setTrangThai("PENDING");
            thanhToanRepository.save(tt);
            
            donHang.setTrangThai("CHO_THANH_TOAN");
            donHangRepository.save(donHang);
        } else {
            // COD
            ThanhToan tt = new ThanhToan();
            tt.setMaThanhToan("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            tt.setDonHang(donHang);
            tt.setPhuongThuc("COD");
            tt.setTrangThai("PENDING");
            thanhToanRepository.save(tt);
        }

        return mapToResponseDTO(donHang, chiTietDTOs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonHangResponseDTO> layLichSuDonHang(String username) {
        return donHangRepository.findAllByKhachHang_TaiKhoan_UsernameOrderByNgayTaoDesc(username)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DonHangResponseDTO layChiTietDonHang(String maDonHang) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + maDonHang));
        return mapToDTO(donHang);
    }

    @Override
    @Transactional
    public void huyDonHang(String maDonHang) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + maDonHang));
        
        if (!"CHO_XAC_NHAN".equals(donHang.getTrangThai()) && !"CHO_THANH_TOAN".equals(donHang.getTrangThai())) {
            throw new RuntimeException("Không thể hủy đơn hàng này ở trạng thái: " + donHang.getTrangThai());
        }
        
        donHang.setTrangThai("DA_HUY");
        donHangRepository.save(donHang);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonHangResponseDTO> layTatCaDonHang() {
        return donHangRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void capNhatTrangThai(String maDonHang, String trangThai) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + maDonHang));
        
        String oldStatus = donHang.getTrangThai();
        donHang.setTrangThai(trangThai);
        donHangRepository.save(donHang);

        // Ghi log thay đổi
        try {
            String currentUser = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            TaiKhoan tk = taiKhoanRepository.findByUsername(currentUser).orElse(null);
            if (tk != null) {
                AuditLog log = new AuditLog();
                log.setTaiKhoan(tk);
                log.setHanhDong("CAP_NHAT_TRANG_THAI_DON_HANG");
                log.setChiTiet("Đơn hàng " + maDonHang + ": " + oldStatus + " -> " + trangThai);
                auditLogRepository.save(log);
            }
        } catch (Exception e) {
            // Log warning but don't fail the transaction
        }
    }

    private DonHangResponseDTO mapToDTO(DonHang donHang) {
        List<ChiTietDonHangDTO> chiTietList = chiTietDonHangRepository.findByDonHang(donHang)
                .stream()
                .map(ct -> {
                    ChiTietDonHangDTO dto = new ChiTietDonHangDTO();
                    dto.setIsbn(ct.getSach().getIsbn());
                    dto.setTenSach(ct.getSach().getTenSach());
                    dto.setSoLuong(ct.getSoLuong());
                    dto.setGiaBanChot(ct.getGiaBanChot());
                    return dto;
                })
                .collect(Collectors.toList());
        
        return mapToResponseDTO(donHang, chiTietList);
    }

    private DonHangResponseDTO mapToResponseDTO(DonHang donHang, List<ChiTietDonHangDTO> chiTietList) {
        DonHangResponseDTO res = new DonHangResponseDTO();
        res.setMaDonHang(donHang.getMaDonHang());
        res.setUsername(donHang.getKhachHang().getTaiKhoan().getUsername());
        res.setHoTenKhachHang(donHang.getKhachHang().getHoTen());
        res.setNgayTao(donHang.getNgayTao());
        res.setTongTienHang(donHang.getTongTienHang());
        res.setPhiVanChuyen(donHang.getPhiVanChuyen());
        res.setTongThanhToan(donHang.getTongThanhToan());
        res.setTrangThai(donHang.getTrangThai());
        res.setDiaChiGiaoHang(donHang.getDiaChiGiaoCuThe());
        res.setMaVoucher(donHang.getVoucher() != null ? donHang.getVoucher().getMaVoucher() : null);
        res.setChiTietDonHangs(chiTietList);
        return res;
    }
}
