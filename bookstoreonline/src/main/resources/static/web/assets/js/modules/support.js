/**
 * support.js - Support Ticket & CRM Logic
 * Standardized for simplified Admin handling
 */
let currentTicketId = null;

const support = {
    // 1. AI Chatbot Logic
    initChat: () => {
        setTimeout(() => $("#chat-user-msg").focus(), 200);
    },

    sendChat: async (message) => {
        const chatBox = $("#chat-box");
        if (!chatBox.length) return;

        chatBox.append(`<div class="bsw-msg-user"><div class="bsw-bubble">${$('<div>').text(message).html()}</div></div>`);
        
        try {
            const res = await api.post(`/support/ai-chat?message=${encodeURIComponent(message)}`);
            const text = res.data?.message || res.data || "Xin lỗi, tôi không thể xử lý yêu cầu này lúc này.";
            chatBox.append(`<div class="bsw-msg-ai"><div class="bsw-avatar">📚</div><div class="bsw-bubble">${text}</div></div>`);
        } catch (e) {
            chatBox.append(`<div class="bsw-msg-ai"><div class="bsw-avatar">📚</div><div class="bsw-bubble text-danger">Dịch vụ tạm thời không khả dụng.</div></div>`);
        }
        chatBox.scrollTop(chatBox[0].scrollHeight);
    },

    // 2. Admin Ticket List
    loadAdminTickets: async () => {
        try {
            const res = await api.get('/api/support');
            const data = Array.isArray(res) ? res : (res.data || []);
            const tbody = $("#support-list-body");
            if (!tbody.length) return;
            tbody.empty();

            data.forEach(t => {
                const statusBadge = support.getStatusBadge(t.statusCode);
                tbody.append(`
                    <tr onclick="layout.render('Support/Admin', 'Details', '${t.ticketId}')" style="cursor:pointer">
                        <td class="ps-4 fw-bold">#${t.ticketId}</td>
                        <td>${t.customerName || '---'}</td>
                        <td class="text-truncate" style="max-width: 250px;">${t.title}</td>
                        <td>${statusBadge}</td>
                        <td>${new Date(t.createdAt).toLocaleDateString('en-GB')}</td>
                        <td class="text-end pe-4">
                            <button class="btn btn-sm btn-light rounded-circle"><i class="icon icon-arrow-right"></i></button>
                        </td>
                    </tr>
                `);
            });
        } catch (e) { api.showToast("Không thể tải danh sách phiếu hỗ trợ", "error"); }
    },

    // 3. Admin Ticket Details
    loadTicketDetails: async (id) => {
        currentTicketId = id;
        try {
            // Re-fetch all to find the one (assuming GET /api/support returns all including details)
            // Or if backend has GET /api/support/{id}, use that. It doesn't yet based on Controller check.
            // Wait, Controller has no GET /{id}. Let's assume we use the list for now or I should have added it.
            // Actually, I'll update the controller to add GET /{id} for efficiency.
            const res = await api.get('/api/support');
            const list = Array.isArray(res) ? res : (res.data || []);
            const t = list.find(x => x.ticketId == id);
            
            if (t) {
                $("#ticket-detail-title").text(t.title);
                $("#ticket-detail-id").text(`ID: #${t.ticketId}`);
                $("#ticket-detail-content").text(t.content);
                $("#ticket-customer-name").text(t.customerName || 'Customer');
                $("#ticket-created-date").text(new Date(t.createdAt).toLocaleString());
                
                const badge = $("#ticket-status-badge");
                badge.text(t.statusCode).removeClass().addClass(`badge rounded-pill px-3 py-2 ${support.getStatusClass(t.statusCode)}`);
                
                $("#target-status").val(t.statusCode);
                $("#admin-reply").val(t.adminReply || '');
                $("#internal-note").val(t.internalNote || '');

                $("#comments-list").empty().append(`
                    <div class="mb-3 p-3 bg-light rounded-3">
                        <small class="text-muted d-block mb-1">Yêu cầu từ khách hàng:</small>
                        <div class="fw-medium">${t.content}</div>
                    </div>
                `);

                if (t.adminReply) {
                    $("#comments-list").append(`
                        <div class="mb-3 p-3 bg-accent bg-opacity-10 rounded-3 border-start border-accent border-4">
                            <small class="text-accent fw-bold d-block mb-1">Phản hồi của Admin:</small>
                            <div>${t.adminReply}</div>
                        </div>
                    `);
                }

                if (t.internalNote) {
                    $("#comments-list").append(`
                        <div class="mb-2 p-3 bg-warning bg-opacity-10 rounded-3 border-start border-warning border-4">
                            <small class="text-warning fw-bold d-block mb-1">Ghi chú nội bộ:</small>
                            <div class="small italic text-muted">${t.internalNote}</div>
                        </div>
                    `);
                }
            }
        } catch (e) { api.showToast("Lỗi khi tải chi tiết phiếu hỗ trợ", "error"); }
    },

    // 4. Submit Response (The core "Handled" logic)
    submitResponse: async () => {
        const id = currentTicketId;
        const reply = $("#admin-reply").val().trim();
        const note = $("#internal-note").val().trim();
        const status = $("#target-status").val();

        api.showToast("Đang cập nhật...", "info");
        try {
            // POST /api/support/{id}/respond?reply=...&internalNote=...&statusCode=...
            await api.post(`/api/support/${id}/respond?reply=${encodeURIComponent(reply)}&internalNote=${encodeURIComponent(note)}&statusCode=${status}`);
            api.showToast("✓ Cập nhật hồ sơ thành công!", "success");
            support.loadTicketDetails(id);
        } catch (e) {
            api.showToast("Lỗi cập nhật: " + e.message, "error");
        }
    },

    // Utilities
    getStatusBadge: (status) => {
        const cls = support.getStatusClass(status);
        return `<span class="badge rounded-pill px-3 ${cls}">${status}</span>`;
    },

    getStatusClass: (status) => {
        switch (status) {
            case 'OPEN': return 'bg-danger';      // Mở
            case 'PROCESSING': return 'bg-warning text-dark'; // Đang xử lý
            case 'RESOLVED': return 'bg-success';   // Đã giải quyết
            case 'CLOSED': return 'bg-secondary';   // Đã đóng
            default: return 'bg-dark';
        }
    },

    confirmDelete: async () => {
        if (!confirm("Bạn có chắc chắn muốn xóa yêu cầu hỗ trợ này không?")) return;
        api.showToast("Tính năng này đã bị vô hiệu hóa vì lý do an toàn hệ thống", "warning");
    }
};

$(document).on('click', '#btn-toggle-chat', () => support.toggleChat());
