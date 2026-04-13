package com.bookstore.service;

import com.bookstore.dto.VoucherDTO;
import java.util.List;

public interface VoucherService {
    List<VoucherDTO> layTatCaVoucher();
    VoucherDTO layVoucherTheoMa(String code);
    VoucherDTO luuVoucher(VoucherDTO dto);
    void xoaVoucher(String code);
}
