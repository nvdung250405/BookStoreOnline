package com.bookstore.service;

import com.bookstore.dto.SupportTicketDTO;
import com.bookstore.entity.SupportTicket;
import com.bookstore.entity.Customer;
import com.bookstore.enums.SupportStatus;
import com.bookstore.repository.SupportTicketRepository;
import com.bookstore.repository.CustomerRepository;
import com.bookstore.service.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<SupportTicketDTO> getAllTickets() {
        return supportTicketRepository.findAll().stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt())) // Mới nhất lên đầu
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
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
    public void updateStatus(Long id, String statusName) {
        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setStatus(SupportStatus.valueOf(statusName.toUpperCase()));
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
    public void respondToTicket(Long id, String reply, String internalNote, SupportStatus status) {
        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (reply != null) ticket.setAdminReply(reply);
        if (internalNote != null) ticket.setInternalNote(internalNote);
        if (status != null) {
            ticket.setStatus(status);
        }

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

    private SupportTicketDTO toDTO(SupportTicket ticket) {
        if (ticket == null) return null;
        SupportTicketDTO dto = new SupportTicketDTO();
        dto.setTicketId(ticket.getTicketId());
        dto.setCustomerName(ticket.getCustomer() != null ? ticket.getCustomer().getFullName() : "Ẩn danh");
        dto.setTitle(ticket.getTitle());
        dto.setContent(ticket.getContent());
        dto.setStatus(ticket.getStatus() != null ? ticket.getStatus().name() : null);
        dto.setAdminReply(ticket.getAdminReply());
        dto.setInternalNote(ticket.getInternalNote());
        dto.setCreatedAt(ticket.getCreatedAt());
        return dto;
    }
}
