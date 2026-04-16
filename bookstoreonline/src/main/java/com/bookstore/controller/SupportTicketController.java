package com.bookstore.controller;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.SupportTicketDTO;
import com.bookstore.service.ChatbotService;
import com.bookstore.service.SupportTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/support")
@CrossOrigin("*")
@Tag(name = "Support Management", description = "Quản lý yêu cầu hỗ trợ và khiếu nại")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;
    private final ChatbotService chatbotService;

    public SupportTicketController(SupportTicketService supportTicketService,
                                   ChatbotService chatbotService) {
        this.supportTicketService = supportTicketService;
        this.chatbotService = chatbotService;
    }

    @GetMapping
    @Operation(summary = "Tất cả yêu cầu")
    public ApiResponse<List<SupportTicketDTO>> getAllRequests() {
        return ApiResponse.success(supportTicketService.getAllTickets());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết một yêu cầu")
    public ApiResponse<SupportTicketDTO> getTicket(@PathVariable Long id) {
        return ApiResponse.success(supportTicketService.getTicketById(id));
    }

    @GetMapping("/user/{username}")
    @Operation(summary = "Yêu cầu của tôi")
    public ApiResponse<List<SupportTicketDTO>> getRequestsByCustomer(@PathVariable String username) {
        return ApiResponse.success(supportTicketService.getTicketsByCustomer(username));
    }

    @PostMapping
    @Operation(summary = "Gửi yêu cầu mới")
    public ApiResponse<String> submitTicket(
            @RequestParam String username,
            @RequestParam String subject,
            @RequestParam String content) {
        supportTicketService.submitTicket(username, subject, content);
        return ApiResponse.success("Yêu cầu của bạn đã được tiếp nhận", null);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật trạng thái")
    public ApiResponse<String> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        supportTicketService.updateStatus(id, status);
        return ApiResponse.success("Đã cập nhật trạng thái hồ sơ", null);
    }

    @PostMapping("/{id}/respond")
    @Operation(summary = "Phản hồi/Ghi chú cho yêu cầu")
    public ApiResponse<String> respondToTicket(
            @PathVariable Long id,
            @RequestParam(required = false) String reply,
            @RequestParam(required = false) String internalNote,
            @RequestParam(required = false) String statusCode) {
        supportTicketService.respondToTicket(id, reply, internalNote, statusCode);
        return ApiResponse.success("Đã tiếp nhận phản hồi/ghi chú", null);
    }

    @PostMapping("/ai-chat")
    @Operation(summary = "Chatbot AI thông minh")
    public ApiResponse<Map<String, Object>> aiChat(
            @RequestParam String message,
            @RequestParam(required = false) String sessionId) {
        Map<String, Object> response = chatbotService.getResponse(message, sessionId);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}/messages")
    @Operation(summary = "Lấy lịch sử tin nhắn của yêu cầu")
    public ApiResponse<List<com.bookstore.dto.SupportMessageDTO>> getTicketMessages(@PathVariable Long id) {
        return ApiResponse.success(supportTicketService.getMessages(id));
    }

    @PostMapping("/{id}/messages")
    @Operation(summary = "Gửi tin nhắn mới cho yêu cầu")
    public ApiResponse<String> sendTicketMessage(
            @PathVariable Long id,
            @RequestParam String senderName,
            @RequestParam boolean isStaff,
            @RequestParam String content) {
        supportTicketService.addMessage(id, senderName, isStaff, content);
        return ApiResponse.success("Đã gửi tin nhắn", null);
    }

    @GetMapping("/notifications")
    @Operation(summary = "Kiểm tra thông báo tin nhắn mới (Cho nhân viên)")
    public ApiResponse<List<com.bookstore.dto.SupportMessageDTO>> getNotifications(
            @RequestParam String since) {
        java.time.LocalDateTime timestamp = java.time.LocalDateTime.parse(since);
        return ApiResponse.success(supportTicketService.getRecentCustomerMessages(timestamp));
    }

    @GetMapping("/user-notifications")
    @Operation(summary = "Kiểm tra thông báo tin nhắn mới (Cho khách hàng)")
    public ApiResponse<List<com.bookstore.dto.SupportMessageDTO>> getUserNotifications(
            @RequestParam String username,
            @RequestParam String since) {
        java.time.LocalDateTime timestamp = java.time.LocalDateTime.parse(since);
        return ApiResponse.success(supportTicketService.getRecentStaffMessagesForUser(timestamp, username));
    }

    @GetMapping("/active-sessions")
    @Operation(summary = "Lấy danh sách các phiên chat đang hoạt động (Cho nhân viên)")
    public ApiResponse<List<SupportTicketDTO>> getActiveSessions() {
        return ApiResponse.success(supportTicketService.getActiveSessions());
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Lấy tổng số tin nhắn chưa đọc của khách hàng")
    public ApiResponse<Long> getUnreadCount(@RequestParam String username) {
        return ApiResponse.success(supportTicketService.getUnreadCountForUser(username));
    }

    @GetMapping("/staff/unread-count")
    @Operation(summary = "Lấy tổng số tin nhắn chưa đọc từ khách hàng (Cho nhân viên)")
    public ApiResponse<Long> getStaffUnreadCount() {
        return ApiResponse.success(supportTicketService.getTotalUnreadCountForStaff());
    }

    @PostMapping("/{id}/mark-read")
    @Operation(summary = "Đánh dấu tin nhắn là đã đọc")
    public ApiResponse<String> markAsRead(
            @PathVariable Long id,
            @RequestParam boolean isStaff) {
        supportTicketService.markMessagesAsRead(id, isStaff);
        return ApiResponse.success("Đã đánh dấu là đã đọc", null);
    }
}
