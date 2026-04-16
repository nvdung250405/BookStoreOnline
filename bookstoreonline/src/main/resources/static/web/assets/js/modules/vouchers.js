/**
 * vouchers.js - Discount & Voucher Management
 */
const vouchers = {
    _adminList: [],

    // Helper: Lấy instance của Modal chuẩn Bootstrap 5
    _getModal: () => {
        return bootstrap.Modal.getOrCreateInstance(document.getElementById('voucher-modal'));
    },

    _getDeleteModal: () => {
        return bootstrap.Modal.getOrCreateInstance(document.getElementById('delete-voucher-modal'));
    },

    loadAdminList: async () => {
        try {
            // Sửa lại thành /admin/vouchers (nếu backend của bạn là /vouchers thì xóa chữ /admin đi nhé)
            const res = await api.get('/vouchers');
            vouchers._adminList = Array.isArray(res) ? res : (res.data || []);
            vouchers.renderAdminTable();
        } catch (e) {
            console.error(e);
            api.showToast("Không thể tải danh sách mã giảm giá", "error");
        }
    },

    renderAdminTable: () => {
        const tbody = $("#vouchers-admin-list");
        if (!tbody.length) return;
        tbody.empty();

        if (vouchers._adminList.length === 0) {
            tbody.html('<tr><td colspan="5" class="text-center py-4">Không tìm thấy mã giảm giá nào.</td></tr>');
            return;
        }

        const now = new Date();

        vouchers._adminList.forEach(v => {
            const expiry = new Date(v.expiryDate);
            const isExpired = expiry < now;
            const statusBadge = isExpired
                ? '<span class="badge bg-danger bg-opacity-15 text-danger rounded-pill px-3">Hết hạn</span>'
                : '<span class="badge bg-success bg-opacity-15 rounded-pill px-3">Hoạt động</span>';

            tbody.append(`
                <tr>
                    <td class="ps-4 fw-bold text-accent">${v.voucherCode}</td>
                    <td><span class="fw-bold text-dark">${v.discountValue}%</span> Giảm</td>
                    <td>Đơn tối thiểu: ${api.formatCurrency(v.minCondition || 0)}</td>
                    <td>${common.formatDate(v.expiryDate)} ${statusBadge}</td>
                    <td class="text-end pe-4">
                        <button onclick="vouchers.openEdit('${v.voucherCode}')" class="btn btn-sm btn-outline-primary rounded-pill px-3 me-2">Sửa</button>
                        <button onclick="vouchers.deleteVoucher('${v.voucherCode}')" class="btn btn-sm btn-outline-danger rounded-pill px-3">Xóa</button>
                    </td>
                </tr>
            `);
        });
    },

    create: async () => {
        const code = $("#v-code").val().trim();
        const discount = $("#v-discount").val();
        const minCond = $("#v-min").val();
        const expiry = $("#v-expiry").val();

        if (!code || !discount || !expiry) {
            api.showToast("Vui lòng điền đầy đủ các trường bắt buộc", "warning"); return;
        }

        const payload = {
            voucherCode: code,
            discountValue: parseInt(discount),
            minCondition: parseFloat(minCond) || 0,
            // Thêm giờ 23:59:59 để voucher có hạn đến cuối ngày
            expiryDate: expiry + "T23:59:59"
        };

        try {
            await api.post('/vouchers', payload);
            api.showToast("Đã tạo mã giảm giá thành công!");
            vouchers._getModal().hide(); // Đóng modal chuẩn BS5
            vouchers.loadAdminList();
        } catch (e) {
            api.showToast("Tạo thất bại: " + e.message, "error");
        }
    },

    openEdit: (code) => {
        const v = vouchers._adminList.find(x => x.voucherCode === code);
        if (!v) return;

        $("#v-code").val(v.voucherCode).attr('readonly', true);
        $("#v-discount").val(v.discountValue);
        $("#v-min").val(v.minCondition);

        // FIX LỖI GIỜ UTC: Cộng bù múi giờ Việt Nam trước khi cắt chuỗi lấy ngày
        const d = new Date(v.expiryDate);
        d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
        const date = d.toISOString().split('T')[0];

        $("#v-expiry").val(date);

        $("#voucher-modal-title").text("Chỉnh sửa Voucher");
        $("#btn-save-voucher").attr('onclick', 'vouchers.update()');
        vouchers._getModal().show(); // Mở modal chuẩn BS5
    },

    update: async () => {
        const code = $("#v-code").val();
        const discount = $("#v-discount").val();
        const minCond = $("#v-min").val();
        const expiry = $("#v-expiry").val();

        const payload = {
            voucherCode: code,
            discountValue: parseInt(discount),
            minCondition: parseFloat(minCond) || 0,
            expiryDate: expiry + "T23:59:59"
        };

        try {
            await api.put(`/vouchers/${code}`, payload);
            api.showToast("Đã cập nhật mã giảm giá thành công!");
            vouchers._getModal().hide();
            vouchers.loadAdminList();
        } catch (e) {
            api.showToast("Cập nhật thất bại: " + e.message, "error");
        }
    },

    openCreate: () => {
        $("#v-code").val('').attr('readonly', false);
        $("#v-discount").val('');
        $("#v-min").val('0');
        $("#v-expiry").val('');
        $("#voucher-modal-title").text("Tạo Voucher mới");
        $("#btn-save-voucher").attr('onclick', 'vouchers.create()');
        vouchers._getModal().show();
    },

    deleteVoucher: (code) => {
        // Gắn mã code vào text để hiển thị cho người dùng biết đang xóa mã nào
        $("#del-voucher-code").text(code);

        // Gắn sự kiện click cho nút "Xóa ngay" trong modal
        $("#btn-confirm-delete-voucher").attr('onclick', `vouchers.performDelete('${code}')`);

        // Hiển thị modal
        vouchers._getDeleteModal().show();
    },

    // 3. Thêm hàm thực thi xóa API này vào cuối cùng
    performDelete: async (code) => {
        // Đổi trạng thái nút bấm để tránh click nhiều lần
        const btn = $("#btn-confirm-delete-voucher");
        const originalText = btn.text();
        btn.html('<span class="spinner-border spinner-border-sm me-1"></span> Đang xóa...').prop('disabled', true);

        try {
            await api.delete(`/vouchers/${code}`);
            api.showToast("Đã xóa mã giảm giá thành công");
            vouchers._getDeleteModal().hide(); // Đóng modal
            vouchers.loadAdminList();          // Tải lại bảng
        } catch (e) {
            api.showToast("Xóa mã giảm giá thất bại", "error");
        } finally {
            // Trả lại trạng thái nút bấm
            btn.html(originalText).prop('disabled', false);
        }
    }
};