package com.bookstore.service;

import com.bookstore.dto.BookDTO;
import com.bookstore.repository.*;
import com.bookstore.dto.BookCreateRequest;
import com.bookstore.dto.BookUpdateRequest;
import com.bookstore.entity.*;
import com.bookstore.service.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final PhysicalBookRepository physicalBookRepository;
    private final EBookRepository eBookRepository;
    private final AuditLogService auditLogService;

    public BookService(BookRepository bookRepository,
            CategoryRepository categoryRepository,
            PublisherRepository publisherRepository,
            AuthorRepository authorRepository,
            PhysicalBookRepository physicalBookRepository,
            EBookRepository eBookRepository,
            AuditLogService auditLogService) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.publisherRepository = publisherRepository;
        this.authorRepository = authorRepository;
        this.physicalBookRepository = physicalBookRepository;
        this.eBookRepository = eBookRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<BookDTO> searchAndFilterBooks(String keyword, String categoryName, String publisherName,
            BigDecimal minPrice, BigDecimal maxPrice) {
        return bookRepository.searchAndFilterBooks(keyword, categoryName, publisherName, minPrice, maxPrice)
                .stream()
                .map(BookDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookDTO getBookByIsbn(String isbn) {
        Book book = bookRepository.findById(java.util.Objects.requireNonNull(isbn))
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ISBN: " + isbn));
        
        if (Boolean.TRUE.equals(book.getIsDeleted())) {
             throw new IllegalArgumentException("This book has been deleted from the system");
        }
        return BookDTO.fromEntity(book);
    }

    @Transactional
    public BookDTO createBook(BookCreateRequest request) {
        if (bookRepository.existsById(java.util.Objects.requireNonNull(request.getIsbn()))) {
            throw new IllegalArgumentException("Book with this ISBN already exists: " + request.getIsbn());
        }

        Book book = new Book();
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setPrice(request.getPrice());
        book.setDescription(request.getDescription());
        book.setCoverImage(request.getCoverImage());
        book.setCoverAlt(request.getCoverAlt());

        Category category = categoryRepository.findById(java.util.Objects.requireNonNull(request.getCategoryId()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Category not found with ID: " + request.getCategoryId()));
        book.setCategory(category);

        Publisher publisher = publisherRepository.findById(java.util.Objects.requireNonNull(request.getPublisherId()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Publisher not found with ID: " + request.getPublisherId()));
        book.setPublisher(publisher);

        if (request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
            List<Author> authors = authorRepository.findAllById(java.util.Objects.requireNonNull(request.getAuthorIds()));
            if (authors.size() != request.getAuthorIds().size()) {
                throw new IllegalArgumentException("One or more author IDs are invalid");
            }
            book.setAuthors(new java.util.HashSet<>(authors));
        }

        Book savedBook = bookRepository.saveAndFlush(book);

        // Core Book + Subtype composition
        if ("PHYSICAL".equalsIgnoreCase(request.getBookType())) {
            PhysicalBook physical = new PhysicalBook();
            physical.setWeight(request.getWeight());
            physical.setBook(savedBook);
            physicalBookRepository.save(physical);
        } else if ("EBOOK".equalsIgnoreCase(request.getBookType())) {
            EBook ebook = new EBook();
            ebook.setFileSize(request.getFileSize());
            ebook.setDownloadUrl(request.getDownloadUrl());
            ebook.setBook(savedBook);
            eBookRepository.save(ebook);
        }

        auditLogService.log("CREATE_BOOK", "Thêm sách mới: " + savedBook.getTitle() + " (ISBN: " + savedBook.getIsbn() + ")");
        return BookDTO.fromEntity(savedBook);
    }

    @Transactional
    public BookDTO updateBook(String isbn, BookUpdateRequest request) {
        Book book = bookRepository.findById(java.util.Objects.requireNonNull(isbn))
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ISBN: " + isbn));

        if (Boolean.TRUE.equals(book.getIsDeleted())) {
            throw new IllegalArgumentException("This book has been deleted and cannot be modified");
        }

        book.setTitle(request.getTitle());
        book.setPrice(request.getPrice());
        book.setDescription(request.getDescription());
        book.setCoverImage(request.getCoverImage());
        book.setCoverAlt(request.getCoverAlt());

        Category category = categoryRepository.findById(java.util.Objects.requireNonNull(request.getCategoryId()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Category not found with ID: " + request.getCategoryId()));
        book.setCategory(category);

        Publisher publisher = publisherRepository.findById(java.util.Objects.requireNonNull(request.getPublisherId()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Publisher not found with ID: " + request.getPublisherId()));
        book.setPublisher(publisher);

        if (request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
            List<Author> authors = authorRepository.findAllById(java.util.Objects.requireNonNull(request.getAuthorIds()));
            if (authors.size() != request.getAuthorIds().size()) {
                throw new IllegalArgumentException("One or more author IDs are invalid");
            }
            book.setAuthors(new java.util.HashSet<>(authors));
        } else {
            book.setAuthors(new java.util.HashSet<>());
        }

        Book savedBook = bookRepository.saveAndFlush(book);

        // TYPE SWITCHING / UPDATE LOGIC
        // 1. Remove old subtypes
        physicalBookRepository.deleteById(java.util.Objects.requireNonNull(isbn));
        eBookRepository.deleteById(java.util.Objects.requireNonNull(isbn));

        // 2. Add new subtype
        if ("PHYSICAL".equalsIgnoreCase(request.getBookType())) {
            PhysicalBook physical = new PhysicalBook();
            physical.setWeight(request.getWeight());
            physical.setBook(savedBook);
            physicalBookRepository.save(physical);
        } else if ("EBOOK".equalsIgnoreCase(request.getBookType())) {
            EBook ebook = new EBook();
            ebook.setFileSize(request.getFileSize());
            ebook.setDownloadUrl(request.getDownloadUrl());
            ebook.setBook(savedBook);
            eBookRepository.save(ebook);
        }

        auditLogService.log("UPDATE_BOOK", "Cập nhật sách: " + savedBook.getTitle() + " (ISBN: " + savedBook.getIsbn() + ")");
        return BookDTO.fromEntity(savedBook);
    }

    @Transactional
    public void softDeleteBook(String isbn) {
        Book book = bookRepository.findById(java.util.Objects.requireNonNull(isbn))
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ISBN: " + isbn));
        
        if (Boolean.TRUE.equals(book.getIsDeleted())) {
             throw new IllegalArgumentException("This book was already deleted");
        }
        
        book.setIsDeleted(true);
        book.setDeletedAt(java.time.LocalDateTime.now());
        bookRepository.save(book);
        auditLogService.log("DELETE_BOOK", "Xóa sách: " + book.getTitle() + " (ISBN: " + book.getIsbn() + ")");
    }
}
