/**
 * cart.js - Shopping Cart Logic
 * Standardized for Full English Backend synchronization
 */

const cart = {

    add: async (isbn, quantity = 1) => {
        const user = api.getUser();
        if (!user) {
            api.showToast("Please login to add items to your cart", "warning");
            layout.render('Auth', 'Login');
            return;
        }
        try {
            // Updated to use /api/cart/add with English param names
            await api.post(`/cart/add?username=${user.username}&isbn=${isbn}&quantity=${quantity}`);
            api.showToast("Added to cart!");
            cart.updateCounter();
        } catch (error) {
            api.showToast("Failed to add to cart", "error");
        }
    },

    load: async () => {
        const user = api.getUser();
        if (!user) { cart.render([]); return; }
        try {
            const result = await api.get(`/cart/${user.username}`);
            const items = result.data || [];
            cart.render(items);
        } catch (error) {
            console.error("Cart load failed", error);
            cart.render([]);
        }
    },

    updateCounter: async () => {
        const user = api.getUser();
        if (!user) { $("#cart-count").text(0); return; }
        try {
            const result = await api.get(`/cart/${user.username}`);
            const items = result.data || [];
            $("#cart-count").text(items.length);
        } catch (error) {
            $("#cart-count").text(0);
        }
    },

    render: (items) => {
        const body = $("#cart-items-body");
        if (!body.length) return;
        body.empty();
        let total = 0;

        if (!items || items.length === 0) {
            body.html(`<tr><td colspan="3" class="text-center py-5 text-muted">Your cart is empty. <a href="javascript:void(0)" onclick="layout.render('Books', 'Index')" class="text-accent">Shop now</a></td></tr>`);
            $("#cart-subtotal").text("0đ");
            $("#cart-final-total").text("0đ");
            $("#cart-item-count").text(0);
            return;
        }

        items.forEach(item => {
            const itemTotal = item.totalPrice || (item.price * item.quantity);
            total += parseFloat(itemTotal);

            body.append(`
                <tr class="border-bottom">
                    <td class="ps-4 py-4">
                        <div class="d-flex align-items-center gap-3">
                            <img src="${item.coverImage ? 'assets/images/' + item.coverImage : 'assets/images/product-item1.jpg'}"
                                 class="rounded-3 shadow-sm" width="80"
                                 onerror="this.src='assets/images/product-item1.jpg'">
                            <div>
                                <h6 class="fw-bold mb-1">${item.title}</h6>
                                <p class="small text-muted mb-0">ISBN: ${item.isbn}</p>
                                <button onclick="cart.remove('${item.isbn}')" class="btn btn-link link-danger p-0 small text-decoration-none mt-2">
                                    <i class="icon icon-close me-1"></i> Remove
                                </button>
                            </div>
                        </div>
                    </td>
                    <td class="text-center">
                        <div class="quantity-input d-inline-flex align-items-center border rounded-pill bg-light mx-auto">
                            <button onclick="cart.update('${item.isbn}', ${item.quantity - 1})" class="btn btn-sm link-dark border-0 p-2 shadow-none"><i class="icon icon-minus"></i></button>
                            <input type="number" class="form-control border-0 bg-transparent text-center fw-bold shadow-none p-0" value="${item.quantity}" style="width: 40px" readonly>
                            <button onclick="cart.update('${item.isbn}', ${item.quantity + 1})" class="btn btn-sm link-dark border-0 p-2 shadow-none"><i class="icon icon-plus"></i></button>
                        </div>
                    </td>
                    <td class="text-end pe-4 fw-bold text-accent fs-5">${api.formatCurrency(itemTotal)}</td>
                </tr>
            `);
        });

        // Demo logic: Free shipping for orders > 200k
        const shippingFee = total >= 200000 ? 0 : 30000;
        $("#cart-item-count").text(items.length);
        $("#cart-subtotal").text(api.formatCurrency(total));
        $("#cart-shipping").text(shippingFee === 0 ? "Free" : api.formatCurrency(shippingFee));
        $("#cart-final-total").text(api.formatCurrency(total + shippingFee));
    },

    update: async (isbn, quantity) => {
        if (quantity < 1) {
            cart.remove(isbn);
            return;
        }
        const user = api.getUser();
        if (!user) return;
        try {
            await api.put(`/cart/update?username=${user.username}&isbn=${isbn}&quantity=${quantity}`);
            cart.load();
            cart.updateCounter();
        } catch (e) {
            api.showToast("Error updating quantity", "error");
        }
    },

    remove: async (isbn) => {
        if (!confirm("Are you sure you want to remove this item?")) return;
        const user = api.getUser();
        if (!user) return;
        try {
            await api.delete(`/cart/remove?username=${user.username}&isbn=${isbn}`);
            api.showToast("Removed from cart");
            cart.load();
            cart.updateCounter();
        } catch (e) {
            api.showToast("Error removing item", "error");
        }
    },

    clearAll: async () => {
        if (!confirm("Are you sure you want to clear your cart?")) return;
        const user = api.getUser();
        if (!user) return;
        try {
            await api.delete(`/cart/clear/${user.username}`);
            api.showToast("Cart cleared");
            cart.render([]);
            cart.updateCounter();
        } catch (e) {
            api.showToast("Error clearing cart", "error");
        }
    },

    loadAdminCarts: async () => {
        try {
            const res = await api.get(`/api/cart/admin/all`);
            const carts = res.data || res;
            const tbody = $("#cart-admin-list-body");
            if (!tbody.length) return;
            tbody.empty();

            if (!carts || carts.length === 0) {
                tbody.html('<tr><td colspan="5" class="text-center py-5 text-muted">No active shopping carts found.</td></tr>');
                return;
            }

            carts.forEach(item => {
                const username = item.customer?.account?.username || 'Guest';
                const bookTitle = item.book?.title || 'Unknown Book';
                const unitPrice = item.book?.price || 0;
                const total = unitPrice * (item.quantity || 1);

                tbody.append(`
                    <tr>
                        <td class="ps-4">
                            <div class="fw-bold">${username}</div>
                            <div class="small text-muted">Customer ID: ${item.customer?.customerId}</div>
                        </td>
                        <td>
                            <div class="fw-medium">${bookTitle}</div>
                            <div class="extra-small text-muted">ISBN: ${item.book?.isbn}</div>
                        </td>
                        <td class="text-center">
                            <span class="badge bg-light text-dark border rounded-pill px-3">${item.quantity}</span>
                        </td>
                        <td class="text-end fw-bold text-accent">${api.formatCurrency(total)}</td>
                        <td class="text-end pe-4">
                             <span class="badge bg-info bg-opacity-10 text-info">Monitoring</span>
                        </td>
                    </tr>
                `);
            });
        } catch (e) {
            api.showToast("Failed to load active carts: " + e.message, "error");
        }
    }
};
