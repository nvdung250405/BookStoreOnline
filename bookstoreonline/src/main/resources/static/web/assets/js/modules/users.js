/**
 * users.js - User Management Logic (Person 1)
 * Covers: GET/PUT /api/users/*, POST/GET/PUT /api/admin/users/*
 */
/**
 * users.js - User Management Logic
 */
const users = {
    // === CÁC BIẾN QUẢN LÝ PHÂN TRANG & TÌM KIẾM ===
    _allUsers: [],        // Mảng gốc chứa toàn bộ user từ API
    _filteredUsers: [],   // Mảng sau khi đã áp dụng tìm kiếm/bộ lọc
    _currentPage: 1,      // Trang hiện tại
    _itemsPerPage: 5,     // Số người trên 1 trang

    // Helpers Modal
    _getRoleModal: () => bootstrap.Modal.getOrCreateInstance(document.getElementById('role-modal')),
    _getStatusModal: () => bootstrap.Modal.getOrCreateInstance(document.getElementById('status-modal')),
    _getDetailsModal: () => bootstrap.Modal.getOrCreateInstance(document.getElementById('user-details-modal')),
    _getResultModal: () => bootstrap.Modal.getOrCreateInstance(document.getElementById('creation-result-modal')),

    // =========================================================
    // ADMIN: User List Management
    // =========================================================
    
    // 1. Tải toàn bộ dữ liệu từ API (Chỉ gọi 1 lần lúc mới vào trang)
    loadAdminList: async () => {
        const tbody = $("#users-admin-list");
        if (tbody.length) {
            tbody.html('<tr><td colspan="6" class="text-center py-5"><div class="spinner-border text-accent spinner-border-sm"></div><span class="ms-2 text-muted">Đang tải dữ liệu...</span></td></tr>');
        }

        try {
            const res = await api.get('/admin/accounts/get-users');
            users._allUsers = Array.isArray(res) ? res : (res.data || []);
            
            // Xóa rỗng các ô input tìm kiếm lúc mới load
            $("#user-search").val('');
            $("#user-role-filter").val('');
            
            users.applyFiltersAndRender();
        } catch (e) {
            api.showToast("Không thể tải danh sách người dùng", "error");
        }
    },

    // 2. Lọc dữ liệu dựa trên Input Tìm kiếm & Phân quyền
    applyFiltersAndRender: () => {
        const searchKeyword = $("#user-search").val().toLowerCase().trim();
        const roleFilter = $("#user-role-filter").val();

        users._filteredUsers = users._allUsers.filter(user => {
            const matchSearch = !searchKeyword || 
                                (user.username && user.username.toLowerCase().includes(searchKeyword)) ||
                                (user.fullName && user.fullName.toLowerCase().includes(searchKeyword)) ||
                                (user.email && user.email.toLowerCase().includes(searchKeyword));
            const matchRole = !roleFilter || user.role === roleFilter;
            
            return matchSearch && matchRole;
        });

        // Tự động quay về trang 1 nếu kết quả lọc làm tổng số trang bị giảm
        const totalPages = Math.ceil(users._filteredUsers.length / users._itemsPerPage);
        if (users._currentPage > totalPages && totalPages > 0) {
            users._currentPage = totalPages;
        } else if (totalPages === 0) {
            users._currentPage = 1;
        }

        users.renderTable();
        users.renderPagination();
    },

    // 3. Render danh sách (Đã tính toán cắt mảng 5 người/trang)
    renderTable: () => {
        const tbody = $("#users-admin-list");
        if (!tbody.length) return;
        tbody.empty();

        if (users._filteredUsers.length === 0) {
            tbody.html('<tr><td colspan="6" class="text-center py-5 text-muted">Không tìm thấy thành viên nào phù hợp với bộ lọc.</td></tr>');
            return;
        }

        // Cắt mảng (Ví dụ: Trang 1 lấy từ 0-4, Trang 2 lấy từ 5-9)
        const startIndex = (users._currentPage - 1) * users._itemsPerPage;
        const paginatedData = users._filteredUsers.slice(startIndex, startIndex + users._itemsPerPage);

        const roleBadge = { ADMIN: 'bg-danger', STAFF: 'bg-primary', STOREKEEPER: 'bg-warning text-dark', CUSTOMER: 'bg-secondary' };

        paginatedData.forEach(user => {
            const isActive = user.isActive === true;
            tbody.append(`
                <tr>
                    <td class="ps-4 fw-bold text-dark">${user.username}</td>
                    <td>${user.fullName || '---'}</td>
                    <td><span class="badge ${roleBadge[user.role] || 'bg-secondary'} rounded-pill px-3">${user.role}</span></td>
                    <td>${user.createdAt ? new Date(user.createdAt).toLocaleDateString('vi-VN') : '---'}</td>
                    <td>
                        <span class="badge ${isActive ? 'bg-success' : 'bg-danger'} text-white rounded-pill px-3 py-2 shadow-sm" style="letter-spacing: 0.5px; font-size: 0.75rem;">
                            ${isActive ? 'ACTIVE' : 'INACTIVE'}
                        </span>
                    </td>
                    <td class="text-end pe-4">
                        <div class="d-flex gap-2 justify-content-end align-items-center">
                            <button class="btn btn-sm btn-light border rounded-pill px-3 fw-bold"
                                onclick="users.openDetails('${user.username}')">
                                Chi tiết
                            </button>
                            <button class="btn btn-sm btn-outline-${isActive ? 'danger' : 'success'} rounded-pill px-3"
                                onclick="users.openStatusModal('${user.username}', ${isActive})">
                                ${isActive ? 'Khóa' : 'Mở khóa'}
                            </button>
                        </div>
                    </td>
                </tr>
            `);
        });
    },

    // 4. Chuyển trang (Thêm tính năng tự động cuộn lên đầu)
    changePage: (page) => {
        users._currentPage = page;
        users.renderTable();
        users.renderPagination();
        
        // Tự động cuộn trang lên đầu thật mượt mà
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    },

    // 5. Render thanh phân trang
    renderPagination: () => {
        if ($("#users-pagination").length === 0) {
            $(".table-responsive").parent().after('<div id="users-pagination" class="d-flex justify-content-center mt-4 mb-5"></div>');
        }

        const container = $("#users-pagination");
        container.empty();

        const totalPages = Math.ceil(users._filteredUsers.length / users._itemsPerPage);
        if (totalPages <= 1) return;

        // Dùng gap-2 để tách rời các nút, bỏ cái khung dính liền mặc định đi
        let html = `<nav><ul class="pagination mb-0 d-flex gap-2 align-items-center border-0">`;

        // Nút Trái
        html += `
            <li class="page-item ${users._currentPage === 1 ? 'disabled' : ''}">
                <a class="page-link custom-page-btn" href="javascript:void(0)" onclick="users.changePage(${users._currentPage - 1})">Trái</a>
            </li>
        `;

        let startPage = Math.max(1, users._currentPage - 1);
        let endPage = Math.min(totalPages, users._currentPage + 1);

        if (users._currentPage === 1) endPage = Math.min(totalPages, 3);
        if (users._currentPage === totalPages) startPage = Math.max(1, totalPages - 2);

        // Nối trang 1 và dấu ... ở đầu
        if (startPage > 1) {
            html += `<li class="page-item"><a class="page-link custom-page-btn" href="javascript:void(0)" onclick="users.changePage(1)">1</a></li>`;
            if (startPage > 2) {
                html += `<li class="page-item disabled"><span class="page-link custom-page-btn bg-transparent border-0 text-muted shadow-none">...</span></li>`;
            }
        }

        // Vòng lặp in các trang ở giữa
        for (let i = startPage; i <= endPage; i++) {
            const isActive = i === users._currentPage;
            html += `
                <li class="page-item ${isActive ? 'active' : ''}">
                    <a class="page-link custom-page-btn" href="javascript:void(0)" onclick="users.changePage(${i})">${i}</a>
                </li>
            `;
        }

        // Nối dấu ... và trang cuối ở đuôi
        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link custom-page-btn bg-transparent border-0 text-muted shadow-none">...</span></li>`;
            }
            html += `<li class="page-item"><a class="page-link custom-page-btn" href="javascript:void(0)" onclick="users.changePage(${totalPages})">${totalPages}</a></li>`;
        }

        // Nút Phải
        html += `
            <li class="page-item ${users._currentPage === totalPages ? 'disabled' : ''}">
                <a class="page-link custom-page-btn" href="javascript:void(0)" onclick="users.changePage(${users._currentPage + 1})">Phải</a>
            </li>
        `;

        html += `</ul></nav>`;
        container.html(html);
    },

    autoCreate: async () => {
        const role = $("input[name='role-select']:checked").val();
        if (!role) {
            api.showToast("Vui lòng chọn vai trò", "warning");
            return;
        }

        const btn = $("#btn-auto-create");
        const originHTML = btn.html();
        btn.html('<span class="spinner-border spinner-border-sm me-2"></span>Đang tạo...').prop('disabled', true);

        try {
            // Debug: Confirm function call
            console.log("autoCreate called for role:", role);
            
            const res = await api.post(`/admin/accounts/auto-generate?role=${role}`, {});
            console.log("API Success:", res);
            
            const data = res.data || res;
            if (!data || !data.username) {
                throw new Error("Dữ liệu phản hồi rỗng");
            }

            $("#res-username").text(data.username);
            $("#res-password").text(data.password || '123456');

            // Force modal show with multiple methods
            const modalEl = document.getElementById('creation-result-modal');
            if (modalEl) {
                const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
                modal.show();
                // Fallback for some jQuery environments
                if (window.jQuery && typeof $(modalEl).modal === 'function') {
                    $(modalEl).modal('show');
                }
            } else {
                alert(`Tạo thành công!\nUsername: ${data.username}\nPassword: ${data.password || '123456'}`);
            }
        } catch (e) {
            console.error("autoCreate Error:", e);
            alert("Lỗi: " + (e.message || "Không thể kết nối máy chủ"));
            api.showToast("Lỗi: " + e.message, "error");
        } finally {
            btn.html(originHTML).prop('disabled', false);
        }
    },

    // --- LOGIC MODAL CHI TIẾT & CHỈNH SỬA ---
    openDetails: (username) => {
        const user = users._allUsers.find(u => u.username === username);
        if (!user) return;

        $("#edit-username").val(user.username);
        $("#edit-fullName").val(user.fullName || '');
        $("#edit-phone").val(user.phone || '');
        $("#edit-role").val(user.role);
        $("#admin-new-password").val('');

        // Logic hiển thị theo Role
        users._toggleEditFields(user.role);
        if (user.role === 'CUSTOMER') {
            $("#edit-address").val(user.shippingAddress || '');
        } else {
            $("#edit-department").val(user.department || 'SALES');
        }

        const statusBadge = `<span class="badge ${user.isActive ? 'bg-success' : 'bg-danger'} rounded-pill px-3">${user.isActive ? 'Đang hoạt động' : 'Đã bị khóa'}</span>`;
        $("#edit-status-display").html(statusBadge);

        users._getDetailsModal().show();
    },

    _toggleEditFields: (role) => {
        if (role === 'CUSTOMER') {
            $("#edit-address-container").show();
            $("#edit-dept-container").hide();
        } else {
            $("#edit-address-container").hide();
            $("#edit-dept-container").show();
        }
    },

    onRoleChangeInModal: () => {
        users._toggleEditFields($("#edit-role").val());
    },

    saveAdminEdit: async () => {
        const username = $("#edit-username").val();
        const role = $("#edit-role").val();
        const data = {
            username: username,
            role: role,
            fullName: $("#edit-fullName").val().trim(),
            phone: $("#edit-phone").val().trim(),
        };

        if (role === 'CUSTOMER') {
            data.shippingAddress = $("#edit-address").val().trim();
        } else {
            data.department = $("#edit-department").val();
        }

        try {
            await api.put(`/admin/accounts/${username}/profile`, data);
            api.showToast("Cập nhật hồ sơ thành công", "success");
            users._getDetailsModal().hide();
            users.loadAdminList();
        } catch (e) {
            api.showToast("Lỗi khi cập nhật: " + e.message, "error");
        }
    },

    adminResetPassword: async () => {
        const username = $("#edit-username").val();
        const newPass = $("#admin-new-password").val().trim();
        if (!newPass) return api.showToast("Vui lòng nhập mật khẩu mới", "warning");

        try {
            await api.put(`/admin/accounts/${username}/reset-password?newPassword=${newPass}`);
            api.showToast("Đã đặt lại mật khẩu thành công", "success");
            $("#admin-new-password").val('');
        } catch (e) {
            api.showToast("Lỗi: " + e.message, "error");
        }
    },

    // --- LOGIC MODAL ĐỔI QUYỀN (Old - redundant but keeping for safety) ---
    openRoleModal: (username, currentRole) => {
        $("#role-username").text(username);
        $("#role-select-modal").val(currentRole);
        $("#btn-confirm-role").attr('onclick', `users.changeRole('${username}')`);
        users._getRoleModal().show();
    },

    changeRole: async (username) => {
        const newRole = $("#role-select-modal").val();
        const btn = $("#btn-confirm-role");
        const originText = btn.text();
        
        btn.html('<span class="spinner-border spinner-border-sm me-1"></span>Đang lưu...').prop('disabled', true);
        
        try {
            await api.put(`/admin/accounts/${username}/role?role=${newRole}`);
            api.showToast(`Đã cập nhật vai trò thành ${newRole}`, "success");
            users._getRoleModal().hide();
            users.loadAdminList();
        } catch (e) {
            api.showToast("Lỗi khi cập nhật vai trò người dùng", "error");
        } finally {
            btn.html(originText).prop('disabled', false);
        }
    },

    // --- LOGIC MODAL KHÓA/MỞ KHÓA ---
    openStatusModal: (username, currentActive) => {
        const action = currentActive ? 'KHÓA' : 'MỞ KHÓA';
        const colorClass = currentActive ? 'danger' : 'success';
        
        // Setup giao diện Modal tùy theo trạng thái
        $("#status-icon-container").html(`<i class="icon icon-lock text-${colorClass}" style="font-size: 3rem;"></i>`);
        $("#status-title").text(`Xác nhận ${action.toLowerCase()}`);
        $("#status-message").html(`Bạn có chắc chắn muốn <strong class="text-${colorClass}">${action}</strong> tài khoản <strong class="text-dark">${username}</strong>?`);

        const btn = $("#btn-confirm-status");
        btn.removeClass('btn-danger btn-success').addClass(`btn-${colorClass}`);
        btn.text(action === 'KHÓA' ? 'Khóa ngay' : 'Mở khóa');
        btn.attr('onclick', `users.toggleStatus('${username}', ${currentActive})`);

        users._getStatusModal().show();
    },

    toggleStatus: async (username, currentActive) => {
        const btn = $("#btn-confirm-status");
        const originText = btn.text();
        
        btn.html('<span class="spinner-border spinner-border-sm me-1"></span>Đang xử lý...').prop('disabled', true);
        
        try {
            await api.put(`/admin/accounts/${username}/status?status=${!currentActive}`);
            api.showToast(`Đã ${currentActive ? 'khóa' : 'mở khóa'} tài khoản thành công!`, "success");
            users._getStatusModal().hide();
            users.loadAdminList();
        } catch (e) {
            api.showToast("Lỗi khi cập nhật trạng thái tài khoản", "error");
        } finally {
            btn.html(originText).prop('disabled', false);
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
            const p = api.parseResponse(res); // Correctly extract profile from ApiResponse

            $("#profile-username").val(p.username || user.username || '');
            $("#profile-fullName").val(p.fullName || '');
            $("#profile-phone").val(p.phone || p.phoneNumber || '');
            $("#profile-address").val(p.shippingAddress || p.address || '');
            $("#profile-role").val(p.role || user.role || '');
        } catch (e) {
            console.error("Load profile error:", e);
            api.showToast("Không thể tải thông tin hồ sơ", "error");
        }
    },

    /**
     * Check if user profile is incomplete to show update reminder
     */
    checkIncompleteProfile: async () => {
        const user = api.getUser();
        if (!user) return;

        try {
            // Fetch fresh data from server to be sure
            const res = await api.get('/users/profile');
            const p = api.parseResponse(res); // Correctly extract profile from ApiResponse

            let isIncomplete = false;
            let reason = "";

            if (user.role === 'CUSTOMER') {
                if (!p.phone || !p.shippingAddress || p.phone.trim() === "" || p.shippingAddress.trim() === "") {
                    isIncomplete = true;
                    reason = "Vui lòng cập nhật Số điện thoại và Địa chỉ để chúng tôi có thể giao hàng cho bạn.";
                }
            } else if (user.role === 'STAFF' || user.role === 'ADMIN' || user.role === 'STOREKEEPER') {
                // If full name still contains "NEW STAFF" or "NEW ADMIN" etc.
                if (!p.fullName || p.fullName.includes("NEW ") || p.fullName.trim() === "") {
                    isIncomplete = true;
                    reason = "Tài khoản của bạn đang sử dụng thông tin mặc định. Hãy cập nhật Họ tên để hoàn thiện hồ sơ.";
                }
            }

            if (isIncomplete) {
                $("#update-reminder-reason").text(reason);
                const modalEl = document.getElementById('update-profile-reminder-modal');
                if (modalEl) {
                    const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
                    modal.show();
                }
            }
        } catch (e) {
            console.error("Check incomplete profile error:", e);
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
            shippingAddress: isCustomer ? $("#profile-address").val().trim() : undefined,
            department:  !isCustomer ? $("#profile-dept").val() : undefined
        };

        try {
            await api.put(endpoint, data);
            api.showToast("Hồ sơ đã được cập nhật thành công!");
            const current = api.getUser();
            if (current) {
                current.fullName = data.fullName;
                current.phone = data.phone;
                if (isCustomer) {
                    current.shippingAddress = data.shippingAddress;
                } else {
                    current.department = data.department;
                }
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
