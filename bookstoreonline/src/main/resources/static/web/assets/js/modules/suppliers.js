/**
 * suppliers.js - Supplier & Partner Management
 */
const suppliers = {
    loadList: async () => {
        try {
            const res = await api.get('/api/admin/suppliers');
            const list = res.data || [];
            const tbody = $("#suppliers-list");
            if (!tbody.length) return;
            tbody.empty();

            if (list.length === 0) {
                tbody.html('<tr><td colspan="4" class="text-center py-4 text-muted">Không tìm thấy nhà cung cấp nào.</td></tr>');
                return;
            }

            list.forEach(s => {
                tbody.append(`
                    <tr>
                        <td class="ps-4 fw-bold text-dark">${s.supplierName}</td>
                        <td>${s.contactInfo || '---'}</td>
                        <td class="text-end pe-4">
                            <button onclick="layout.render('Suppliers/Admin', 'Edit', {id: ${s.supplierId}})" class="btn btn-sm btn-outline-accent rounded-pill px-3 me-1">Sửa</button>
                            <button onclick="suppliers.delete(${s.supplierId})" class="btn btn-sm btn-outline-danger rounded-pill px-3">Xóa</button>
                        </td>
                    </tr>
                `);
            });
        } catch (e) {
            console.error(e);
            api.showToast("Không thể tải danh sách nhà cung cấp", "error");
        }
    },

    save: async (event) => {
        if (event) event.preventDefault();
        const id = $("#supplier-id").val();
        const dto = {
            supplierName: $("#supplier-name").val().trim(),
            contactInfo: $("#supplier-contact").val().trim()
        };

        if (!dto.supplierName) {
            api.showToast("Vui lòng nhập tên nhà cung cấp", "warning"); return;
        }

        try {
            if (id) {
                await api.put(`/api/admin/suppliers/${id}`, dto);
                api.showToast("Đã cập nhật nhà cung cấp thành công");
            } else {
                await api.post('/api/admin/suppliers', dto);
                api.showToast("Đã thêm nhà cung cấp mới");
            }
            layout.render('Suppliers/Admin', 'Index');
        } catch (e) {
            api.showToast("Lỗi vận hành: " + e.message, "error");
        }
    },

    loadDetail: async (id) => {
        if (!id) return;
        try {
            const res = await api.get(`/api/admin/suppliers`);
            const list = res.data || [];
            const s = list.find(item => item.supplierId == id);
            if (s) {
                $("#supplier-id").val(s.supplierId);
                $("#supplier-name").val(s.supplierName);
                $("#supplier-contact").val(s.contactInfo);
            }
        } catch (e) { console.error(e); }
    },

    delete: async (id) => {
        if (!confirm("Bạn có chắc chắn muốn xóa nhà cung cấp này không?")) return;
        try {
            await api.delete(`/api/admin/suppliers/${id}`);
            api.showToast("Đã xóa bản ghi nhà cung cấp thành công");
            suppliers.loadList();
        } catch (e) { api.showToast("Không thể xóa nhà cung cấp", "error"); }
    }
};
