/**
 * inventory.js - Logistics & Warehouse Management
 * Standardized for Full English Backend synchronization (Hybrid Layer: Admin English)
 */
const inventory = {
    currentRawData: [],

    // 1. Load Inventory List with Low-Stock highlights
    // 1. Load Inventory List with Low-Stock highlights
    loadList: async () => {
        try {
            const lowStockRes = await api.get('/inventory/low-stock');
            const lowStockData = lowStockRes.data?.data || lowStockRes.data || [];
            $("#low-stock-count").text(lowStockData.length);

            const invRes = await api.get('/inventory/all'); 
            const inventoryData = invRes.data?.data || invRes.data || [];

            const booksRes = await api.get('/books');
            const booksData = booksRes.data?.data || booksRes.data || [];

            let totalValue = 0; // Khởi tạo biến tính tổng

            // CHỈ DÙNG 1 VÒNG LẶP DUY NHẤT
            inventory.currentRawData = inventoryData.map(invItem => {
                const bookInfo = booksData.find(b => b.isbn === invItem.isbn) || {};
                const isLow = lowStockData.find(l => l.isbn === invItem.isbn);
                
                const tenSach = invItem.tenSach || invItem.title || bookInfo.title || bookInfo.tenSach || (invItem.book ? invItem.book.title : 'N/A');
                const tonKho = invItem.tonKho || invItem.stockQuantity || 0;
                const viTriKe = invItem.viTriKe || invItem.shelfLocation || 'Chưa xếp kệ';
                const giaBan = bookInfo.price || bookInfo.giaBan || 0;

                // Cộng dồn tiền kho thực tế
                totalValue += (tonKho * giaBan);
                
                return {
                    isbn: invItem.isbn,
                    tenSach: tenSach,
                    tonKho: tonKho,
                    viTriKe: viTriKe,
                    giaBan: giaBan,
                    isLowStock: !!isLow
                };
            });

            $("#total-inventory-items").text(inventory.currentRawData.length);
            $("#total-inventory-value").text(api.formatCurrency(totalValue)); // Render tổng tiền

            inventory.renderTable(inventory.currentRawData);
        } catch (e) {
            console.error("Inventory load error:", e);
            api.showToast("Lỗi tải dữ liệu kho", "warning");
        }
    },

    filterStatus: (status) => {
        inventory.filterStock($("#inventory-search").val());
    },

    // THÊM HÀM BỊ MẤT NÀY VÀO:
    filterStock: (searchTerm) => {
        const term = (searchTerm || "").toLowerCase().trim();
        const status = $("#inventory-filter").val() || 'ALL';

        // Lọc theo từ khóa (Tìm theo ISBN hoặc Tên sách)
        let filtered = inventory.currentRawData.filter(item => {
            return item.isbn.toLowerCase().includes(term) || item.tenSach.toLowerCase().includes(term);
        });

        // Đẩy kết quả đã lọc từ khóa sang hàm renderFiltered để lọc tiếp theo Trạng thái (status)
        inventory.renderFiltered(filtered, status);
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

            tbody.append(`
                <tr class="${bgClass}">
                    <td class="ps-4">
                        <div class="fw-bold text-dark">${item.tenSach}</div>
                        <div class="small text-muted">ISBN: ${item.isbn}</div>
                    </td>
                    <td class="text-center">
                        <span class="badge ${item.isLowStock ? 'bg-danger' : 'bg-dark'} rounded-pill px-3 fs-6">
                            ${item.tonKho}
                        </span>
                    </td>
                    <td>
                        <div class="small fw-bold text-accent">${item.viTriKe}</div>
                        <div class="extra-small text-muted">Giá tham chiếu: ${api.formatCurrency(item.giaBan)}</div>
                    </td>
                    <td>${statusBadge} <br><span class="extra-small text-muted">Ngưỡng tối thiểu: 5</span></td>
                    <td class="text-end pe-4">
                        <button onclick="inventory.editStock('${item.isbn}')" class="btn btn-sm btn-light rounded-3 me-1">Sửa</button>
                        <button onclick="api.showToast('In mã vạch for ${item.isbn}...')" class="btn btn-sm btn-outline-secondary rounded-pill">Barcode</button>
                    </td>
                </tr>
            `);
        });
    },

    editStock: (isbn) => {
        const item = inventory.currentRawData.find(d => d.isbn === isbn);
        if (!item) return;

        $("#edit-inv-isbn").val(item.isbn);
        $("#edit-inv-title").val(item.tenSach);
        $("#edit-inv-stock").val(item.tonKho);
        $("#edit-inv-shelf").val(item.viTriKe);

        const modal = new bootstrap.Modal(document.getElementById('inventoryEditModal'));
        modal.show();
    },

    saveStock: async () => {
        const isbn = $("#edit-inv-isbn").val();
        const data = {
            isbn: isbn,
            newQuantity: parseInt($("#edit-inv-stock").val()) || 0,
            reason: "Manual Adjustment",
            staffId: api.getUser()?.id || 1
        };

        try {
            // Using the adjust API for quantity changes
            await api.post(`/inventory/adjust`, data); 
            
            api.showToast("Cập nhật tồn kho thành công!", "success");
            bootstrap.Modal.getInstance(document.getElementById('inventoryEditModal')).hide();
            inventory.loadList();
        } catch (e) {
            console.error(e);
            api.showToast("Cập nhật tồn kho thất bại", "error");
        }
    },

    addImportRow: () => {
        const tbody = $("#import-items-table tbody");
        const rowId = Date.now();
        tbody.append(`
            <tr id="row-${rowId}" class="import-row">
                <td>
                    <input type="text" class="form-control form-control-sm border-0 bg-light rounded-pill px-3 d-isbn fw-bold text-accent" placeholder="ISBN" onchange="inventory.fetchBookInfo(this, ${rowId})">
                    <div id="book-name-${rowId}" class="extra-small text-muted ps-3 mt-1"></div>
                </td>
                <td><input type="number" class="form-control form-control-sm border-0 bg-light rounded-pill px-3 d-qty" value="10" min="1" oninput="inventory.calcImportSubtotal()"></td>
                <td><input type="number" class="form-control form-control-sm border-0 bg-light rounded-pill px-3 d-unitPrice" value="50000" oninput="inventory.calcImportSubtotal()"></td>
                <td><input type="text" class="form-control form-control-sm border-0 bg-light rounded-pill px-3 d-shelf" placeholder="Kệ A1"></td>
                <td class="fw-bold text-accent d-subtotal align-middle text-end">500,000đ</td>
                <td class="text-center">
                    <button class="btn btn-sm text-danger" onclick="$('#row-${rowId}').remove(); inventory.calcImportSubtotal();">
                        <i class="icon icon-trash"></i>
                    </button>
                </td>
            </tr>
        `);
    },

    fetchBookInfo: async (inputElem, rowId) => {
        const isbn = $(inputElem).val().trim();
        const nameDiv = $(`#book-name-${rowId}`);
        const row = $(`#row-${rowId}`);
        if (!isbn) { nameDiv.text(""); return; }

        nameDiv.html('<i class="fas fa-spinner fa-spin"></i> Đang tìm...');
        try {
            const res = await api.get(`/books/${isbn}`);
            const bookInfo = res.data?.data || res.data;
            const title = bookInfo.title || bookInfo.tenSach;

            if (bookInfo && title) {
                row.attr('data-title', title); 
                nameDiv.html(`<span class="text-success fw-bold"><i class="fas fa-check-circle"></i> ${title}</span>`);
            } else {
                row.attr('data-title', '');
                nameDiv.html(`
                    <span class="text-warning"><i class="fas fa-magic"></i> Sách mới. </span>
                    <a href="javascript:void(0)" onclick="inventory.quickAddBookName(${rowId})" class="small text-accent text-decoration-underline">Đặt tên ngay?</a>
                `);
            }
        } catch (e) {
            row.attr('data-title', '');
            nameDiv.html(`
                <span class="text-warning"><i class="fas fa-magic"></i> Sách mới. </span>
                <a href="javascript:void(0)" onclick="inventory.quickAddBookName(${rowId})" class="small text-accent text-decoration-underline">Đặt tên ngay?</a>
            `);
        }
        inventory.calcImportSubtotal();
    },

    quickAddBookName: (rowId) => {
        const name = prompt("Nhập tên cho cuốn sách mới này:");
        if (name) {
            $(`#row-${rowId}`).attr('data-title', name); 
            $(`#book-name-${rowId}`).html(`<span class="text-success fw-bold"><i class="fas fa-plus-circle"></i> ${name} (Sách mới)</span>`);
        }
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
             const suppliersData = res.data?.data || res.data || []; 
             const sel = $("#supplier-select");
             sel.empty().append('<option value="">--- Chọn Nhà Cung Cấp ---</option>');
             
             suppliersData.forEach(s => {
                  sel.append(`<option value="${s.supplierId}">${s.supplierName}</option>`);
             });
        } catch(e) {
             console.error("Lỗi tải NCC:", e);
        }
    },

    importScanner: null, // Biến lưu trữ phiên bản scanner của phần Nhập kho

    startImportScanner: () => {
        // Tránh trường hợp bật nhiều camera cùng lúc
        if (inventory.importScanner) {
            inventory.importScanner.clear().catch(e => console.log(e));
        }

        // Khởi tạo thư viện quét mã vạch cho thẻ div id="import-reader"
        inventory.importScanner = new Html5QrcodeScanner(
            "import-reader",
            { fps: 10, qrbox: { width: 250, height: 120 } },
            false
        );

        inventory.importScanner.render(
            (decodedText) => {
                // XỬ LÝ KHI QUÉT THÀNH CÔNG:
                api.showToast(`Đã quét mã: ${decodedText}`, "success");
                
                // 1. Tự động thêm một dòng mới vào bảng
                inventory.addImportRow();
                
                // 2. Tìm dòng cuối cùng vừa thêm và điền mã ISBN vào
                const lastRow = $("#import-items-table tbody tr").last();
                const inputEl = lastRow.find('.d-isbn');
                inputEl.val(decodedText);
                
                // 3. Gọi hàm tự động tìm tên sách
                const rowId = lastRow.attr('id').split('-')[1];
                inventory.fetchBookInfo(inputEl[0], rowId);
                
                // 4. Quét xong thì tự động tắt cam và chuyển về tab "Thêm thủ công" để nhập số lượng
                inventory.switchImportTab('manual');
            },
            (error) => {
                // Bỏ qua các lỗi quét khung hình trống (vì nó chạy liên tục)
            }
        );
    },

    switchImportTab: (tabName) => {
        // 1. Ẩn tất cả các khu vực
        $("#import-manual-area, #import-camera-area, #import-ocr-area").addClass("d-none");
        
        // 2. Đổi màu nút bấm về trạng thái chưa chọn (outline)
        $("#btn-tab-manual, #btn-tab-camera, #btn-tab-ocr")
            .removeClass("btn-accent")
            .addClass("btn-outline-accent");

        // TẮT CAMERA NẾU ĐANG BẬT MÀ CHUYỂN TAB KHÁC
        if (tabName !== 'camera' && inventory.importScanner) {
            inventory.importScanner.clear().catch(e => console.log(e));
            inventory.importScanner = null;
        }

        // 3. Hiện khu vực được chọn và xử lý
        if (tabName === 'manual') {
            $("#import-manual-area").removeClass("d-none");
            $("#btn-tab-manual").removeClass("btn-outline-accent").addClass("btn-accent");
            
        } else if (tabName === 'camera') {
            $("#import-camera-area").removeClass("d-none");
            $("#btn-tab-camera").removeClass("btn-outline-accent").addClass("btn-accent");
            
            // GỌI HÀM BẬT CAMERA Ở ĐÂY!
            inventory.startImportScanner();
            
        } else if (tabName === 'ocr') {
            $("#import-ocr-area").removeClass("d-none");
            $("#btn-tab-ocr").removeClass("btn-outline-accent").addClass("btn-accent");
        }
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
            const title = $(this).attr('data-title') || ""; 
            const quantity = parseInt($(this).find('.d-qty').val()) || 0;
            const unitPrice = parseFloat($(this).find('.d-unitPrice').val()) || 0;
            const shelfLocation = $(this).find('.d-shelf').val() || "Chưa xếp kệ"; 

            if (isbn && quantity > 0) {
                items.push({ 
                    isbn, 
                    quantity, 
                    unitPrice, 
                    shelfLocation, 
                    title 
                });
            }
        });

        if(items.length === 0) {
             api.showToast("Biễu mẫu trống!", "error"); return;
        }

        const dto = {
             supplierId: parseInt(supplierId),
             staffId: api.getUser()?.id || 1, 
             details: items
        };

        api.showToast("Đang thực hiện nhập kho...", "info");
        try {
             await api.post('/inventory/import', dto);
             api.showToast("✓ Đã nhập hàng thành công!", "success");
             setTimeout(() => { layout.render('Inventory/Admin', 'Index'); }, 1500);
        } catch(e) {
             api.showToast("Nhập kho thất bại: " + e.message, "error");
        }
    },

    // Export & Scanning Logic
    exportOrderData: null,
    exportScanner: null,

    loadOrderForExport: async () => {
        const orderId = $("#export-order-id").val()?.trim();
        if (!orderId) { api.showToast("Vui lòng nhập Order ID", "warning"); return; }

        try {
            const res = await api.get(`/orders/${orderId}`);
            const data = res.data?.data || res.data;

            if (!data || !data.orderDetails) {
                api.showToast("Đơn hàng không có chi tiết sản phẩm!", "error"); return;
            }

            inventory.exportOrderData = {
                orderId: data.orderId || data.id,
                items: data.orderDetails.map(item => {
                    const bookObj = item.book || item.sach || item; 
                    return {
                        isbn: bookObj.isbn || "N/A",
                        title: bookObj.tenSach || bookObj.title || "Sách không tên",
                        requiredQty: item.quantity || item.soLuong || 0,
                        scannedQty: 0
                    };
                })
            };

            $("#export-display-orderId").text(inventory.exportOrderData.orderId);
            inventory.renderExportTable();
            
            $("#export-camera-placeholder").addClass('d-none');
            $("#export-camera-area").removeClass('d-none');
            inventory.startExportScanner();

        } catch (e) {
            console.error(e);
            api.showToast("Không thể tải đơn hàng.", "error");
        }
    },

    startExportScanner: () => {
        if (inventory.exportScanner) {
            inventory.exportScanner.clear().catch(e => console.log(e));
        }

        inventory.exportScanner = new Html5QrcodeScanner(
            "export-reader",
            { fps: 10, qrbox: { width: 250, height: 120 } }, 
            false
        );

        inventory.exportScanner.render(
            (decodedText) => { inventory.handleExportScan(decodedText); },
            (error) => {}
        );
    },

    handleExportScan: (isbn) => {
        if (!inventory.exportOrderData) return;
        const item = inventory.exportOrderData.items.find(i => i.isbn === isbn);
        if (item) {
            if (item.scannedQty < item.requiredQty) {
                item.scannedQty++;
                api.showToast(`Đã quét: ${item.title}`, "success");
                inventory.renderExportTable();
            } else {
                api.showToast("Sản phẩm đã đủ số lượng!", "warning");
            }
        } else {
            api.showToast(`Lỗi: ISBN ${isbn} không có trong đơn này!`, "error");
        }
    },

    renderExportTable: () => {
        const tbody = $("#export-verify-table tbody");
        tbody.empty();
        let allDone = true;

        inventory.exportOrderData.items.forEach(item => {
            const isFinished = item.scannedQty === item.requiredQty;
            if (!isFinished) allDone = false;

            tbody.append(`
                <tr class="${isFinished ? 'bg-success bg-opacity-10' : ''}">
                    <td class="ps-3">
                        <div class="fw-bold">${item.title}</div>
                        <div class="extra-small text-muted">ISBN: ${item.isbn}</div>
                    </td>
                    <td class="text-center fw-bold">${item.requiredQty}</td>
                    <td class="text-center text-accent fw-bold">${item.scannedQty}</td>
                    <td class="text-end pe-3">
                        ${isFinished ? '<span class="badge bg-success">Xong</span>' : '<span class="badge bg-light text-dark">Chờ...</span>'}
                    </td>
                </tr>
            `);
        });

        const totalReq = inventory.exportOrderData.items.reduce((a, b) => a + b.requiredQty, 0);
        const totalScan = inventory.exportOrderData.items.reduce((a, b) => a + b.scannedQty, 0);
        $("#export-progress").text(`${totalScan}/${totalReq}`);
        $("#btn-confirm-export").prop('disabled', !allDone);
    },

    processExport: async () => {
        const orderId = inventory.exportOrderData?.orderId;
        if (!orderId) return;

        try {
            await api.post('/inventory/export', { orderId: orderId });
            api.showToast("✓ Đã hoàn tất xuất kho cho đơn hàng!", "success");
            setTimeout(() => { layout.render('Inventory/Admin', 'Index'); }, 1500);
        } catch (e) {
            api.showToast("Lỗi xuất kho: " + e.message, "error");
        }
    },

    scanIsbn: async function() {
        const isbnInput = document.getElementById('isbnInput');
        const resultArea = document.getElementById('scanResultArea');
        const btnScan = document.getElementById('btnScan');
        const isbn = isbnInput.value.trim();

        if (!isbn) {
            api.showToast('Vui lòng nhập hoặc quét ISBN', 'warning');
            return;
        }

        btnScan.innerHTML = '<div class="spinner-border spinner-border-sm text-accent"></div>';
        btnScan.disabled = true;

        try {
            const response = await api.get(`/inventory/scan/${isbn}`);
            if (response && response.data) { 
                const data = response.data;
                resultArea.innerHTML = `
                    <div class="card bg-light border-0 rounded-4 p-3 mb-0">
                        <h5 class="fw-bold mb-1">${data.bookTitle || data.tenSach}</h5>
                        <div class="small text-muted mb-2">ISBN: ${data.isbn}</div>
                        <div class="d-flex gap-3">
                            <span class="badge bg-white text-dark border p-2">Tồn: <b>${data.stockQuantity || data.tonKho}</b></span>
                            <span class="badge bg-white text-dark border p-2">Kệ: <b>${data.shelfLocation || data.viTriKe}</b></span>
                        </div>
                    </div>
                `;
                resultArea.classList.remove('d-none');
            }
        } catch (error) {
            api.showToast('Không tìm thấy thông tin sản phẩm', 'error');
            resultArea.classList.add('d-none');
        } finally {
            btnScan.innerHTML = 'Tra cứu';
            btnScan.disabled = false;
        }
    }
};
