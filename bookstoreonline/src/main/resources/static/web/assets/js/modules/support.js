/**
 * support.js - Support Ticket & CRM Logic
 * Standardized for simplified Admin handling
 */
let currentTicketId = null;
let customUpdatedTime = null; // Biến toàn cục để lưu thời gian Admin vừa cập nhật

const support = {
    // 1. AI Chatbot Logic
    initChat: () => {
        setTimeout(() => $("#chat-user-msg").focus(), 200);
    },

    sendChat: async (message) => {
        const chatBox = $("#chat-box");
        if (!chatBox.length) return;

        chatBox.find('.bsw-quick-replies').remove();

        chatBox.append(`
            <div class="bsw-msg-user">
                <div class="bsw-bubble">${support.escapeHtml(message)}</div>
            </div>
        `);
        support.scrollToBottom();

        const typingId = "typing-" + Date.now();
        chatBox.append(`
            <div id="${typingId}" class="bsw-msg-ai">
                <div class="bsw-avatar" style="background: linear-gradient(135deg, #1A73E8, #9B72CB, #D96570); box-shadow: 0 3px 8px rgba(155, 114, 203, 0.2);">✨</div>
                <div class="bsw-bubble" style="color:#1A73E8; display:flex; align-items:center; gap:4px; border:none; background:transparent; box-shadow:none;">
                    <span style="animation:bsw-dot 1.2s infinite 0s">●</span>
                    <span style="animation:bsw-dot 1.2s infinite 0.2s">●</span>
                    <span style="animation:bsw-dot 1.2s infinite 0.4s">●</span>
                </div>
            </div>
        `);

        if (!document.getElementById('bsw-dot-style')) {
            $('<style id="bsw-dot-style">@keyframes bsw-dot{0%,80%,100%{opacity:.2;transform:scale(.8)}40%{opacity:1;transform:scale(1)}}</style>').appendTo('head');
        }

        support.scrollToBottom();

        try {
            const res = await api.post(`/support/ai-chat?message=${encodeURIComponent(message)}`);
            $(`#${typingId}`).remove();

            const data = res.data;
            const text = (typeof data === 'object' && data.message) ? data.message :
                           (typeof data === 'string' ? data : 'Xin lỗi, tôi chưa hiểu ý bạn.');

            chatBox.append(`
                <div class="bsw-msg-ai" style="animation: bsw-fade-in 0.4s cubic-bezier(0.16, 1, 0.3, 1);">
                    <div class="bsw-avatar" style="background: linear-gradient(135deg, #1A73E8, #9B72CB, #D96570); box-shadow: 0 3px 8px rgba(155, 114, 203, 0.2);">✨</div>
                    <div class="bsw-bubble" style="background: linear-gradient(135deg, rgba(26,115,232,0.05), rgba(155,114,203,0.05)); border: 1px solid rgba(155,114,203,0.1);">${support.renderMarkdown(text)}</div>
                </div>
            `);

        } catch (e) {
            $(`#${typingId}`).remove();
            chatBox.append(`
                <div class="bsw-msg-ai" style="animation: bsw-fade-in 0.4s ease;">
                    <div class="bsw-avatar" style="background: linear-gradient(135deg, #1A73E8, #9B72CB, #D96570); box-shadow: 0 3px 8px rgba(155, 114, 203, 0.2);">✨</div>
                    <div class="bsw-bubble" style="color:#D96570; border: 1px solid rgba(217, 101, 112, 0.2);">
                        Hệ thống đang bận, vui lòng thử lại sau nhé!
                    </div>
                </div>
            `);
        }
        support.scrollToBottom();
    },

    openDetails: (id) => {
        currentTicketId = id;
        customUpdatedTime = null; // Reset biến này khi mở một ticket mới
        layout.render('Support/Admin', 'Details', id);
    },

    // 2. Admin Ticket List
    loadAdminTickets: async () => {
        try {
            const res = await api.get('/support');
            const data = Array.isArray(res) ? res : (res.data || []);
            const tbody = $("#support-list-body");
            if (!tbody.length) return;
            tbody.empty();

            const searchTerm = $("#support-search").val()?.toLowerCase() || '';
            const statusFilter = $("#support-status-filter").val() || '';

            const filteredData = data.filter(t => {
                const matchesSearch = !searchTerm || 
                    t.title.toLowerCase().includes(searchTerm) || 
                    t.customerName?.toLowerCase().includes(searchTerm) ||
                    t.ticketId.toString().includes(searchTerm);
                const matchesStatus = !statusFilter || t.statusCode === statusFilter;
                return matchesSearch && matchesStatus;
            });

            if (filteredData.length === 0) {
                tbody.append('<tr><td colspan="7" class="text-center py-4 text-muted small italic">Không tìm thấy phiếu hỗ trợ nào phù hợp</td></tr>');
                return;
            }

            filteredData.forEach(t => {
                const statusBadge = support.getStatusBadge(t.statusCode);
                tbody.append(`
                    <tr onclick="support.openDetails('${t.ticketId}')" style="cursor:pointer">
                        <td class="ps-4 fw-bold text-accent">#${t.ticketId}</td>
                        <td class="small fw-bold">${t.customerName || 'Khách hàng'}</td>
                        <td class="text-truncate" style="max-width: 250px;">
                            <div class="fw-bold">${t.title}</div>
                            <div class="extra-small text-muted text-truncate">${t.content}</div>
                        </td>
                        <td>${statusBadge}</td>
                        <td><span class="badge bg-light text-muted small border">Bình thường</span></td>
                        <td class="text-muted small">${new Date(t.createdAt).toLocaleDateString('vi-VN')}</td>
                        <td class="text-end pe-4">
                            <button class="btn btn-sm btn-light rounded-pill px-3 extra-small fw-bold shadow-sm">Chi tiết <i class="icon icon-arrow-right ms-1"></i></button>
                        </td>
                    </tr>
                `);
            });
        } catch (e) { 
            console.error(e);
            api.showToast("Không thể tải danh sách phiếu hỗ trợ", "error"); 
        }
    },

    // 3. Admin Ticket Details
    loadTicketDetails: async (id) => {
        currentTicketId = id;
        try {
            const res = await api.get('/support/' + id);
            
            let t = res.data || res;
            if (t.data) t = t.data; 

            if (t && t.ticketId) {
                $("#ticket-detail-title").text(t.title);
                $("#ticket-detail-id").text(`ID: #${t.ticketId}`);
                $("#ticket-detail-content").text(t.content);
                $("#ticket-customer-name").text(t.customerName || 'Khách hàng');
                
                // Format ngày tạo
                const createdDate = t.createdAt ? new Date(t.createdAt).toLocaleString('vi-VN') : '---';
                $("#ticket-created-date").text(createdDate);

                // Ưu tiên hiển thị customUpdatedTime nếu Admin vừa nhấn lưu
                if (customUpdatedTime) {
                    $("#ticket-updated-date").text(customUpdatedTime);
                } else {
                    $("#ticket-updated-date").text(createdDate);
                }
                
                const badge = $("#ticket-status-badge");
                const badgeClass = support.getStatusClass(t.statusCode);
                const statusText = $(`#target-status option[value='${t.statusCode}']`).text() || t.statusCode;
                badge.text(statusText).removeClass().addClass(`badge rounded-pill px-3 py-2 ${badgeClass}`);
                
                $("#target-status").val(t.statusCode);

                $("#admin-reply").val(t.adminReply || '');
                $("#internal-note").val(t.internalNote || '');

                $("#comments-list").empty().append(`
                    <div class="mb-3 p-3 bg-light rounded-4 border-0">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <small class="text-muted extra-small fw-bold text-uppercase">Yêu cầu từ khách hàng:</small>
                            <span class="badge bg-white text-muted extra-small border">Khởi tạo</span>
                        </div>
                        <div class="fw-medium text-dark">${support.escapeHtml(t.content)}</div>
                    </div>
                `);

                if (t.adminReply) {
                    $("#comments-list").append(`
                        <div class="mb-3 p-3 bg-accent bg-opacity-10 rounded-4 border-start border-accent border-4 shadow-sm">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <small class="text-accent extra-small fw-bold text-uppercase">Phản hồi của Admin:</small>
                                <span class="badge bg-accent text-white extra-small">Đã gửi khách</span>
                            </div>
                            <div class="text-dark">${support.escapeHtml(t.adminReply)}</div>
                        </div>
                    `);
                }

                if (t.internalNote) {
                    $("#comments-list").append(`
                        <div class="mb-2 p-3 bg-warning bg-opacity-10 rounded-4 border-start border-warning border-4 shadow-sm">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <small class="text-warning extra-small fw-bold text-uppercase">Ghi chú nội bộ:</small>
                                <span class="badge bg-warning text-dark extra-small">Private</span>
                            </div>
                            <div class="small italic text-muted">${support.escapeHtml(t.internalNote)}</div>
                        </div>
                    `);
                }
            }
        } catch (e) { 
            console.error(e);
            api.showToast("Lỗi khi tải chi tiết phiếu hỗ trợ", "error"); 
        }
    },

    // 4. Submit Response
    submitResponse: async () => {
        const id = currentTicketId;
        const reply = $("#admin-reply").val().trim();
        const note = $("#internal-note").val().trim();
        const status = $("#target-status").val();

        api.showToast("Đang cập nhật...", "info");
        try {
            await api.post(`/support/${id}/respond?reply=${encodeURIComponent(reply)}&internalNote=${encodeURIComponent(note)}&statusCode=${status}`);
            
            api.showToast("✓ Cập nhật hồ sơ thành công!", "success");

            // Lưu thời gian hiện tại vào biến toàn cục
            customUpdatedTime = new Date().toLocaleString('vi-VN');

            // Xóa rỗng ô nhập sau khi gửi thành công
            $("#admin-reply").val('');
            $("#internal-note").val('');

            // Tải lại chi tiết (hàm này sẽ tự động lấy customUpdatedTime để hiển thị)
            await support.loadTicketDetails(id);
            
        } catch (e) {
            api.showToast("Lỗi cập nhật: " + e.message, "error");
        }
    },

    // Utilities
    getStatusBadge: (status) => {
        const cls = support.getStatusClass(status);
        const text = status === 'OPEN' ? 'Mở' : 
                     status === 'PROCESSING' ? 'Đang xử lý' : 
                     status === 'RESOLVED' ? 'Đã giải quyết' : 
                     status === 'CLOSED' ? 'Đã đóng' : status;
        return `<span class="badge rounded-pill px-3 ${cls}">${text}</span>`;
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

    // AI Helper Utilities
    escapeHtml: (text) => {
        if (!text) return '';
        return $('<div>').text(text).html();
    },

    scrollToBottom: () => {
        const chatBox = $("#chat-box");
        if (chatBox.length) chatBox.scrollTop(chatBox[0].scrollHeight);
    },

    renderMarkdown: (text) => {
        if (!text) return "";
        let html = text
            .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
            .replace(/\*(.*?)\*/g, '<em>$1</em>')
            .replace(/^\s*•\s*(.*)/gm, '<li>$1</li>')
            .replace(/\n/g, '<br>');
        
        if (html.includes('<li>')) {
            html = html.replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>');
        }
        return html;
    },

    toggleChat: () => {
        $("#chatbot-container").toggleClass("active");
        if ($("#chatbot-container").hasClass("active")) {
            setTimeout(() => $("#chat-user-msg").focus(), 300);
            support.scrollToBottom();
        }
    }
};

$(document).on('click', '#btn-toggle-chat', () => support.toggleChat());