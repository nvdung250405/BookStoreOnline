package com.bookstore.service;

import com.bookstore.dto.VoucherDTO;
import java.util.List;

public interface VoucherService {
    List<VoucherDTO> getAllVouchers();
    List<VoucherDTO> getActiveVouchers();
    VoucherDTO getVoucherByCode(String code);
    VoucherDTO saveVoucher(VoucherDTO dto);
    void deleteVoucher(String code);
}
