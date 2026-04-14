package com.bookstore.dto;

public record LowStockAlertDTO(
        String isbn,
        String tenSach,
        Integer soLuongTon,
        Integer nguongBaoDong
) {}