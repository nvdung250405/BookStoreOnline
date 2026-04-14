package com.bookstore.dto;

import jakarta.validation.constraints.NotBlank;

public class AiSearchRequest {
    @NotBlank(message = "Vui lòng nhập câu truy vấn (Ví dụ: Tìm sách kinh doanh dưới 150 nghìn)")
    private String query;

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
}
