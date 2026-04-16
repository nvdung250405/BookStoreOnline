package com.bookstore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Integer inventoryId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isbn", unique = true, nullable = false)
    private Book book;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Column(name = "shelf_location", length = 50)
    private String shelfLocation;

    @Column(name = "alert_threshold")
    private Integer alertThreshold = 5;

    public Inventory() {}

    // 2. THÊM MỚI: Constructor 3 tham số để bạn gọi ở Service
    public Inventory(Book book, Integer stockQuantity, String shelfLocation) {
        this.book = book;
        this.stockQuantity = stockQuantity;
        this.shelfLocation = shelfLocation;
        this.alertThreshold = 5;
    }

    public Integer getInventoryId() { return inventoryId; }
    public void setInventoryId(Integer inventoryId) { this.inventoryId = inventoryId; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public String getShelfLocation() { return shelfLocation; }
    public void setShelfLocation(String shelfLocation) { this.shelfLocation = shelfLocation; }
    public Integer getAlertThreshold() { return alertThreshold; }
    public void setAlertThreshold(Integer alertThreshold) { this.alertThreshold = alertThreshold; }
}
