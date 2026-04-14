package com.bookstore.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ChiTietDonHangDTO {
    private String isbn;
    private String tenSach;
    private Integer soLuong;
    private BigDecimal giaBanChot;
}
