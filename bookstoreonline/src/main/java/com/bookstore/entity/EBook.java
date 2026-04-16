package com.bookstore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ebooks")
public class EBook {
    @Id
    @Column(length = 13)
    private String isbn;

    @Column(name = "file_size", precision = 10, scale = 2)
    private java.math.BigDecimal fileSize;

    @Column(name = "download_url", length = 255)
    private String downloadUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "isbn")
    private Book book;

    public EBook() {}

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public java.math.BigDecimal getFileSize() { return fileSize; }
    public void setFileSize(java.math.BigDecimal fileSize) { this.fileSize = fileSize; }
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
}
