package com.bookstore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ebooks")
@PrimaryKeyJoinColumn(name = "isbn")
public class EBook extends Book {

    @Column(name = "file_size", precision = 10, scale = 2)
    private java.math.BigDecimal fileSize;

    @Column(name = "download_url", length = 255)
    private String downloadUrl;

    public EBook() {}

    public java.math.BigDecimal getFileSize() { return fileSize; }
    public void setFileSize(java.math.BigDecimal fileSize) { this.fileSize = fileSize; }
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
}
