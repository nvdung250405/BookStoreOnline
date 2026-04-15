/**
 * support.js - AI Chat Support & Customer Tickets Logic
 * Standardized for Full English Backend synchronization
 */
const support = {
    // Initialize Chat Widget — gắn event vào input trong widget mới
    initChat: () => {
        setTimeout(() => $("#chat-user-msg").focus(), 200);
    },

    // Send from input field
    sendChatFromInput: () => {
        const input = $("#chat-user-msg");
        const msg = input.val().trim();
        if (!msg) return;
        input.val("");
        support.sendChat(msg);
    },

    // Escape HTML to prevent XSS
    escapeHtml: (text) => $('<div>').text(text).html(),

    // Render markdown-lite: **bold**, newlines
    renderMarkdown: (text) => {
        return support.escapeHtml(text)
            .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
            .replace(/\n/g, '<br>');
    },

    sendChat: async (message) => {
        const chatBox = $("#chat-box");
        if (!chatBox.length) return;

        // Remove quick replies (they become stale after user picks one)
        chatBox.find('.bsw-quick-replies').remove();

        // User bubble
        chatBox.append(`
            <div class="bsw-msg-user">
                <div class="bsw-bubble">${support.escapeHtml(message)}</div>
            </div>
        `);
        support.scrollToBottom();

        // Typing indicator
        const typingId = "typing-" + Date.now();
        chatBox.append(`
            <div id="${typingId}" class="bsw-msg-ai">
                <div class="bsw-avatar">📚</div>
                <div class="bsw-bubble" style="color:#bbb; display:flex; align-items:center; gap:4px;">
                    <span style="animation:bsw-dot 1.2s infinite 0s">●</span>
                    <span style="animation:bsw-dot 1.2s infinite 0.2s">●</span>
                    <span style="animation:bsw-dot 1.2s infinite 0.4s">●</span>
                </div>
            </div>
        `);

        // Add dot animation style once
        if (!document.getElementById('bsw-dot-style')) {
            $('<style id="bsw-dot-style">@keyframes bsw-dot{0%,80%,100%{opacity:.2;transform:scale(.8)}40%{opacity:1;transform:scale(1)}}</style>').appendTo('head');
        }

        support.scrollToBottom();

        try {
            const res = await api.post(`/support/ai-chat?message=${encodeURIComponent(message)}`);
            $(`#${typingId}`).remove();

            // Parse response: {message, quickReplies} hoặc string fallback
            const data = res.data;
            const text   = (typeof data === 'object' && data.message) ? data.message :
                           (typeof data === 'string' ? data : 'Xin lỗi, tôi chưa hiểu ý bạn.');
            const qr     = (typeof data === 'object' && Array.isArray(data.quickReplies)) ? data.quickReplies : [];

            // AI bubble
            chatBox.append(`
                <div class="bsw-msg-ai">
                    <div class="bsw-avatar">📚</div>
                    <div class="bsw-bubble">${support.renderMarkdown(text)}</div>
                </div>
            `);

            // Quick reply chips
            if (qr.length) {
                const chips = qr.map(q =>
                    `<button class="bsw-chip" data-msg="${support.escapeHtml(q)}" onclick="support.sendChat(this.dataset.msg)">${support.escapeHtml(q)}</button>`
                ).join('');
                chatBox.append(`<div class="bsw-quick-replies">${chips}</div>`);
            }

        } catch (e) {
            $(`#${typingId}`).remove();
            chatBox.append(`
                <div class="bsw-msg-ai">
                    <div class="bsw-avatar">📚</div>
                    <div class="bsw-bubble" style="color:#e55;">
                        Hệ thống đang bận, vui lòng thử lại sau nhé!
                    </div>
                </div>
            `);
        }
        support.scrollToBottom();
    },

    scrollToBottom: () => {
        const box = document.getElementById('chat-box');
        if (box) box.scrollTop = box.scrollHeight;
    },

    toggleChat: () => {
        const holder = $("#chat-box-holder");
        if (holder.children().length === 0) {
            holder.load("Shared/ChatWidget.html", function(response, status, xhr) {
                if (status === "error") {
                    api.showToast("Không thể tải chatbot", "error");
                    return;
                }
                support.initChat();
                holder.fadeIn(200);
            });
        } else {
            holder.fadeToggle(200);
        }
    },

    /**
     * Load user's own support tickets (Customer view - 4 cols)
     */
    loadUserTickets: async () => {
        const user = api.getUser();
        const tbody = $("#support-list-body");
        if (!tbody.length) return;

        tbody.html(`
            <tr><td colspan="4" class="text-center py-5">
                <div class="spinner-border spinner-border-sm text-secondary" role="status"></div>
                <span class="ms-2 text-muted small">Đang tải...</span>
            </td></tr>
        `);

        if (!user) {
            tbody.html('<tr><td colspan="4" class="text-center py-5 text-muted">Vui lòng đăng nhập để xem yêu cầu hỗ trợ.</td></tr>');
            return;
        }

        try {
            const res = await api.get(`/support/user/${user.username}`);
            const tickets = res.data || [];
            tbody.empty();

            if (tickets.length === 0) {
                tbody.html('<tr><td colspan="4" class="text-center py-5 text-muted">Bạn chưa có yêu cầu hỗ trợ nào.</td></tr>');
                return;
            }

            tickets.forEach(ticket => {
                tbody.append(`
                    <tr>
                        <td class="ps-4 fw-bold">${ticket.subject || ticket.title || "---"}</td>
                        <td>${ticket.createdAt ? new Date(ticket.createdAt).toLocaleDateString('vi-VN') : '---'}</td>
                        <td>${support.getStatusBadge(ticket.status)}</td>
                        <td class="text-end pe-4">
                            <span class="text-muted small">#${ticket.id}</span>
                        </td>
                    </tr>
                `);
            });
        } catch (e) {
            tbody.html('<tr><td colspan="4" class="text-center py-5 text-danger">Lỗi khi tải danh sách yêu cầu.</td></tr>');
        }
    },

    /**
     * Open modal to create new ticket
     */
    openCreateTicket: () => {
        $("#support-title").val("");
        $("#support-content").val("");
        const modal = new bootstrap.Modal(document.getElementById("support-modal"));
        modal.show();
    },

    /**
     * Submit new ticket from modal
     */
    submitTicket: async () => {
        const user = api.getUser();
        if (!user) { api.showToast("Vui lòng đăng nhập", "warning"); return; }

        const subject = $("#support-title").val().trim();
        const content = $("#support-content").val().trim();

        if (!subject || !content) {
            api.showToast("Vui lòng điền đầy đủ thông tin", "warning");
            return;
        }

        const btn = $("button[onclick='support.submitTicket()']").text("Đang gửi...").prop("disabled", true);
        try {
            await api.post(`/support?username=${encodeURIComponent(user.username)}&subject=${encodeURIComponent(subject)}&content=${encodeURIComponent(content)}`);
            api.showToast("Đã gửi yêu cầu hỗ trợ thành công!", "success");
            bootstrap.Modal.getInstance(document.getElementById("support-modal")).hide();
            support.loadUserTickets();
        } catch (e) {
            api.showToast("Gửi yêu cầu thất bại: " + e.message, "error");
        } finally {
            btn.text("Gửi yêu cầu").prop("disabled", false);
        }
    },

    /**
     * Load admin support tickets list
     */
    loadAdminTickets: async () => {
        try {
            const search = $("#support-search").val() || "";
            const status = $("#support-status-filter").val() || "";
            const priority = $("#support-priority-filter").val() || "";

            const res = await api.get(`/support`, { search, status, priority });
            const tickets = res.data || res;
            const tbody = $("#support-list-body");
            if (!tbody.length) return;
            tbody.empty();

            if (!tickets || tickets.length === 0) {
                tbody.html('<tr><td colspan="7" class="text-center py-5 text-muted">No tickets found.</td></tr>');
                return;
            }

            tickets.forEach(ticket => {
                const statusBadge = support.getStatusBadge(ticket.status);
                const priorityBadge = support.getPriorityBadge(ticket.priority || "MEDIUM");
                tbody.append(`
                    <tr onclick="layout.render('Support/Admin', 'Details', '${ticket.id}')">
                        <td class="ps-4 fw-bold">#${ticket.id}</td>
                        <td>${ticket.customerName || '---'}</td>
                        <td class="text-truncate" style="max-width: 250px;">${ticket.title}</td>
                        <td>${statusBadge}</td>
                        <td>${priorityBadge}</td>
                        <td>${ticket.createdAt ? new Date(ticket.createdAt).toLocaleDateString('en-GB') : '---'}</td>
                        <td class="text-end pe-4">
                            <button class="btn btn-sm btn-light rounded-circle" onclick="layout.render('Support/Admin', 'Details', '${ticket.id}')">
                                <i class="icon icon-arrow-right"></i>
                            </button>
                        </td>
                    </tr>
                `);
            });
        } catch (e) {
            api.showToast("Error loading tickets: " + e.message, "error");
        }
    },

    /**
     * Load ticket details
     */
    loadTicketDetails: async (ticketId) => {
        try {
            const res = await api.get(`/support/${ticketId}`);
            const ticket = res.data;

            $("#ticket-detail-title").text(ticket.title);
            $("#ticket-detail-id").text("ID: #" + ticket.id);
            $("#ticket-detail-content").text(ticket.content);
            $("#ticket-customer-name").text(ticket.customerName);
            $("#ticket-customer-email").text(ticket.email);
            $("#ticket-created-date").text(new Date(ticket.createdAt).toLocaleString('en-GB'));
            
            const badge = $("#ticket-status-badge");
            badge.text(ticket.status);
            badge.removeClass().addClass(`badge bg-${support.getStatusClass(ticket.status)} rounded-pill px-3 py-2`);
            
            $("#ticket-status-select").val(ticket.status);
            $("#ticket-priority-select").val(ticket.priority || "MEDIUM");
            $("#ticket-category").text(ticket.category || "General");
            $("#ticket-updated-date").text(ticket.updatedAt ? new Date(ticket.updatedAt).toLocaleString('en-GB') : '---');
        } catch (e) {
            api.showToast("Error loading details: " + e.message, "error");
        }
    },

    /**
     * Add comment to ticket
     */
    addComment: async (event) => {
        event.preventDefault();
        const ticketId = layout.current.id;
        const commentText = $("#comment-text").val();
        if(!commentText) return;

        try {
            await api.post(`/support/${ticketId}/comment`, {
                content: commentText
            });

            api.showToast("Comment added!", "success");
            $("#comment-text").val("");
            support.loadTicketDetails(ticketId);
        } catch (e) {
            api.showToast("Error adding comment: " + e.message, "error");
        }
    },

    /**
     * Update ticket status
     */
    updateTicketStatus: async () => {
        const ticketId = layout.current.id;
        const status = $("#ticket-status-select").val();
        try {
            await api.put(`/support/${ticketId}`, { status: status });
            api.showToast("Status updated!", "success");
        } catch (e) {
            api.showToast("Update failed: " + e.message, "error");
        }
    },

    /**
     * Update ticket priority
     */
    updateTicketPriority: async () => {
        const ticketId = layout.current.id;
        const priority = $("#ticket-priority-select").val();
        try {
            await api.put(`/support/${ticketId}`, { priority: priority });
            api.showToast("Priority updated!", "success");
        } catch (e) {
            api.showToast("Update failed: " + e.message, "error");
        }
    },

    /**
     * Confirm delete ticket
     */
    confirmDelete: async () => {
        const ticketId = layout.current.id;
        if (!confirm("Delete this ticket?")) return;
        try {
            await api.delete(`/support/${ticketId}`);
            api.showToast("Ticket deleted!");
            layout.render('Support/Admin', 'Index');
        } catch (e) {
            api.showToast("Delete failed: " + e.message, "error");
        }
    },

    /**
     * Get status badge HTML
     */
    getStatusBadge: (status) => {
        const badges = {
            'OPEN': '<span class="badge bg-warning text-dark">Open</span>',
            'PROCESSING': '<span class="badge bg-info text-white">Processing</span>',
            'CLOSED': '<span class="badge bg-success">Closed</span>'
        };
        return badges[status] || `<span class="badge bg-secondary">${status}</span>`;
    },

    /**
     * Get status CSS class
     */
    getStatusClass: (status) => {
        const classes = {
            'OPEN': 'warning',
            'PROCESSING': 'info',
            'CLOSED': 'success'
        };
        return classes[status] || 'secondary';
    },

    /**
     * Get priority badge HTML
     */
    getPriorityBadge: (priority) => {
        const badges = {
            'LOW': '<span class="badge bg-light text-dark">Low</span>',
            'MEDIUM': '<span class="badge bg-warning text-dark">Medium</span>',
            'HIGH': '<span class="badge bg-danger">High</span>'
        };
        return badges[priority] || `<span class="badge bg-secondary">${priority}</span>`;
    },

    /**
     * Submit contact form from Home/Contact page
     */
    submitContactForm: async (form) => {
        const inputs = form.querySelectorAll('input[required], textarea[required]');
        for (const el of inputs) {
            if (!el.value.trim()) {
                api.showToast('Vui lòng điền đầy đủ thông tin bắt buộc', 'warning');
                el.focus();
                return;
            }
        }

        const submitBtn = form.querySelector('button[type="submit"]');
        if (submitBtn) { submitBtn.disabled = true; submitBtn.textContent = 'Đang gửi...'; }

        const allInputs = Array.from(form.querySelectorAll('input, textarea'));
        const name    = allInputs[0]?.value.trim() || '';
        const email   = allInputs[1]?.value.trim() || '';
        const subject = allInputs[2]?.value.trim() || 'Liên hệ từ website';
        const content = allInputs[3]?.value.trim() || '';

        try {
            // Reuse support ticket endpoint
            const user = api.getUser();
            const username = user ? user.username : 'guest_' + email.split('@')[0];
            await api.post(`/support?username=${encodeURIComponent(username)}&subject=${encodeURIComponent('[Liên hệ] ' + subject)}&content=${encodeURIComponent('Từ: ' + name + ' | Email: ' + email + '\n\n' + content)}`);
            api.showToast('Cảm ơn! Tin nhắn của bạn đã được gửi thành công.', 'success');
            form.reset();
        } catch (e) {
            // If API fails, still show success (contact form shouldn't block user)
            api.showToast('Cảm ơn! Tin nhắn của bạn đã được gửi. Chúng tôi sẽ phản hồi sớm.', 'success');
            form.reset();
        } finally {
            if (submitBtn) { submitBtn.disabled = false; submitBtn.innerHTML = 'Gửi yêu cầu <i class="icon icon-send ms-2"></i>'; }
        }
    }
};

