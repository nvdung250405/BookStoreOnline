/**
 * cart.js - Shopping Cart Logic
 */
const cart = {
    // Add item to cart
    add: async (isbn, quantity = 1) => {
        const user = api.getUser();
        
        if (!user) {
            api.showToast("Vui lòng đăng nhập để thêm vào giỏ hàng", "error");
            layout.render('Auth', 'Login');
            return;
        }

        try {
            // Using the API endpoint identified: POST /api/cart/add?username=...&isbn=...&soLuong=...
            const result = await api.post(`/cart/add?username=${user.username}&isbn=${isbn}&soLuong=${quantity}`);
            if (result.success) {
                api.showToast("Đã thêm vào giỏ hàng!");
                cart.updateCounter();
            }
        } catch (error) {
            api.showToast("Lỗi khi thêm vào giỏ hàng", "error");
        }
    },

    // Get cart content
    load: async () => {
        const user = api.getUser();
        if (!user) return;

        try {
            const result = await api.get(`/cart/${user.username}`);
            if (result.success && result.data) {
                cart.render(result.data);
            }
        } catch (error) {
            console.error("Cart load failed", error);
        }
    },

    // Update global cart badge
    updateCounter: async () => {
        const user = api.getUser();
        if (!user) {
            $("#cart-count").text(0);
            return;
        }
        
        try {
            const result = await api.get(`/cart/${user.username}`);
            if (result.success && result.data) {
                $("#cart-count").text(result.data.length);
            }
        } catch (error) {
            $("#cart-count").text(0);
        }
    },

    // Render cart table
    render: (items) => {
        const body = $("#cart-items-body");
        if (!body.length) return;
        
        body.empty();
        let total = 0;

        items.forEach(item => {
            const itemTotal = item.gia * item.soLuong;
            total += itemTotal;
            
            body.append(`
                <tr class="border-bottom">
                    <td class="ps-4 py-4">
                        <div class="d-flex align-items-center gap-3">
                            <img src="${item.hinhAnh || 'assets/images/product-item1.jpg'}" class="rounded-3 shadow-sm" width="80">
                            <div>
                                <h6 class="fw-bold mb-1">${item.tenSach}</h6>
                                <p class="small text-muted mb-0">ISBN: ${item.isbn}</p>
                                <button onclick="cart.remove(${item.id})" class="btn btn-link link-danger p-0 small text-decoration-none mt-2">Xóa</button>
                            </div>
                        </div>
                    </td>
                    <td class="text-center">
                        <div class="quantity-input d-inline-flex align-items-center border rounded-pill bg-light mx-auto">
                            <button onclick="cart.update(${item.id}, ${item.soLuong - 1})" class="btn btn-sm link-dark border-0 p-2 shadow-none"><i class="icon icon-minus"></i></button>
                            <input type="number" class="form-control border-0 bg-transparent text-center fw-bold shadow-none p-0" value="${item.soLuong}" style="width: 40px" readonly>
                            <button onclick="cart.update(${item.id}, ${item.soLuong + 1})" class="btn btn-sm link-dark border-0 p-2 shadow-none"><i class="icon icon-plus"></i></button>
                        </div>
                    </td>
                    <td class="text-end pe-4 fw-bold text-accent fs-5">${formatPrice(itemTotal)}</td>
                </tr>
            `);
        });

        // Update totals
        $("#cart-total").text(formatPrice(total));
        $("#cart-final-total").text(formatPrice(total + 30000)); // Sample shipping calc
    },

    update: async (id, quantity) => {
        if (quantity < 1) return;
        try {
            await api.put(`/cart/update?id=${id}&soLuong=${quantity}`);
            cart.load();
        } catch (e) { api.showToast("Lỗi cập nhật số lượng", "error"); }
    },

    remove: async (id) => {
        try {
            await api.delete(`/cart/remove/${id}`);
            api.showToast("Đã xóa khỏi giỏ hàng");
            cart.load();
            cart.updateCounter();
        } catch (e) { api.showToast("Lỗi khi xóa", "error"); }
    }
};
