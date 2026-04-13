package com.bookstore.service;

import com.bookstore.dto.LoginRequest;
import com.bookstore.dto.LoginResponse;
import com.bookstore.entity.TaiKhoan;
import com.bookstore.repository.TaiKhoanRepository;
import com.bookstore.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(TaiKhoanRepository taiKhoanRepository, 
                        PasswordEncoder passwordEncoder, 
                        AuthenticationManager authenticationManager, 
                        JwtTokenProvider tokenProvider) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        // 1. Xác thực bằng Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 2. Lưu vào Security Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Tạo JWT Token
        String jwt = tokenProvider.generateToken(authentication);

        // 4. Lấy thông tin tài khoản để trả về
        TaiKhoan taiKhoan = taiKhoanRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại"));

        return new LoginResponse(taiKhoan.getUsername(), taiKhoan.getRole(), jwt);
    }

    @Transactional
    public LoginResponse register(LoginRequest request) {
        // 1. Kiểm tra username đã tồn tại chưa
        if (taiKhoanRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }

        // 2. Tạo tài khoản mới với vai trò Khách hàng (CUSTOMER)
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setUsername(request.getUsername());
        // MÃ HÓA MẬT KHẨU TRƯỚC KHI LƯU
        taiKhoan.setPassword(passwordEncoder.encode(request.getPassword()));
        taiKhoan.setRole("CUSTOMER");
        taiKhoan.setTrangThai(true);

        // 3. Lưu vào database
        taiKhoanRepository.save(taiKhoan);

        // 4. Trả về thông tin đăng ký (không trả về token ở bước này, hoặc có thể login luôn)
        return new LoginResponse(taiKhoan.getUsername(), taiKhoan.getRole(), null);
    }
}
