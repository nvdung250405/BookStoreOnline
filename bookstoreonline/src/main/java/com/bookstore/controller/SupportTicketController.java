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

    @PostMapping("/ai-chat")
    @Operation(summary = "Chatbot AI thông minh")
    public ApiResponse<Map<String, Object>> aiChat(
            @RequestParam String message,
            @RequestParam(required = false) String sessionId) {
        Map<String, Object> response = chatbotService.getResponse(message, sessionId);
        return ApiResponse.success(response);
    }
}
