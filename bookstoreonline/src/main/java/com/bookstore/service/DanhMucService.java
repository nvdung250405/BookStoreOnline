package com.bookstore.service;

import com.bookstore.dto.DanhMucDTO;
import com.bookstore.entity.DanhMuc;
import com.bookstore.repository.DanhMucRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DanhMucService {

    private final DanhMucRepository danhMucRepository;

    // Constructor thủ công thay cho @RequiredArgsConstructor
    public DanhMucService(DanhMucRepository danhMucRepository) {
        this.danhMucRepository = danhMucRepository;
    }

    @Transactional(readOnly = true)
    public List<DanhMucDTO> getAllCategories() {
        return danhMucRepository.findByDanhMucChaIsNull()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private DanhMucDTO convertToDTO(DanhMuc entity) {
        DanhMucDTO dto = new DanhMucDTO();
        dto.setMaDanhMuc(entity.getMaDanhMuc());
        dto.setTenDanhMuc(entity.getTenDanhMuc());
        dto.setMaDanhMucCha(entity.getDanhMucCha() != null ? entity.getDanhMucCha().getMaDanhMuc() : null);
        
        if (entity.getDanhMucCon() != null) {
            dto.setDanhMucCon(entity.getDanhMucCon()
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    @Transactional
    public DanhMucDTO createCategory(DanhMucDTO dto) {
        DanhMuc entity = new DanhMuc();
        entity.setTenDanhMuc(dto.getTenDanhMuc());
        
        if (dto.getMaDanhMucCha() != null) {
            DanhMuc parent = danhMucRepository.findById(dto.getMaDanhMucCha())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục cha với ID: " + dto.getMaDanhMucCha()));
            entity.setDanhMucCha(parent);
        }
        
        return convertToDTO(danhMucRepository.save(entity));
    }
}
