package com.bookstore.config;

import com.bookstore.entity.NhanVien;
import com.bookstore.entity.TaiKhoan;
import com.bookstore.repository.NhanVienRepository;
import com.bookstore.repository.TaiKhoanRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TaiKhoanRepository taiKhoanRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(TaiKhoanRepository taiKhoanRepository, 
                           NhanVienRepository nhanVienRepository, 
                           PasswordEncoder passwordEncoder) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Khởi tạo hoặc cập nhật tài khoản ADMIN gốc
        TaiKhoan adminAccount = taiKhoanRepository.findById("admin").orElse(null);
        
        if (adminAccount == null) {
            System.out.println(">>> Creating Root Admin account...");
            adminAccount = new TaiKhoan();
            adminAccount.setUsername("admin");
            adminAccount.setRole("ADMIN");
            adminAccount.setTrangThai(true);
        } else {
            System.out.println(">>> Resetting Root Admin password...");
        }

        // Luôn cập nhật mật khẩu về admin123 để chắc chắn đăng nhập được
        adminAccount.setPassword(passwordEncoder.encode("admin123"));
        taiKhoanRepository.save(adminAccount);

        if (nhanVienRepository.findByTaiKhoan_Username("admin").isEmpty()) {
             System.out.println(">>> Creating Admin profile...");
             NhanVien adminProfile = new NhanVien();
             adminProfile.setTaiKhoan(adminAccount);
             adminProfile.setHoTen("Hệ thống (Admin)");
             adminProfile.setSdt("0000000000");
             adminProfile.setBoPhan("QUAN_LY");
             nhanVienRepository.save(adminProfile);
        }
        
        System.out.println(">>> Root Admin ready: admin / admin123");
    }
}
