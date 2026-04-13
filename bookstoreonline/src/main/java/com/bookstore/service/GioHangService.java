package com.bookstore.service;

import com.bookstore.dto.GioHangDTO;
import java.util.List;

public interface GioHangService {
    List<GioHangDTO> layGioHang(String username);
    void themVaoGioHang(String username, String isbn, Integer soLuong);
    void capNhatSoLuong(Long id, Integer soLuong);
    void xoaKhoiGioHang(Long id);
    void lamTrongGioHang(String username);
}
