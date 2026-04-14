package com.bookstore.service;

import com.bookstore.dto.TrackingResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShippingService {

    public TrackingResponseDto trackOrder(String maDonHang) {
        // Giả lập logic: Cứ ném mã đơn hàng vào là trả về 1 lộ trình nhìn rất "uy tín"
        // (Trong dự án thật sau này, chỗ này sẽ là đoạn code gọi RestTemplate sang Server của GHN)

        List<TrackingResponseDto.ChiTietTracking> history = List.of(
                new TrackingResponseDto.ChiTietTracking("10:30 14-04-2026", "Đang giao hàng", "Bưu cục Cầu Giấy, Hà Nội"),
                new TrackingResponseDto.ChiTietTracking("08:15 14-04-2026", "Đã đến bưu cục giao", "Bưu cục Cầu Giấy, Hà Nội"),
                new TrackingResponseDto.ChiTietTracking("22:00 13-04-2026", "Đang luân chuyển", "Kho phân loại SOC Hà Nội"),
                new TrackingResponseDto.ChiTietTracking("18:45 13-04-2026", "Đã lấy hàng thành công", "Kho tổng Nhà Sách Online")
        );

        // Tạo mã vận đơn fake dựa trên mã đơn hàng (Ví dụ DH-123 -> GHN-123-9981)
        String fakeTrackingId = "GHN-" + maDonHang.replace("DH-", "") + "-9981";

        return new TrackingResponseDto(
                maDonHang,
                "Giao Hàng Nhanh (GHN)",
                fakeTrackingId,
                "DANG_GIAO_HANG",
                history
        );
    }
}