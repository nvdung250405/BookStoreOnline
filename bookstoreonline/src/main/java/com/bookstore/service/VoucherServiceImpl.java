package com.bookstore.service;

import com.bookstore.dto.VoucherDTO;
import com.bookstore.entity.Voucher;
import com.bookstore.repository.VoucherRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

    public VoucherServiceImpl(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    @Override
    public List<VoucherDTO> getAllVouchers() {
        return voucherRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<VoucherDTO> getActiveVouchers() {
        return voucherRepository.findByExpiryDateAfter(java.time.LocalDateTime.now()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public VoucherDTO getVoucherByCode(String code) {
        return voucherRepository.findById(code)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Voucher code does not exist"));
    }

    @Override
    public VoucherDTO saveVoucher(VoucherDTO dto) {
        Voucher entity = new Voucher();
        entity.setVoucherCode(dto.getVoucherCode());
        entity.setDiscountValue(dto.getDiscountValue());
        entity.setMinCondition(dto.getMinCondition());
        entity.setExpiryDate(dto.getExpiryDate());
        
        Voucher saved = voucherRepository.save(entity);
        return toDTO(saved);
    }

    @Override
    public void deleteVoucher(String code) {
        voucherRepository.deleteById(code);
    }

    private VoucherDTO toDTO(Voucher entity) {
        VoucherDTO dto = new VoucherDTO();
        dto.setVoucherCode(entity.getVoucherCode());
        dto.setDiscountValue(entity.getDiscountValue());
        dto.setMinCondition(entity.getMinCondition());
        dto.setExpiryDate(entity.getExpiryDate());
        return dto;
    }
}
