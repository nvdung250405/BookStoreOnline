package com.bookstore.controller;

import com.bookstore.dto.DanhGiaDTO;
import com.bookstore.service.DanhGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin("*")
public class ReviewController {

    @Autowired
    private DanhGiaService danhGiaService;

    // Lấy tất cả đánh giá của một cuốn sách
    @GetMapping("/book/{isbn}")
    public ResponseEntity<List<DanhGiaDTO>> getReviewsByBook(@PathVariable String isbn) {
        return ResponseEntity.ok(danhGiaService.layDanhGiaTheoSach(isbn));
    }

    // Gửi đánh giá mới
    @PostMapping("/submit")
    public ResponseEntity<String> submitReview(
            @RequestParam String username,
            @RequestParam String isbn,
            @RequestParam Integer diem,
            @RequestParam String nhanXet) {
        danhGiaService.guiDanhGia(username, isbn, diem, nhanXet);
        return ResponseEntity.ok("Cảm ơn bạn đã gửi đánh giá!");
    }
}
