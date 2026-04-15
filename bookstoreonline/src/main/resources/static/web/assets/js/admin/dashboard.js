/**
 * dashboard.js - Admin Dashboard Logic
 * Aligned with AdminDashboardController.java:
 *   GET /api/admin/dashboard/revenue → RevenueReportDTO { totalRevenue, totalOrders, statusCheck }
 *   GET /api/admin/dashboard/ranking → List<BookRankingDTO> { isbn, tenSach, totalSold }
 *   GET /api/admin/audit-logs        → List<AuditLogDTO> { id, username, hanhDong, chiTiet, thoiDiem (String) }
 */
const dashboard = {
    init: () => {
        dashboard.loadStats();
        dashboard.loadRanking();
        dashboard.loadAuditLogs();
    },

    // 1. Comprehensive Stats
    loadStats: async () => {
        try {
            const res = await api.get('/admin/dashboard/stats');
            const stats = res.data || res;
            if (!stats) return;

            const statRevEl   = document.getElementById('stat-revenue');
            const statOrdEl   = document.getElementById('stat-orders');
            const statBooksEl = document.getElementById('stat-books');
            const statCustEl  = document.getElementById('stat-customers');
            const statLowEl   = document.getElementById('stat-low-stock');

            if (statRevEl)   statRevEl.innerText   = api.formatCurrency(stats.totalRevenue || 0);
            if (statOrdEl)   statOrdEl.innerText   = stats.pendingOrders   || 0;
            if (statBooksEl) statBooksEl.innerText = stats.totalBooks      || 0;
            if (statCustEl)  statCustEl.innerText  = stats.totalUsers      || 0;
            if (statLowEl)   statLowEl.innerText   = stats.lowStockCount   || 0;
            
        } catch (error) {
            console.error('Dashboard Stats Error:', error);
        }
    },

    // 2. Top Selling Books — BookRankingDTO uses 'title' not 'tenSach'
    loadRanking: async () => {
        try {
            const res = await api.get('/admin/dashboard/ranking');
            const data = res.data || [];
            const tbody = document.getElementById('ranking-list');
            if (!tbody) return;
            tbody.innerHTML = '';

            if (!data || data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted py-3">No ranking data available</td></tr>';
                return;
            }

            data.forEach((item, i) => {
                tbody.innerHTML += `
                    <tr>
                        <td class="ps-3">
                            <span class="badge ${i < 3 ? 'bg-warning text-dark' : 'bg-light text-dark'} rounded-pill">#${i + 1}</span>
                        </td>
                        <td class="fw-bold">${item.title || item.isbn}</td>
                        <td class="text-end pe-3 fw-bold text-accent">${item.totalSold || 0} books</td>
                    </tr>
                `;
            });
        } catch (e) {
            console.error('Ranking Error:', e);
            const tbody = document.getElementById('ranking-list');
            if (tbody) tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted py-3">Failed to load data</td></tr>';
        }
    },

    // 3. Audit Logs — Uses 'action', 'details', 'timestamp'
    loadAuditLogs: async () => {
        try {
            const res = await api.get('/admin/audit-logs');
            const data = res.data || [];
            const tbody = document.getElementById('audit-log-list');
            if (!tbody) return;
            tbody.innerHTML = '';

            if (!data || data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted py-3">No system logs found</td></tr>';
                return;
            }

            data.slice(0, 20).forEach(log => {
                tbody.innerHTML += `
                    <tr>
                        <td class="ps-3 fw-bold text-dark">${log.username || '---'}</td>
                        <td class="text-wrap">${log.details || log.action || '---'}</td>
                        <td class="text-end pe-3 text-muted small text-nowrap">${log.timestamp || '---'}</td>
                    </tr>
                `;
            });
        } catch (e) {
            console.error('Audit Log Error:', e);
            const tbody = document.getElementById('audit-log-list');
            if (tbody) tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted py-3">Không thể tải nhật ký</td></tr>';
        }
    },

    // 4. Export Report (tải báo cáo đơn giản dạng JSON)
    exportReport: async () => {
        api.showToast('Đang tạo báo cáo...', 'info');
        try {
            const [statsRes, rankRes] = await Promise.all([
                api.get('/admin/dashboard/stats'),
                api.get('/admin/dashboard/ranking')
            ]);
            const stats = statsRes.data || {};
            const ranking = rankRes.data || [];

            const report = {
                generatedAt: new Date().toLocaleString('vi-VN'),
                stats: {
                    totalRevenue: stats.totalRevenue || 0,
                    pendingOrders: stats.pendingOrders || 0,
                    totalBooks: stats.totalBooks || 0,
                    totalUsers: stats.totalUsers || 0,
                    lowStockCount: stats.lowStockCount || 0
                },
                topBooks: ranking.slice(0, 10).map((b, i) => ({
                    rank: i + 1,
                    title: b.title || b.isbn,
                    totalSold: b.totalSold || 0
                }))
            };

            const blob = new Blob([JSON.stringify(report, null, 2)], { type: 'application/json' });
            const url  = URL.createObjectURL(blob);
            const a    = document.createElement('a');
            a.href     = url;
            a.download = `booksaw_report_${new Date().toISOString().slice(0,10)}.json`;
            a.click();
            URL.revokeObjectURL(url);
            api.showToast('Xuất báo cáo thành công!', 'success');
        } catch (e) {
            api.showToast('Lỗi khi xuất báo cáo: ' + e.message, 'error');
        }
    }
};
