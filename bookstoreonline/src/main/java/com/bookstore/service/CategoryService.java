package com.bookstore.service;

import com.bookstore.constant.AuditAction;
import com.bookstore.dto.CategoryDTO;
import com.bookstore.dto.CategoryRequest;
import com.bookstore.entity.Account;
import com.bookstore.entity.AuditLog;
import com.bookstore.entity.Category;
import com.bookstore.repository.AccountRepository;
import com.bookstore.repository.AuditLogRepository;
import com.bookstore.repository.CategoryRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AuditLogRepository auditLogRepository;
    private final AccountRepository accountRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           AuditLogRepository auditLogRepository,
                           AccountRepository accountRepository) {
        this.categoryRepository = categoryRepository;
        this.auditLogRepository = auditLogRepository;
        this.accountRepository = accountRepository;
    }

    private void saveAuditLog(AuditAction action, String details) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Account account = accountRepository.findByUsername(username).orElse(null);
            if (account != null) {
                AuditLog log = new AuditLog();
                log.setAccount(account);
                log.setAction(action.name());
                log.setDetails(details);
                auditLogRepository.save(log);
            }
        } catch (Exception e) {
            // Failure to log shouldn't break the business flow
        }
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        java.util.Set<Integer> visited = new java.util.HashSet<>();
        return categoryRepository.findByParentIsNull()
                .stream()
                .map(entity -> convertToDTO(entity, visited))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    private CategoryDTO convertToDTO(Category entity) {
        return convertToDTO(entity, new java.util.HashSet<>());
    }

    private CategoryDTO convertToDTO(Category entity, java.util.Set<Integer> visited) {
        if (entity == null || visited.contains(entity.getCategoryId())) return null;
        visited.add(entity.getCategoryId());

        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(entity.getCategoryId());
        dto.setCategoryName(entity.getCategoryName());
        dto.setParentId(entity.getParent() != null ? entity.getParent().getCategoryId() : null);
        
        if (entity.getChildren() != null) {
            dto.setSubCategories(entity.getChildren()
                    .stream()
                    .map(child -> convertToDTO(child, visited))
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    @Transactional
    public CategoryDTO createCategory(CategoryRequest request) {
        Category entity = new Category();
        entity.setCategoryName(request.getCategoryName());
        
        Integer parentId = request.getParentId();
        if (parentId != null) {
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found with ID: " + parentId));
            entity.setParent(parent);
        }
        
        CategoryDTO result = convertToDTO(categoryRepository.save(entity));
        saveAuditLog(AuditAction.CREATE_CATEGORY, "Created category: " + result.getCategoryName());
        return result;
    }

    @Transactional
    public CategoryDTO updateCategory(Integer id, CategoryRequest request) {
        if (id == null) throw new IllegalArgumentException("Category ID cannot be empty");
        Category entity = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));
        
        if (request.getCategoryName() != null && !request.getCategoryName().isBlank()) {
            entity.setCategoryName(request.getCategoryName());
        }
        
        Integer parentId = request.getParentId();
        if (parentId != null) {
            if (id.equals(parentId)) {
                 throw new IllegalArgumentException("A category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found with ID: " + parentId));
            entity.setParent(parent);
        } else {
            entity.setParent(null);
        }
        
        CategoryDTO result = convertToDTO(categoryRepository.save(entity));
        saveAuditLog(AuditAction.UPDATE_CATEGORY, "Updated category ID " + id + ": " + result.getCategoryName());
        return result;
    }

    @Transactional
    public void deleteCategory(Integer id) {
        if (id == null) return;
        Category entity = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));
        try {
            String catName = entity.getCategoryName();
            categoryRepository.delete(entity);
            categoryRepository.flush();
            saveAuditLog(AuditAction.DELETE_CATEGORY, "Deleted category: " + catName + " (ID: " + id + ")");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Cannot delete this category because it is still referenced by books or has data constraints.");
        }
    }
}
