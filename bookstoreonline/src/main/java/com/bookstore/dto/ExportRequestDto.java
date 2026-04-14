package com.bookstore.dto;

public record ExportRequestDto(
        String maDonHang,   // Mã của đơn hàng cần xuất
        String nguoiXuat  // Tên hoặc Username của nhân viên kho
) {}