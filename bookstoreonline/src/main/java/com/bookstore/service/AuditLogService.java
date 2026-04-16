package com.bookstore.service;

import com.bookstore.entity.AuditLog;
import com.bookstore.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    @Transactional(readOnly = true)
    public List<AuditLog> filterLogs(String username, String actionKeyword, LocalDateTime startDate, LocalDateTime endDate) {
        List<AuditLog> logs = auditLogRepository.findAllByOrderByTimestampDesc();
        
        return logs.stream()
                .filter(log -> username == null || log.getAccount().getUsername().toLowerCase().contains(username.toLowerCase()))
                .filter(log -> actionKeyword == null || log.getAction().toLowerCase().contains(actionKeyword.toLowerCase()))
                .filter(log -> startDate == null || !log.getTimestamp().isBefore(startDate))
                .filter(log -> endDate == null || !log.getTimestamp().isAfter(endDate))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public java.util.Optional<AuditLog> getLogById(Long id) {
        if (id == null) return java.util.Optional.empty();
        return auditLogRepository.findById(id);
    }

    @Transactional
    public void log(com.bookstore.entity.Account account, String action, String details) {
        AuditLog log = new AuditLog();
        log.setAccount(account);
        log.setAction(action);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }
}
