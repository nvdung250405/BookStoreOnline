package com.bookstore.service;

import com.bookstore.dto.TacGiaDTO;
import com.bookstore.entity.TacGia;
import com.bookstore.repository.TacGiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TacGiaService {

    private final TacGiaRepository tacGiaRepository;

    public TacGiaService(TacGiaRepository tacGiaRepository) {
        this.tacGiaRepository = tacGiaRepository;
    }

    @Transactional(readOnly = true)
    public List<TacGiaDTO> getAllAuthors() {
        return tacGiaRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private TacGiaDTO convertToDTO(TacGia entity) {
        TacGiaDTO dto = new TacGiaDTO();
        dto.setMaTacGia(entity.getMaTacGia());
        dto.setTenTacGia(entity.getTenTacGia());
        dto.setTieuSu(entity.getTieuSu());
        return dto;
    }
}
