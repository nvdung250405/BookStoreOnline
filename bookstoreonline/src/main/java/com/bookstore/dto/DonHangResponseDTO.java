package com.bookstore.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DonHangResponseDTO {
    private String maDonHang;
    private String username;
    private String hoTenKhachHang;
    private LocalDateTime ngayTao;
    private BigDecimal tongTienHang;
    private BigDecimal phiVanChuyen;
    private BigDecimal tongThanhToan;
    private String trangThai;
    private String diaChiGiaoHang;
    private String maVoucher;
    private List<ChiTietDonHangDTO> chiTietDonHangs;
}
