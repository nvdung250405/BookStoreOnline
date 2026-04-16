package com.bookstore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "physical_books")
public class PhysicalBook {
    @Id
    @Column(length = 13)
    private String isbn;

    @Column(name = "weight", precision = 5, scale = 2)
    private java.math.BigDecimal weight;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "isbn")
    private Book book;

    public PhysicalBook() {}

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public java.math.BigDecimal getWeight() { return weight; }
    public void setWeight(java.math.BigDecimal weight) { this.weight = weight; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
}
