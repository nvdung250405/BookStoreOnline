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
        // Khởi tạo tài khoản ADMIN gốc nếu chưa có
        if (!taiKhoanRepository.existsById("admin")) {
            System.out.println(">>> Initializing Root Admin account...");
            
            TaiKhoan adminAccount = new TaiKhoan();
            adminAccount.setUsername("admin");
            adminAccount.setPassword(passwordEncoder.encode("admin123"));
            adminAccount.setRole("ADMIN");
            adminAccount.setTrangThai(true);
            taiKhoanRepository.save(adminAccount);

            NhanVien adminProfile = new NhanVien();
            adminProfile.setTaiKhoan(adminAccount);
            adminProfile.setHoTen("Hệ thống (Admin)");
            adminProfile.setSdt("0000000000");
            adminProfile.setBoPhan("QUAN_LY");
            nhanVienRepository.save(adminProfile);
            
            System.out.println(">>> Root Admin initialized: admin / admin123");
        }
    }
}
