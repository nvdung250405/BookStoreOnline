package com.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Set;

public class BookUpdateRequest {

    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotNull(message = "Price cannot be empty")
    @PositiveOrZero(message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    @NotNull(message = "Category ID cannot be empty")
    private Integer categoryId;

    @NotNull(message = "Publisher ID cannot be empty")
    private Integer publisherId;

    private String description;

    @NotBlank(message = "Book type cannot be empty")
    private String bookType; // PHYSICAL | EBOOK

    private java.math.BigDecimal weight;

    private java.math.BigDecimal fileSize;

    private String downloadUrl;

    private String coverImage;

    private Set<Integer> authorIds;

    public String getBookType() { return bookType; }
    public void setBookType(String bookType) { this.bookType = bookType; }
    public java.math.BigDecimal getWeight() { return weight; }
    public void setWeight(java.math.BigDecimal weight) { this.weight = weight; }
    public java.math.BigDecimal getFileSize() { return fileSize; }
    public void setFileSize(java.math.BigDecimal fileSize) { this.fileSize = fileSize; }
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public Integer getPublisherId() { return publisherId; }
    public void setPublisherId(Integer publisherId) { this.publisherId = publisherId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public Set<Integer> getAuthorIds() { return authorIds; }
    public void setAuthorIds(Set<Integer> authorIds) { this.authorIds = authorIds; }
}
