/**
 * suppliers.js - Supplier & Partner Management
 */
const suppliers = {
    loadList: async () => {
        try {
            const res = await api.get('/admin/suppliers');
            const list = res.data || [];
            const tbody = $("#suppliers-list");
            if (!tbody.length) return;
            tbody.empty();

            if (list.length === 0) {
                tbody.html('<tr><td colspan="3" class="text-center py-4 text-muted">Không tìm thấy nhà cung cấp nào.</td></tr>');
                return;
            }

            list.forEach(s => {
                tbody.append(`
                    <tr>
                        <td class="ps-4">
                            <div class="fw-bold text-dark">${s.supplierName}</div>
                            <div class="extra-small text-muted">ID: ${s.supplierId}</div>
                        </td>
                        <td>
                            <div class="small"><i class="icon icon-map-pin me-1 opacity-50"></i>${s.contactInfo || 'Chưa cập nhật'}</div>
                        </td>
                        <td class="text-end pe-4">
                            <div class="d-flex justify-content-end gap-2">
                                <button onclick="layout.render('Suppliers/Admin', 'Details', ${s.supplierId})" class="btn btn-sm btn-light rounded-pill px-3" title="Chi tiết">
                                    <i class="icon icon-eye me-1"></i> Chi tiết
                                </button>
                                <button onclick="layout.render('Suppliers/Admin', 'Edit', ${s.supplierId})" class="btn btn-sm btn-outline-accent rounded-pill px-3" title="Sửa">
                                    <i class="icon icon-edit me-1"></i> Sửa
                                </button>
                                <button onclick="layout.render('Suppliers/Admin', 'Delete', ${s.supplierId})" class="btn btn-sm btn-outline-danger rounded-pill px-3" title="Xóa">
                                    <i class="icon icon-trash me-1"></i> Xóa
                                </button>
                            </div>
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
                await api.put(`/admin/suppliers/${id}`, dto);
                api.showToast("Đã cập nhật nhà cung cấp thành công");
            } else {
                await api.post('/admin/suppliers', dto);
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
            // Usually we'd use GET /api/admin/suppliers/{id}, 
            // but sticking to searching the list if specific endpoint not confirmed
            const res = await api.get(`/admin/suppliers`);
            const list = res.data || [];
            const s = list.find(item => item.supplierId == id);
            
            if (s) {
                // For Edit View
                $("#supplier-id").val(s.supplierId);
                $("#supplier-name").val(s.supplierName);
                $("#supplier-contact").val(s.contactInfo);

                // For Details View
                $("#detail-supplier-id").text(s.supplierId);
                $("#detail-supplier-name").text(s.supplierName);
                $("#disp-supplier-name").text(s.supplierName);
                $("#disp-supplier-contact").text(s.contactInfo || 'Chưa cập nhật');
                $("#btn-edit-redirect").attr("onclick", `layout.render('Suppliers/Admin', 'Edit', ${s.supplierId})`);

                // For Delete View
                $("#del-supplier-id").text(s.supplierId);
                $("#del-supplier-name").text(s.supplierName);
                $("#btn-confirm-delete").attr("onclick", `suppliers.performDelete(${s.supplierId})`);
            }
        } catch (e) { console.error(e); }
    },

    performDelete: async (id) => {
        try {
            await api.delete(`/admin/suppliers/${id}`);
            api.showToast("Đã xóa bản ghi nhà cung cấp thành công");
            layout.render('Suppliers/Admin', 'Index');
        } catch (e) { api.showToast("Không thể xóa nhà cung cấp", "error"); }
    }
};
