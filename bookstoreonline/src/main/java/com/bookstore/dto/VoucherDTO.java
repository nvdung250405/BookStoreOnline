package com.bookstore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VoucherDTO {
    private String maVoucher;
    private BigDecimal giaTriGiam;
    private BigDecimal dieuKienToiThieu;
    private LocalDateTime thoiHan;

    public VoucherDTO() {}

    public String getMaVoucher() { return maVoucher; }
    public void setMaVoucher(String maVoucher) { this.maVoucher = maVoucher; }
    public BigDecimal getGiaTriGiam() { return giaTriGiam; }
    public void setGiaTriGiam(BigDecimal giaTriGiam) { this.giaTriGiam = giaTriGiam; }
    public BigDecimal getDieuKienToiThieu() { return dieuKienToiThieu; }
    public void setDieuKienToiThieu(BigDecimal dieuKienToiThieu) { this.dieuKienToiThieu = dieuKienToiThieu; }
    public LocalDateTime getThoiHan() { return thoiHan; }
    public void setThoiHan(LocalDateTime thoiHan) { this.thoiHan = thoiHan; }
}
