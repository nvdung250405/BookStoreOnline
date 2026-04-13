package com.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public class RevenueReportDTO {
    @Schema(example = "15500000.00")
    private BigDecimal totalRevenue;

    @Schema(example = "42")
    private long totalOrders;

    @Schema(example = "HOAN_TAT")
    private String statusCheck;

    public RevenueReportDTO(BigDecimal totalRevenue, long totalOrders, String statusCheck) {
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.totalOrders = totalOrders;
        this.statusCheck = statusCheck;
    }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public long getTotalOrders() { return totalOrders; }
    public String getStatusCheck() { return statusCheck; }
}
