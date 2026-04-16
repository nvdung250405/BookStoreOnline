package com.bookstore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "publishers")
public class Publisher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "publisher_id")
    private Integer publisherId;

    @Column(name = "publisher_name", nullable = false, length = 150)
    private String publisherName;

    @OneToMany(mappedBy = "publisher", fetch = FetchType.LAZY)
    private java.util.Set<Book> books;

    public Publisher() {}

    public Integer getPublisherId() { return publisherId; }
    public void setPublisherId(Integer publisherId) { this.publisherId = publisherId; }
    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }
    public java.util.Set<Book> getBooks() { return books; }
    public void setBooks(java.util.Set<Book> books) { this.books = books; }
}
