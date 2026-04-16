/**
 * auditlog.js - Audit Log & System Activity Tracking
 */

const auditlog = {
    /**
     * Load audit logs with filters
     */
    loadLogs: async () => {
        try {
            const username = $("#audit-username").val() || "";
            const action = $("#audit-action-filter").val() || "";
            
            let url = `/api/admin/audit-logs?`;
            if (username) url += `username=${encodeURIComponent(username)}&`;
            if (action) url += `action=${encodeURIComponent(action)}&`;

            const res = await api.get(url); 
            const logs = res.data || res;
            const tbody = $("#audit-list-body");
            if (!tbody.length) return;
            tbody.empty();

            if (!logs || logs.length === 0) {
                tbody.html('<tr><td colspan="5" class="text-center py-5 text-muted">Không tìm thấy hoạt động hệ thống nào.</td></tr>');
                return;
            }

            logs.forEach(log => {
                const actionBadge = auditlog.getActionBadge(log.action);
                const displayUser = log.account ? log.account.username : 'System';
                const timeStr = api.formatDate ? api.formatDate(log.timestamp, true) : log.timestamp;
                
                tbody.append(`
                    <tr>
                        <td class="ps-4">${timeStr}</td>
                        <td><strong>${displayUser}</strong></td>
                        <td>${actionBadge}</td>
                        <td title="${log.details}" class="text-truncate" style="max-width: 400px;">${log.details || '--'}</td>
                        <td class="text-end pe-4">
                            <button class="btn btn-sm btn-outline-secondary rounded-pill" onclick="auditlog.viewDetail('${log.logId}')">
                                Chi tiết
                            </button>
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
            const res = await api.get(`/admin/audit-stats`);
            const stats = res.data;

            $("#audit-total-count").text(stats.totalLogs || 0);
            $("#audit-today-count").text(stats.loginCount || 0);
            $("#audit-last-scan").text(stats.lastScan || '---');
        } catch (e) {
            console.error("Stats load error:", e);
        }
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
            'LOGIN':           '<span class="badge bg-dark text-white">Đăng nhập</span>',
            'CHANGE_PASSWORD': '<span class="badge bg-warning text-dark">Mật khẩu</span>',
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
