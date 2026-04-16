package com.bookstore.dto;

import java.math.BigDecimal;

public class UserContext {
    private String categoryName;
    private String publisherName;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String lastKeyword;
    
    public UserContext() {}

    public UserContext(String categoryName, String publisherName, BigDecimal minPrice, BigDecimal maxPrice, String lastKeyword) {
        this.categoryName = categoryName;
        this.publisherName = publisherName;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.lastKeyword = lastKeyword;
    }

    public void update(UserContext other) {
        if (other.getCategoryName() != null) this.categoryName = other.getCategoryName();
        if (other.getPublisherName() != null) this.publisherName = other.getPublisherName();
        if (other.getMinPrice() != null) this.minPrice = other.getMinPrice();
        if (other.getMaxPrice() != null) this.maxPrice = other.getMaxPrice();
        if (other.getLastKeyword() != null) this.lastKeyword = other.getLastKeyword();
    }

    // Getters and Setters
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    public String getLastKeyword() { return lastKeyword; }
    public void setLastKeyword(String lastKeyword) { this.lastKeyword = lastKeyword; }

    // Static builder-like method for convenience
    public static UserContextBuilder builder() {
        return new UserContextBuilder();
    }

    public static class UserContextBuilder {
        private String categoryName;
        private String publisherName;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private String lastKeyword;

        public UserContextBuilder categoryName(String categoryName) { this.categoryName = categoryName; return this; }
        public UserContextBuilder publisherName(String publisherName) { this.publisherName = publisherName; return this; }
        public UserContextBuilder minPrice(BigDecimal minPrice) { this.minPrice = minPrice; return this; }
        public UserContextBuilder maxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; return this; }
        public UserContextBuilder lastKeyword(String lastKeyword) { this.lastKeyword = lastKeyword; return this; }
        
        public UserContext build() {
            return new UserContext(categoryName, publisherName, minPrice, maxPrice, lastKeyword);
        }
    }
}
