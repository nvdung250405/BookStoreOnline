package com.bookstore.dto;

import java.util.List;

public record ImportRequestDto(
        Integer maNcc,
        Integer maNhanVien,
        List<ChiTietImportRequest> chiTietList
) {}