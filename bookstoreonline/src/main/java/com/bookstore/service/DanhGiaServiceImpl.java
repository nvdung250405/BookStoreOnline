package com.bookstore.service;

import com.bookstore.dto.DanhGiaDTO;
import com.bookstore.entity.DanhGia;
import com.bookstore.entity.KhachHang;
import com.bookstore.entity.Sach;
import com.bookstore.repository.DanhGiaRepository;
import com.bookstore.repository.KhachHangRepository;
import com.bookstore.repository.SachRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DanhGiaServiceImpl implements DanhGiaService {

    @Autowired
    private DanhGiaRepository danhGiaRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private SachRepository sachRepository;

    @Override
    public List<DanhGiaDTO> layDanhGiaTheoSach(String isbn) {
        return danhGiaRepository.findBySach_Isbn(isbn).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void guiDanhGia(String username, String isbn, Integer diem, String nhanXet) {
        KhachHang kh = khachHangRepository.findByTaiKhoan_Username(username)
                .orElseThrow(() -> new RuntimeException("Chưa đăng nhập hoặc không tìm thấy khách hàng"));
        Sach sach = sachRepository.findById(isbn)
                .orElseThrow(() -> new RuntimeException("Sách không tồn tại"));

        DanhGia dg = new DanhGia();
        dg.setKhachHang(kh);
        dg.setSach(sach);
        dg.setDiemDg(diem);
        dg.setNhanXet(nhanXet);
        danhGiaRepository.save(dg);
    }

    private DanhGiaDTO toDTO(DanhGia dg) {
        DanhGiaDTO dto = new DanhGiaDTO();
        dto.setMaDg(dg.getMaDg());
        dto.setIsbn(dg.getSach().getIsbn());
        dto.setTenKhachHang(dg.getKhachHang().getHoTen());
        dto.setDiemDg(dg.getDiemDg());
        dto.setNhanXet(dg.getNhanXet());
        dto.setNgayDg(dg.getNgayDg());
        return dto;
    }
}
