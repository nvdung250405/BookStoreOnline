package com.bookstore.dto;

import com.bookstore.entity.Book;
import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

public class BookDTO {
    private String isbn;
    private String title;
    private BigDecimal price;
    private Integer categoryId;
    private String categoryName;
    private Integer publisherId;
    private String publisherName;
    private String coverImage;
    private Set<String> authorNames;
    private String description;
    private Integer stockQuantity;
    private Set<Integer> authorIds;

    public BookDTO() {}

    public static BookDTO fromEntity(Book book) {
        BookDTO dto = new BookDTO();
        dto.setIsbn(book.getIsbn());
        dto.setTitle(book.getTitle());
        dto.setPrice(book.getPrice());
        dto.setDescription(book.getDescription());
        if (book.getCategory() != null) {
            dto.setCategoryId(book.getCategory().getCategoryId());
            dto.setCategoryName(book.getCategory().getCategoryName());
        }
        if (book.getPublisher() != null) {
            dto.setPublisherId(book.getPublisher().getPublisherId());
            dto.setPublisherName(book.getPublisher().getPublisherName());
        }
        dto.setCoverImage(book.getCoverImage());
        if (book.getAuthors() != null) {
            dto.setAuthorNames(book.getAuthors().stream()
                .map(author -> author.getAuthorName())
                .collect(Collectors.toSet()));
            dto.setAuthorIds(book.getAuthors().stream()
                .map(author -> author.getAuthorId())
                .collect(Collectors.toSet()));
        }
        if (book.getInventory() != null) {
            dto.setStockQuantity(book.getInventory().getStockQuantity());
        } else {
            dto.setStockQuantity(0);
        }
        return dto;
    }

    public Set<Integer> getAuthorIds() { return authorIds; }
    public void setAuthorIds(Set<Integer> authorIds) { this.authorIds = authorIds; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getPublisherId() { return publisherId; }
    public void setPublisherId(Integer publisherId) { this.publisherId = publisherId; }
    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public Set<String> getAuthorNames() { return authorNames; }
    public void setAuthorNames(Set<String> authorNames) { this.authorNames = authorNames; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
