package com.bookstore.service;

import com.bookstore.dto.*;
import com.bookstore.entity.*;
import com.bookstore.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new RuntimeException("Item not found in inventory with ISBN: " + isbn));

        // Refactored to Composition: Check if record exists in PhysicalBook table
        if (!physicalBookRepository.existsById(java.util.Objects.requireNonNull(isbn))) {
            throw new RuntimeException("Warning: ISBN " + isbn + " is NOT a physical book. E-books do not exist in physical inventory!");
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
        // Requirement: stock_quantity <= alert_threshold
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
        if (request.getSupplierId() == null) throw new IllegalArgumentException("Supplier ID cannot be empty");
        Supplier supplier = supplierRepository.findById(java.util.Objects.requireNonNull(request.getSupplierId()))
                .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + request.getSupplierId()));

        if (request.getStaffId() == null) throw new IllegalArgumentException("Staff ID cannot be empty");
        Staff staff = staffRepository.findById(java.util.Objects.requireNonNull(request.getStaffId()))
                .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + request.getStaffId()));

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
            Book book = bookRepository.findById(java.util.Objects.requireNonNull(item.getIsbn()))
                    .orElseThrow(() -> new RuntimeException("Book not found with ISBN: " + item.getIsbn()));

            if (!physicalBookRepository.existsById(java.util.Objects.requireNonNull(item.getIsbn()))) {
                throw new RuntimeException("Logic Error: Book " + item.getIsbn() + " is an E-Book, cannot be imported into physical inventory!");
            }

            PurchaseOrderDetailId detailId = new PurchaseOrderDetailId(purchaseOrderId, item.getIsbn());

            PurchaseOrderDetail detail = new PurchaseOrderDetail();
            detail.setId(detailId);
            detail.setPurchaseOrder(purchaseOrder);
            detail.setBook(book);
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(item.getUnitPrice());

            purchaseOrderDetailRepository.save(detail);
            
            // Log the change
            logInventoryChange(book, staff, "IMPORT", item.getQuantity(), "Purchased via " + purchaseOrderId);
        }

        return new PurchaseOrderResponseDTO(purchaseOrderId, totalAmount, "Stock imported successfully! Inventory updated.");
    }

    @Transactional
    public String exportStock(ExportOrderRequestDTO request) {
        String orderId = request.getOrderId();
        if (orderId == null) throw new IllegalArgumentException("Order ID cannot be empty");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (exportOrderRepository.existsByOrder(order)) {
            throw new RuntimeException("This order has already been exported!");
        }

        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(order);
        if (orderDetails.isEmpty()) {
            throw new RuntimeException("Error: Order has no products!");
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
        
        // ADDED: Log to Central Audit Trail
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
}