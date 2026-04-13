package com.bookstore.service;

import com.bookstore.dto.DanhGiaDTO;
import java.util.List;

public interface DanhGiaService {
    List<DanhGiaDTO> layDanhGiaTheoSach(String isbn);
    void guiDanhGia(String username, String isbn, Integer diem, String nhanXet);
}
