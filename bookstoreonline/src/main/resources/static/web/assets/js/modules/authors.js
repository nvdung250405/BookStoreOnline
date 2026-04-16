/**
 * authors.js - Author Management Logic
 */
const authors = {
    _adminList: [],

    loadAdminList: async () => {
        try {
            const res = await api.get('/authors');
            authors._adminList = Array.isArray(res) ? res : (res.data || []);
            authors.renderAdminTable();
        } catch (e) {
            console.error("Authors fetch error", e);
            api.showToast("Không thể tải danh sách tác giả", "error");
        }
    },

    renderAdminTable: () => {
        const tbody = $("#authors-admin-list");
        if (!tbody.length) return;
        tbody.empty();

        if (authors._adminList.length === 0) {
            tbody.html('<tr><td colspan="3" class="text-center py-4">Không tìm thấy tác giả nào.</td></tr>');
            return;
        }

        authors._adminList.forEach(a => {
            tbody.append(`
                <tr>
                    <td class="ps-4 fw-bold text-accent">#${a.authorId}</td>
                    <td>${a.authorName}</td>
                    <td class="text-end pe-4">
                        <button onclick="authors.openEdit(${a.authorId})" class="btn btn-sm btn-outline-primary rounded-pill px-3 me-2">Sửa</button>
                        <button onclick="authors.delete(${a.authorId})" class="btn btn-sm btn-outline-danger rounded-pill px-3">Xóa</button>
                    </td>
                </tr>
            `);
        });
    },

    create: async () => {
        const name = $("#author-name").val().trim();
        const bio = $("#author-bio").val().trim();
        if (!name) { api.showToast("Vui lòng nhập tên tác giả", "warning"); return; }
        try {
            await api.post('/authors', { authorName: name, biography: bio });
            api.showToast("Đã tạo tác giả thành công!");
            layout.render('Authors/Admin', 'Index');
        } catch (e) {
            api.showToast("Tạo thất bại: " + e.message, "error");
        }
    },

    openEdit: async (id) => {
        const author = authors._adminList.find(a => a.authorId === id);
        if (!author) return;
        layout.render('Authors/Admin', 'Edit', { id: id });
        // After render, fill values (requires small delay or callback)
        setTimeout(() => {
            $("#edit-author-id").val(author.authorId);
            $("#edit-author-name").val(author.authorName);
            $("#edit-author-bio").val(author.biography);
        }, 100);
    },

    update: async () => {
        const id = $("#edit-author-id").val();
        const name = $("#edit-author-name").val().trim();
        const bio = $("#edit-author-bio").val().trim();
        if (!name) { api.showToast("Vui lòng nhập tên tác giả", "warning"); return; }
        try {
            await api.put(`/authors/${id}`, { authorName: name, biography: bio });
            api.showToast("Đã cập nhật tác giả thành công!");
            layout.render('Authors/Admin', 'Index');
        } catch (e) {
            api.showToast("Cập nhật thất bại: " + e.message, "error");
        }
    },

    delete: async (id) => {
        if (!confirm("Bạn có chắc chắn muốn xóa tác giả này không?")) return;
        try {
            await api.delete(`/authors/${id}`);
            api.showToast("Đã xóa tác giả thành công!");
            authors.loadAdminList();
        } catch (e) {
            api.showToast("Xóa thất bại: " + e.message, "error");
        }
    }
};
