/**
 * inventory.js - Logistics & Warehouse Management
 * Standardized for Full English Backend synchronization (Hybrid Layer: Admin English)
 */
const inventory = {
    currentRawData: [],

    // 1. Load Inventory List with Low-Stock highlights
    loadList: async () => {
        try {
            // Lấy danh sách cảnh báo sắp hết
            const lowStockRes = await api.get('/inventory/low-stock');
            const lowStockData = lowStockRes.data?.data || lowStockRes.data || [];
            $("#low-stock-count").text(lowStockData.length);

            // LẤY DỮ LIỆU KHO (Chứa tonKho, viTriKe từ InventoryDetailDTO)
            // Lưu ý: Gọi vào endpoint /all bạn vừa thêm ở Bước 1
            const invRes = await api.get('/inventory/all'); 
            const inventoryData = invRes.data?.data || invRes.data || [];

            // LẤY DỮ LIỆU SÁCH (Chứa giá tiền từ BookDTO)
            const booksRes = await api.get('/books');
            const booksData = booksRes.data?.data || booksRes.data || [];

            // GỘP (MERGE) DỮ LIỆU THEO ISBN
            inventory.currentRawData = inventoryData.map(invItem => {
                // Tìm thông tin sách tương ứng để lấy giá bán
                const bookInfo = booksData.find(b => b.isbn === invItem.isbn) || {};
                const isLow = lowStockData.find(l => l.isbn === invItem.isbn);
                
                return {
                    isbn: invItem.isbn,
                    // Bắt đúng @JsonProperty từ DTO của bạn
                    tenSach: invItem.tenSach || bookInfo.title || 'N/A',
                    tonKho: invItem.tonKho || 0,
                    viTriKe: invItem.viTriKe || 'Chưa xếp kệ',
                    giaBan: bookInfo.price || bookInfo.giaBan || 0,
                    isLowStock: !!isLow
                };
            });

            $("#total-inventory-items").text(inventory.currentRawData.length);
            
            // Tính lại tổng giá trị kho một cách chuẩn xác
            let totalValue = 0;
            // Trong inventory.js -> hàm loadList
            inventory.currentRawData = inventoryData.map(invItem => {
                const bookInfo = booksData.find(b => b.isbn === invItem.isbn) || {};
                const isLow = lowStockData.find(l => l.isbn === invItem.isbn);
                
                return {
                    isbn: invItem.isbn,
                    // Thử mọi trường có thể chứa tên: tenSach, title, book.title...
                    tenSach: invItem.tenSach || invItem.title || bookInfo.title || bookInfo.tenSach || (invItem.book ? invItem.book.title : 'N/A'),
                    tonKho: invItem.tonKho || 0,
                    viTriKe: invItem.viTriKe || 'Chưa xếp kệ',
                    giaBan: bookInfo.price || bookInfo.giaBan || 0,
                    isLowStock: !!isLow
                };
            });
            $("#total-inventory-value").text(api.formatCurrency(totalValue));

            inventory.renderTable(inventory.currentRawData);
        } catch (e) {
            console.error("Inventory load error:", e);
            api.showToast("Lỗi kết nối hoặc chưa có API GET /api/inventory/all", "warning");
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
            tbody.html('<tr><td colspan="5" class="text-center py-4 text-muted">No inventory data available</td></tr>');
            return;
        }

        data.forEach(item => {
            const bgClass = item.isLowStock ? 'bg-danger bg-opacity-10' : '';
            const statusBadge = item.isLowStock ? '<span class="badge bg-danger">Low Stock</span>' : '<span class="badge bg-success">In Stock</span>';

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
                        <div class="extra-small text-muted">Ref Price: ${api.formatCurrency(item.giaBan)}</div>
                    </td>
                    <td>${statusBadge} <br><span class="extra-small text-muted">Min Threshold: 5</span></td>
                    <td class="text-end pe-4">
                        <button onclick="inventory.editStock('${item.isbn}')" class="btn btn-sm btn-light rounded-3 me-1">Edit</button>
                    </td>
                </tr>
            `);
        });
    },

    // 3. Đổ dữ liệu vào Modal (Thay vì nhảy trang sửa sách)
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

    // 4. Lưu dữ liệu kho
    saveStock: async () => {
        const isbn = $("#edit-inv-isbn").val();
        const data = {
            tonKho: parseInt($("#edit-inv-stock").val()) || 0,
            viTriKe: $("#edit-inv-shelf").val().trim()
        };

        try {
            // Lưu ý: Nếu backend bạn có API riêng để cập nhật kho thì sửa URL ở đây
            // Ví dụ: await api.put(`/inventory/${isbn}`, data);
            await api.put(`/admin/books/${isbn}`, data); 
            
            api.showToast("Inventory updated successfully!", "success");
            bootstrap.Modal.getInstance(document.getElementById('inventoryEditModal')).hide();
            inventory.loadList();
        } catch (e) {
            console.error(e);
            api.showToast("Failed to update inventory", "error");
        }
    },

    // 2. Import Goods logic
    addImportRow: () => {
        const tbody = $("#import-items-table tbody");
        const rowId = Date.now();
        tbody.append(`
            <tr id="row-${rowId}" class="import-row">
                <td>
                    <input type="text" class="form-control form-control-sm border-0 bg-light rounded-pill px-3 d-isbn fw-bold text-accent" onchange="inventory.fetchBookInfo(this, ${rowId})">
                    <div id="book-name-${rowId}" class="extra-small text-muted ps-3 mt-1"></div>
                </td>
                <td><input type="number" class="form-control form-control-sm border-0 bg-light rounded-pill px-3 d-qty" value="1" oninput="inventory.calcImportSubtotal()"></td>
                <td><input type="number" class="form-control form-control-sm border-0 bg-light rounded-pill px-3 d-unitPrice" value="0" oninput="inventory.calcImportSubtotal()"></td>
                <td><input type="text" class="form-control form-control-sm border-0 bg-light rounded-pill px-3 d-shelf" placeholder="VD: Kệ A1"></td>
                <td class="fw-bold text-accent d-subtotal align-middle text-end">0đ</td>
                <td class="text-center">
                    <button class="btn btn-sm text-danger" onclick="$('#row-${rowId}').remove(); inventory.calcImportSubtotal();">
                        <i class="icon icon-trash"></i>
                    </button>
                </td>
            </tr>
        `);
    },

    // Hàm phụ trợ: Tự động gọi API lấy tên sách khi điền xong ISBN
    fetchBookInfo: async (inputElem, rowId) => {
        const isbn = $(inputElem).val().trim();
        const nameDiv = $(`#book-name-${rowId}`);
        const row = $(`#row-${rowId}`); // Lấy đối tượng dòng
        if (!isbn) { nameDiv.text(""); return; }

        nameDiv.html('<i class="fas fa-spinner fa-spin"></i> Đang tìm...');
        try {
            const res = await api.get(`/books/${isbn}`);
            const bookInfo = res.data?.data || res.data;
            const title = bookInfo.title || bookInfo.tenSach;

            if (bookInfo && title) {
                // QUAN TRỌNG: Phải gán data-title để lúc processImport nó bốc đi
                row.attr('data-title', title); 
                nameDiv.html(`<span class="text-success fw-bold"><i class="fas fa-check-circle"></i> ${title}</span>`);
            } else {
                row.attr('data-title', ''); // Reset nếu không thấy
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

    // Hàm bổ trợ để thủ kho nhập tên sách nhanh ngay trên bảng nhập kho
    quickAddBookName: (rowId) => {
        const name = prompt("Nhập tên cho cuốn sách mới này:");
        if (name) {
            // Lưu tên vào thuộc tính data-title của dòng để tí nữa processImport bốc đi
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
             // Bóc tách đúng lớp data
             const suppliersData = res.data?.data || res.data || []; 
             const sel = $("#supplier-select");
             sel.empty().append('<option value="">--- Chọn Nhà Cung Cấp ---</option>');
             
             suppliersData.forEach(s => {
                  // SỬA Ở ĐÂY: Dùng đúng key supplierId và supplierName
                  sel.append(`<option value="${s.supplierId}">${s.supplierName}</option>`);
             });
        } catch(e) {
             console.error("Lỗi tải NCC:", e);
        }
    },

    processImport: async () => {
        const supplierId = $("#supplier-select").val();
        if (!supplierId) {
             api.showToast("Warehouse Alert: Supplier selection required!", "warning");
             return;
        }

        const items = [];
        $(".import-row").each(function() {
            const isbn = $(this).find('.d-isbn').val()?.trim();
            const title = $(this).attr('data-title') || ""; // LẤY TÊN SÁCH ĐÃ ĐẶT
            const quantity = parseInt($(this).find('.d-qty').val()) || 0;
            const unitPrice = parseFloat($(this).find('.d-unitPrice').val()) || 0;
            const shelfLocation = $(this).find('.d-shelf').val() || "Chưa xếp kệ"; // Lấy vị trí từ UI

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
             api.showToast("Empty form! Please enter book details", "error"); return;
        }

        const dto = {
             supplierId: parseInt(supplierId),
             staffId: api.getUser()?.id || 1, // Fallback to 1 for now
             details: items
        };

        api.showToast("Initiating Import Transaction...", "info");
        try {
             const res = await api.post('/inventory/import', dto);
             api.showToast("✓ Batch recorded successfully!", "success");
             setTimeout(() => { layout.render('Inventory/Admin', 'Index'); }, 1500);
        } catch(e) {
             api.showToast("Import failed: " + e.message, "error");
        }
    },

    // 3. Export Goods logic
    processExport: async () => {
        const orderId = $("#export-order-id").val()?.trim();
        if(!orderId) {
            api.showToast("Please enter Order ID to track.", "warning"); return;
        }

        const logView = $("#export-result-log");
        logView.append(`<br><br>> Checking Order ID: <b>${orderId}</b>...`);

        try {
             const dto = { orderId: orderId, note: "Auto-logistic export via Admin Dashboard" };
             await api.post('/inventory/export', dto);
             logView.removeClass("text-muted text-danger text-warning").addClass("text-success fw-bold");
             logView.append(`<br>> ✓ Stock verification: VALID.`);
             logView.append(`<br>> ✓ Inventory deducted: SUCCESS.`);
             api.showToast("Goods have been successfully deducted from inventory!");
        } catch(e) {
             logView.removeClass("text-muted text-success text-warning").addClass("text-danger fw-bold");
             logView.append(`<br>> ✗ CHECK FAILED (ROLLBACK). Error: ${e.message}`);
             api.showToast("Warehouse operational failure", "error");
        }
    },

    // --- PHẦN XỬ LÝ XUẤT KHO ---
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

            // LOG ĐỂ SOI KÈO: Bạn mở Console (F12) xem cái item này có gì nhé
            console.log("Cấu trúc 1 dòng sản phẩm:", data.orderDetails[0]);

            inventory.exportOrderData = {
                orderId: data.orderId || data.id,
                items: data.orderDetails.map(item => {
                    // CÁCH FIX: Kiểm tra đa tầng để tránh lỗi undefined
                    const bookObj = item.book || item.sach || item; // Nếu không có .book thì lấy chính item (kiểu flat)
                    
                    return {
                        isbn: bookObj.isbn || "N/A",
                        // Ưu tiên tenSach (vì bạn D hay dùng JsonProperty), sau đó đến title
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
            console.error("Lỗi chi tiết:", e);
            api.showToast("Không thể tải đơn hàng. Kiểm tra Console!", "error");
        }
    },

    startExportScanner: () => {
        // Nếu đã có scanner rồi thì xóa đi tạo mới để tránh lỗi lồng camera
        if (inventory.exportScanner) {
            inventory.exportScanner.clear().catch(e => console.log(e));
        }

        inventory.exportScanner = new Html5QrcodeScanner(
            "export-reader", // ID của thẻ div trong Export.html
            { 
                fps: 10, 
                qrbox: { width: 250, height: 120 },
                supportedScanTypes: [Html5QrcodeScanType.SCAN_TYPE_CAMERA]
            }, 
            false
        );

        inventory.exportScanner.render(
            (decodedText) => {
                // Khi quét thành công, gọi hàm xử lý đối soát
                inventory.handleExportScan(decodedText);
            },
            (error) => { /* Bỏ qua lỗi quét không thấy mã */ }
        );
    },

    handleExportScan: (isbn) => {
        if (!inventory.exportOrderData) return;
        
        const item = inventory.exportOrderData.items.find(i => i.isbn === isbn);
        if (item) {
            if (item.scannedQty < item.requiredQty) {
                item.scannedQty++;
                api.showToast(`Đã xác nhận: ${item.title}`, "success");
                inventory.renderExportTable();
            } else {
                api.showToast("Sản phẩm này đã quét đủ số lượng!", "warning");
            }
        } else {
            api.showToast(`Lỗi: Sách mã ${isbn} không có trong đơn hàng này!`, "error");
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
                        ${isFinished ? '<span class="badge bg-success">Đủ hàng</span>' : '<span class="badge bg-light text-dark border">Chờ quét...</span>'}
                    </td>
                </tr>
            `);
        });

        const totalReq = inventory.exportOrderData.items.reduce((a, b) => a + b.requiredQty, 0);
        const totalScan = inventory.exportOrderData.items.reduce((a, b) => a + b.scannedQty, 0);
        $("#export-progress").text(`${totalScan}/${totalReq}`);
        
        $("#btn-confirm-export").prop('disabled', !allDone);
    },

    // 4. Shipping Tracking Logic
    trackOrder: async (trackingId) => {
        if (!trackingId) return;
        api.showToast("Collecting GPS tracking data...", "info");
        try {
            const res = await api.get(`/shipping/track/${trackingId}`);
            const data = res.data || res;
            if (data && data.trackingNumber) {
                $("#tracking-result-container").fadeIn(400);
                $("#tracking-display-id").text("Tracking #: " + data.trackingNumber);
                
                const latestStatus = data.statusHistory[data.statusHistory.length - 1]?.status || 'Initialized';
                $("#tracking-status-badge").text(latestStatus);
                
                inventory.renderTrackingTimeline(data);
                api.showToast("Real-time tracking successful!", "success");
            }
        } catch (e) {
            api.showToast("Tracking number not found in Logistic system", "error");
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
                    <div class="extra-small opacity-75 fw-medium">Location: ${step.location}</div>
                </div>
            `;
        });
        container.html(html);
    },
    scanIsbn: async function() {
        const isbnInput = document.getElementById('isbnInput');
        const resultArea = document.getElementById('scanResultArea');
        const btnScan = document.getElementById('btnScan');
        const isbn = isbnInput.value.trim();

        if (!isbn) {
            api.showToast('Please enter or scan ISBN', 'warning');
            return;
        }

        // 1. Hiển thị trạng thái loading theo quy chuẩn UI
        btnScan.innerHTML = '<div class="spinner-border spinner-border-sm text-accent"></div> Đang xử lý...';
        btnScan.disabled = true;
        resultArea.classList.add('d-none'); // Ẩn kết quả cũ

        try {
            // 2. Gọi API tra cứu tồn kho (GET /api/inventory/scan/{isbn})
            const response = await api.get(`/inventory/scan/${isbn}`);
            
            // Giả định response.success là cách bọc kết quả của ApiResponse<T>
            if (response && response.data) { 
                const data = response.data;
                
                // 3. Render Card thông tin sách trực tiếp không cần load lại trang
                resultArea.innerHTML = `
                    <div class="card bg-light border-0 rounded-4">
                        <div class="card-body d-flex align-items-center p-3">
                            <div>
                                <h5 class="fw-bold mb-1">${data.tenSach}</h5>
                                <p class="text-muted mb-2">Mã ISBN: <strong>${data.isbn}</strong></p>
                                <div class="d-flex gap-3 mt-2">
                                    <span class="badge bg-white text-dark border p-2 rounded-3">
                                        <i class="fas fa-box text-accent me-1"></i> Tồn kho: 
                                        <span class="${data.tonKho < 10 ? 'text-danger' : 'text-success'} fw-bold fs-6">${data.tonKho}</span> cuốn
                                    </span>
                                    <span class="badge bg-white text-dark border p-2 rounded-3">
                                        <i class="fas fa-map-marker-alt text-accent me-1"></i> Vị trí kệ: <strong>${data.viTriKe || 'Chưa xác định'}</strong>
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                `;
                resultArea.classList.remove('d-none');
                api.showToast('Tra cứu thành công!', 'success');
            } else {
                // Xử lý khi không tìm thấy sách
                resultArea.innerHTML = `<div class="alert alert-danger rounded-4 mb-0"><i class="fas fa-exclamation-triangle me-2"></i>Không tìm thấy thông tin trong kho cho mã ISBN: <strong>${isbn}</strong></div>`;
                resultArea.classList.remove('d-none');
            }
        } catch (error) {
            console.error("Lỗi scan barcode:", error);
            
            // Lấy câu chửi từ backend (do common.js truyền ra)
            let errorMsg = error.message || 'Mã vạch không hợp lệ hoặc không có trong hệ thống!';
            
            // Dọn dẹp bớt mấy chữ tiếng Anh thừa của Spring Boot (nếu có) cho UI đẹp hơn
            errorMsg = errorMsg.replace('Warning: ', '').replace('Logic Error: ', '');

            // Hiển thị Toast đỏ (common.js đã lo CSS)
            api.showToast(errorMsg, 'error');

            // Render lỗi ra khu vực quét mã để thủ kho nhìn rõ
            resultArea.innerHTML = `
                <div class="alert alert-danger rounded-4 mb-0 border-0 shadow-sm d-flex align-items-center p-3">
                    <div class="bg-danger bg-opacity-10 p-2 rounded-circle me-3">
                        <i class="fas fa-exclamation-triangle fs-4 text-danger"></i>
                    </div>
                    <div>
                        <h6 class="fw-bold mb-1 text-danger">Tra cứu thất bại</h6>
                        <p class="mb-0 small text-dark">${errorMsg}</p>
                    </div>
                </div>`;
            resultArea.classList.remove('d-none');
            
        } finally {
            // Phục hồi nút bấm
            btnScan.innerHTML = 'Tra cứu';
            btnScan.disabled = false;
            isbnInput.select();
        }
    },

    // --- KHỐI LOGIC CHUYỂN TAB VÀ CAMERA CHO IMPORT ---
    importScanner: null,

    // Hàm chuyển đổi qua lại giữa 3 Tab
    switchImportTab: function(tabName) {
        // 1. Reset màu toàn bộ nút về viền trắng (outline)
        $('#btn-tab-manual, #btn-tab-camera, #btn-tab-ocr').removeClass('btn-accent fw-bold').addClass('btn-outline-accent');
        
        // 2. Ẩn toàn bộ 3 khu vực
        $('#import-manual-area, #import-camera-area, #import-ocr-area').addClass('d-none');

        // 3. Nếu chuyển khỏi Tab Camera thì phải tắt đèn Camera đi cho đỡ tốn pin
        if (tabName !== 'camera') {
            this.closeImportScanner();
        }

        // 4. Bật màu nút và khu vực tương ứng
        if (tabName === 'manual') {
            $('#btn-tab-manual').removeClass('btn-outline-accent').addClass('btn-accent fw-bold');
            $('#import-manual-area').removeClass('d-none');
        } 
        else if (tabName === 'camera') {
            $('#btn-tab-camera').removeClass('btn-outline-accent').addClass('btn-accent fw-bold');
            $('#import-camera-area').removeClass('d-none');
            this.openImportScanner(); // Bật camera
        } 
        else if (tabName === 'ocr') {
            $('#btn-tab-ocr').removeClass('btn-outline-accent').addClass('btn-accent fw-bold');
            $('#import-ocr-area').removeClass('d-none');
        }
    },

    openImportScanner: function() {
        if (!this.importScanner) {
            this.importScanner = new Html5QrcodeScanner(
                "import-reader", 
                { fps: 10, qrbox: {width: 250, height: 100}, supportedScanTypes: [Html5QrcodeScanType.SCAN_TYPE_CAMERA] }, 
                false
            );
        }
        this.importScanner.render(
            this.onImportScanSuccess.bind(this), 
            (error) => {} // Bỏ qua lỗi khung rỗng
        );
    },

    closeImportScanner: function() {
        if (this.importScanner) {
            this.importScanner.clear().catch(e => console.log("Camera clear delay"));
        }
    },

    // Khi quét thành công
    onImportScanSuccess: function(decodedText) {
        api.showToast(`Đã nhận diện mã: ${decodedText}`, 'success');

        // KIỂM TRA: Xem mã ISBN này đã có trong bảng nhập chưa?
        let existingRow = null;
        $(".import-row").each(function() {
            if ($(this).find('.d-isbn').val() === decodedText) {
                existingRow = $(this);
            }
        });

        if (existingRow) {
            // NẾU CÓ RỒI: Tăng số lượng lên 1
            let qtyInput = existingRow.find('.d-qty');
            qtyInput.val(parseInt(qtyInput.val()) + 1);
            inventory.calcImportSubtotal();
            
            // Highlight dòng đó lên cho user dễ nhìn
            existingRow.addClass('bg-warning bg-opacity-25');
            setTimeout(() => existingRow.removeClass('bg-warning bg-opacity-25'), 600);
        } else {
            // NẾU CHƯA CÓ: Tìm dòng trống cuối cùng để điền, hoặc tạo dòng mới
            let lastRow = $(".import-row").last();
            if (lastRow.find('.d-isbn').val().trim() !== '') {
                this.addImportRow();
                lastRow = $(".import-row").last();
            }
            
            // Điền mã và tự gọi tên sách
            lastRow.find('.d-isbn').val(decodedText).trigger('change');
            
            // CHỈNH SỬA Ở ĐÂY: Focus con trỏ chuột thẳng vào ô Số lượng (d-qty)
            setTimeout(() => {
                lastRow.find('.d-qty').focus().select();
            }, 300);
        }
        
        // Chuyển về màn hình Manual để thủ kho nhìn thấy dòng vừa quét
        this.switchImportTab('manual');
    },
    
    // Hàm này cần được gọi trong layout-manager.js -> initViewLogic()
    initScannerEvent: function() {
        const btnScan = document.getElementById('btnScan');
        const isbnInput = document.getElementById('isbnInput');
        
        if (btnScan && isbnInput) {
            // Lắng nghe sự kiện click nút
            btnScan.addEventListener('click', () => this.scanIsbn());
            
            // Máy quét mã vạch (Barcode Scanner hardware) thường mô phỏng thao tác gõ phím và tự động gửi phím "Enter" ở cuối.
            isbnInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    this.scanIsbn();
                }
            });
        }
    },

    connectMobileScanner: () => {
        // Thay 'localhost' bằng IP của máy bạn khi chạy thực tế
        const socket = new WebSocket("ws://localhost:8080/ws-barcode");

        socket.onmessage = (event) => {
            const isbn = event.data;
            api.showToast("📱 Nhận mã từ điện thoại: " + isbn, "info");

            // TỰ ĐỘNG ĐIỀN: Nếu đang ở màn hình Xuất kho, tự điền vào ô Order ID
            if ($("#export-order-id").length) {
                $("#export-order-id").val(isbn);
                inventory.loadOrderForExport(); // Tự gọi hàm load luôn cho "pro"
            }
            
            // Nếu đang ở màn hình Nhập kho, tự điền vào dòng ISBN cuối cùng
            const lastIsbnInput = $(".d-isbn").last();
            if (lastIsbnInput.length && !lastIsbnInput.val()) {
                lastIsbnInput.val(isbn).trigger('change');
            }
        };

        socket.onopen = () => console.log("Connected to Mobile Scanner Server");
    };
};
