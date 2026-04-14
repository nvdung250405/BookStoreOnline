package com.bookstore.dto;

import java.math.BigDecimal;

public record ImportResponseDto(
        String maPhieuNhap,
        BigDecimal tongTien,
        String message
) {}