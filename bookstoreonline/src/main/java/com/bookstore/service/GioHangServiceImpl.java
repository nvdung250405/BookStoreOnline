package com.bookstore.service;

import com.bookstore.dto.GioHangDTO;
import com.bookstore.entity.GioHang;
import com.bookstore.entity.KhachHang;
import com.bookstore.entity.Sach;
import com.bookstore.repository.GioHangRepository;
import com.bookstore.repository.KhachHangRepository;
import com.bookstore.repository.SachRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings("null")
public class GioHangServiceImpl implements GioHangService {

    private final GioHangRepository gioHangRepository;
    private final KhachHangRepository khachHangRepository;
    private final SachRepository sachRepository;

    public GioHangServiceImpl(GioHangRepository gioHangRepository, 
                              KhachHangRepository khachHangRepository, 
                              SachRepository sachRepository) {
        this.gioHangRepository = gioHangRepository;
        this.khachHangRepository = khachHangRepository;
        this.sachRepository = sachRepository;
    }

    @Override
    public List<GioHangDTO> layGioHang(String username) {
        return gioHangRepository.findByKhachHang_TaiKhoan_Username(username)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void themVaoGioHang(String username, String isbn, Integer soLuong) {
        gioHangRepository.findByKhachHang_TaiKhoan_UsernameAndSach_Isbn(username, isbn)
                .ifPresentOrElse(
                        item -> {
                            item.setSoLuong(item.getSoLuong() + soLuong);
                            gioHangRepository.save(item);
                        },
                        () -> {
                            KhachHang kh = khachHangRepository.findByTaiKhoan_Username(username)
                                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
                            Sach sach = sachRepository.findById(isbn)
                                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sách"));
                            
                            GioHang item = new GioHang();
                            item.setKhachHang(kh);
                            item.setSach(sach);
                            item.setSoLuong(soLuong);
                            gioHangRepository.save(item);
                        }
                );
    }

    @Override
    public void capNhatSoLuong(Long id, Integer soLuong) {
        if (id == null) throw new IllegalArgumentException("ID không được để trống");
        GioHang item = gioHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng"));
        item.setSoLuong(soLuong);
        gioHangRepository.save(item);
    }

    @Override
    public void xoaKhoiGioHang(Long id) {
        if (id == null) return;
        gioHangRepository.deleteById(id);
    }

    @Override
    public void lamTrongGioHang(String username) {
        if (username == null) throw new IllegalArgumentException("Username không được để trống");
        gioHangRepository.deleteByKhachHang_TaiKhoan_Username(username);
    }

    private GioHangDTO toDTO(GioHang entity) {
        GioHangDTO dto = new GioHangDTO();
        dto.setId(entity.getId());
        dto.setIsbn(entity.getSach().getIsbn());
        dto.setTenSach(entity.getSach().getTenSach());
        dto.setAnhBia(entity.getSach().getAnhBia());
        dto.setGiaNiemYet(entity.getSach().getGiaNiemYet());
        dto.setSoLuong(entity.getSoLuong());
        
        if (entity.getSach().getGiaNiemYet() != null) {
            dto.setThanhTien(entity.getSach().getGiaNiemYet().multiply(new BigDecimal(entity.getSoLuong())));
        }
        
        return dto;
    }
}
