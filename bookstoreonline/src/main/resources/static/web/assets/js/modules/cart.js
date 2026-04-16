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
                        <div class="d-inline-flex align-items-center bg-light rounded-pill border shadow-sm" style="height: 36px; min-width: 100px; padding: 0 4px; justify-content: space-between;">
                            <button onclick="cart.update('${item.isbn}', ${Number(item.quantity) - 1})" 
                                    class="btn btn-sm btn-white rounded-circle shadow-sm border-0 d-flex align-items-center justify-content-center p-0" 
                                    style="width: 28px; height: 28px; background: white; transition: all 0.2s; flex-shrink: 0;"
                                    onmouseover="this.style.transform='scale(1.1)'; this.style.background='#f8f9fa'"
                                    onmouseout="this.style.transform='scale(1)'; this.style.background='white'">
                                <span style="font-size: 1.2rem; line-height: 1; margin-top: -2px;">-</span>
                            </button>
                            <span class="fw-bolder" style="flex: 1; text-align: center; font-size: 0.95rem; font-family: 'Inter', sans-serif; display: flex; align-items: center; justify-content: center; height: 100%; color: #111;">
                                ${item.quantity}
                            </span>
                            <button onclick="cart.update('${item.isbn}', ${Number(item.quantity) + 1})" 
                                    class="btn btn-sm btn-white rounded-circle shadow-sm border-0 d-flex align-items-center justify-content-center p-0" 
                                    style="width: 28px; height: 28px; background: white; transition: all 0.2s; flex-shrink: 0;"
                                    onmouseover="this.style.transform='scale(1.1)'; this.style.background='#f8f9fa'"
                                    onmouseout="this.style.transform='scale(1)'; this.style.background='white'">
                                <span style="font-size: 1.1rem; line-height: 1;">+</span>
                            </button>
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
    }
};
