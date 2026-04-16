package com.bookstore.service;

import com.bookstore.dto.BookDTO;
import com.bookstore.enums.BookStatus;
import com.bookstore.repository.*;
import com.bookstore.dto.BookCreateRequest;
import com.bookstore.dto.BookUpdateRequest;
import com.bookstore.entity.*;
import com.bookstore.service.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final PhysicalBookRepository physicalBookRepository;
    private final EBookRepository eBookRepository;
    private final InventoryRepository inventoryRepository;
    private final AuditLogService auditLogService;

    public BookService(BookRepository bookRepository,
            CategoryRepository categoryRepository,
            PublisherRepository publisherRepository,
            AuthorRepository authorRepository,
            PhysicalBookRepository physicalBookRepository,
            EBookRepository eBookRepository,
            InventoryRepository inventoryRepository) {
            AuditLogService auditLogService) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.publisherRepository = publisherRepository;
        this.authorRepository = authorRepository;
        this.physicalBookRepository = physicalBookRepository;
        this.eBookRepository = eBookRepository;
        this.inventoryRepository = inventoryRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<BookDTO> searchAndFilterBooks(
            String keyword, Long categoryId, com.bookstore.enums.BookStatus status,
            java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, org.springframework.data.domain.Pageable pageable) {
        return bookRepository.searchAndFilterBooks(keyword, categoryId, status, minPrice, maxPrice, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public BookDTO getBookByIsbn(String isbn) {
        Book book = bookRepository.findById(java.util.Objects.requireNonNull(isbn))
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ISBN: " + isbn));
        
        if (book.getStatus() == null || book.getStatus() != BookStatus.ACTIVE) {
             throw new IllegalArgumentException("This book has been deleted from the system");
        }
        return convertToDTO(book);
    }

    @Transactional
    public BookDTO createBook(BookCreateRequest request) {
        if (bookRepository.existsById(java.util.Objects.requireNonNull(request.getIsbn()))) {
            throw new IllegalArgumentException("Mã ISBN " + request.getIsbn() + " đã tồn tại trong hệ thống. Vui lòng nhập mã khác.");
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

        return convertToDTO(savedBook);
        auditLogService.log("CREATE_BOOK", "Thêm sách mới: " + savedBook.getTitle() + " (ISBN: " + savedBook.getIsbn() + ")");
        return BookDTO.fromEntity(savedBook);
    }

    @Transactional
    public BookDTO updateBook(String isbn, BookUpdateRequest request) {
        Book book = bookRepository.findById(java.util.Objects.requireNonNull(isbn))
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ISBN: " + isbn));

        if (book.getStatus() == null || book.getStatus() != BookStatus.ACTIVE) {
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

        return convertToDTO(savedBook);
        auditLogService.log("UPDATE_BOOK", "Cập nhật sách: " + savedBook.getTitle() + " (ISBN: " + savedBook.getIsbn() + ")");
        return BookDTO.fromEntity(savedBook);
    }

    @Transactional
    public void softDeleteBook(String isbn) {
        Book book = bookRepository.findById(java.util.Objects.requireNonNull(isbn))
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ISBN: " + isbn));
        
        if (book.getStatus() == null || book.getStatus() != BookStatus.ACTIVE) {
             throw new IllegalArgumentException("This book was already deleted");
        }
        
        book.setStatus(BookStatus.INACTIVE);
        book.setDeletedAt(java.time.LocalDateTime.now());
        bookRepository.save(book);
        auditLogService.log("DELETE_BOOK", "Xóa sách: " + book.getTitle() + " (ISBN: " + book.getIsbn() + ")");
    }

    private BookDTO convertToDTO(Book book) {
        BookDTO dto = BookDTO.fromEntity(book);
        
        // Populate Subtype Info
        physicalBookRepository.findById(book.getIsbn()).ifPresent(pb -> {
            dto.setBookType("PHYSICAL");
            dto.setWeight(pb.getWeight());
        });
        
        eBookRepository.findById(book.getIsbn()).ifPresent(eb -> {
            dto.setBookType("EBOOK");
            dto.setFileSize(eb.getFileSize());
            dto.setDownloadUrl(eb.getDownloadUrl());
        });

        if (book != null && book.getIsbn() != null && inventoryRepository != null) {
            inventoryRepository.findByBook_Isbn(book.getIsbn()).ifPresent(inv -> {
                dto.setStockQuantity(inv.getStockQuantity());
                dto.setInventoryStatus(inv.getStatus() != null ? inv.getStatus().name() : null);
            });
        }
        return dto;
    }

    @Transactional
    public BookDTO updateStatus(String isbn, com.bookstore.enums.BookStatus status) {
        Book book = bookRepository.findById(java.util.Objects.requireNonNull(isbn))
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ISBN: " + isbn));
        
        book.setStatus(status);
        if (status == com.bookstore.enums.BookStatus.INACTIVE) {
            book.setDeletedAt(java.time.LocalDateTime.now());
        } else {
            book.setDeletedAt(null);
        }
        
        return convertToDTO(bookRepository.save(book));
    }
}
