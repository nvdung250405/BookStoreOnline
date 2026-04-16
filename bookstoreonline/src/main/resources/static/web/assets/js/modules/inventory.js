/**
 * inventory.js - Logistics & Warehouse Management
 * Standardized for Full English Backend synchronization (Hybrid Layer: Admin English)
 */
const inventory = {
    currentRawData: [],

    // 1. Load Inventory List with Low-Stock highlights
    loadList: async () => {
        try {
            const lowStockRes = await api.get('/inventory/low-stock');
            const lowStockData = lowStockRes.data || lowStockRes || [];
            
            $("#low-stock-count").text(lowStockData.length);

            // Fetch current stock from primary Books API
            const res = await api.get('/books');
            const data = res.data || res || [];
            
            inventory.currentRawData = data.map(item => {
                const isLow = lowStockData.find(l => l.isbn === item.isbn);
                return { book: item, isLowStock: !!isLow };
            });

            $("#total-inventory-items").text(data.length);
            
            // Calculate total inventory value
            let totalValue = 0;
            data.forEach(i => totalValue += (i.price || 0) * (i.stockQuantity || 0));
            $("#total-inventory-value").text(api.formatCurrency(totalValue));

            inventory.renderTable(inventory.currentRawData);
        } catch (e) {
            console.error("Inventory load error:", e);
        }
    },

    filterStock: (searchValue) => {
        const query = searchValue.toLowerCase();
        let filtered = inventory.currentRawData;
        if(query) {
             filtered = filtered.filter(row => 
                 row.book.title.toLowerCase().includes(query) || 
                 row.book.isbn.toLowerCase().includes(query)
             );
        }
        const status = $("#inventory-filter").val();
        inventory.renderFiltered(filtered, status);
    },

    filterStatus: (status) => {
        inventory.renderFiltered(inventory.currentRawData, status);
    },

    renderFiltered: (dataList, status) => {
         let finalData = dataList;
         if (status === 'LOW_STOCK') {
             finalData = dataList.filter(d => d.isLowStock);
         } else if (status === 'IN_STOCK') {
             finalData = dataList.filter(d => !d.isLowStock);
         }
         inventory.renderTable(finalData);
    },

    renderTable: (data) => {
        const tbody = $("#inventory-admin-body");
        if (!tbody.length) return;
        tbody.empty();

        if (data.length === 0) {
            tbody.html('<tr><td colspan="5" class="text-center py-4 text-muted">Không có dữ liệu tồn kho</td></tr>');
            return;
        }

        data.forEach(item => {
            const bgClass = item.isLowStock ? 'bg-danger bg-opacity-10' : '';
            const statusBadge = item.isLowStock ? '<span class="badge bg-danger">Sắp hết hàng</span>' : '<span class="badge bg-success">Còn hàng</span>';
            const price = api.formatCurrency(item.book.price);

            tbody.append(`
                <tr class="${bgClass}">
                    <td class="ps-4">
                        <div class="fw-bold text-dark">${item.book.title}</div>
                        <div class="small text-muted">ISBN: ${item.book.isbn}</div>
                    </td>
                    <td class="text-center">
                        <span class="badge ${item.isLowStock ? 'bg-danger' : 'bg-dark'} rounded-pill px-3 fs-6">
                            ${item.book.stockQuantity || 0}
                        </span>
                    </td>
                    <td>
                        <div class="small fw-bold">Kệ A-12 (Mặc định)</div>
                        <div class="extra-small text-muted">Giá tham chiếu: ${price}</div>
                    </td>
                    <td>${statusBadge} <br><span class="extra-small text-muted">Ngưỡng tối thiểu: 5</span></td>
                    <td class="text-end pe-4">
                        <button onclick="api.showToast('In mã vạch cho sản phẩm...')" class="btn btn-sm btn-outline-secondary rounded-pill">Kho vận/Barcode</button>
                    </td>
                </tr>
            `);
        });
    },

    // 2. Import Goods logic
    addImportRow: () => {
        const tbody = $("#import-items-table tbody");
        const rowId = Date.now();
        tbody.append(`
            <tr id="row-${rowId}" class="import-row">
                <td><input type="text" class="form-control form-control-sm border-0 bg-light rounded-pill px-3 d-isbn fw-bold text-accent" placeholder="Nhập ISBN" onblur="inventory.calcImportSubtotal()"></td>
                <td><input type="number" class="form-control form-control-sm border-0 bg-light rounded-pill px-3 d-qty" value="10" min="1" onchange="inventory.calcImportSubtotal()"></td>
                <td><input type="number" class="form-control form-control-sm border-0 bg-light rounded-pill px-3 d-unitPrice" value="50000" onchange="inventory.calcImportSubtotal()"></td>
                <td class="fw-bold text-accent d-subtotal align-middle">500,000đ</td>
                <td class="text-end align-middle"><button onclick="$('#row-${rowId}').remove(); inventory.calcImportSubtotal();" class="btn btn-link link-danger p-0"><i class="icon icon-close text-danger"></i></button></td>
            </tr>
        `);
        inventory.calcImportSubtotal();
    },

    calcImportSubtotal: () => {
        let total = 0;
        $(".import-row").each(function() {
             const qty = parseInt($(this).find('.d-qty').val()) || 0;
             const price = parseFloat($(this).find('.d-unitPrice').val()) || 0;
             const sub = qty * price;
             $(this).find('.d-subtotal').text(api.formatCurrency(sub));
             total += sub;
        });
        $("#import-total-value").text(api.formatCurrency(total));
    },

    initImportForm: async () => {
        $("#import-items-table tbody").empty();
        inventory.addImportRow();
        
        try {
             const res = await api.get('/admin/suppliers');
             const suppliersData = res.data || res || [];
             const sel = $("#supplier-select");
             sel.empty().append('<option value="">--- Chọn nhà cung cấp ---</option>');
             suppliersData.forEach(s => {
                  sel.append(`<option value="${s.id}">${s.name}</option>`);
             });
        } catch(e) {}
    },

    processImport: async () => {
        const supplierId = $("#supplier-select").val();
        if (!supplierId) {
             api.showToast("Cảnh báo Kho: Vui lòng chọn nhà cung cấp!", "warning");
             return;
        }

        const items = [];
        $(".import-row").each(function() {
             const isbn = $(this).find('.d-isbn').val()?.trim();
             const quantity = parseInt($(this).find('.d-qty').val()) || 0;
             const unitPrice = parseFloat($(this).find('.d-unitPrice').val()) || 0;
             if (isbn && quantity > 0 && unitPrice > 0) {
                 items.push({ isbn, quantity, unitPrice });
             }
        });

        if(items.length === 0) {
             api.showToast("Biểu mẫu trống! Vui lòng nhập thông tin sản phẩm", "error"); return;
        }

        const dto = {
             supplierId: parseInt(supplierId),
             staffId: api.getUser()?.id || 1, // Fallback to 1 for now
             details: items
        };

        api.showToast("Đang khởi tạo giao dịch nhập kho...", "info");
        try {
             const res = await api.post('/inventory/import', dto);
             api.showToast("✓ Đã ghi nhận lô hàng thành công! Mã nội bộ: #" + (res.data?.id || 'OK'));
             setTimeout(() => { layout.render('Inventory/Admin', 'Index'); }, 1500);
        } catch(e) {
             api.showToast("Nhập kho thất bại: " + e.message, "error");
        }
    },

    // 3. Export Goods logic
    processExport: async () => {
        const orderId = $("#export-order-id").val()?.trim();
        if(!orderId) {
            api.showToast("Vui lòng nhập Mã Đơn hàng để theo dõi.", "warning"); return;
        }

        const logView = $("#export-result-log");
        logView.append(`<br><br>> Đang kiểm tra Mã Đơn hàng: <b>${orderId}</b>...`);

        try {
             const dto = { orderId: orderId, note: "Auto-logistic export via Admin Dashboard" };
             await api.post('/inventory/export', dto);
             logView.removeClass("text-muted text-danger text-warning").addClass("text-success fw-bold");
             logView.append(`<br>> ✓ Xác minh tồn kho: HỢP LỆ.`);
             logView.append(`<br>> ✓ Trừ kho: THÀNH CÔNG.`);
             api.showToast("Hàng hóa đã được trừ khỏi kho thành công!");
        } catch(e) {
             logView.removeClass("text-muted text-success text-warning").addClass("text-danger fw-bold");
             logView.append(`<br>> ✗ KIỂM TRA THẤT BẠI (ROLLBACK). Lỗi: ${e.message}`);
             api.showToast("Lỗi vận hành kho vận", "error");
        }
    },

    // 4. Shipping Tracking Logic
    trackOrder: async (trackingId) => {
        if (!trackingId) return;
        api.showToast("Đang thu thập dữ liệu định vị GPS...", "info");
        try {
            const res = await api.get(`/shipping/track/${trackingId}`);
            const data = res.data || res;
            if (data && data.trackingNumber) {
                $("#tracking-result-container").fadeIn(400);
                $("#tracking-display-id").text("Mã vận đơn: " + data.trackingNumber);
                
                const latestStatus = data.statusHistory[data.statusHistory.length - 1]?.status || 'Initialized';
                $("#tracking-status-badge").text(latestStatus);
                
                inventory.renderTrackingTimeline(data);
                api.showToast("Truy xuất theo dõi thời gian thực thành công!", "success");
            }
        } catch (e) {
            api.showToast("Không tìm thấy mã vận đơn trong hệ thống Logistic", "error");
            $("#tracking-result-container").hide();
        }
    },

    renderTrackingTimeline: (data) => {
        const container = $("#tracking-timeline");
        if (!container.length) return;
        container.empty();
        
        let html = "";
        data.statusHistory.forEach((step, index) => {
            const isLast = index === (data.statusHistory.length - 1);
            const timeStr = step.timestamp ? common.formatDate(step.timestamp, true) : 'Hidden';
            html += `
                <div class="timeline-item pb-4 border-start border-2 ${isLast ? 'border-accent' : 'border-light'} ps-4 position-relative">
                    <span class="position-absolute translate-middle-x ${isLast ? 'bg-accent shadow p-1 border border-accent border-4' : 'bg-light border'} rounded-circle" 
                          style="width:${isLast ? '18px':'14px'}; height:${isLast ? '18px':'14px'}; left:0; top:0; z-index:2;"></span>
                    <div class="small fw-bold ${isLast ? 'text-accent fs-6' : 'text-dark'}">${step.status}</div>
                    <div class="small text-muted mb-1">${timeStr}</div>
                    <div class="extra-small opacity-75 fw-medium">Vị trí: ${step.location}</div>
                </div>
            `;
        });
        container.html(html);
    },

    lookupForAdjustment: async () => {
        const isbn = $("#adj-isbn").val().trim();
        if(!isbn) return;
        try {
            const res = await api.get(`/inventory/scan/${isbn}`);
            const data = res.data || res;
            $("#adj-book-title").text(data.bookTitle).removeClass("text-muted").addClass("text-dark fw-bold");
            $("#adj-current-stock").text(data.stockQuantity);
            $("#adj-new-qty").val(data.stockQuantity);
        } catch(e) {
            api.showToast("Không tìm thấy sản phẩm trong kho vật lý", "error");
            $("#adj-book-title").text("Chưa chọn sản phẩm").addClass("text-muted").removeClass("text-dark fw-bold");
            $("#adj-current-stock").text("--");
        }
    },

    submitAdjustment: async () => {
        const isbn = $("#adj-isbn").val().trim();
        const newQty = parseInt($("#adj-new-qty").val());
        let reason = $("#adj-reason").val();
        const notes = $("#adj-notes").val().trim();

        if(!isbn || isNaN(newQty) || !reason) {
            api.showToast("Vui lòng điền đầy đủ các trường bắt buộc", "warning"); return;
        }

        if(reason === 'Khác (Other)' && notes) {
            reason = "Other: " + notes;
        }

        if(!confirm(`Xác nhận điều chỉnh tồn kho cho ISBN ${isbn} thành ${newQty}?`)) return;

        const payload = {
            isbn: isbn,
            newQuantity: newQty,
            reason: reason,
            staffId: api.getUser()?.id || 1
        };

        api.showToast("Đang xử lý điều chỉnh...", "info");
        try {
            await api.post('/inventory/adjust', payload);
            api.showToast("✓ Số lượng tồn kho đã được cập nhật thành công!", "success");
            setTimeout(() => { layout.render('Inventory/Admin', 'Index'); }, 1000);
        } catch(e) {
            api.showToast("Điều chỉnh thất bại: " + e.message, "error");
        }
    }
};
