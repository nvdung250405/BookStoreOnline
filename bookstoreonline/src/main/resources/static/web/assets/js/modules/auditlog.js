/**
 * auditlog.js - Audit Log & System Activity Tracking
 */

const auditlog = {
    /**
     * Load audit logs with filters
     */
    loadLogs: async () => auditlog.loadList(),
    loadList: async () => {
    loadLogs: async () => {
        try {
            const username = $("#audit-username").val() || "";
            const action = $("#audit-action-filter").val() || "";
            
            let url = `/admin/audit-logs?`;
            if (username) url += `username=${encodeURIComponent(username)}&`;
            if (action) url += `action=${encodeURIComponent(action)}&`;

            const res = await api.get(url); 
            const logs = res.data || [];
            const tbody = $("#audit-list-body");
            if (!tbody.length) return;
            tbody.empty();

            if (!logs || logs.length === 0) {
                tbody.html('<tr><td colspan="4" class="text-center py-5 text-muted">Không tìm thấy hoạt động hệ thống nào.</td></tr>');
                return;
            }

            logs.forEach(log => {
                const actionBadge = auditlog.getActionBadge(log.action);
                const displayUser = log.username || 'System';
                const timeStr = formatDateTime(log.timestamp);
                
                tbody.append(`
                    <tr class="transition-all hvr-light">
                        <td class="ps-4 py-4 text-muted" style="font-size: 0.9rem;">${timeStr}</td>
                        <td class="py-4"><span class="fw-bold text-dark">${displayUser}</span></td>
                        <td class="py-4">${actionBadge}</td>
                        <td class="py-4 text-secondary" title="${log.details}" style="font-size: 0.95rem; max-width: 500px;">
                            ${log.details || '--'}
                        </td>
                    </tr>
                `);
            });
        } catch (e) {
            api.showToast("Không thể tải nhật ký hệ thống: " + e.message, "error");
        }
    },

    /**
     * View audit log detail
     */
    viewDetail: async (logId) => {
        try {
            const res = await api.get(`/admin/audit-logs/${logId}`);
            const log = res.data;

            console.log("Audit log detail:", log);
            
            // In a real scenario, this would populate a modal
            api.showToast("Chi tiết nhật ký đã được in ra console", "info");
        } catch (e) {
            api.showToast("Lỗi tải chi tiết: " + e.message, "error");
        }
    },

    /**
     * Update statistics
     */
    updateStatistics: async () => {
        try {
            const res = await api.get(`/admin/audit-logs/stats`);
            const stats = res.data;

            if (stats) {
                $("#audit-total-count").text(stats.totalLogs.toLocaleString() || 0);
                $("#audit-today-count").text(stats.todayLogs.toLocaleString() || 0);
                $("#audit-active-users").text(stats.activeUsersCount.toLocaleString() || 0);
                $("#audit-changes-count").text(stats.dataChangesCount.toLocaleString() || 0);
            }
        } catch (e) {
            console.error("Stats load error:", e);
        }
    },

    init: () => {
        auditlog.updateStatistics();
        auditlog.loadLogs();
        
        // Auto-refresh every 30s
        setInterval(() => auditlog.updateStatistics(), 30000);
    },

    /**
     * Get action badge HTML - Aligned with AuditAction.java Enum
     */
    getActionBadge: (action) => {
        const badges = {
            'CREATE_BOOK':     '<span class="badge bg-success text-white">Tạo sách</span>',
            'UPDATE_BOOK':     '<span class="badge bg-info text-white">Cập nhật sách</span>',
            'DELETE_BOOK':     '<span class="badge bg-danger text-white">Xóa sách</span>',
            'CREATE_CATEGORY': '<span class="badge bg-success text-white">Danh mục mới</span>',
            'UPDATE_ORDER_STATUS': '<span class="badge bg-primary text-white">Trạng thái đơn</span>',
            'ORDER_CHECKOUT':  '<span class="badge bg-purple text-white" style="background-color: #6f42c1">Đặt hàng</span>',
            'CANCEL_ORDER':    '<span class="badge bg-secondary text-white">Hủy đơn</span>',
            'CREATE_TICKET':   '<span class="badge bg-warning text-dark">Gửi Ticket</span>',
            'RESPOND_TICKET':  '<span class="badge bg-info text-dark">Phản hồi Ticket</span>',
            'ACCOUNT_CREATE':  '<span class="badge bg-primary text-white">Tạo NV</span>',
            'LOGIN':           '<span class="badge bg-dark text-white">Đăng nhập</span>',
            'STOCK_UPDATE':    '<span class="badge bg-secondary text-white">Tồn kho</span>'
        };
        
        return badges[action] || `<span class="badge bg-light text-dark border">${action || 'Hoạt động'}</span>`;
    }
};

/**
 * Format date and time
 */
function formatDateTime(timestamp) {
    return new Date(timestamp).toLocaleString('vi-VN');
}

/**
 * Format currency to VND
 */
function formatCurrency(value) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0);
}
