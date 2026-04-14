package com.bookstore.service;

import com.bookstore.dto.CheckoutRequest;
import com.bookstore.dto.DonHangDTO;
import java.util.List;

public interface DonHangService {
    DonHangDTO checkout(String username, CheckoutRequest request);
    List<DonHangDTO> getHistory(String username);
    DonHangDTO getDetail(String maDonHang);
    void cancelOrder(String username, String maDonHang);
    
    // Admin/Staff methods
    List<DonHangDTO> getAllOrders();
    void updateStatus(String maDonHang, String status);
}
