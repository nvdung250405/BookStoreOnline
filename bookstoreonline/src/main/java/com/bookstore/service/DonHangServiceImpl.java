package com.bookstore.service;

import com.bookstore.dto.CheckoutRequest;
import com.bookstore.dto.ChiTietDonHangDTO;
import com.bookstore.dto.DonHangDTO;
import com.bookstore.entity.*;
import com.bookstore.repository.DonHangRepository;
import com.bookstore.repository.ChiTietDonHangRepository;
import com.bookstore.repository.GioHangRepository;
import com.bookstore.repository.KhachHangRepository;
import com.bookstore.repository.VoucherRepository;
import com.bookstore.repository.SachRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DonHangServiceImpl implements DonHangService {

    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;
    private final GioHangRepository gioHangRepository;
    private final KhachHangRepository khachHangRepository;
    private final VoucherRepository voucherRepository;
    private final SachRepository sachRepository;

    public DonHangServiceImpl(DonHangRepository donHangRepository,
                              ChiTietDonHangRepository chiTietDonHangRepository,
                              GioHangRepository gioHangRepository,
                              KhachHangRepository khachHangRepository,
                              VoucherRepository voucherRepository,
                              SachRepository sachRepository) {
        this.donHangRepository = donHangRepository;
        this.chiTietDonHangRepository = chiTietDonHangRepository;
        this.gioHangRepository = gioHangRepository;
        this.khachHangRepository = khachHangRepository;
        this.voucherRepository = voucherRepository;
        this.sachRepository = sachRepository;
    }

    @Override
    public DonHangDTO checkout(String username, CheckoutRequest request) {
        // 1. Get Customer
        KhachHang khachHang = khachHangRepository.findByTaiKhoan_Username(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        // 2. Get Cart Items
        List<GioHang> cartItems = gioHangRepository.findByKhachHang_TaiKhoan_Username(username);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng của bạn đang trống");
        }

        // 3. Calculate total
        BigDecimal tongTienHang = cartItems.stream()
                .map(item -> item.getSach().getGiaNiemYet().multiply(new BigDecimal(item.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Handle Voucher
        Voucher voucher = null;
        BigDecimal giamGia = BigDecimal.ZERO;
        if (request.getMaVoucher() != null && !request.getMaVoucher().isEmpty()) {
            voucher = voucherRepository.findById(request.getMaVoucher())
                    .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));
            
            // Basic condition check
            if (tongTienHang.compareTo(voucher.getDieuKienToiThieu()) < 0) {
                throw new RuntimeException("Đơn hàng chưa đủ điều kiện áp dụng voucher");
            }
            if (voucher.getThoiHan() != null && voucher.getThoiHan().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Voucher đã hết hạn sử dụng");
            }
            giamGia = voucher.getGiaTriGiam();
        }

        BigDecimal phiVanChuyen = new BigDecimal("30000"); // Flat rate for demo
        BigDecimal tongThanhToan = tongTienHang.add(phiVanChuyen).subtract(giamGia);
        if (tongThanhToan.compareTo(BigDecimal.ZERO) < 0) tongThanhToan = BigDecimal.ZERO;

        // 5. Create Order
        String maDonHang = "DH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
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

        // 6. Create Order Details
        for (GioHang item : cartItems) {
            ChiTietDonHang chiTiet = new ChiTietDonHang();
            ChiTietDonHangId id = new ChiTietDonHangId(maDonHang, item.getSach().getIsbn());
            chiTiet.setId(id);
            chiTiet.setDonHang(donHang);
            chiTiet.setSach(item.getSach());
            chiTiet.setSoLuong(item.getSoLuong());
            chiTiet.setGiaBanChot(item.getSach().getGiaNiemYet());
            
            chiTietDonHangRepository.save(chiTiet);
        }

        // 7. Clear Cart
        gioHangRepository.deleteByKhachHang_TaiKhoan_Username(username);

        return getDetail(maDonHang);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonHangDTO> getHistory(String username) {
        return donHangRepository.findByKhachHang_TaiKhoan_UsernameOrderByNgayTaoDesc(username)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DonHangDTO getDetail(String maDonHang) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        
        DonHangDTO dto = toDTO(donHang);
        
        List<ChiTietDonHangDTO> chiTietDTOs = chiTietDonHangRepository.findByDonHang(donHang)
                .stream()
                .map(this::toChiTietDTO)
                .collect(Collectors.toList());
        
        dto.setChiTiet(chiTietDTOs);
        return dto;
    }

    @Override
    public void cancelOrder(String username, String maDonHang) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        
        if (!donHang.getKhachHang().getTaiKhoan().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này");
        }
        
        if (!"MOI".equals(donHang.getTrangThai()) && 
            !"CHO_XAC_NHAN".equals(donHang.getTrangThai()) && 
            !"DA_XAC_NHAN".equals(donHang.getTrangThai())) {
            throw new RuntimeException("Không thể hủy đơn hàng ở trạng thái này");
        }
        
        donHang.setTrangThai("DA_HUY");
        donHangRepository.save(donHang);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonHangDTO> getAllOrders() {
        return donHangRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateStatus(String maDonHang, String status) {
        DonHang donHang = donHangRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        donHang.setTrangThai(status);
        donHangRepository.save(donHang);
    }

    private DonHangDTO toDTO(DonHang entity) {
        DonHangDTO dto = new DonHangDTO();
        dto.setMaDonHang(entity.getMaDonHang());
        dto.setNgayTao(entity.getNgayTao());
        dto.setTongTienHang(entity.getTongTienHang());
        dto.setPhiVanChuyen(entity.getPhiVanChuyen());
        dto.setTongThanhToan(entity.getTongThanhToan());
        dto.setTrangThai(entity.getTrangThai());
        dto.setDiaChiGiaoCuThe(entity.getDiaChiGiaoCuThe());
        dto.setHoTenKhachHang(entity.getKhachHang().getHoTen());
        return dto;
    }

    private ChiTietDonHangDTO toChiTietDTO(ChiTietDonHang entity) {
        ChiTietDonHangDTO dto = new ChiTietDonHangDTO();
        dto.setIsbn(entity.getSach().getIsbn());
        dto.setTenSach(entity.getSach().getTenSach());
        dto.setAnhBia(entity.getSach().getAnhBia());
        dto.setSoLuong(entity.getSoLuong());
        dto.setGiaBan(entity.getGiaBanChot());
        dto.setThanhTien(entity.getGiaBanChot().multiply(new BigDecimal(entity.getSoLuong())));
        return dto;
    }
}
