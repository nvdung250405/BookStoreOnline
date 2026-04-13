package com.bookstore.service;

import com.bookstore.dto.AdminCreateAccountRequest;
import com.bookstore.dto.AdminUserResponseDTO;
import com.bookstore.dto.UserProfileDTO;
import com.bookstore.entity.NhanVien;
import com.bookstore.entity.TaiKhoan;
import com.bookstore.entity.KhachHang;
import com.bookstore.repository.NhanVienRepository;
import com.bookstore.repository.TaiKhoanRepository;
import com.bookstore.repository.KhachHangRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final NhanVienRepository nhanVienRepository;
    private final KhachHangRepository khachHangRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(TaiKhoanRepository taiKhoanRepository, 
                        NhanVienRepository nhanVienRepository,
                        KhachHangRepository khachHangRepository,
                        PasswordEncoder passwordEncoder) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.khachHangRepository = khachHangRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AdminUserResponseDTO createAccount(AdminCreateAccountRequest request) {
        // 1. Kiểm tra tồn tại
        if (taiKhoanRepository.existsById(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' đã tồn tại");
        }

        // 2. Tạo tài khoản mới
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setUsername(request.getUsername());
        taiKhoan.setPassword(passwordEncoder.encode(request.getPassword()));
        taiKhoan.setRole(request.getRole().toUpperCase());
        taiKhoan.setTrangThai(true);
        taiKhoanRepository.save(taiKhoan);

        // 3. Tự động xác định bộ phận dựa trên vai trò
        String boPhan;
        String role = request.getRole().toUpperCase();
        if ("ADMIN".equals(role)) {
            boPhan = "QUAN_LY";
        } else if ("STOREKEEPER".equals(role)) {
            boPhan = "KHO";
        } else {
            boPhan = "BAN_HANG"; // Mặc định cho STAFF
        }

        // 4. Tạo hồ sơ chờ cho nhân sự mới
        NhanVien nhanVien = new NhanVien();
        nhanVien.setTaiKhoan(taiKhoan);
        nhanVien.setHoTen("NHÂN VIÊN MỚI");
        nhanVien.setBoPhan(boPhan);
        nhanVienRepository.save(nhanVien);

        // 5. Trả về DTO thông tin quản trị
        AdminUserResponseDTO response = new AdminUserResponseDTO();
        response.setUsername(taiKhoan.getUsername());
        response.setRole(taiKhoan.getRole());
        response.setTrangThai(taiKhoan.getTrangThai());
        response.setNgayTao(taiKhoan.getNgayTao());
        response.setBoPhan(nhanVien.getBoPhan());
        return response;
    }


    @Transactional(readOnly = true)
    public java.util.List<UserProfileDTO> getAllUsers() {
        return taiKhoanRepository.findAll().stream()
            .map(this::mapToUserProfileDTO)
            .collect(java.util.stream.Collectors.toList());
    }

    private UserProfileDTO mapToUserProfileDTO(TaiKhoan taiKhoan) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setUsername(taiKhoan.getUsername());
        dto.setRole(taiKhoan.getRole());

        if ("CUSTOMER".equalsIgnoreCase(taiKhoan.getRole())) {
            khachHangRepository.findByTaiKhoan_Username(taiKhoan.getUsername()).ifPresent(kh -> {
                dto.setHoTen(kh.getHoTen());
                dto.setSdt(kh.getSdt());
                dto.setDiaChiGiaoHang(kh.getDiaChiGiaoHang());
                dto.setDiemTichLuy(kh.getDiemTichLuy());
            });
        } else {
            // Bao gồm cả ADMIN, STAFF, STOREKEEPER
            nhanVienRepository.findByTaiKhoan_Username(taiKhoan.getUsername()).ifPresent(nv -> {
                dto.setHoTen(nv.getHoTen());
                dto.setSdt(nv.getSdt());
                dto.setBoPhan(nv.getBoPhan());
            });
        }
        return dto;
    }

    @Transactional
    public void updateUserStatus(String username, boolean status) {
        // 1. Kiểm tra self-lock
        String currentAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        if (currentAdmin.equals(username)) {
            throw new IllegalArgumentException("Bạn không thể tự khóa hoặc mở khóa tài khoản của chính mình");
        }

        // 2. Cập nhật trạng thái
        TaiKhoan taiKhoan = taiKhoanRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản: " + username));
        
        taiKhoan.setTrangThai(status);
        taiKhoanRepository.save(taiKhoan);
    }

    @Transactional
    public void updateUserRole(String username, String role) {
        role = role.toUpperCase();
        if (!"ADMIN".equals(role) && !"STAFF".equals(role) && !"STOREKEEPER".equals(role)) {
            throw new IllegalArgumentException("Vai trò không hợp lệ. Chỉ chấp nhận ADMIN, STAFF, STOREKEEPER");
        }

        // 1. Kiểm tra self-role change (Tùy chọn bảo mật)
        String currentAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        if (currentAdmin.equals(username)) {
            throw new IllegalArgumentException("Bạn không thể tự thay đổi quyền hạn của chính mình");
        }

        // 2. Cập nhật Role trong TaiKhoan
        TaiKhoan taiKhoan = taiKhoanRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản: " + username));
        
        // Chặn đổi Khách hàng sang nhân viên ở bước này như đã thỏa thuận
        if ("CUSTOMER".equals(taiKhoan.getRole())) {
            throw new IllegalArgumentException("Không thể đổi quyền của Khách hàng trực tiếp tại đây");
        }

        taiKhoan.setRole(role);
        taiKhoanRepository.save(taiKhoan);

        // 3. Đồng bộ hóa Bộ phận trong NhanVien
        String boPhan;
        if ("ADMIN".equals(role)) boPhan = "QUAN_LY";
        else if ("STOREKEEPER".equals(role)) boPhan = "KHO";
        else boPhan = "BAN_HANG";

        nhanVienRepository.findByTaiKhoan_Username(username).ifPresent(nv -> {
            nv.setBoPhan(boPhan);
            nhanVienRepository.save(nv);
        });
    }
}
