package com.bookstore.dto;

import java.util.List;

public record TrackingResponseDto(
        String maDonHang,
        String donViVanChuyen,
        String maVanDon,
        String trangThaiHienTai,
        List<ChiTietTracking> lichSuTrangThai
) {
    // Record con (Nested Record) đại diện cho từng mốc thời gian giao hàng
    public record ChiTietTracking(
            String thoiGian,
            String trangThai,
            String viTri
    ) {}
}