package com.bookstore.service;

import com.bookstore.dto.CartDTO;
import java.util.List;

public interface CartService {
    List<CartDTO> getCart(String username);
    void addToCart(String username, String isbn, Integer quantity);
    void updateQuantity(String username, String isbn, Integer quantity);
    void removeFromCart(String username, String isbn);
    void clearCart(String username);
    java.util.List<com.bookstore.entity.Cart> getAllActiveCarts();
}
