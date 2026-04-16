/**
 * orders.js - Payment & Order Management
 * Standardized for Full English Backend synchronization
 */

const orders = {
    // 1. Checkout Process
    initCheckout: async () => {
        const user = api.getUser();
        if (user) {
            $("#customer-name").val(user.fullName || user.username);
            $("#customer-email").val(user.email);
            $("#customer-phone").val(user.phone);
            $("#customer-address").val(user.address);
        }
        orders.renderSummary();
    },

    // 2. VNPay QR Payment logic
    qrPollingInterval: null,
    qrCountdownInterval: null,

    showVNPAYQR: async (paymentUrl, orderId, totalAmount) => {
        // Clear previous QR if any
        const qrContainer = document.getElementById("vnpay-qrcode");
        if (!qrContainer) return;
        qrContainer.innerHTML = "";

        // Generate QR Code
        new QRCode(qrContainer, {
            text: paymentUrl,
            width: 250,
            height: 250,
            colorDark: "#2F2F2F",
            colorLight: "#ffffff",
            correctLevel: QRCode.CorrectLevel.H
        });

        // Set UI info
        $("#qr-order-id").text(`#${orderId}`);
        $("#qr-total-amount").text(api.formatCurrency(totalAmount));
        $("#vnpay-direct-link").attr("href", paymentUrl);
        $("#payment-timer").text("05:00");

        // Show Modal
        const modalElement = document.getElementById('vnpayQRModal');
        const modal = new bootstrap.Modal(modalElement);
        modal.show();

        // Timer Logic (5 minutes)
        let timeLeft = 300;
        if (orders.qrCountdownInterval) clearInterval(orders.qrCountdownInterval);
        orders.qrCountdownInterval = setInterval(() => {
            timeLeft--;
            const mins = Math.floor(timeLeft / 60);
            const secs = timeLeft % 60;
            $("#payment-timer").text(`${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`);

            if (timeLeft <= 0) {
                clearInterval(orders.qrCountdownInterval);
                clearInterval(orders.qrPollingInterval);
                const inst = bootstrap.Modal.getInstance(modalElement);
                if (inst) inst.hide();
                api.showToast("Mã thanh toán đã hết hạn. Vui lòng thử lại.", "error");
            }
        }, 1000);

        // Start Polling
        if (orders.qrPollingInterval) clearInterval(orders.qrPollingInterval);

        orders.qrPollingInterval = setInterval(async () => {
            try {
                const res = await api.get(`/orders/${orderId}`);
                const order = res.data;
                // If status is no longer NEW (e.g., CONFIRMED), payment was processed
                if (order && order.status !== 'NEW') {
                    clearInterval(orders.qrPollingInterval);
                    clearInterval(orders.qrCountdownInterval);
                    const modalInstance = bootstrap.Modal.getInstance(modalElement);
                    if (modalInstance) modalInstance.hide();

                    if (order.status === 'CONFIRMED') {
                        api.showToast("Thanh toán thành công!", "success");
                        layout.render('Orders', 'PaymentResult', orderId);
                    } else if (order.status === 'CANCELLED') {
                        api.showToast("Giao dịch đã bị hủy hoặc thất bại", "error");
                    }
                }
            } catch (e) {
                console.error("Polling error:", e);
            }
        }, 3000);

        // Handle modal manual close
        modalElement.addEventListener('hidden.bs.modal', function handler() {
            clearInterval(orders.qrPollingInterval);
            clearInterval(orders.qrCountdownInterval);
            modalElement.removeEventListener('hidden.bs.modal', handler);
        });
    },

    cancelQR: () => {
        clearInterval(orders.qrPollingInterval);
        clearInterval(orders.qrCountdownInterval);
        const modalElement = document.getElementById('vnpayQRModal');
        const modal = bootstrap.Modal.getInstance(modalElement);
        if (modal) modal.hide();
        api.showToast("Đã hủy thanh toán QR", "info");
    },

    renderSummary: async () => {
        const user = api.getUser();
        let cartData = [];
        if (user) {
             const res = await api.get('/cart');
             cartData = api.parseResponse(res) || [];
            const res = await api.get(`/cart/${user.username}`);
            cartData = res.data || [];
        }

        const container = $("#checkout-summary");
        if (!container.length) return;
        container.empty();

        let total = 0;
        cartData.forEach(item => {
            const itemTotal = item.totalPrice || (item.price * item.quantity);
            total += parseFloat(itemTotal);
            container.append(`
                <div class="d-flex justify-content-between mb-2">
                    <span class="text-muted">${item.title} x${item.quantity}</span>
                    <span class="fw-bold">${api.formatCurrency(itemTotal)}</span>
                </div>
            `);
        });

        const shipping = total >= 200000 ? 0 : 30000;
        const discount = parseFloat(sessionStorage.getItem('applied_discount') || 0);

        $("#summary-subtotal").text(api.formatCurrency(total));
        $("#summary-shipping").text(shipping === 0 ? "Miễn phí" : api.formatCurrency(shipping));
        $("#summary-discount").text(discount > 0 ? "-" + api.formatCurrency(discount) : "0đ");
        $("#summary-total").text(api.formatCurrency(total + shipping - discount));
    },

    applyVoucher: async () => {
        const voucherCode = $("#voucher-input").val().trim();
        if (!voucherCode) {
            $("#voucher-message").text("Vui lòng nhập mã giảm giá").addClass("text-danger").show();
            return;
        }

        try {
            const res = await api.get(`/vouchers/${voucherCode}`);
            if (res.status === 200) {
                const voucher = res.data;
                sessionStorage.setItem('applied_voucher', voucher.voucherCode);
                sessionStorage.setItem('applied_discount', voucher.discountValue);

                const msgDiv = $("#voucher-message");
                msgDiv.html(`<i class="icon icon-check text-success me-2"></i><span class="text-success">Đã áp dụng mã! Giảm: ${api.formatCurrency(voucher.discountValue)}</span>`)
                    .removeClass("text-danger").addClass("text-success").show();

                orders.renderSummary();
            }
        } catch (e) {
            const msgDiv = $("#voucher-message");
            msgDiv.text("Mã giảm giá không hợp lệ hoặc đã hết hạn").addClass("text-danger").show();
        }
    },

    processCheckout: async () => {
        const form = $("#checkout-form");
        if (!form[0].checkValidity()) {
            form[0].reportValidity();
            return;
        }

        const user = api.getUser();
        if (!user) return;
        
        const cartRes = await api.get('/cart');
        const cartData = api.parseResponse(cartRes) || [];
        

        const cartRes = await api.get(`/cart/${user.username}`);
        const cartData = cartRes.data || [];

        if (cartData.length === 0) {
            api.showToast("Giỏ hàng của bạn đang trống!", "error");
            return;
        }

        const paymentMethod = $("input[name='paymentMethod']:checked").val();
        const orderData = {
            username: user.username,
            voucherCode: sessionStorage.getItem('applied_voucher') || "",
            shippingAddress: $("#customer-address").val(),
            paymentMethod: paymentMethod
        };

        api.showToast("Đang tạo đơn hàng...", "info");
        try {
            const orderInfo = api.parseResponse(res);
            const orderId = orderInfo.orderId;

            // Cleanup session
            sessionStorage.removeItem('applied_voucher');
            sessionStorage.removeItem('applied_discount');

            if (paymentMethod === 'VNPAY') {
                const vnpayRes = await api.get(`/payments/vnpay/create?orderId=${orderId}`);
                // Redirect directly to VNPay site
                window.location.href = vnpayRes.data;
            } else {
                api.showToast("Đặt hàng thành công!");
                layout.render('Orders', 'PaymentResult', orderId);
            }
        } catch (error) {
            api.showToast("Không thể tạo đơn hàng: " + error.message, "error");
        }
    },

    loadHistory: async () => {
        const user = api.getUser();
        const tbody = $("#order-history-body");
        if (!tbody.length) return;

        if (!user) {
            tbody.html('<tr><td colspan="5" class="text-center py-5 text-muted">Vui lòng đăng nhập để xem lịch sử đơn hàng.</td></tr>');
            return;
        }

        // Show loading spinner
        tbody.html(`
            <tr>
                <td colspan="5" class="text-center py-5">
                    <div class="spinner-border text-secondary spinner-border-sm" role="status"></div>
                    <span class="ms-2 text-muted small">Đang tải lịch sử đơn hàng...</span>
                </td>
            </tr>
        `);

        try {
            const res = await api.get('/orders/history');
            const list = api.parseResponse(res) || [];
            tbody.empty();

            if (list.length === 0) {
                tbody.html('<tr><td colspan="5" class="text-center py-5 text-muted">Bạn chưa có đơn hàng nào.</td></tr>');
                return;
            }

            list.forEach(order => {
                tbody.append(`
                    <tr>
                        <td class="ps-4 fw-bold">#${order.orderId}</td>
                        <td>${common.formatDate(order.createdAt)}</td>
                        <td class="fw-bold">${api.formatCurrency(order.totalAmount)}</td>
                        <td><span class="badge ${orders.getStatusClass(order.status)}">${order.status}</span></td>
                        <td class="text-end pe-4">
                            <button onclick="layout.render('Orders', 'Details', '${order.orderId}')" class="btn btn-sm btn-outline-dark rounded-pill px-3">Xem chi tiết</button>
                        </td>
                    </tr>
                `);
            });
        } catch (e) {
            tbody.html('<tr><td colspan="5" class="text-center py-5 text-danger">Lỗi khi tải lịch sử đơn hàng.</td></tr>');
            api.showToast("Lỗi khi tải lịch sử đơn hàng", "error");
        }
    },

    loadDetail: async (orderId) => {
        // Show loading in items table
        const tbody = $("#invoice-items");
        if (tbody.length) {
            tbody.html(`<tr><td colspan="4" class="text-center py-4">
                <div class="spinner-border spinner-border-sm text-secondary" role="status"></div>
                <span class="ms-2 text-muted small">Đang tải chi tiết đơn hàng...</span>
            </td></tr>`);
        }

        try {
            const res = await api.get(`/orders/${orderId}`);
            const o = api.parseResponse(res);

            // Header info
            $("#invoice-id").text(`#${o.orderId}`);
            $("#invoice-date").text(common.formatDate(o.createdAt, true));
            $("#invoice-status")
                .text(o.status)
                .attr("class", `badge ${orders.getStatusClass(o.status)}`);
            $("#invoice-payment").text(o.paymentMethod || "---");

            // Customer info
            $("#invoice-customer-name").text(o.customerName || o.username || "---");
            $("#invoice-customer-phone").text(o.customerPhone || o.phone || "---");
            $("#invoice-customer-address").text(o.shippingAddress || "---");

            // Items table
            tbody.empty();
            const details = o.orderDetails || o.items || [];
            let subtotal = 0;

            details.forEach(item => {
                const unitPrice = item.finalPrice || item.unitPrice || item.price || 0;
                const lineTotal = unitPrice * item.quantity;
                subtotal += lineTotal;
                tbody.append(`
                    <tr>
                        <td class="ps-4">
                            <strong>${item.title || item.bookTitle || "---"}</strong>
                            ${item.isbn ? `<br><small class="text-muted">ISBN: ${item.isbn}</small>` : ""}
                        </td>
                        <td class="text-center">${item.quantity}</td>
                        <td class="text-end">${api.formatCurrency(unitPrice)}</td>
                        <td class="text-end pe-4 fw-bold">${api.formatCurrency(lineTotal)}</td>
                    </tr>
                `);
            });

            if (details.length === 0) {
                tbody.html('<tr><td colspan="4" class="text-center py-4 text-muted">Không có sản phẩm nào.</td></tr>');
            }

            // Totals
            const total = o.totalAmount || subtotal;
            const shipping = total >= 200000 ? 0 : 30000;
            $("#invoice-subtotal").text(api.formatCurrency(subtotal));
            $("#invoice-shipping").text(shipping === 0 ? "Miễn phí" : api.formatCurrency(shipping));
            $("#invoice-total").text(api.formatCurrency(total));

        } catch (e) {
            if (tbody.length) tbody.html('<tr><td colspan="4" class="text-center py-4 text-danger">Lỗi khi tải chi tiết đơn hàng.</td></tr>');
            api.showToast("Lỗi khi tải chi tiết đơn hàng", "error");
        }
    },

    loadAdminOrders: async () => {
        try {
            const res = await api.get('/orders/admin');
            const list = api.parseResponse(res) || [];
            const tbody = $("#admin-orders-list");
            if (!tbody.length) return;
            tbody.empty();

            list.forEach(o => {
                tbody.append(`
                    <tr>
                        <td class="ps-4 fw-bold">#${o.orderId}</td>
                        <td>${o.customerName}</td>
                        <td>${common.formatDate(o.createdAt)}</td>
                        <td class="fw-bold">${api.formatCurrency(o.totalAmount)}</td>
                        <td><span class="badge ${orders.getStatusClass(o.status)}">${o.status}</span></td>
                        <td class="text-end pe-4">
                            <select class="form-select form-select-sm d-inline-block w-auto me-2" onchange="orders.updateStatus('${o.orderId}', this.value)">
                                <option value="NEW" ${o.status === 'NEW' ? 'selected' : ''}>NEW</option>
                                <option value="CONFIRMED" ${o.status === 'CONFIRMED' ? 'selected' : ''}>CONFIRMED</option>
                                <option value="SHIPPING" ${o.status === 'SHIPPING' ? 'selected' : ''}>SHIPPING</option>
                                <option value="COMPLETED" ${o.status === 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                                <option value="CANCELLED" ${o.status === 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                            </select>
                        </td>
                    </tr>
                `);
            });
        } catch (e) {
            api.showToast("Lỗi khi tải danh sách đơn hàng", "error");
        }
    },

    updateStatus: async (orderId, status) => {
        try {
            await api.put(`/orders/admin/${orderId}/status?status=${status}`);
            api.showToast("Đã cập nhật trạng thái đơn hàng");
            orders.loadAdminOrders();
        } catch (e) {
            api.showToast("Cập nhật trạng thái thất bại", "error");
        }
    },

    getStatusClass: (status) => {
        switch (status) {
            case 'NEW': return 'bg-warning text-dark';
            case 'CONFIRMED': return 'bg-info text-white';
            case 'SHIPPING': return 'bg-primary text-white';
            case 'COMPLETED': return 'bg-success text-white';
            case 'CANCELLED': return 'bg-danger text-white';
            default: return 'bg-secondary text-white';
        }
    },

    trackOrder: async (orderId) => {
        if (!orderId || !orderId.trim()) {
            api.showToast('Vui lòng nhập mã đơn hàng', 'warning');
            return;
        }
        orderId = orderId.trim();
        const resultContainer = $('#tracking-result-container');
        resultContainer.hide();

        try {
            const res = await api.get(`/orders/${orderId}`);
            const o = api.parseResponse(res);
            if (!o) {
                api.showToast('Không tìm thấy đơn hàng #' + orderId, 'error');
                return;
            }

            $('#tracking-display-id').text('#' + o.orderId);
            $('#tracking-carrier').text('Giao Hàng Nhanh (GHN)');
            $('#tracking-status-badge').text(o.status).attr('class', `badge ${orders.getStatusClass(o.status)} px-3 py-2 rounded-pill fw-bold`);

            // Build timeline
            const timeline = $('#tracking-timeline');
            timeline.empty();
            const steps = [
                { status: 'NEW', label: 'Đơn hàng mới', icon: '📋', date: o.createdAt },
                { status: 'CONFIRMED', label: 'Xác nhận đơn hàng', icon: '✅', date: o.confirmedAt || null },
                { status: 'SHIPPING', label: 'Đang giao hàng', icon: '🚚', date: o.shippedAt || null },
                { status: 'COMPLETED', label: 'Giao hàng thành công', icon: '🎉', date: o.completedAt || null },
            ];

            const statusOrder = ['NEW', 'CONFIRMED', 'SHIPPING', 'COMPLETED'];
            const currentIdx = statusOrder.indexOf(o.status);

            steps.forEach((step, idx) => {
                const isActive = idx <= currentIdx;
                timeline.append(`
                    <div class="tracking-step ${isActive ? 'active' : ''} mb-4">
                        <div class="tracking-step-dot"></div>
                        <div class="ms-3">
                            <div class="fw-bold ${isActive ? '' : 'text-muted'}">${step.icon} ${step.label}</div>
                            <div class="small text-muted">${step.date ? common.formatDate(step.date, true) : (isActive ? 'Hoạt động' : 'Chưa thực hiện')}</div>
                        </div>
                    </div>
                `);
            });

            resultContainer.fadeIn();
        } catch (e) {
            api.showToast('Không tìm thấy đơn hàng #' + orderId + '. Kiểm tra lại mã.', 'error');
        }
    },

    exportAdminOrders: async (format = 'json') => {
        api.showToast('Đang xuất dữ liệu...', 'info');
        try {
            const res = await api.get('/orders/admin');
            const list = api.parseResponse(res) || [];

            const data = list.map(o => ({
                id: o.orderId,
                customer: o.customerName,
                date: o.createdAt,
                total: o.totalAmount,
                status: o.status,
                payment: o.paymentMethod || 'N/A'
            }));

            const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `orders_export_${new Date().toISOString().slice(0, 10)}.json`;
            a.click();
            URL.revokeObjectURL(url);
            api.showToast(`Xuất ${list.length} đơn hàng thành công!`, 'success');
        } catch (e) {
            api.showToast('Lỗi khi xuất dữ liệu: ' + e.message, 'error');
        }
    }
};

