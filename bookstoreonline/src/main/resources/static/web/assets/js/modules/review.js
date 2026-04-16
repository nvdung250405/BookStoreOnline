/**
 * review.js - Product Reviews & Feedback Logic
 */
const review = {
    // Client: Load reviews for a specific book
    loadForBook: async (isbn) => {
        try {
            const res = await api.get(`/reviews/book/${isbn}`);
            if (res.status === 200) {
                review.renderSection(res.data);
            }
        } catch (e) { console.error("Reviews load failed", e); }
    },

    renderSection: (data) => {
        const container = $("#reviews-container");
        if (!container.length) return;
        
        container.empty();
        if (!data || data.length === 0) {
            container.html('<p class="text-muted italic">Chưa có đánh giá nào cho sản phẩm này.</p>');
            return;
        }

        data.forEach(r => {
            const stars = '★'.repeat(r.diemDg || r.rating || 0) + '☆'.repeat(5 - (r.diemDg || r.rating || 0));
            container.append(`
                <div class="review-item mb-4 pb-4 border-bottom">
                    <div class="d-flex justify-content-between mb-2">
                        <span class="fw-bold text-dark">${r.tenKhachHang || r.username || 'Khách hàng'}</span>
                        <span class="text-warning">${stars}</span>
                    </div>
                    <p class="text-muted mb-1">${r.nhanXet || r.comment}</p>
                    <small class="text-muted-50">${Array.isArray(r.ngayDg) ? new Date(r.ngayDg[0], r.ngayDg[1]-1, r.ngayDg[2]).toLocaleDateString('vi-VN') : new Date(r.ngayDg || r.createdAt).toLocaleDateString('vi-VN')}</small>
                </div>
            `);
        });
    },

    // Client: Submit new review
    submit: async (isbn) => {
        const user = api.getUser();
        if (!user) {
            api.showToast("Cần đăng nhập để đánh giá", "warning");
            return;
        }

        const diem = $("#review-rating").val();
        const noidung = $("#review-comment").val();

        if (!noidung) {
            api.showToast("Vui lòng nhập nội dung đánh giá", "warning");
            return;
        }

        try {
            const res = await api.post(`/reviews/submit?username=${user.username}&isbn=${isbn}&diem=${diem}&nhanXet=${encodeURIComponent(noidung)}`);

            if (res.status === 200) {
                api.showToast("Cảm ơn đánh giá của bạn!");
                $("#review-comment").val('');
                review.loadForBook(isbn);
            }
        } catch (e) {
            api.showToast("Không thể gửi đánh giá lúc này", "error");
        }
    },

    // Admin: Load all reviews for moderation
    loadAdminList: async () => {
        try {
            const res = await api.get('/reviews');
            if (res.status === 200) {
                review.renderAdminTable(res.data);
            }
        } catch (e) { api.showToast("Lỗi tải danh sách đánh giá", "error"); }
    },

    renderAdminTable: (data) => {
        const tbody = $("#reviews-admin-list");
        if (!tbody.length) return;
        tbody.empty();

        data.forEach(r => {
            tbody.append(`
                <tr>
                    <td class="ps-4">
                        <div class="fw-bold">${r.tenKhachHang || r.username || 'Khách hàng'}</div>
                        <div class="small text-muted">ISBN: ${r.isbn || '---'}</div>
                    </td>
                    <td><span class="text-warning">${'★'.repeat(r.diemDg || r.rating || 0)}</span></td>
                    <td style="max-width: 300px;" class="text-truncate">${r.nhanXet || r.comment}</td>
                    <td>${Array.isArray(r.ngayDg) ? new Date(r.ngayDg[0], r.ngayDg[1]-1, r.ngayDg[2]).toLocaleDateString('vi-VN') : new Date(r.ngayDg || r.createdAt).toLocaleDateString('vi-VN')}</td>
                    <td class="text-end pe-4">
                        <button onclick="review.delete(${r.maDg || r.id})" class="btn btn-sm btn-outline-danger border-0">Xóa</button>
                    </td>
                </tr>
            `);
        });
    },

    delete: async (id) => {
        if (!confirm("Xóa đánh giá này?")) return;
        try {
            const res = await api.delete(`/reviews/${id}`);
            if (res.status === 200) {
                api.showToast("Đã xóa đánh giá thành công");
                review.loadAdminList();
            }
        } catch (e) { api.showToast("Lỗi khi xóa: " + e.message, "error"); }
    }
};

$(document).on('click', '#btn-submit-review', function() {
    const isbn = $('#book-isbn').text();
    if (isbn && isbn !== '---') {
        review.submit(isbn);
    } else {
        api.showToast("Lỗi: Không xác định được mã sách", "error");
    }
});
