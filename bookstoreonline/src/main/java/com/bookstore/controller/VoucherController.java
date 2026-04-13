package com.bookstore.controller;

import com.bookstore.dto.VoucherDTO;
import com.bookstore.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@CrossOrigin("*")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @GetMapping
    public ResponseEntity<List<VoucherDTO>> getAllVouchers() {
        return ResponseEntity.ok(voucherService.layTatCaVoucher());
    }

    @GetMapping("/{code}")
    public ResponseEntity<VoucherDTO> getVoucherByCode(@PathVariable String code) {
        return ResponseEntity.ok(voucherService.layVoucherTheoMa(code));
    }

    @PostMapping
    public ResponseEntity<VoucherDTO> createVoucher(@RequestBody VoucherDTO dto) {
        return ResponseEntity.ok(voucherService.luuVoucher(dto));
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<String> deleteVoucher(@PathVariable String code) {
        voucherService.xoaVoucher(code);
        return ResponseEntity.ok("Đã xóa mã giảm giá thành công");
    }
}
