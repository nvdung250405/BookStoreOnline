package com.bookstore.service;

import jakarta.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface ThanhToanService {
    String createVNPayPayment(String maDonHang, HttpServletRequest request) throws UnsupportedEncodingException;
    void processVNPayCallback(Map<String, String> params);
}
