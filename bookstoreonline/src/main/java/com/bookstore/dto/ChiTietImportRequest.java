package com.bookstore.dto;

import java.math.BigDecimal;

public record ChiTietImportRequest(
        String isbn,
        Integer soLuong,
        BigDecimal donGiaNhap
) {}