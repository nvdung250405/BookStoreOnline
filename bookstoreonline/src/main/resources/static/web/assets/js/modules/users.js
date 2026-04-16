/**
 * users.js - User Management Logic (Person 1)
 * Covers: GET/PUT /api/users/*, POST/GET/PUT /api/admin/users/*
 */
const users = {

    // =========================================================
    // ADMIN: User List Management
    // =========================================================
    loadAdminList: async () => {
        try {
            const res = await api.get('/admin/accounts/get-users');
            const data = Array.isArray(res) ? res : (res.data || []);
            const tbody = $("#users-admin-list");
            if (!tbody.length) return;
            tbody.empty();

            if (data.length === 0) {
                tbody.html('<tr><td colspan="6" class="text-center py-4 text-muted">Không tìm thấy người dùng nào trong hệ thống.</td></tr>');
                return;
            }

            const roleBadge = { ADMIN: 'bg-danger', STAFF: 'bg-primary', STOREKEEPER: 'bg-warning text-dark', CUSTOMER: 'bg-secondary' };

            data.forEach(user => {
                const isActive = user.isActive === true; // Aligned with AccountDTO isActive
                tbody.append(`
                    <tr>
                        <td class="ps-4 fw-bold text-dark">${user.username}</td>
                        <td>${user.fullName || '---'}</td>
                        <td><span class="badge ${roleBadge[user.role] || 'bg-secondary'} rounded-pill px-3">${user.role}</span></td>
                        <td>${user.createdAt ? new Date(user.createdAt).toLocaleDateString('en-GB') : '---'}</td>
                        <td><span class="badge ${isActive ? 'bg-success' : 'bg-danger'} bg-opacity-15 ${isActive ? 'text-success' : 'text-danger'} rounded-pill px-3">${isActive ? 'Hoạt động' : 'Bị khóa'}</span></td>
                        <td class="text-end pe-4 d-flex gap-2 justify-content-end">
                            <select class="form-select form-select-sm rounded-pill" style="width: auto"
                                onchange="users.changeRole('${user.username}', this.value)">
                                ${['ADMIN','STAFF','STOREKEEPER','CUSTOMER'].map(r =>
                                    `<option value="${r}" ${r === user.role ? 'selected' : ''}>${r}</option>`
                                ).join('')}
                            </select>
                            <button class="btn btn-sm btn-outline-${isActive ? 'danger' : 'success'} rounded-pill px-3"
                                onclick="users.toggleStatus('${user.username}', ${isActive})">
                                ${isActive ? 'Khóa' : 'Mở khóa'}
                            </button>
                        </td>
                    </tr>
                `);
            });
        } catch (e) {
            api.showToast("Không thể tải danh sách người dùng", "error");
        }
    },

    toggleStatus: async (username, currentActive) => {
        const action = currentActive ? 'KHÓA' : 'MỞ KHÓA';
        if (!confirm(`Bạn có chắc chắn muốn ${action} tài khoản: ${username}?`)) return;
        try {
            // Backend: PUT /admin/accounts/{username}/status?status=true/false
            await api.put(`/admin/accounts/${username}/status?status=${!currentActive}`);
            api.showToast(`Đã ${action} tài khoản thành công!`, "success");
            users.loadAdminList();
        } catch (e) {
            api.showToast("Lỗi khi cập nhật trạng thái tài khoản", "error");
        }
    },

    changeRole: async (username, newRole) => {
        if (!confirm(`Thay đổi vai trò của người dùng ${username} thành ${newRole}?`)) {
            users.loadAdminList(); // reset select
            return;
        }
        try {
            await api.put(`/admin/accounts/${username}/role?role=${newRole}`);
            api.showToast(`Đã cập nhật vai trò thành ${newRole}`, "success");
            users.loadAdminList();
        } catch (e) {
            api.showToast("Lỗi khi cập nhật vai trò người dùng", "error");
            users.loadAdminList();
        }
    },

    adminCreate: async () => {
        const username    = $("#new-username").val().trim();
        const password    = $("#new-password").val().trim();
        const role        = $("#new-role").val();
        const fullName    = $("#new-fullName").val().trim();
        const phone       = $("#new-phone").val().trim();

        if (!username || !password || !role) {
            api.showToast("Vui lòng điền đầy đủ các trường bắt buộc", "warning");
            return;
        }

        try {
            await api.post('/admin/accounts/create', { username, password, role, fullName, phone });
            api.showToast("Đã tạo tài khoản thành công!");
            users.loadAdminList();
            ["#new-username","#new-password","#new-fullName","#new-phone"].forEach(id => $(id).val(''));
        } catch (e) {
            api.showToast("Lỗi khi tạo tài khoản: " + e.message, "error");
        }
    },

    // =========================================================
    // CLIENT: Profile Management
    // =========================================================
    loadProfile: async () => {
        const user = api.getUser();
        if (!user) { layout.render('Auth', 'Login'); return; }

        try {
            const res = await api.get('/users/profile');
            const p = res.data || res;

            $("#profile-username").val(p.username || user.username);
            $("#profile-fullName").val(p.fullName || '');
            $("#profile-phone").val(p.phoneNumber || p.phone || '');
            $("#profile-address").val(p.address || '');
            $("#profile-email").val(p.email || '');
            $("#profile-role").val(p.role || user.role || '');
        } catch (e) {
            api.showToast("Không thể tải thông tin hồ sơ", "error");
        }
    },

    updateProfile: async () => {
        const user = api.getUser();
        if (!user) return;

        const isCustomer = user.role === 'CUSTOMER';
        const endpoint   = isCustomer ? '/users/profile/update-customer' : '/users/profile/update-staff';

        const data = {
            fullName:    $("#profile-fullName").val().trim(),
            phone:       $("#profile-phone").val().trim(),
            address:     isCustomer ? $("#profile-address").val().trim() : undefined,
        };

        try {
            await api.put(endpoint, data);
            api.showToast("Hồ sơ đã được cập nhật thành công!");
            // Update local storage if needed
            const current = api.getUser();
            if (current) {
                current.fullName = data.fullName;
                localStorage.setItem('user', JSON.stringify(current));
                layout.updateUserHeader();
            }
        } catch (e) {
            api.showToast("Lỗi khi cập nhật hồ sơ", "error");
        }
    },

    changePassword: async () => {
        const oldPass  = $("#pw-old").val().trim();
        const newPass  = $("#pw-new").val().trim();
        const confirm2 = $("#pw-confirm").val().trim();

        if (!oldPass || !newPass) {
            api.showToast("Vui lòng nhập đầy đủ các trường mật khẩu", "warning");
            return;
        }
        if (newPass !== confirm2) {
            api.showToast("Mật khẩu xác nhận không khớp", "error");
            return;
        }
        if (newPass.length < 6) {
            api.showToast("Mật khẩu mới phải có ít nhất 6 ký tự", "warning");
            return;
        }

        try {
            await api.put('/users/profile/change-password', { oldPassword: oldPass, newPassword: newPass });
            api.showToast("Đã thay đổi mật khẩu thành công!");
            ["#pw-old","#pw-new","#pw-confirm"].forEach(id => $(id).val(''));
        } catch (e) {
            api.showToast("Đổi mật khẩu thất bại: " + e.message, "error");
        }
    }
};
