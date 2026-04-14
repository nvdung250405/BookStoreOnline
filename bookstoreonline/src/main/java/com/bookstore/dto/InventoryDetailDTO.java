package com.bookstore.dto;

public record InventoryDetailDTO(
        String isbn,
        String tenSach,
        Integer soLuongTon,
        String viTriKe
) {}