package com.bookstore.service;

import com.bookstore.dto.SupportMessageDTO;
import com.bookstore.dto.SupportTicketDTO;
import com.bookstore.entity.SupportMessage;
import com.bookstore.entity.SupportTicket;
import com.bookstore.entity.Customer;
import com.bookstore.repository.SupportMessageRepository;
import com.bookstore.repository.SupportTicketRepository;
import com.bookstore.repository.CustomerRepository;
import com.bookstore.service.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings("null")
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final CustomerRepository customerRepository;
    private final AuditLogService auditLogService;

    public SupportTicketServiceImpl(SupportTicketRepository supportTicketRepository, 
                                   CustomerRepository customerRepository,
                                   AuditLogService auditLogService) {
        this.supportTicketRepository = supportTicketRepository;
        this.customerRepository = customerRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketDTO> getAllTickets() {
        return supportTicketRepository.findAll().stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt())) // Mới nhất lên đầu
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketDTO> getTicketsByCustomer(String username) {
        return supportTicketRepository.findByCustomer_Account_Username(username).stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt())) // Mới nhất lên đầu
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void submitTicket(String username, String subject, String content) {
        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng: " + username));
        
        SupportTicket ticket = new SupportTicket();
        ticket.setCustomer(customer);
        ticket.setTitle(subject); // Mapped subject to title in entity
        ticket.setContent(content);
        ticket.setStatusCode("OPEN");
        supportTicketRepository.save(ticket);
        
        // Log hành động của Khách hàng
        auditLogService.log(customer.getAccount(), "CREATE_TICKET", "Khách hàng gửi yêu cầu hỗ trợ mới: " + subject);
    }

    @Override
    public void updateStatus(Long id, String statusCode) {
        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi hỗ trợ với ID: " + id));
        ticket.setStatusCode(statusCode);
        supportTicketRepository.save(ticket);

        // Log hành động cập nhật trạng thái
        auditLogService.log("UPDATE_TICKET_STATUS", "Cập nhật trạng thái phiếu hỗ trợ #" + id + " thành " + statusCode);
    }

    @Override
    public SupportTicketDTO getTicketById(Long id) {
        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu hỗ trợ ID: " + id));
        return toDTO(ticket);
    }

    @Override
    @Transactional
    public void respondToTicket(Long id, String reply, String internalNote, String statusCode) {
        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi hỗ trợ với ID: " + id));
        
        if (reply != null && !reply.isBlank()) {
            ticket.setAdminReply(reply);
        }
        if (internalNote != null && !internalNote.isBlank()) {
            ticket.setInternalNote(internalNote);
        }
        if (statusCode != null && !statusCode.isBlank()) {
            ticket.setStatusCode(statusCode);
        }
        
        supportTicketRepository.save(ticket);

        // Log hành động phản hồi
        String logDetails = "Phản hồi phiếu hỗ trợ #" + id;
        if (statusCode != null) logDetails += " (Đổi trạng thái sang " + statusCode + ")";
        auditLogService.log("RESPOND_TICKET", logDetails);
    }

    @Override
    public List<SupportMessageDTO> getMessages(Long ticketId) {
        return supportMessageRepository.findByTicket_TicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(SupportMessageDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addMessage(Long ticketId, String senderName, boolean isStaff, String content) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        SupportMessage message = new SupportMessage();
        message.setTicket(ticket);
        message.setSenderName(senderName);
        message.setStaff(isStaff);
        message.setContent(content);
        supportMessageRepository.save(message);
        
        // If it's a customer message, set status to OPEN or PROCESSING if it was RESOLVED/CLOSED
        if (!isStaff && (ticket.getStatusCode().equals("RESOLVED") || ticket.getStatusCode().equals("CLOSED"))) {
            ticket.setStatusCode("PROCESSING");
            supportTicketRepository.save(ticket);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportMessageDTO> getRecentCustomerMessages(java.time.LocalDateTime since) {
        return supportMessageRepository.findByCreatedAtAfterAndIsStaffFalse(since).stream()
                .map(SupportMessageDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportMessageDTO> getRecentStaffMessagesForUser(java.time.LocalDateTime since, String username) {
        return supportMessageRepository.findByCreatedAtAfterAndIsStaffTrueAndTicket_Customer_Account_Username(since, username).stream()
                .map(SupportMessageDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketDTO> getActiveSessions() {
        return supportTicketRepository.findByStatusCodeIn(List.of("OPEN", "PROCESSING", "ai-chat")).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCountForUser(String username) {
        return supportMessageRepository.countByTicket_Customer_Account_UsernameAndIsStaffTrueAndIsReadFalse(username);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalUnreadCountForStaff() {
        return supportMessageRepository.countByIsStaffFalseAndIsReadFalse();
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Long ticketId, boolean forStaff) {
        List<SupportMessage> unread = supportMessageRepository.findByTicket_TicketIdAndIsStaffAndIsReadFalse(ticketId, !forStaff);
        unread.forEach(m -> m.setRead(true));
        supportMessageRepository.saveAll(unread);
    }

    private SupportTicketDTO toDTO(SupportTicket ticket) {
        if (ticket == null) return null;
        SupportTicketDTO dto = new SupportTicketDTO();
        dto.setTicketId(ticket.getTicketId());
        dto.setCustomerName(ticket.getCustomer() != null ? ticket.getCustomer().getFullName() : "Ẩn danh");
        dto.setTitle(ticket.getTitle());
        dto.setContent(ticket.getContent());
        dto.setStatusCode(ticket.getStatusCode());
        dto.setAdminReply(ticket.getAdminReply());
        dto.setInternalNote(ticket.getInternalNote());
        dto.setCreatedAt(ticket.getCreatedAt());
        
        // Determine unread status: latest message is not from staff
        List<SupportMessage> messages = supportMessageRepository.findByTicket_TicketIdOrderByCreatedAtAsc(ticket.getTicketId());
        if (!messages.isEmpty()) {
            SupportMessage last = messages.get(messages.size() - 1);
            dto.setHasUnreadMessages(!last.isStaff());
        } else {
            dto.setHasUnreadMessages(true); // Content from ticket creator
        }
        
        return dto;
    }
}
