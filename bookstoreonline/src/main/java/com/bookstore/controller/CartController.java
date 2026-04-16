package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.CartDTO;
import com.bookstore.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin("*")
@Tag(name = "Cart Management", description = "Quản lý giỏ hàng của khách hàng")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/admin/all")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy tất cả giỏ hàng (ADMIN)", description = "Admin theo dõi danh sách tất cả các giỏ hàng đang hoạt động trong hệ thống.")
    public ApiResponse<java.util.List<com.bookstore.entity.Cart>> getAllCarts() {
        return ApiResponse.success(cartService.getAllActiveCarts());
    }

    @GetMapping("/{username}")
    @Operation(summary = "Lấy giỏ hàng", description = "Xem danh sách các sản phẩm đang có trong giỏ hàng của người dùng")
    public ApiResponse<List<CartDTO>> getCart(@PathVariable String username) {
        return ApiResponse.success(cartService.getCart(username));
    }

    @PostMapping("/add")
    @Operation(summary = "Thêm vào giỏ", description = "Thêm một cuốn sách vào giỏ hàng hoặc tăng số lượng nếu đã tồn tại")
    public ApiResponse<String> addToCart(
            @RequestParam String username,
            @RequestParam String isbn,
            @RequestParam Integer quantity) {
        cartService.addToCart(username, isbn, quantity);
        return ApiResponse.success("Đã thêm vào giỏ hàng thành công", null);
    }

    @PutMapping("/update")
    @Operation(summary = "Cập nhật số lượng", description = "Thay đổi số lượng của một mục trong giỏ hàng bằng ISBN")
    public ApiResponse<String> updateQuantity(
            @RequestParam String username,
            @RequestParam String isbn,
            @RequestParam Integer quantity) {
        cartService.updateQuantity(username, isbn, quantity);
        return ApiResponse.success("Đã cập nhật số lượng", null);
    }

    @DeleteMapping("/remove")
    @Operation(summary = "Xóa một mục", description = "Xóa bỏ hoàn toàn một cuốn sách khỏi giỏ hàng bằng ISBN")
    public ApiResponse<String> removeItem(
            @RequestParam String username, 
            @RequestParam String isbn) {
        cartService.removeFromCart(username, isbn);
        return ApiResponse.success("Đã xóa khỏi giỏ hàng", null);
    }

    @DeleteMapping("/clear/{username}")
    @Operation(summary = "Dọn sạch giỏ hàng", description = "Xóa toàn bộ các sản phẩm trong giỏ hàng của người dùng")
    public ApiResponse<String> clearCart(@PathVariable String username) {
        cartService.clearCart(username);
        return ApiResponse.success("Đã dọn sạch giỏ hàng", null);
    }
}
