package com.bookstore.service;

import com.bookstore.constant.AuditAction;
import com.bookstore.dto.AdminOrderRequest;
import com.bookstore.dto.CheckoutRequest;
import com.bookstore.dto.OrderDetailDTO;
import com.bookstore.dto.OrderResponseDTO;
import com.bookstore.entity.*;
import com.bookstore.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class OrderServiceImpl implements OrderService {

    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CustomerRepository customerRepository;
    private final CartRepository cartRepository;
    private final VoucherRepository voucherRepository;
    private final PaymentRepository paymentRepository;
    private final AuditLogRepository auditLogRepository;
    private final AccountRepository accountRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final AuditLogService auditLogService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderDetailRepository orderDetailRepository,
                            CustomerRepository customerRepository,
                            CartRepository cartRepository,
                            VoucherRepository voucherRepository,
                            PaymentRepository paymentRepository,
                            AuditLogRepository auditLogRepository,
                            AccountRepository accountRepository,
                            BookRepository bookRepository,
                            InventoryRepository inventoryRepository,
                            InventoryLogRepository inventoryLogRepository,
                            AuditLogService auditLogService) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.customerRepository = customerRepository;
        this.cartRepository = cartRepository;
        this.voucherRepository = voucherRepository;
        this.paymentRepository = paymentRepository;
        this.auditLogRepository = auditLogRepository;
        this.accountRepository = accountRepository;
        this.bookRepository = bookRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryLogRepository = inventoryLogRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public OrderResponseDTO checkout(CheckoutRequest request) {
        Customer customer = customerRepository.findByAccount_Username(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + request.getUsername()));

        List<Cart> cartList = cartRepository.findByCustomer(customer);
        if (cartList.isEmpty()) {
            throw new RuntimeException("Your cart is empty!");
        }

        BigDecimal subtotal = cartList.stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Voucher voucher = null;
        BigDecimal discount = BigDecimal.ZERO;
        if (request.getVoucherCode() != null && !request.getVoucherCode().isEmpty()) {
            voucher = voucherRepository.findById(request.getVoucherCode())
                    .orElseThrow(() -> new RuntimeException("Invalid voucher code"));
            
            if (voucher.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Voucher has expired");
            }
            if (subtotal.compareTo(voucher.getMinCondition()) < 0) {
                throw new RuntimeException("Order does not meet minimum condition for this voucher");
            }
            discount = voucher.getDiscountValue();
        }

        BigDecimal shippingFee = (subtotal.compareTo(new BigDecimal("200000")) >= 0) ? BigDecimal.ZERO : new BigDecimal("30000");
        BigDecimal totalPayment = subtotal.add(shippingFee).subtract(discount);
        if (totalPayment.compareTo(BigDecimal.ZERO) < 0) totalPayment = BigDecimal.ZERO;

        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order order = new Order();
        order.setOrderId(orderId);
        order.setCustomer(customer);
        order.setVoucher(voucher);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalItemsPrice(subtotal);
        order.setShippingFee(shippingFee);
        order.setTotalPayment(totalPayment);
        order.setStatusCode("NEW");
        order.setShippingAddress(request.getShippingAddress());

        orderRepository.save(order);

        List<OrderDetailDTO> detailDTOs = new ArrayList<>();
        for (Cart cart : cartList) {
            OrderDetail detail = new OrderDetail();
            detail.setId(new OrderDetailId(orderId, cart.getBook().getIsbn()));
            detail.setOrder(order);
            detail.setBook(cart.getBook());
            detail.setQuantity(cart.getQuantity());
            detail.setFinalPrice(cart.getBook().getPrice());
            
            orderDetailRepository.save(detail);
            
            OrderDetailDTO dto = new OrderDetailDTO();
            dto.setIsbn(cart.getBook().getIsbn());
            dto.setTitle(cart.getBook().getTitle());
            dto.setQuantity(cart.getQuantity());
            dto.setFinalPrice(cart.getBook().getPrice());
            detailDTOs.add(dto);
        }

        cartRepository.deleteAll(cartList);

        Payment payment = new Payment();
        payment.setPaymentId("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setOrder(order);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatusCode("PENDING");
        paymentRepository.save(payment);

        auditLogService.log(customer.getAccount(), "ORDER_CHECKOUT", "Khách hàng đặt hàng mới: #" + orderId + " (Tổng tiền: " + totalPayment + ")");

        return mapToResponseDTO(order, detailDTOs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrderHistory(String username) {
        return orderRepository.findAllByCustomer_Account_UsernameOrderByCreatedAtDesc(username)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderDetail(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return mapToDTO(order);
    }

    @Override
    @Transactional
    public void cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        if (!"NEW".equals(order.getStatusCode()) && !"CONFIRMED".equals(order.getStatusCode())) {
            throw new RuntimeException("Cannot cancel order in status: " + order.getStatusCode());
        }
        
        order.setStatusCode("CANCELLED");
        orderRepository.save(order);

        restockInventory(order);
        
        auditLogService.log(order.getCustomer().getAccount(), "CANCEL_ORDER", "Đơn hàng #" + orderId + " đã bị hủy.");
    }

    private void restockInventory(Order order) {
        String orderId = order.getOrderId();
        List<OrderDetail> details = orderDetailRepository.findByOrder(order);
        for (OrderDetail detail : details) {
            Book book = detail.getBook();
            Inventory inventory = inventoryRepository.findByBook_Isbn(book.getIsbn())
                    .orElseGet(() -> {
                        Inventory newInventory = new Inventory();
                        newInventory.setBook(book);
                        newInventory.setStockQuantity(0);
                        return newInventory;
                    });
            
            int restockQuantity = detail.getQuantity();
            int oldStock = inventory.getStockQuantity();
            inventory.setStockQuantity(oldStock + restockQuantity);
            inventoryRepository.save(inventory);

            InventoryLog log = new InventoryLog();
            log.setBook(book);
            log.setChangeType("RETURN");
            log.setQuantityChanged(restockQuantity);
            log.setQuantityAfter(oldStock + restockQuantity);
            log.setNotes("Restock from cancelled order: " + orderId);
            inventoryLogRepository.save(log);
        }
    }

    @Override
    @Transactional
    public OrderResponseDTO createAdminOrder(AdminOrderRequest request) {
        Customer customer = customerRepository.findById(Long.parseLong(request.getCustomerId()))
                .orElseThrow(() -> new RuntimeException("Customer not found: " + request.getCustomerId()));

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderDetailDTO> detailDTOs = new ArrayList<>();
        
        for (AdminOrderRequest.AdminOrderItemRequest item : request.getOrderDetails()) {
            bookRepository.findById(item.getIsbn())
                    .orElseThrow(() -> new RuntimeException("Book not found: " + item.getIsbn()));
            
            BigDecimal lineTotal = BigDecimal.valueOf(item.getUnitPrice()).multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(lineTotal);
        }

        BigDecimal shippingFee = (subtotal.compareTo(new BigDecimal("500000")) >= 0) ? BigDecimal.ZERO : new BigDecimal("30000");
        BigDecimal totalPayment = subtotal.add(shippingFee);

        String orderId = "ADM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order order = new Order();
        order.setOrderId(orderId);
        order.setCustomer(customer);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalItemsPrice(subtotal);
        order.setShippingFee(shippingFee);
        order.setTotalPayment(totalPayment);
        order.setStatusCode("CONFIRMED");
        order.setShippingAddress(request.getShippingAddress());

        orderRepository.save(order);

        for (AdminOrderRequest.AdminOrderItemRequest item : request.getOrderDetails()) {
            Book book = bookRepository.findById(item.getIsbn()).get();
            
            OrderDetail detail = new OrderDetail();
            detail.setId(new OrderDetailId(orderId, book.getIsbn()));
            detail.setOrder(order);
            detail.setBook(book);
            detail.setQuantity(item.getQuantity());
            detail.setFinalPrice(BigDecimal.valueOf(item.getUnitPrice()));
            
            orderDetailRepository.save(detail);
            
            OrderDetailDTO dto = new OrderDetailDTO();
            dto.setIsbn(book.getIsbn());
            dto.setTitle(book.getTitle());
            dto.setQuantity(item.getQuantity());
            dto.setFinalPrice(BigDecimal.valueOf(item.getUnitPrice()));
            detailDTOs.add(dto);
        }

        Payment payment = new Payment();
        payment.setPaymentId("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setOrder(order);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatusCode("PENDING");
        paymentRepository.save(payment);

        auditLogService.log(customer.getAccount(), "ADMIN_CREATE_ORDER", "Admin tạo đơn hàng #" + orderId + " cho khách hàng " + customer.getFullName());

        return mapToResponseDTO(order, detailDTOs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateOrderStatus(String orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        String oldStatus = order.getStatusCode();
        order.setStatusCode(status);
        orderRepository.save(order);

        if ("CANCELLED".equals(status) && !"CANCELLED".equals(oldStatus)) {
            restockInventory(order);
        }

        auditLogService.log("UPDATE_ORDER_STATUS", "Cập nhật đơn hàng #" + orderId + " sang trạng thái " + status);

        try {
            String currentUser = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            Account account = accountRepository.findByUsername(currentUser).orElse(null);
            if (account != null) {
                AuditLog log = new AuditLog();
                log.setAccount(account);
                log.setAction(AuditAction.UPDATE_ORDER_STATUS.name());
                log.setDetails("Order " + orderId + ": " + oldStatus + " -> " + status);
                auditLogRepository.save(log);
            }
        } catch (Exception e) {
            // Log audit failure silently
        }
    }

    private OrderResponseDTO mapToDTO(Order order) {
        List<OrderDetailDTO> detailList = orderDetailRepository.findByOrder(order)
                .stream()
                .map(detail -> {
                    OrderDetailDTO dto = new OrderDetailDTO();
                    dto.setIsbn(detail.getBook().getIsbn());
                    dto.setTitle(detail.getBook().getTitle());
                    dto.setQuantity(detail.getQuantity());
                    dto.setFinalPrice(detail.getFinalPrice());
                    return dto;
                })
                .collect(Collectors.toList());
        
        return mapToResponseDTO(order, detailList);
    }

    private OrderResponseDTO mapToResponseDTO(Order order, List<OrderDetailDTO> detailList) {
        OrderResponseDTO res = new OrderResponseDTO();
        res.setOrderId(order.getOrderId());
        res.setUsername(order.getCustomer().getAccount().getUsername());
        res.setCustomerName(order.getCustomer().getFullName());
        res.setCreatedAt(order.getCreatedAt());
        res.setSubtotal(order.getTotalItemsPrice());
        res.setShippingFee(order.getShippingFee());
        res.setTotalAmount(order.getTotalPayment());
        res.setStatus(order.getStatusCode());
        res.setShippingAddress(order.getShippingAddress());
        res.setVoucherId(order.getVoucher() != null ? order.getVoucher().getVoucherCode() : null);
        res.setOrderDetails(detailList);
        return res;
    }
}
