package com.bookstore.service;

import com.bookstore.dto.AuditLogDTO;
import com.bookstore.dto.AuditLogStatsDTO;
import com.bookstore.entity.Account;
import com.bookstore.entity.AuditLog;
import com.bookstore.repository.AuditLogRepository;
import com.bookstore.repository.AccountRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AccountRepository accountRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, AccountRepository accountRepository) {
        this.auditLogRepository = auditLogRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<AuditLogDTO> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogDTO> filterLogs(String username, String actionKeyword, LocalDateTime startDate, LocalDateTime endDate) {
        List<AuditLog> logs = auditLogRepository.findAllByOrderByTimestampDesc();
        
        return logs.stream()
                .filter(log -> username == null || log.getAccount().getUsername().toLowerCase().contains(username.toLowerCase()))
                .filter(log -> actionKeyword == null || log.getAction().toLowerCase().contains(actionKeyword.toLowerCase()))
                .filter(log -> startDate == null || !log.getTimestamp().isBefore(startDate))
                .filter(log -> endDate == null || !log.getTimestamp().isAfter(endDate))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<AuditLogDTO> getLogById(Long id) {
        if (id == null) return Optional.empty();
        return auditLogRepository.findById(id).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public AuditLogStatsDTO getAuditStats() {
        List<AuditLog> allLogs = auditLogRepository.findAll();
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        long totalLogs = allLogs.size();
        long todayLogs = allLogs.stream().filter(l -> !l.getTimestamp().isBefore(todayStart)).count();
        long activeUsers = allLogs.stream().map(l -> l.getAccount() != null ? l.getAccount().getUsername() : "System").distinct().count();
        long dataChanges = allLogs.stream().filter(l -> l.getAction().matches(".*(CREATE|UPDATE|DELETE|STOCK).*")).count();
        
        return new AuditLogStatsDTO(totalLogs, todayLogs, activeUsers, dataChanges);
    }

    @Transactional
    public void log(String action, String details) {
        Account account = getCurrentAccount();
        if (account == null) return; // Skip logging if no user is authenticated

        AuditLog log = new AuditLog();
        log.setAccount(account);
        log.setAction(action);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    @Transactional
    public void log(Account account, String action, String details) {
        if (account == null) return;
        AuditLog log = new AuditLog();
        log.setAccount(account);
        log.setAction(action);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    private Account getCurrentAccount() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            return accountRepository.findById(username).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private AuditLogDTO convertToDTO(AuditLog log) {
        return new AuditLogDTO(
                log.getLogId(),
                log.getAccount().getUsername(),
                log.getAction(),
                log.getDetails(),
                log.getTimestamp()
        );
    }
}
