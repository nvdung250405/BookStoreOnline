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

    public void updateShippingStatus(String maVanChuyen, String status, String notes) {
        // This would be used to update shipping status in the database
        // In a real scenario, this would integrate with shipping provider APIs
        // and update the van_chuyen table in the database
        
        // Mock implementation for now - in production:
        // 1. Validate shipping ID exists
        // 2. Validate status is one of the allowed values
        // 3. Update van_chuyen record with new status
        // 4. Log the change for audit trail
        // 5. Trigger notifications if needed
        
        System.out.println("Updating shipping " + maVanChuyen + " to status: " + status);
        if (notes != null && !notes.isEmpty()) {
            System.out.println("Notes: " + notes);
        }
    }
}