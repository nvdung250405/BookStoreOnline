package com.bookstore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "authors")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "author_id")
    private Integer authorId;

    @Column(name = "author_name", nullable = false, length = 100)
    private String authorName;

    @Column(name = "biography", columnDefinition = "NVARCHAR(MAX)")
    private String biography;

    @ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
    private java.util.Set<Book> books;

    public Author() {}

    public Integer getAuthorId() { return authorId; }
    public void setAuthorId(Integer authorId) { this.authorId = authorId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }
    public java.util.Set<Book> getBooks() { return books; }
    public void setBooks(java.util.Set<Book> books) { this.books = books; }
}
