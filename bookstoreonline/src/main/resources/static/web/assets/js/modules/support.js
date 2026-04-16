/**
 * support.js - AI Chat Support & Customer Tickets Logic
 * Standardized for Full English Backend synchronization
 */
let currentTicketId = null;

const support = {
    sessionId: null,

    // Initialize Chat Widget — gắn event vào input trong widget mới
    initChat: () => {
        support.initSessionId();
        setTimeout(() => $("#chat-user-msg").focus(), 200);
    },

    initSessionId: () => {
        if (!support.sessionId) {
            support.sessionId = 'sess-' + Math.random().toString(36).substr(2, 9) + '-' + Date.now();
        }
    },

    // Send from input field
    sendChatFromInput: () => {
        const input = $("#chat-user-msg");
        const msg = input.val().trim();
        if (!msg) return;
        input.val("");

        if (support.isHumanMode) {
            // User bubble (immediate UI feedback)
            $("#chat-box").append(`
                <div class="bsw-msg-user">
                    <div class="bsw-bubble">${support.escapeHtml(msg)}</div>
                </div>
            `);
            support.scrollToBottom();
            support.sendHumanMessage(msg);
        } else {
            support.sendChat(msg);
        }
    },

    // Escape HTML to prevent XSS
    escapeHtml: (text) => $('<div>').text(text).html(),

    // Render markdown-lite: **bold**, newlines, bullet points
    renderMarkdown: (text) => {
        if (!text) return "";
        let html = support.escapeHtml(text)
            .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
            .replace(/\*(.*?)\*/g, '<em>$1</em>')
            .replace(/^\s*•\s*(.*)/gm, '<li>$1</li>')
            .replace(/\n/g, '<br>');

        if (html.includes('<li>')) {
            html = html.replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>');
        }
        return html;
    },

    sendChat: async (message) => {
        const chatBox = $("#chat-box");
        if (!chatBox.length) return;

        support.initSessionId();

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
                <div class="bsw-avatar" style="background: linear-gradient(135deg, #1A73E8, #9B72CB, #D96570); box-shadow: 0 3px 8px rgba(155, 114, 203, 0.2);">✨</div>
                <div class="bsw-bubble" style="color:#1A73E8; display:flex; align-items:center; gap:4px; border:none; background:transparent; box-shadow:none;">
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
            const url = `/support/ai-chat?message=${encodeURIComponent(message)}&sessionId=${support.sessionId}`;
            const res = await api.post(url);
            $(`#${typingId}`).remove();

            // Parse response: {message, quickReplies, sessionId}
            const data = res.data;
            const text = (typeof data === 'object' && data.message) ? data.message :
                (typeof data === 'string' ? data : 'Xin lỗi, tôi chưa hiểu ý bạn.');

            // AI bubble (Google Gemini Style with Fade In Effect)
            chatBox.append(`
                <div class="bsw-msg-ai" style="animation: bsw-fade-in 0.4s cubic-bezier(0.16, 1, 0.3, 1);">
                    <div class="bsw-avatar" style="background: linear-gradient(135deg, #1A73E8, #9B72CB, #D96570); box-shadow: 0 3px 8px rgba(155, 114, 203, 0.2);">✨</div>
                    <div class="bsw-bubble" style="background: linear-gradient(135deg, rgba(26,115,232,0.05), rgba(155,114,203,0.05)); border: 1px solid rgba(155,114,203,0.1);">${support.renderMarkdown(text)}</div>
                </div>
            `);

            if (data.quickReplies && data.quickReplies.length > 0) {
                support.appendQuickReplies(data.quickReplies);
            }

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

    appendQuickReplies: (replies) => {
        const chatBox = $("#chat-box");
        let html = '<div class="bsw-quick-replies d-flex flex-wrap gap-2 mt-2 px-5" style="animation: bsw-fade-in 0.6s ease;">';
        replies.forEach(r => {
            html += `<button class="btn btn-sm btn-outline-primary rounded-pill bg-white" onclick="support.sendChat('${r.replace(/'/g, "\\'")}')" style="font-size: 0.85rem; border-color: rgba(26,115,232,0.3); color: #1A73E8;">${r}</button>`;
        });
        html += '</div>';
        chatBox.append(html);
    },

    scrollToBottom: () => {
        const chatBox = $("#chat-box");
        if (chatBox.length) chatBox.scrollTop(chatBox[0].scrollHeight);
    },

    toggleChat: () => {
        const holder = $("#chat-box-holder");
        if (holder.children().length === 0) {
            holder.load("Shared/ChatWidget.html", function (response, status, xhr) {
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

    toggleChatMenu: () => {
        const user = api.getUser();
        const isAdmin = user && (user.role === 'STAFF' || user.role === 'ADMIN');

        const menu = $("#chat-options-menu");
        const isVisible = menu.is(":visible");

        if (isVisible) {
            $(".chat-bubble-opt").removeClass("chat-bubble-show");
            setTimeout(() => {
                menu.hide();
                // Clear dynamic bubbles for staff if needed
                if (isAdmin) menu.empty();
            }, 300);
            $("#main-chat-icon").show();
            $("#close-chat-icon").hide();
        } else {
            // Hide notification dot when opening menu; re-baseline so no false re-alert
            $("#staff-notif-dot").fadeOut(300).text("");
            support._staffLastCount = -1;
            support._customerLastCount = -1;

            // If chat box is open, close it first
            if ($("#chat-box-holder").is(":visible")) {
                $("#chat-box-holder").fadeOut(200);
            }

            if (isAdmin) {
                support.renderCustomerBubbles();
            } else {
                // Restore static options for customers if they were removed
                menu.html(`
                    <div class="chat-bubble-opt" onclick="support.startStaffChat()" title="Gặp Nhân Viên" style="
                        width: 50px; height: 50px; border-radius: 50%; background: #27ae60; color: white;
                        display: flex; align-items: center; justify-content: center; cursor: pointer;
                        box-shadow: 0 4px 15px rgba(39, 174, 96, 0.3); transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
                        transform: translateY(20px); opacity: 0; position: relative;
                    " onmouseover="this.style.transform='translateY(0) scale(1.1)'" onmouseout="this.style.transform='translateY(0) scale(1)'">
                        <span style="font-size: 1.2rem;">👨‍💼</span>
                        <span id="staff-bubble-dot" style="display:none; position:absolute; top:-2px; right:-2px; width:14px; height:14px; background:#e74c3c; border:2.5px solid #fff; border-radius:50%; box-shadow:0 2px 5px rgba(231,76,60,0.3)"></span>
                    </div>
                    <div class="chat-bubble-opt" onclick="support.startAiChat()" title="Trò chuyện AI" style="
                        width: 50px; height: 50px; border-radius: 50%; background: linear-gradient(135deg, #1A73E8, #9B72CB); color: white;
                        display: flex; align-items: center; justify-content: center; cursor: pointer;
                        box-shadow: 0 4px 15px rgba(26, 115, 232, 0.3); transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
                        transform: translateY(20px); opacity: 0;
                    " onmouseover="this.style.transform='translateY(0) scale(1.1)'" onmouseout="this.style.transform='translateY(0) scale(1)'">
                        <span style="font-size: 1.2rem;">🤖</span>
                    </div>
                `);

                // Show bubble dot if there's a global notification
                if ($("#staff-notif-dot").is(":visible")) {
                    setTimeout(() => $("#staff-bubble-dot").fadeIn(300), 200);
                }
                menu.show().css('display', 'flex');
                setTimeout(() => {
                    $(".chat-bubble-opt").each(function (i) {
                        setTimeout(() => $(this).addClass("chat-bubble-show"), i * 50);
                    });
                }, 10);
            }

            $("#main-chat-icon").hide();
            $("#close-chat-icon").show();
        }
    },

    renderCustomerBubbles: async () => {
        const menu = $("#chat-options-menu");
        menu.empty().show().css('display', 'flex');

        try {
            const res = await api.get('/support/active-sessions');
            const sessions = res.data || [];

            if (sessions.length === 0) {
                menu.append('<div class="chat-bubble-opt" style="width:auto; height:auto; padding:8px 16px; border-radius:20px; background:rgba(255,255,255,0.7); backdrop-filter:blur(10px); color:#666; font-size:0.75rem; font-weight:600; box-shadow:0 4px 10px rgba(0,0,0,0.05); border:1px solid rgba(0,0,0,0.05);">Không có khách đang chờ</div>');
            } else {
                // Show top 5 sessions
                sessions.slice(0, 5).forEach((session, i) => {
                    const initials = (session.customerName || 'C').split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase();
                    const hasUnread = session.hasUnreadMessages;

                    const bubble = $(`
                        <div class="chat-bubble-opt ${hasUnread ? 'has-new-pulse' : ''}" onclick="support.openStaffChatSession(${session.ticketId})" title="${session.customerName}" style="
                            width: 52px; height: 52px; border-radius: 50%; background: #ffffff; color: #1A73E8;
                            display: flex; align-items: center; justify-content: center; cursor: pointer;
                            box-shadow: 0 8px 20px rgba(0,0,0,0.08); border: 2.5px solid #1A73E8;
                            transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
                            transform: translateY(20px); opacity: 0; position: relative;
                            font-weight: 800; font-size: 0.95rem;
                        " onmouseover="this.style.transform='scale(1.15) rotate(5deg)'; this.style.background='#1A73E8'; this.style.color='#fff'" onmouseout="this.style.transform='scale(1) rotate(0deg)'; this.style.background='#ffffff'; this.style.color='#1A73E8'">
                            ${initials}
                            ${hasUnread ? '<span style="position:absolute; top:-2px; right:-2px; width:16px; height:16px; background:#e74c3c; border:3.5px solid #fff; border-radius:50%; box-shadow:0 2px 5px rgba(231,76,60,0.3)"></span>' : ''}
                        </div>
                    `);
                    menu.append(bubble);
                    setTimeout(() => bubble.addClass("chat-bubble-show"), i * 80);
                });
            }
        } catch (e) {
            console.error("Failed to load active sessions", e);
            menu.hide();
        }
    },

    openStaffChatSession: (ticketId) => {
        support.toggleChatMenu(); // Close the bubbles
        support.activeTicketId = ticketId;
        support.isHumanMode = true;
        support.lastMessageCount = 0;

        const holder = $("#chat-box-holder");
        holder.load("Shared/ChatWidget.html", function () {
            support.initChat();
            $("#chat-box").empty();

            $("#chat-title").text("Hỗ trợ: " + ticketId).css('color', '#1A73E8');
            $("#chat-status-dot").css('background', '#27ae60');
            $("#chat-status-text").text("Đang trực tuyến (Ưu tiên)");
            $("#chat-user-msg").attr("placeholder", "Gửi phản hồi cho khách hàng...");

            // Mark customer messages as read immediately when staff opens ticket
            api.post(`/support/${ticketId}/mark-read?isStaff=true`).catch(() => { });
            // Re-baseline staff badge
            support._staffLastCount = -1;

            holder.hide().fadeIn(400);
            support.startPolling();
        });
    },

    startAiChat: () => {
        support.toggleChatMenu();
        if (support.pollTimer) clearInterval(support.pollTimer);
        support.isHumanMode = false;

        const holder = $("#chat-box-holder");
        const alreadyLoaded = holder.children().length > 0;

        if (!alreadyLoaded) {
            support.toggleChat();
        } else {
            support.resetAiUI();
            holder.fadeIn(200);
        }
    },

    resetAiUI: () => {
        $("#chat-title").text("Booksaw Spark AI");
        $("#chat-status-dot").css('background', '#1A73E8');
        $("#chat-status-text").text("Thông minh · Tức thì");

        const chatBox = $("#chat-box");
        chatBox.html(`
            <!-- Welcome message -->
            <div class="bsw-msg-ai">
                <div class="bsw-avatar">✨</div>
                <div class="bsw-bubble" style="background: linear-gradient(135deg, rgba(26,115,232,0.05), rgba(155,114,203,0.05)); border: 1px solid rgba(155,114,203,0.1);">
                    Chào bạn! Tôi là <strong>Booksaw Spark AI</strong>. ✨<br><br>
                    Tôi có thể giúp bạn tìm kiếm sách, tra cứu thông tin nhanh chóng với trí tuệ nhân tạo. Bạn muốn tìm sách gì hôm nay?
                </div>
            </div>
        `);
    },

    startStaffChat: () => {
        support.toggleChatMenu();
        const holder = $("#chat-box-holder");
        if (holder.children().length === 0) {
            holder.load("Shared/ChatWidget.html", function (response, status, xhr) {
                if (status === "error") {
                    api.showToast("Không thể tải chatbot", "error");
                    return;
                }
                support.initChat();
                holder.fadeIn(200);
                setTimeout(() => support.switchToHumanMode(), 500);
            });
        } else {
            holder.fadeIn(200);
            // If already opened but in AI mode, switch it
            support.switchToHumanMode();
        }
    },

    // ── HUMAN CHAT LOGIC ──
    isHumanMode: false,
    pollTimer: null,
    activeTicketId: null,

    switchToHumanMode: async () => {
        const user = api.getUser();
        if (!user) {
            api.showToast("Vui lòng đăng nhập để chat với nhân viên", "warning");
            layout.render('Auth', 'Login');
            return;
        }

        // Staff and Admin should use the Support Hub, not the customer chat
        if (user.role === 'ADMIN' || user.role === 'STAFF') {
            api.showToast("Bạn đang đăng nhập với quyền Nhân viên. Vui lòng sử dụng Trung tâm hỗ trợ trong Dashboard.", "info");
            layout.render('Dashboard', 'Admin/Index');
            return;
        }

        support.isHumanMode = true;
        $("#chat-title").text("Hỗ trợ bởi Nhân viên");
        $("#chat-status-dot").css('background', '#27ae60');
        $("#chat-status-text").text("Đang trực tuyến");

        const chatBox = $("#chat-box");
        // Clear AI messages if switching for the first time in this session
        if (chatBox.find(".bsw-msg-ai").first().find("strong").text().includes("Spark AI")) {
            chatBox.empty();
        }

        chatBox.append(`
            <div class="text-center my-3" id="connecting-divider" style="font-size: 0.75rem; color: #999;">
                <hr class="mb-1">
                Đang kết nối với nhân viên hỗ trợ...
            </div>
        `);

        try {
            console.log("Connecting to human support for user:", user.username);

            // Check for existing active ticket or create new
            const res = await api.get(`/support/user/${user.username}`);
            const tickets = res.data || [];
            // Sort by ticketId descending to get latest
            tickets.sort((a, b) => (b.ticketId || b.id) - (a.ticketId || a.id));

            const active = tickets.find(t => t.statusCode !== 'CLOSED' && t.statusCode !== 'RESOLVED');

            if (active) {
                support.activeTicketId = active.ticketId || active.id;
                console.log("Joined existing active ticket:", support.activeTicketId);
            } else {
                console.log("No active ticket found, creating a new chat session...");
                // Create a new "Chat Session" ticket
                const createRes = await api.post(`/support?username=${encodeURIComponent(user.username)}&subject=Chat Session&content=Customer started a live chat.`, null);

                // Re-fetch to get the new ticket ID
                const res2 = await api.get(`/support/user/${user.username}`);
                const tickets2 = res2.data || [];
                tickets2.sort((a, b) => (b.ticketId || b.id) - (a.ticketId || a.id));

                if (tickets2.length > 0) {
                    support.activeTicketId = tickets2[0].ticketId || tickets2[0].id;
                    console.log("New ticket created:", support.activeTicketId);
                } else {
                    throw new Error("Could not retrieve newly created ticket");
                }
            }

            if (!support.activeTicketId) throw new Error("No ticket ID available");

            // Mark staff messages as read immediately when customer opens chat
            api.post(`/support/${support.activeTicketId}/mark-read?isStaff=false`).catch(() => { });
            // Re-baseline customer badge
            support._customerLastCount = -1;

            support.startPolling();
            chatBox.append(`
                <div class="bsw-msg-ai">
                    <div class="bsw-avatar" style="background:#27ae60">👨‍💼</div>
                    <div class="bsw-bubble">Chào ${user.fullName || user.username}, tôi là nhân viên hỗ trợ. Tôi có thể giúp gì cho bạn?</div>
                </div>
            `);
            support.scrollToBottom();
        } catch (e) {
            console.error("Staff connection error:", e);
            const msg = (e.message && e.message.includes("Customer not found"))
                ? "Lỗi: Tài khoản của bạn chưa được liên kết với dữ liệu Khách hàng."
                : ("Không thể kết nối với nhân viên: " + (e.message || "Lỗi không xác định"));
            api.showToast(msg, "error");
        }
    },

    startPolling: () => {
        if (support.pollTimer) clearInterval(support.pollTimer);
        // Load immediately once
        support.pollHumanMessages();
        // Then set interval
        support.pollTimer = setInterval(support.pollHumanMessages, 4000);
    },

    pollHumanMessages: async () => {
        if (!support.isHumanMode || !support.activeTicketId) return;

        try {
            const res = await api.get(`/support/${support.activeTicketId}/messages`);
            const messages = res.data || [];
            support.renderHumanMessages(messages);

            // Always mark as read while chat is visible
            const isVisible = $("#chat-box-holder").is(":visible");
            if (isVisible) {
                const user = api.getUser();
                const isStaff = user && (user.role === 'STAFF' || user.role === 'ADMIN');
                // Fire-and-forget — mark opposite side's messages as read
                api.post(`/support/${support.activeTicketId}/mark-read?isStaff=${isStaff}`).catch(() => { });
                // Optimistically clear badge immediately
                $("#staff-notif-dot").hide().text("");
                if (isStaff) support._staffLastCount = -1;
                else support._customerLastCount = -1;
            }
        } catch (e) { console.error("Polling failed", e); }
    },

    lastMessageCount: 0,
    renderHumanMessages: (messages) => {
        if (messages.length <= support.lastMessageCount) return;

        const chatBox = $("#chat-box");
        const user = api.getUser();
        const curUserIsStaff = user && (user.role === 'STAFF' || user.role === 'ADMIN');

        // Only append new messages
        const newOnes = messages.slice(support.lastMessageCount);
        newOnes.forEach(m => {
            const isMe = (m.staff === curUserIsStaff);

            if (isMe) {
                // RIGHT side (Me)
                chatBox.append(`
                    <div class="bsw-msg-user" style="animation: bsw-fade-in 0.4s ease;">
                        <div class="bsw-bubble">${support.escapeHtml(m.content)}</div>
                    </div>
                `);
            } else {
                // LEFT side (The other person)
                const avatar = m.staff ? '👨‍💼' : '👤';
                const avatarBg = m.staff ? '#27ae60' : '#1A73E8';

                chatBox.append(`
                    <div class="bsw-msg-ai" style="animation: bsw-fade-in 0.4s ease;">
                        <div class="bsw-avatar" style="background:${avatarBg}">${avatar}</div>
                        <div class="bsw-bubble">${support.renderMarkdown(m.content)}</div>
                    </div>
                `);
            }
        });
        support.lastMessageCount = messages.length;
        support.scrollToBottom();
    },

    sendHumanMessage: async (msg) => {
        if (!support.activeTicketId) return;
        const user = api.getUser();
        const isAdmin = user && (user.role === 'STAFF' || user.role === 'ADMIN');

        try {
            await api.post(`/support/${support.activeTicketId}/messages?senderName=${encodeURIComponent(user.username)}&isStaff=${isAdmin}&content=${encodeURIComponent(msg)}`);
            // We don't need to do anything here, polling will pick it up or it's already in UI
            support.lastMessageCount++; // Increment to skip the one we just sent
        } catch (e) { api.showToast("Gửi tin nhắn thất bại", "error"); }
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
            const res = await api.get('/api/support');
            const data = Array.isArray(res) ? res : (res.data || []);
            const tbody = $("#support-list-body");
            if (!tbody.length) return;
            tbody.empty();

            data.forEach(t => {
                const statusBadge = support.getStatusBadge(t.statusCode || t.status);
                tbody.append(`
                    <tr onclick="layout.render('Support/Admin', 'Details', '${t.ticketId || t.id}')" style="cursor:pointer">
                        <td class="ps-4 fw-bold">#${t.ticketId || t.id}</td>
                        <td>${t.customerName || '---'}</td>
                        <td class="text-truncate" style="max-width: 250px;">${t.title || t.subject}</td>
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

    /**
     * Load ticket details (Admin)
     */
    loadTicketDetails: async (id) => {
        currentTicketId = id;
        try {
            const res = await api.get('/api/support');
            const list = Array.isArray(res) ? res : (res.data || []);
            const t = list.find(x => (x.ticketId || x.id) == id);

            if (t) {
                const title = t.title || t.subject;
                const statusCode = t.statusCode || t.status;
                const ticketId = t.ticketId || t.id;

                $("#ticket-detail-title").text(title);
                $("#ticket-detail-id").text(`ID: #${ticketId}`);
                $("#ticket-detail-content").text(t.content);
                $("#ticket-customer-name").text(t.customerName || 'Customer');
                $("#ticket-customer-initial").text((t.customerName || 'C').charAt(0).toUpperCase());
                $("#ticket-created-date").text(new Date(t.createdAt).toLocaleString());

                const badge = $("#ticket-status-badge");
                badge.text(statusCode).removeClass().addClass(`badge rounded-pill px-3 py-2 ${support.getStatusClass(statusCode)}`);

                $("#target-status").val(statusCode);
                $("#admin-reply").val('');
                $("#internal-note").val(t.internalNote || '');

                // Start polling for this ticket's messages in Admin view
                support.isHumanMode = true; // reusing variable for context
                support.activeTicketId = ticketId;
                support.startAdminPolling();
            }
        } catch (e) { api.showToast("Lỗi khi tải chi tiết phiếu hỗ trợ", "error"); }
    },

    startAdminPolling: () => {
        if (support.pollTimer) clearInterval(support.pollTimer);
        support.pollTimer = setInterval(async () => {
            const res = await api.get(`/support/${support.activeTicketId}/messages`);
            const messages = res.data || [];
            support.renderAdminChat(messages);
        }, 3000);
    },

    adminLastMsgCount: 0,
    renderAdminChat: (messages) => {
        const chatBox = $("#admin-chat-box");
        if (messages.length === 0) {
            chatBox.html('<div class="text-center py-5 text-muted">Chưa có tin nhắn nào.</div>');
            return;
        }
        if (messages.length <= support.adminLastMsgCount) return;

        if (support.adminLastMsgCount === 0) chatBox.empty();

        const newOnes = messages.slice(support.adminLastMsgCount);
        newOnes.forEach(m => {
            const isMe = m.staff;
            chatBox.append(`
                <div class="${isMe ? 'align-self-end text-end' : 'align-self-start'}" style="max-width: 80%;">
                    <div class="small text-muted mb-1 px-2">${m.senderName} • ${new Date(m.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</div>
                    <div class="p-3 rounded-4 ${isMe ? 'bg-accent text-white rounded-tr-0' : 'bg-white shadow-sm border rounded-tl-0'}" 
                         style="border-radius: ${isMe ? '20px 20px 4px 20px' : '20px 20px 20px 4px'};">
                        ${m.content}
                    </div>
                </div>
            `);
        });

        support.adminLastMsgCount = messages.length;
        chatBox.scrollTop(chatBox[0].scrollHeight);
    },

    submitAdminChatMessage: async () => {
        const msg = $("#admin-reply").val().trim();
        if (!msg) return;

        const ticketId = support.activeTicketId;
        const autoClose = $("#change-status-check").is(":checked");

        try {
            await api.post(`/api/support/${ticketId}/messages?senderName=Admin&isStaff=true&content=${encodeURIComponent(msg)}`);
            $("#admin-reply").val("");

            if (autoClose) {
                await api.post(`/api/support/${ticketId}/respond?reply=&statusCode=RESOLVED`);
                $("#target-status").val("RESOLVED");
                const badge = $("#ticket-status-badge");
                badge.text("RESOLVED").removeClass().addClass(`badge rounded-pill px-3 py-2 bg-success`);
            }

            // Re-fetch immediately for smooth UI
            const res = await api.get(`/support/${ticketId}/messages`);
            support.renderAdminChat(res.data);
        } catch (e) { api.showToast("Không thể gửi tin nhắn", "error"); }
    },

    updateTicketStatusFromSelect: async () => {
        const status = $("#target-status").val();
        try {
            await api.post(`/api/support/${support.activeTicketId}/respond?statusCode=${status}`);
            api.showToast("Đã cập nhật trạng thái", "success");
            const badge = $("#ticket-status-badge");
            badge.text(status).removeClass().addClass(`badge rounded-pill px-3 py-2 ${support.getStatusClass(status)}`);
        } catch (e) { api.showToast("Cập nhật thất bại", "error"); }
    },

    updateInternalNote: async () => {
        const note = $("#internal-note").val();
        try {
            await api.post(`/api/support/${support.activeTicketId}/respond?internalNote=${encodeURIComponent(note)}`);
            api.showToast("Đã lưu ghi chú", "success");
        } catch (e) { api.showToast("Lưu ghi chú thất bại", "error"); }
    },

    /**
     * Get status badge HTML
     */
    getStatusBadge: (status) => {
        const cls = support.getStatusClass(status);
        return `<span class="badge rounded-pill px-3 ${cls}">${status}</span>`;
    },

    /**
     * Get status CSS class
     */
    getStatusClass: (status) => {
        switch (status) {
            case 'OPEN': return 'bg-danger';
            case 'PROCESSING': return 'bg-warning text-dark';
            case 'RESOLVED': return 'bg-success';
            case 'CLOSED': return 'bg-secondary';
            default: return 'bg-dark';
        }
    },

    confirmDelete: async () => {
        if (!confirm("Bạn có chắc chắn muốn xóa yêu cầu hỗ trợ này không?")) return;
        api.showToast("Tính năng này đã bị vô hiệu hóa vì lý do an toàn hệ thống", "warning");
    },
    // ========== NOTIFICATIONS: STAFF ==========
    staffNotifyTimer: null,
    _staffLastCount: -1, // -1 = not yet initialized

    startGlobalStaffNotifications: () => {
        if (support.staffNotifyTimer) return;
        support._staffLastCount = -1; // Reset on start
        support.staffNotifyTimer = setInterval(support.checkStaffNotifications, 5000);
    },

    checkStaffNotifications: async () => {
        try {
            const user = api.getUser();
            if (!user || (user.role !== 'STAFF' && user.role !== 'ADMIN')) {
                clearInterval(support.staffNotifyTimer);
                support.staffNotifyTimer = null;
                return;
            }

            const res = await api.get('/support/staff/unread-count');
            const count = parseInt(res.data) || 0;

            // Update badge always
            if (count > 0) {
                $("#staff-notif-dot").text(count).show();
            } else {
                $("#staff-notif-dot").hide().text("");
            }

            // Toast + sound only when count genuinely increased AFTER first load
            if (support._staffLastCount >= 0 && count > support._staffLastCount) {
                const newMsgs = count - support._staffLastCount;
                api.showToast(`💬 ${newMsgs} tin nhắn mới từ khách hàng!`, "warning");
                try {
                    const audio = new Audio('https://assets.mixkit.co/active_storage/sfx/2358/2358-preview.mp3');
                    audio.volume = 0.4;
                    audio.play();
                } catch (e) { }
            }

            support._staffLastCount = count;
        } catch (e) {
            console.error("Staff notification check failed:", e);
        }
    },

    // ========== NOTIFICATIONS: CUSTOMER ==========
    customerNotifyTimer: null,
    _customerLastCount: -1, // -1 = not yet initialized

    startGlobalCustomerNotifications: () => {
        if (support.customerNotifyTimer) return;
        support._customerLastCount = -1; // Reset on start
        support.customerNotifyTimer = setInterval(support.checkCustomerNotifications, 5000);
    },

    checkCustomerNotifications: async () => {
        try {
            const user = api.getUser();
            if (!user || user.role === 'STAFF' || user.role === 'ADMIN') {
                clearInterval(support.customerNotifyTimer);
                support.customerNotifyTimer = null;
                return;
            }

            const res = await api.get(`/support/unread-count?username=${encodeURIComponent(user.username)}`);
            const count = parseInt(res.data) || 0;

            // Update badge always
            if (count > 0) {
                $("#staff-notif-dot").text(count).show();
            } else {
                $("#staff-notif-dot").hide().text("");
            }

            // Toast + sound only when count genuinely increased AFTER first load
            if (support._customerLastCount >= 0 && count > support._customerLastCount) {
                const newMsgs = count - support._customerLastCount;
                api.showToast(`💬 ${newMsgs} tin nhắn mới từ nhân viên!`, "info");
                try {
                    const audio = new Audio('https://assets.mixkit.co/active_storage/sfx/2358/2358-preview.mp3');
                    audio.volume = 0.4;
                    audio.play();
                } catch (e) { }
            }

            support._customerLastCount = count;
        } catch (e) {
            console.error("Customer notification check failed:", e);
        }
    }
};

// Global toggle event
$(document).on('click', '#btn-toggle-chat', () => support.toggleChat());