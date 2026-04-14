package com.bookstore.service;

import com.bookstore.dto.NhaCungCapDto;
import com.bookstore.entity.NhaCungCap;
import com.bookstore.repository.NhaCungCapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NhaCungCapService {

    private final NhaCungCapRepository nhaCungCapRepository;

    public List<NhaCungCapDto> layTatCaNhaCungCap() {
        return nhaCungCapRepository.findAll().stream()
                .map(ncc -> new NhaCungCapDto(
                        ncc.getMaNcc(),
                        ncc.getTenNcc(),
                        ncc.getThongTinLienHe()
                ))
                .collect(Collectors.toList());
    }

    public NhaCungCapDto themNhaCungCap(NhaCungCapDto dto) {
        NhaCungCap ncc = new NhaCungCap();
        ncc.setTenNcc(dto.tenNcc());
        ncc.setThongTinLienHe(dto.thongTinLienHe());

        NhaCungCap saved = nhaCungCapRepository.save(ncc);
        return new NhaCungCapDto(saved.getMaNcc(), saved.getTenNcc(), saved.getThongTinLienHe());
    }

    public NhaCungCapDto suaNhaCungCap(Integer id, NhaCungCapDto dto) {
        NhaCungCap ncc = nhaCungCapRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Nhà cung cấp!"));

        ncc.setTenNcc(dto.tenNcc());
        ncc.setThongTinLienHe(dto.thongTinLienHe());

        NhaCungCap updated = nhaCungCapRepository.save(ncc);
        return new NhaCungCapDto(updated.getMaNcc(), updated.getTenNcc(), updated.getThongTinLienHe());
    }

    public void xoaNhaCungCap(Integer id) {
        if (!nhaCungCapRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy Nhà cung cấp để xóa!");
        }
        nhaCungCapRepository.deleteById(id);
    }
}