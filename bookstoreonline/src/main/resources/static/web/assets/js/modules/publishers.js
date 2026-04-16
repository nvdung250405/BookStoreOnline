/**
 * publishers.js - Publisher Management Logic
 */
const publishers = {
    _adminList: [],

    loadAdminList: async () => {
        try {
            const res = await api.get('/publishers');
            publishers._adminList = Array.isArray(res) ? res : (res.data || []);
            publishers.renderAdminTable();
        } catch (e) {
            console.error("Publishers fetch error", e);
            api.showToast("Không thể tải danh sách nhà xuất bản", "error");
        }
    },

    renderAdminTable: () => {
        const tbody = $("#publishers-admin-list");
        if (!tbody.length) return;
        tbody.empty();

        if (publishers._adminList.length === 0) {
            tbody.html('<tr><td colspan="3" class="text-center py-4">Không tìm thấy nhà xuất bản nào.</td></tr>');
            return;
        }

        publishers._adminList.forEach(p => {
            tbody.append(`
                <tr>
                    <td class="ps-4 fw-bold text-accent">#${p.publisherId}</td>
                    <td>${p.publisherName}</td>
                    <td class="text-end pe-4">
                        <button onclick="publishers.openEdit(${p.publisherId})" class="btn btn-sm btn-outline-primary rounded-pill px-3 me-2">Sửa</button>
                        <button onclick="publishers.delete(${p.publisherId})" class="btn btn-sm btn-outline-danger rounded-pill px-3">Xóa</button>
                    </td>
                </tr>
            `);
        });
    },

    create: async () => {
        const name = $("#publisher-name").val().trim();
        if (!name) { api.showToast("Vui lòng nhập tên nhà xuất bản", "warning"); return; }
        try {
            await api.post('/publishers', { publisherName: name });
            api.showToast("Đã tạo nhà xuất bản thành công!");
            layout.render('Publishers/Admin', 'Index');
        } catch (e) {
            api.showToast("Tạo thất bại: " + e.message, "error");
        }
    },

    openEdit: async (id) => {
        const pub = publishers._adminList.find(p => p.publisherId === id);
        if (!pub) return;
        layout.render('Publishers/Admin', 'Edit', { id: id });
        setTimeout(() => {
            $("#edit-publisher-id").val(pub.publisherId);
            $("#edit-publisher-name").val(pub.publisherName);
        }, 100);
    },

    update: async () => {
        const id = $("#edit-publisher-id").val();
        const name = $("#edit-publisher-name").val().trim();
        if (!name) { api.showToast("Vui lòng nhập tên nhà xuất bản", "warning"); return; }
        try {
            await api.put(`/publishers/${id}`, { publisherName: name });
            api.showToast("Đã cập nhật nhà xuất bản thành công!");
            layout.render('Publishers/Admin', 'Index');
        } catch (e) {
            api.showToast("Cập nhật thất bại: " + e.message, "error");
        }
    },

    delete: async (id) => {
        if (!confirm("Bạn có chắc chắn muốn xóa nhà xuất bản này không?")) return;
        try {
            await api.delete(`/publishers/${id}`);
            api.showToast("Đã xóa nhà xuất bản thành công!");
            publishers.loadAdminList();
        } catch (e) {
            api.showToast("Xóa thất bại: " + e.message, "error");
        }
    }
};
