package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.VoucherDTO;
import com.bookstore.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@CrossOrigin("*")
@Tag(name = "Voucher Management", description = "Quản lý mã giảm giá")
public class VoucherController {

    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping
    @Operation(summary = "Danh sách Voucher", description = "Lấy toàn bộ danh sách các mã giảm giá hiện có trên hệ thống")
    public ApiResponse<List<VoucherDTO>> getAllVouchers() {
        return ApiResponse.success(voucherService.getAllVouchers());
    }

    @GetMapping("/{code}")
    @Operation(summary = "Chi tiết Voucher", description = "Kiểm tra thông tin chi tiết của một mã giảm giá qua mã (Code)")
    public ApiResponse<VoucherDTO> getVoucherByCode(@PathVariable String code) {
        return ApiResponse.success(voucherService.getVoucherByCode(code));
    }

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo mới Voucher (ADMIN)", description = "Tạo một mã giảm giá mới.")
    public ApiResponse<VoucherDTO> createVoucher(@RequestBody VoucherDTO dto) {
        return ApiResponse.created(voucherService.saveVoucher(dto));
    }

    @PutMapping("/{code}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật Voucher (ADMIN)", description = "Cập nhật thông tin mã giảm giá.")
    public ApiResponse<VoucherDTO> updateVoucher(@PathVariable String code, @RequestBody VoucherDTO dto) {
        dto.setVoucherCode(code);
        return ApiResponse.success(voucherService.saveVoucher(dto));
    }

    @DeleteMapping("/{code}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa Voucher (ADMIN)", description = "Xóa mã giảm giá khỏi hệ thống.")
    public ApiResponse<String> deleteVoucher(@PathVariable String code) {
        voucherService.deleteVoucher(code);
        return ApiResponse.success("Đã xóa mã giảm giá thành công", null);
    }
}
