package com.bookstore.service;

import com.bookstore.dto.HoTroDTO;
import java.util.List;

public interface HoTroService {
    List<HoTroDTO> layTatCaYeuCau();
    List<HoTroDTO> layChoKhachHang(String username);
    void guiYeuCau(String username, String tieuDe, String noiDung);
    void capNhatTrangThai(Long id, String trangThai);
}
