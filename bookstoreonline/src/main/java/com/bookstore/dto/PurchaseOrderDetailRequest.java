package com.bookstore.dto;

import jakarta.persistence.Column;

import java.math.BigDecimal;

public class PurchaseOrderDetailRequest {
    private String isbn;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String shelfLocation; // Thêm để hứng dữ liệu từ UI
    private String title; // THÊM TRƯỜNG NÀY ĐỂ NHẬN TÊN TỪ UI

    public PurchaseOrderDetailRequest() {}

    public PurchaseOrderDetailRequest(String isbn, Integer quantity, BigDecimal unitPrice) {
        this.isbn = isbn;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getShelfLocation() { return shelfLocation; }
    public void setShelfLocation(String shelfLocation) { this.shelfLocation = shelfLocation; }
    // Thêm Getter/Setter cho title
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}