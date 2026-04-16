package com.bookstore.service;

import com.bookstore.dto.PublisherDTO;
import com.bookstore.entity.Publisher;
import com.bookstore.repository.PublisherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PublisherService {

    private final PublisherRepository publisherRepository;

    public PublisherService(PublisherRepository publisherRepository) {
        this.publisherRepository = publisherRepository;
    }

    @Transactional(readOnly = true)
    public List<PublisherDTO> getAllPublishers() {
        return publisherRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PublisherDTO savePublisher(PublisherDTO dto) {
        Publisher publisher = new Publisher();
        publisher.setPublisherName(dto.getPublisherName());
        return convertToDTO(publisherRepository.save(publisher));
    }

    @Transactional
    public PublisherDTO updatePublisher(Integer id, PublisherDTO dto) {
        Publisher publisher = publisherRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new RuntimeException("Publisher not found with ID: " + id));
        publisher.setPublisherName(dto.getPublisherName());
        return convertToDTO(publisherRepository.save(publisher));
    }

    @Transactional
    public void deletePublisher(Integer id) {
        Publisher publisher = publisherRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new RuntimeException("Publisher not found with ID: " + id));
        
        // Check if publisher has books
        if (publisher.getBooks() != null && !publisher.getBooks().isEmpty()) {
            throw new RuntimeException("Cannot delete publisher with associated books. Please remove or reassign books first.");
        }
        
        publisherRepository.delete(publisher);
    }

    private PublisherDTO convertToDTO(Publisher entity) {
        PublisherDTO dto = new PublisherDTO();
        dto.setPublisherId(entity.getPublisherId());
        dto.setPublisherName(entity.getPublisherName());
        return dto;
    }
}
