package com.bookstore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class InventoryAdjustmentRequest {
    @NotBlank(message = "ISBN is required")
    private String isbn;

    @NotNull(message = "New quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer newQuantity;

    @NotBlank(message = "Reason is required")
    private String reason;

    private Integer staffId;

    public InventoryAdjustmentRequest() {}

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Integer getNewQuantity() { return newQuantity; }
    public void setNewQuantity(Integer newQuantity) { this.newQuantity = newQuantity; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }
}
