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

    renderSummary: async () => {
        const user = api.getUser();
        let cartData = [];
        if (user) {
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
        $("#summary-shipping").text(shipping === 0 ? "Free" : api.formatCurrency(shipping));
        $("#summary-discount").text(discount > 0 ? "-" + api.formatCurrency(discount) : "0đ");
        $("#summary-total").text(api.formatCurrency(total + shipping - discount));
    },

    applyVoucher: async () => {
        const voucherCode = $("#voucher-input").val().trim();
        if (!voucherCode) {
            $("#voucher-message").text("Please enter a voucher code").addClass("text-danger").show();
            return;
        }

        try {
            const res = await api.get(`/vouchers/${voucherCode}`);
            if (res.status === 200) {
                const voucher = res.data;
                sessionStorage.setItem('applied_voucher', voucher.voucherCode);
                sessionStorage.setItem('applied_discount', voucher.discountValue);
                
                const msgDiv = $("#voucher-message");
                msgDiv.html(`<i class="icon icon-check text-success me-2"></i><span class="text-success">Voucher applied! Discount: ${api.formatCurrency(voucher.discountValue)}</span>`)
                    .removeClass("text-danger").addClass("text-success").show();
                
                orders.renderSummary(); 
            }
        } catch (e) {
            const msgDiv = $("#voucher-message");
            msgDiv.text("Invalid or expired voucher code").addClass("text-danger").show();
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
        
        const cartRes = await api.get(`/cart/${user.username}`);
        const cartData = cartRes.data || [];
        
        if (cartData.length === 0) {
            api.showToast("Your cart is empty!", "error");
            return;
        }

        const paymentMethod = $("input[name='paymentMethod']:checked").val();
        const orderData = {
            username: user.username,
            voucherCode: sessionStorage.getItem('applied_voucher') || "",
            shippingAddress: $("#customer-address").val(),
            paymentMethod: paymentMethod
        };

        api.showToast("Creating order...", "info");
        try {
            const res = await api.post('/orders/checkout', orderData);
            const orderInfo = res.data;
            const orderId = orderInfo.orderId;

            // Cleanup session
            sessionStorage.removeItem('applied_voucher');
            sessionStorage.removeItem('applied_discount');

            if (paymentMethod === 'VNPAY') {
                const vnpayRes = await api.get(`/payments/vnpay/create?orderId=${orderId}`);
                window.location.href = vnpayRes.data; 
            } else {
                api.showToast("Order placed successfully!");
                layout.render('Orders', 'PaymentResult', orderId);
            }
        } catch (error) {
            api.showToast("Failed to create order: " + error.message, "error");
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
            const res = await api.get(`/orders/history?username=${user.username}`);
            const list = res.data || [];
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
            api.showToast("Error loading order history", "error");
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
            const o = res.data;

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
                const unitPrice  = item.finalPrice || item.unitPrice || item.price || 0;
                const lineTotal  = unitPrice * item.quantity;
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
            const total    = o.totalAmount || subtotal;
            const shipping = total >= 200000 ? 0 : 30000;
            $("#invoice-subtotal").text(api.formatCurrency(subtotal));
            $("#invoice-shipping").text(shipping === 0 ? "Miễn phí" : api.formatCurrency(shipping));
            $("#invoice-total").text(api.formatCurrency(total));

        } catch (e) {
            if (tbody.length) tbody.html('<tr><td colspan="4" class="text-center py-4 text-danger">Lỗi khi tải chi tiết đơn hàng.</td></tr>');
            api.showToast("Error loading order details", "error");
        }
    },

    loadAdminOrders: async () => {
        try {
            const res = await api.get('/orders/admin');
            const list = res.data || [];
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
            api.showToast("Error loading orders", "error");
        }
    },

    updateStatus: async (orderId, status) => {
        try {
            await api.put(`/orders/admin/${orderId}/status?status=${status}`);
            api.showToast("Order status updated");
            orders.loadAdminOrders();
        } catch (e) {
            api.showToast("Failed to update status", "error");
        }
    },

    getStatusClass: (status) => {
        switch(status) {
            case 'NEW': return 'bg-warning text-dark';
            case 'CONFIRMED': return 'bg-info text-white';
            case 'SHIPPING': return 'bg-primary text-white';
            case 'COMPLETED': return 'bg-success text-white';
            case 'CANCELLED': return 'bg-danger text-white';
            default: return 'bg-secondary text-white';
        }
    }
};
