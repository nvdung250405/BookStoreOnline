package com.bookstore.service;

import com.bookstore.dto.*;
import com.bookstore.entity.*;
import com.bookstore.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderDetailRepository purchaseOrderDetailRepository;
    private final SupplierRepository supplierRepository;
    private final StaffRepository staffRepository;
    private final BookRepository bookRepository;
    private final ExportOrderRepository exportOrderRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ExportOrderDetailRepository exportOrderDetailRepository;
    private final PhysicalBookRepository physicalBookRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final AuditLogService auditLogService;

    public InventoryService(InventoryRepository inventoryRepository,
                            PurchaseOrderRepository purchaseOrderRepository,
                            PurchaseOrderDetailRepository purchaseOrderDetailRepository,
                            SupplierRepository supplierRepository,
                            StaffRepository staffRepository,
                            BookRepository bookRepository,
                            ExportOrderRepository exportOrderRepository,
                            OrderRepository orderRepository,
                            OrderDetailRepository orderDetailRepository,
                            ExportOrderDetailRepository exportOrderDetailRepository,
                            PhysicalBookRepository physicalBookRepository,
                            InventoryLogRepository inventoryLogRepository,
                            AuditLogService auditLogService) {
        this.inventoryRepository = inventoryRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderDetailRepository = purchaseOrderDetailRepository;
        this.supplierRepository = supplierRepository;
        this.staffRepository = staffRepository;
        this.bookRepository = bookRepository;
        this.exportOrderRepository = exportOrderRepository;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.exportOrderDetailRepository = exportOrderDetailRepository;
        this.physicalBookRepository = physicalBookRepository;
        this.inventoryLogRepository = inventoryLogRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public InventoryDetailDTO scanBarcode(String isbn) {
        Inventory inventory = inventoryRepository.findByBook_Isbn(isbn)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm trong kho với mã ISBN: " + isbn));

        // Refactored to Composition: Check if record exists in PhysicalBook table
        if (!physicalBookRepository.existsById(java.util.Objects.requireNonNull(isbn))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Warning: ISBN " + isbn + " belongs to an E-Book. E-books do not exist in physical inventory!");
        }

        return new InventoryDetailDTO(
                isbn,
                inventory.getBook().getTitle(),
                inventory.getStockQuantity(),
                inventory.getShelfLocation()
        );
    }

    @Transactional(readOnly = true)
    public List<LowStockAlertDTO> getLowStockAlerts() {
        List<Inventory> all = inventoryRepository.findAll();

        return all.stream()
                .filter(inv -> inv.getStockQuantity() <= inv.getAlertThreshold())
                .map(inventory -> new LowStockAlertDTO(
                        inventory.getBook().getIsbn(),
                        inventory.getBook().getTitle(),
                        inventory.getStockQuantity(),
                        inventory.getAlertThreshold()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public PurchaseOrderResponseDTO importStock(PurchaseOrderRequestDTO request) {
        if (request.getSupplierId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Supplier ID cannot be empty");
        }
        Integer supplierId = request.getSupplierId();
        Supplier supplier = supplierRepository.findById(java.util.Objects.requireNonNull(supplierId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found with ID: " + supplierId));

        if (request.getStaffId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff ID cannot be empty");
        }
        Integer staffId = request.getStaffId();
        Staff staff = staffRepository.findById(java.util.Objects.requireNonNull(staffId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff not found with ID: " + staffId));

        BigDecimal totalAmount = request.getDetails().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String purchaseOrderId = "PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setPurchaseOrderId(purchaseOrderId);
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setStaff(staff);
        purchaseOrder.setOrderDate(LocalDateTime.now());
        purchaseOrder.setTotalAmount(totalAmount);

        purchaseOrderRepository.save(purchaseOrder);

        for (PurchaseOrderDetailRequest item : request.getDetails()) {
            if (item.getIsbn() == null) continue;
            Book book = bookRepository.findById(item.getIsbn())
                    .orElseGet(() -> {
                        // Nếu không tìm thấy, hệ thống sẽ tự động tạo một bản ghi PhysicalBook mới
                        PhysicalBook newBook = new PhysicalBook();
                        newBook.setIsbn(item.getIsbn());
                        newBook.setTitle(item.getTitle() != null ? item.getTitle() : "Sách mới"); // Tên tạm thời

                        // Fix vụ Ref Price = 0: Lấy giá nhập nhân 1.2 làm giá bán mặc định
                        newBook.setPrice(item.getUnitPrice().multiply(new BigDecimal("1.2")));

                        // Lưu sách mới vào database trước khi tiếp tục
                        return bookRepository.save(newBook);
                    });

            // Chặn đứng nhập kho nếu là sách điện tử (E-Book không có kho vật lý)
            if (!(book instanceof PhysicalBook)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Logic Error: Sách " + book.getTitle() + " là E-Book, không thể nhập kho vật lý!");
            }

            // 2. CẬP NHẬT GIÁ THAM CHIẾU (Ref Price)
            // Nếu giá bằng 0, tự động đặt giá bán = giá nhập * 1.2
            if (book.getPrice() == null || book.getPrice().compareTo(BigDecimal.ZERO) == 0) {
                book.setPrice(item.getUnitPrice().multiply(new BigDecimal("1.2")));
                bookRepository.save(book);
            }

            // 3. CẬP NHẬT TỒN KHO THỰC TẾ (Inventory)
            Inventory inv = inventoryRepository.findByBook_Isbn(item.getIsbn())
                    .orElseGet(() -> {
                        Inventory newInv = new Inventory();
                        newInv.setBook(book);
                        newInv.setStockQuantity(0);
                        newInv.setAlertThreshold(5);
                        return newInv;
                    });

            // Cộng dồn số lượng và cập nhật vị trí kệ mới nhất từ DTO
            inv.setStockQuantity((inv.getStockQuantity() == null ? 0 : inv.getStockQuantity()) + item.getQuantity());
            inv.setShelfLocation(item.getShelfLocation());
            inventoryRepository.save(inv); // Lưu thông tin kho thực tế

            // 4. LƯU CHI TIẾT PHIẾU NHẬP (History)
            // Tạo ID phức hợp cho chi tiết đơn nhập
            PurchaseOrderDetailId detailId = new PurchaseOrderDetailId(purchaseOrderId, item.getIsbn());

            PurchaseOrderDetail detail = new PurchaseOrderDetail();
            detail.setId(detailId);
            detail.setPurchaseOrder(purchaseOrder);
            detail.setBook(book);
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(item.getUnitPrice());

            purchaseOrderDetailRepository.save(detail); // Lưu vào bảng lịch sử nhập hàng
        }

        return new PurchaseOrderResponseDTO(purchaseOrderId, totalAmount, "Stock imported successfully!");
    }

    @Transactional
    public String exportStock(ExportOrderRequestDTO request) {
        String orderId = request.getOrderId();
        if (orderId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order ID cannot be empty");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + orderId));

        if (exportOrderRepository.existsByOrder(order)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This order has already been exported!");
        }

        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(order);
        if (orderDetails.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error: Order has no products!");
        }

        ExportOrder exportOrder = new ExportOrder();
        String exportOrderId = "EX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        exportOrder.setExportOrderId(exportOrderId);
        exportOrder.setOrder(order);
        exportOrder.setExportDate(LocalDateTime.now());
        exportOrderRepository.save(exportOrder);

        for (OrderDetail detail : orderDetails) {
            String isbn = detail.getBook().getIsbn();
            if (physicalBookRepository.existsById(java.util.Objects.requireNonNull(isbn))) {
                ExportOrderDetailId detailId = new ExportOrderDetailId(exportOrderId, isbn);
                ExportOrderDetail exportDetail = new ExportOrderDetail(detailId, detail.getQuantity());
                exportOrderDetailRepository.save(exportDetail);
                
                logInventoryChange(detail.getBook(), null, "EXPORT", -detail.getQuantity(), "Exported for Order " + orderId);
            }
        }

        order.setStatusCode("AWAITING_SHIPMENT");
        orderRepository.save(order);

        return "Stock exported successfully for order " + orderId + ". Export ID: " + exportOrderId;
    }

    @Transactional
    public void adjustStock(String isbn, Integer newQuantity, String reason, Integer staffId) {
        Inventory inv = inventoryRepository.findByBook_Isbn(isbn)
                .orElseThrow(() -> new RuntimeException("Inventory record not found for ISBN: " + isbn));
        
        Staff staff = staffId != null ? staffRepository.findById(staffId).orElse(null) : null;
        
        int oldQty = inv.getStockQuantity();
        int diff = newQuantity - oldQty;
        
        inv.setStockQuantity(newQuantity);
        inventoryRepository.save(inv);
        
        logInventoryChange(inv.getBook(), staff, "ADJUST", diff, reason);
        
        if (staff != null && staff.getAccount() != null) {
            String details = String.format("Stock Adjustment for %s [%s]: %d -> %d. Reason: %s", 
                    inv.getBook().getTitle(), isbn, oldQty, newQuantity, reason);
            auditLogService.log(staff.getAccount(), "STOCK_ADJUSTMENT", details);
        }
    }

    private void logInventoryChange(Book book, Staff staff, String type, int change, String notes) {
        Inventory inv = inventoryRepository.findByBook_Isbn(book.getIsbn()).orElse(null);
        int after = (inv != null) ? inv.getStockQuantity() : 0;
        
        InventoryLog log = new InventoryLog();
        log.setBook(book);
        log.setStaff(staff);
        log.setChangeType(type);
        log.setQuantityChanged(change);
        log.setQuantityAfter(after);
        log.setTimestamp(LocalDateTime.now());
        log.setNotes(notes);
        inventoryLogRepository.save(log);
    }

    public List<InventoryDetailDTO> getAllInventory() {
        return inventoryRepository.findAllInventoryDetails();
    }
}