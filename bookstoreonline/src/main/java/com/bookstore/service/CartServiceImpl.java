package com.bookstore.service;

import com.bookstore.dto.CartDTO;
import com.bookstore.entity.Cart;
import com.bookstore.entity.Customer;
import com.bookstore.entity.Book;
import com.bookstore.entity.CartId;
import com.bookstore.repository.CartRepository;
import com.bookstore.repository.CustomerRepository;
import com.bookstore.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings("null")
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final BookRepository bookRepository;

    public CartServiceImpl(CartRepository cartRepository, 
                               CustomerRepository customerRepository, 
                               BookRepository bookRepository) {
        this.cartRepository = cartRepository;
        this.customerRepository = customerRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public List<CartDTO> getCart(String username) {
        return cartRepository.findByCustomer_Account_Username(username)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void addToCart(String username, String isbn, Integer quantity) {
        cartRepository.findByCustomer_Account_UsernameAndBook_Isbn(username, isbn)
                .ifPresentOrElse(
                        item -> {
                            item.setQuantity(item.getQuantity() + quantity);
                            cartRepository.save(item);
                        },
                        () -> {
                            Customer customer = customerRepository.findByAccount_Username(username)
                                    .orElseThrow(() -> new RuntimeException("Customer not found"));
                            Book book = bookRepository.findById(isbn)
                                    .orElseThrow(() -> new RuntimeException("Book not found"));
                            
                            Cart item = new Cart();
                            item.setCustomer(customer);
                            item.setBook(book);
                            item.setQuantity(quantity);
                            
                            CartId id = new CartId();
                            id.setCustomerId(customer.getCustomerId());
                            id.setIsbn(book.getIsbn());
                            item.setId(id);
                            
                            cartRepository.save(item);
                        }
                );
    }

    @Override
    public void updateQuantity(String username, String isbn, Integer quantity) {
        Cart item = cartRepository.findByCustomer_Account_UsernameAndBook_Isbn(username, isbn)
                .orElseThrow(() -> new RuntimeException("Product not in cart"));
        item.setQuantity(quantity);
        cartRepository.save(item);
    }

    @Override
    public void removeFromCart(String username, String isbn) {
        cartRepository.findByCustomer_Account_UsernameAndBook_Isbn(username, isbn)
                .ifPresent(cartRepository::delete);
    }

    @Override
    public void clearCart(String username) {
        if (username == null) throw new IllegalArgumentException("Username cannot be empty");
        cartRepository.deleteByCustomer_Account_Username(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cart> getAllActiveCarts() {
        return cartRepository.findAll();
    }

    private CartDTO toDTO(Cart entity) {
        CartDTO dto = new CartDTO();
        dto.setIsbn(entity.getBook().getIsbn());
        dto.setTitle(entity.getBook().getTitle());
        dto.setCoverImage(entity.getBook().getCoverImage());
        dto.setPrice(entity.getBook().getPrice());
        dto.setQuantity(entity.getQuantity());
        
        if (entity.getBook().getPrice() != null) {
            dto.setTotalPrice(entity.getBook().getPrice().multiply(new BigDecimal(entity.getQuantity())));
        }
        
        return dto;
    }
}
