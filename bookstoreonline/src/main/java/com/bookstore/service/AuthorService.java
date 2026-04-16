package com.bookstore.service;

import com.bookstore.dto.AuthorDTO;
import com.bookstore.entity.Author;
import com.bookstore.repository.AuthorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Transactional(readOnly = true)
    public List<AuthorDTO> getAllAuthors() {
        return authorRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AuthorDTO saveAuthor(AuthorDTO dto) {
        Author author = new Author();
        author.setAuthorName(dto.getAuthorName());
        author.setBiography(dto.getBiography());
        Author saved = authorRepository.save(author);
        return convertToDTO(saved);
    }

    @Transactional
    public AuthorDTO updateAuthor(int id, AuthorDTO dto) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with ID: " + id));
        author.setAuthorName(dto.getAuthorName());
        author.setBiography(dto.getBiography());
        return convertToDTO(authorRepository.save(author));
    }

    @Transactional
    public void deleteAuthor(int id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with ID: " + id));
        
        // Check if author has books (Many-to-Many check)
        if (author.getBooks() != null && !author.getBooks().isEmpty()) {
            throw new RuntimeException("Cannot delete author with associated books. Please remove books first.");
        }
        
        authorRepository.delete(author);
    }

    private AuthorDTO convertToDTO(Author entity) {
        AuthorDTO dto = new AuthorDTO();
        dto.setAuthorId(entity.getAuthorId());
        dto.setAuthorName(entity.getAuthorName());
        dto.setBiography(entity.getBiography());
        return dto;
    }
}
