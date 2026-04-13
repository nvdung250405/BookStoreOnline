package com.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class BookRankingDTO {
    @Schema(example = "9786041123456")
    private String isbn;

    @Schema(example = "Dế Mèn Phiêu Lưu Ký")
    private String tenSach;

    @Schema(example = "125")
    private long totalSold;

    public BookRankingDTO(String isbn, String tenSach, long totalSold) {
        this.isbn = isbn;
        this.tenSach = tenSach;
        this.totalSold = totalSold;
    }

    public String getIsbn() { return isbn; }
    public String getTenSach() { return tenSach; }
    public long getTotalSold() { return totalSold; }
}
