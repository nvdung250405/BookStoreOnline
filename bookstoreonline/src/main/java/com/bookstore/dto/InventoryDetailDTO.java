package com.bookstore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InventoryDetailDTO {
    private String isbn;
    @JsonProperty("tenSach")
    private String title;

    @JsonProperty("tonKho")
    private int stockQuantity;

    @JsonProperty("viTriKe")
    private String shelfLocation;

    public InventoryDetailDTO() {}

    public InventoryDetailDTO(String isbn, String title, int stockQuantity, String shelfLocation) {
        this.isbn = isbn;
        this.title = title;
        this.stockQuantity = stockQuantity;
        this.shelfLocation = shelfLocation;
    }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public String getShelfLocation() { return shelfLocation; }
    public void setShelfLocation(String shelfLocation) { this.shelfLocation = shelfLocation; }
}