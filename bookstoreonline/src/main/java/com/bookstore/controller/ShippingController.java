package com.bookstore.controller;

import com.bookstore.dto.TrackingResponseDto;
import com.bookstore.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    // API 42: Live Tracking lộ trình giao hàng (Mock)
    // Method: GET
    // URL: http://localhost:8080/api/shipping/track/{id}
    @GetMapping("/track/{id}")
    public ResponseEntity<TrackingResponseDto> trackOrder(@PathVariable String id) {
        TrackingResponseDto response = shippingService.trackOrder(id);
        return ResponseEntity.ok(response);
    }
}