package com.bookstore.dto;

import java.util.List;

public class AiSearchResult {
    private List<BookDTO> books;
    private UserContext extractedContext;

    public AiSearchResult() {}

    public AiSearchResult(List<BookDTO> books, UserContext extractedContext) {
        this.books = books;
        this.extractedContext = extractedContext;
    }

    public List<BookDTO> getBooks() { return books; }
    public void setBooks(List<BookDTO> books) { this.books = books; }
    public UserContext getExtractedContext() { return extractedContext; }
    public void setExtractedContext(UserContext extractedContext) { this.extractedContext = extractedContext; }

    public static AiSearchResultBuilder builder() {
        return new AiSearchResultBuilder();
    }

    public static class AiSearchResultBuilder {
        private List<BookDTO> books;
        private UserContext extractedContext;

        public AiSearchResultBuilder books(List<BookDTO> books) { this.books = books; return this; }
        public AiSearchResultBuilder extractedContext(UserContext extractedContext) { this.extractedContext = extractedContext; return this; }

        public AiSearchResult build() {
            return new AiSearchResult(books, extractedContext);
        }
    }
}
