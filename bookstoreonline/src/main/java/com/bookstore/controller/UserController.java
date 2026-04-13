package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.ChangePasswordRequest;
import com.bookstore.dto.CustomerProfileRequest;
import com.bookstore.dto.StaffProfileRequest;
import com.bookstore.dto.UserProfileDTO;
import com.bookstore.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Profile", description = "Quản lý thông tin cá nhân người dùng")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/get-profile")
    @Operation(summary = "Lấy hồ sơ cá nhân hiện tại", description = "Lấy thông tin tài khoản và profile (Khách hàng/Nhân viên)")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getProfile() {
        UserProfileDTO profile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success("Lấy hồ sơ thành công", profile));
    }

    @PutMapping("/update-customer-profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Cập nhật hồ sơ Khách hàng", description = "Chỉ dành cho tài khoản Khách hàng đã có hồ sơ")
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateCustomerProfile(@Valid @RequestBody CustomerProfileRequest request) {
        UserProfileDTO updated = userService.updateCustomerProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ khách hàng thành công", updated));
    }

    @PutMapping("/update-staff-profile")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN') or hasRole('STOREKEEPER')")
    @Operation(summary = "Cập nhật hồ sơ Nhân viên", description = "Chỉ dành cho tài khoản Nhân viên/Admin đã có hồ sơ")
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateStaffProfile(@Valid @RequestBody StaffProfileRequest request) {
        UserProfileDTO updated = userService.updateStaffProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ nhân viên thành công", updated));
    }

    @PutMapping("/update-password")
    @Operation(summary = "Đổi mật khẩu", description = "Kiểm tra mật khẩu cũ và cập nhật mật khẩu mới")
    public ResponseEntity<ApiResponse<String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công", null));
    }

    @PostMapping("/create-customer-profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Khởi tạo hồ sơ Khách hàng", description = "Dành cho Khách hàng mới chưa có thông tin hồ sơ")
    public ResponseEntity<ApiResponse<UserProfileDTO>> createCustomerProfile(@Valid @RequestBody CustomerProfileRequest request) {
        UserProfileDTO profile = userService.createCustomerProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Khởi tạo hồ sơ khách hàng thành công", profile));
    }

}
