package com.bookstore.service;

import com.bookstore.dto.ChangePasswordRequest;
import com.bookstore.dto.CustomerProfileRequest;
import com.bookstore.dto.StaffProfileRequest;
import com.bookstore.dto.UserProfileDTO;
import com.bookstore.entity.KhachHang;
import com.bookstore.entity.NhanVien;
import com.bookstore.entity.TaiKhoan;
import com.bookstore.repository.KhachHangRepository;
import com.bookstore.repository.NhanVienRepository;
import com.bookstore.repository.TaiKhoanRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(TaiKhoanRepository taiKhoanRepository, 
                       KhachHangRepository khachHangRepository, 
                       NhanVienRepository nhanVienRepository,
                       PasswordEncoder passwordEncoder) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.khachHangRepository = khachHangRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getCurrentUserProfile() {
        // 1. Lấy username từ SecurityContext
        String username = getCurrentUsername();

        // 2. Tìm tài khoản
        TaiKhoan taiKhoan = taiKhoanRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tài khoản"));

        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setUsername(taiKhoan.getUsername());
        profileDTO.setRole(taiKhoan.getRole());

        // 3. Tìm thông tin chi tiết dựa trên Role
        if ("CUSTOMER".equalsIgnoreCase(taiKhoan.getRole())) {
            KhachHang khachHang = khachHangRepository.findByTaiKhoanUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin hồ sơ khách hàng"));
            profileDTO.setHoTen(khachHang.getHoTen());
            profileDTO.setSdt(khachHang.getSdt());
            profileDTO.setDiaChiGiaoHang(khachHang.getDiaChiGiaoHang());
            profileDTO.setDiemTichLuy(khachHang.getDiemTichLuy());
        } else {
            // ADMIN hoặc STAFF
            NhanVien nhanVien = nhanVienRepository.findByTaiKhoanUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin hồ sơ nhân viên"));
            profileDTO.setHoTen(nhanVien.getHoTen());
            profileDTO.setSdt(nhanVien.getSdt());
            profileDTO.setBoPhan(nhanVien.getBoPhan());
        }

        return profileDTO;
    }

    @Transactional
    public UserProfileDTO updateCustomerProfile(CustomerProfileRequest request) {
        String username = getCurrentUsername();
        KhachHang khachHang = khachHangRepository.findByTaiKhoanUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ khách hàng"));

        khachHang.setHoTen(request.getHoTen());
        khachHang.setSdt(request.getSdt());
        khachHang.setDiaChiGiaoHang(request.getDiaChiGiaoHang());
        khachHangRepository.save(khachHang);

        return getCurrentUserProfile();
    }

    @Transactional
    public UserProfileDTO updateStaffProfile(StaffProfileRequest request) {
        String username = getCurrentUsername();
        NhanVien nhanVien = nhanVienRepository.findByTaiKhoanUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ nhân viên"));

        nhanVien.setHoTen(request.getHoTen());
        nhanVien.setSdt(request.getSdt());
        // Khóa: Tuyệt đối không cập nhật bộ phận tại đây.
        nhanVienRepository.save(nhanVien);

        return getCurrentUserProfile();
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String username = getCurrentUsername();
        TaiKhoan taiKhoan = taiKhoanRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        // 1. Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(request.getOldPassword(), taiKhoan.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác");
        }

        // 2. Kiểm tra mật khẩu mới và xác nhận mật khẩu
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Xác nhận mật khẩu mới không khớp");
        }

        // 3. Mã hóa và lưu mật khẩu mới
        taiKhoan.setPassword(passwordEncoder.encode(request.getNewPassword()));
        taiKhoanRepository.save(taiKhoan);
    }

    @Transactional
    public UserProfileDTO createCustomerProfile(CustomerProfileRequest request) {
        String username = getCurrentUsername();
        TaiKhoan taiKhoan = taiKhoanRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        if (!"CUSTOMER".equalsIgnoreCase(taiKhoan.getRole())) {
            throw new IllegalArgumentException("Tài khoản không phải là khách hàng");
        }

        if (khachHangRepository.findByTaiKhoanUsername(username).isPresent()) {
            throw new IllegalArgumentException("Hồ sơ khách hàng đã tồn tại");
        }

        KhachHang khachHang = new KhachHang();
        khachHang.setTaiKhoan(taiKhoan);
        khachHang.setHoTen(request.getHoTen());
        khachHang.setSdt(request.getSdt());
        khachHang.setDiaChiGiaoHang(request.getDiaChiGiaoHang());
        khachHangRepository.save(khachHang);

        return getCurrentUserProfile();
    }

}
