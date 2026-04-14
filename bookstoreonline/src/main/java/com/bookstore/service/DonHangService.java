package com.bookstore.service;

import com.bookstore.dto.CheckoutRequest;
import com.bookstore.dto.DonHangResponseDTO;
import java.util.List;

public interface DonHangService {
    DonHangResponseDTO checkout(CheckoutRequest request);
    List<DonHangResponseDTO> layLichSuDonHang(String username);
    DonHangResponseDTO layChiTietDonHang(String maDonHang);
    void huyDonHang(String maDonHang);
    
    // Admin / Staff methods
    List<DonHangResponseDTO> layTatCaDonHang();
    void capNhatTrangThai(String maDonHang, String trangThai);
}
