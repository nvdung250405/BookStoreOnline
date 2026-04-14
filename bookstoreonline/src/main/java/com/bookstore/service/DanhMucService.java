package com.bookstore.service;

import com.bookstore.dto.DanhMucDTO;
import com.bookstore.dto.DanhMucRequest;
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
    public DanhMucDTO createCategory(DanhMucRequest request) {
        DanhMuc entity = new DanhMuc();
        entity.setTenDanhMuc(request.getTenDanhMuc());
        
        Integer maChaId = request.getMaDanhMucCha();
        if (maChaId != null) {
            DanhMuc parent = danhMucRepository.findById(maChaId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục cha với ID: " + maChaId));
            entity.setDanhMucCha(parent);
        }
        
        return convertToDTO(danhMucRepository.save(entity));
    }

    @Transactional
    public DanhMucDTO updateCategory(Integer id, DanhMucRequest request) {
        DanhMuc entity = danhMucRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + id));
        
        if (request.getTenDanhMuc() != null && !request.getTenDanhMuc().isBlank()) {
            entity.setTenDanhMuc(request.getTenDanhMuc());
        }
        
        Integer maChaId = request.getMaDanhMucCha();
        if (maChaId != null) {
            if (id.equals(maChaId)) {
                 throw new IllegalArgumentException("Danh mục không thể là cha của chính nó");
            }
            DanhMuc parent = danhMucRepository.findById(maChaId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục cha với ID: " + maChaId));
            entity.setDanhMucCha(parent);
        } else {
            // If maDanhMucCha is explicitly sent as null in a partial update, we might want to clear it.
            // Assuming full update here as DTO usually sends all values.
            entity.setDanhMucCha(null);
        }
        
        return convertToDTO(danhMucRepository.save(entity));
    }

    @Transactional
    public void deleteCategory(Integer id) {
        DanhMuc entity = danhMucRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + id));
        try {
            danhMucRepository.delete(entity);
            danhMucRepository.flush(); // Đẩy lệnh xuống DB ngay lập tức để bắt lỗi Foreign Key nếu có
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Không thể xóa danh mục này vì vẫn đang được tham chiếu bởi các sách hoặc gặp lỗi ràng buộc dữ liệu.");
        }
    }
}
