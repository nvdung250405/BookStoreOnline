package com.bookstore.controller;

import com.bookstore.dto.HoTroDTO;
import com.bookstore.service.HoTroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support")
@CrossOrigin("*")
public class SupportController {

    @Autowired
    private HoTroService hoTroService;

    // Lấy tất cả yêu cầu (Dành cho Admin/Staff)
    @GetMapping
    public ResponseEntity<List<HoTroDTO>> getAllRequests() {
        return ResponseEntity.ok(hoTroService.layTatCaYeuCau());
    }

    // Lấy yêu cầu của một khách hàng cụ thể
    @GetMapping("/user/{username}")
    public ResponseEntity<List<HoTroDTO>> getRequestsByUser(@PathVariable String username) {
        return ResponseEntity.ok(hoTroService.layChoKhachHang(username));
    }

    // Gửi yêu cầu hỗ trợ mới
    @PostMapping
    public ResponseEntity<String> createRequest(
            @RequestParam String username,
            @RequestParam String tieuDe,
            @RequestParam String noiDung) {
        hoTroService.guiYeuCau(username, tieuDe, noiDung);
        return ResponseEntity.ok("Yêu cầu của bạn đã được tiếp nhận");
    }

    // Cập nhật trạng thái yêu cầu (Dành cho Admin/Staff)
    @PutMapping("/{id}")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long id,
            @RequestParam String trangThai) {
        hoTroService.capNhatTrangThai(id, trangThai);
        return ResponseEntity.ok("Đã cập nhật trạng thái hồ sơ</strong>");
    }
}
