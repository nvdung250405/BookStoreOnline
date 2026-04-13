package com.bookstore.controller;

import com.bookstore.dto.GioHangDTO;
import com.bookstore.service.GioHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin("*") // Cho phép gọi từ Frontend bất kỳ (React/Vue/Angular)
public class CartController {

    @Autowired
    private GioHangService gioHangService;

    // Lấy danh sách sản phẩm trong giỏ theo username
    @GetMapping("/{username}")
    public ResponseEntity<List<GioHangDTO>> getCart(@PathVariable String username) {
        return ResponseEntity.ok(gioHangService.layGioHang(username));
    }

    // Thêm sách vào giỏ
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(
            @RequestParam String username,
            @RequestParam String isbn,
            @RequestParam Integer soLuong) {
        gioHangService.themVaoGioHang(username, isbn, soLuong);
        return ResponseEntity.ok("Đã thêm vào giỏ hàng thành công");
    }

    // Cập nhật số lượng sản phẩm trong giỏ
    @PutMapping("/update")
    public ResponseEntity<String> updateQuantity(
            @RequestParam Long id,
            @RequestParam Integer soLuong) {
        gioHangService.capNhatSoLuong(id, soLuong);
        return ResponseEntity.ok("Đã cập nhật số lượng");
    }

    // Xóa một món đồ khỏi giỏ
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<String> removeItem(@PathVariable Long id) {
        gioHangService.xoaKhoiGioHang(id);
        return ResponseEntity.ok("Đã xóa khỏi giỏ hàng");
    }

    // Làm trống toàn bộ giỏ hàng
    @DeleteMapping("/clear/{username}")
    public ResponseEntity<String> clearCart(@PathVariable String username) {
        gioHangService.lamTrongGioHang(username);
        return ResponseEntity.ok("Đã dọn sạch giỏ hàng");
    }
}
