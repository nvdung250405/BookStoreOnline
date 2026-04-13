package com.bookstore.service;

import com.bookstore.dto.VoucherDTO;
import com.bookstore.entity.Voucher;
import com.bookstore.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VoucherServiceImpl implements VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    @Override
    public List<VoucherDTO> layTatCaVoucher() {
        return voucherRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public VoucherDTO layVoucherTheoMa(String code) {
        return voucherRepository.findById(code)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại"));
    }

    @Override
    public VoucherDTO luuVoucher(VoucherDTO dto) {
        Voucher entity = new Voucher();
        entity.setMaVoucher(dto.getMaVoucher());
        entity.setGiaTriGiam(dto.getGiaTriGiam());
        entity.setDieuKienToiThieu(dto.getDieuKienToiThieu());
        entity.setThoiHan(dto.getThoiHan());
        
        Voucher saved = voucherRepository.save(entity);
        return toDTO(saved);
    }

    @Override
    public void xoaVoucher(String code) {
        voucherRepository.deleteById(code);
    }

    private VoucherDTO toDTO(Voucher entity) {
        VoucherDTO dto = new VoucherDTO();
        dto.setMaVoucher(entity.getMaVoucher());
        dto.setGiaTriGiam(entity.getGiaTriGiam());
        dto.setDieuKienToiThieu(entity.getDieuKienToiThieu());
        dto.setThoiHan(entity.getThoiHan());
        return dto;
    }
}
