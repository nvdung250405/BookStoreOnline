package com.bookstore.dto;

import com.bookstore.entity.SupportMessage;
import java.time.LocalDateTime;

public class SupportMessageDTO {
    private Long messageId;
    private String senderName;
    private boolean isStaff;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;

    public SupportMessageDTO() {}

    public static SupportMessageDTO fromEntity(SupportMessage message) {
        SupportMessageDTO dto = new SupportMessageDTO();
        dto.setMessageId(message.getMessageId());
        dto.setSenderName(message.getSenderName());
        dto.setStaff(message.isStaff());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setRead(message.isRead());
        return dto;
    }

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public boolean isStaff() { return isStaff; }
    public void setStaff(boolean staff) { isStaff = staff; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
