package com.bookstore.service;

import com.bookstore.dto.NxbDTO;
import com.bookstore.entity.Nxb;
import com.bookstore.repository.NxbRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NxbService {

    private final NxbRepository nxbRepository;

    public NxbService(NxbRepository nxbRepository) {
        this.nxbRepository = nxbRepository;
    }

    @Transactional(readOnly = true)
    public List<NxbDTO> getAllNxbs() {
        return nxbRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private NxbDTO convertToDTO(Nxb entity) {
        NxbDTO dto = new NxbDTO();
        dto.setMaNxb(entity.getMaNxb());
        dto.setTenNxb(entity.getTenNxb());
        return dto;
    }
}
