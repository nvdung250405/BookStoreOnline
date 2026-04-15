package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.TrackingResponseDto;
import com.bookstore.service.ShippingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    private final ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    // API 42: Live Tracking lộ trình giao hàng (Mock)
    // Method: GET
    // URL: http://localhost:8080/api/shipping/track/{id}
    @GetMapping("/track/{id}")
    public ResponseEntity<TrackingResponseDto> trackOrder(@PathVariable String id) {
        TrackingResponseDto response = shippingService.trackOrder(id);
        return ResponseEntity.ok(response);
    }

    // API 43: Update Shipping Status
    // Method: PUT
    // URL: http://localhost:8080/api/shipping/{id}/status
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateShippingStatus(
            @PathVariable String id,
            @RequestParam String status,
            @RequestParam(required = false) String notes) {
        
        shippingService.updateShippingStatus(id, status, notes);
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật trạng thái vận chuyển thành công", "OK"));
    }
}