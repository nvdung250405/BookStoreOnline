package com.bookstore.service;

import com.bookstore.dto.HoTroDTO;
import com.bookstore.entity.HoTro;
import com.bookstore.entity.KhachHang;
import com.bookstore.repository.HoTroRepository;
import com.bookstore.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HoTroServiceImpl implements HoTroService {

    @Autowired
    private HoTroRepository hoTroRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Override
    public List<HoTroDTO> layTatCaYeuCau() {
        return hoTroRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<HoTroDTO> layChoKhachHang(String username) {
        return hoTroRepository.findByKhachHang_TaiKhoan_Username(username).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void guiYeuCau(String username, String tieuDe, String noiDung) {
        KhachHang kh = khachHangRepository.findByTaiKhoan_Username(username)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
        
        HoTro ht = new HoTro();
        ht.setKhachHang(kh);
        ht.setTieuDe(tieuDe);
        ht.setNoiDung(noiDung);
        hoTroRepository.save(ht);
    }

    @Override
    public void capNhatTrangThai(Long id, String trangThai) {
        HoTro ht = hoTroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Yêu cầu không tồn tại"));
        ht.setTrangThai(trangThai);
        hoTroRepository.save(ht);
    }

    private HoTroDTO toDTO(HoTro ht) {
        HoTroDTO dto = new HoTroDTO();
        dto.setMaHoTro(ht.getMaHoTro());
        dto.setTenKhachHang(ht.getKhachHang().getHoTen());
        dto.setTieuDe(ht.getTieuDe());
        dto.setNoiDung(ht.getNoiDung());
        dto.setTrangThai(ht.getTrangThai());
        dto.setThoiGian(ht.getThoiGian());
        return dto;
    }
}
