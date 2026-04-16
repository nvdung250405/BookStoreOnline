package com.bookstore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_details")
public class PurchaseOrderDetail {
    @EmbeddedId
    private PurchaseOrderDetailId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("purchaseOrderId")
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isbn")
    @MapsId("isbn")
    private Book book;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "purchase_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    public PurchaseOrderDetail() {}

    public PurchaseOrderDetailId getId() { return id; }
    public void setId(PurchaseOrderDetailId id) { this.id = id; }
    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
