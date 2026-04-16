/**
 * books.js - Product Management & AI Search
 * Unified logic for Admin & Public Customer Views
 */

let currentBookIsbn = null;

const books = {
    // --- PUBLIC VIEWS (Customer) ---
    
    // Load homepage content
    loadAll: async () => {
        console.log("books.loadAll triggered");
        // If we are on Books/Index.html (Store), load full list + sidebar
        if ($("#books-grid").length && $("#categories-filter-list").length) {
            books.loadStore();
        } else {
            // Default home view (Featured/Popular)
            books.loadFeatured();
        }
    },

    loadFeatured: async () => {
        try {
            const res = await api.get('/books');
            const itemList = api.parseResponse(res) || [];
            if (Array.isArray(itemList)) {
                books.renderGrid("#featured-books-container", itemList.slice(0, 8));
                books.renderPopular("#popular-books-container", itemList.slice(0, 8));
                if (itemList.length > 0) books.renderBestSeller(itemList[0]);
            }
        } catch (error) {
            console.error("Failed to load featured books", error);
        }
    },

    loadStore: async (categoryId = null) => {
        console.log("Loading store content...", { categoryId });
        try {
            // 1. Load Categories for Sidebar (Always run or check if loaded)
            await books.loadCategoriesSidebar();

            // 2. Load Books
            let url = '/books';
            if (categoryId) url = `/books/search?categoryId=${categoryId}&status=ACTIVE`;
            
            const res = await api.get(url);
            const itemList = api.normalizeList(res) || [];
            
            books.renderGrid("#books-grid", itemList);
            $("#product-count").text(itemList.length);

            // Update title if category filtered
            if (categoryId) {
                 const catItem = $(`#cat-item-${categoryId}`);
                 if (catItem.length) $("#category-title").text(catItem.text().trim());
            } else {
                $("#category-title").text("Tất cả sản phẩm");
            }

        } catch (error) {
            console.error("Failed to load store", error);
            $("#books-grid").html('<div class="col-12 text-center py-5 text-danger">Không thể tải danh sách sản phẩm. Vui lòng thử lại.</div>');
        }
    // 1d. Public Load all books & categories for Index page
    _allBooksCache: [],

    loadAll: async () => {
        const grid = $("#books-grid");
        const catList = $("#categories-filter-list");

        try {
            // Load Categories for sidebar
            const catRes = await api.get('/categories');
            const categoriesData = catRes.data || [];
            books.renderCategories(categoriesData);

            // Load Books
            grid.html(`
                <div class="col-12 text-center py-5">
                    <div class="spinner-border text-accent mb-3"></div>
                    <div class="text-muted">Đang tải danh sách sách...</div>
                </div>
            `);

            const res = await api.get('/books');
            books._allBooksCache = res.data || [];
            books.renderGrid("#books-grid", books._allBooksCache);

        } catch (error) {
            console.error("Failed to load books index", error);
            grid.html('<div class="col-12 text-center py-5 text-danger">Lỗi khi tải dữ liệu. Vui lòng thử lại.</div>');
        }
    },

    renderCategories: (data) => {
        const container = $("#categories-filter-list");
        if (!container.length) return;
        container.empty();

        container.append(`
            <li>
                <a href="javascript:void(0)" onclick="books.loadAll()" class="text-decoration-none text-dark d-flex justify-content-between align-items-center py-1 category-link active">
                    <span class="small fw-bold">Tất cả</span>
                    <i class="icon icon-chevron-right extra-small"></i>
                </a>
            </li>
        `);

        data.forEach(cat => {
            container.append(`
                <li>
                    <a href="javascript:void(0)" onclick="books.filterByCategory(${cat.categoryId}, '${cat.categoryName}')" class="text-decoration-none text-muted d-flex justify-content-between align-items-center py-1 category-link">
                        <span class="small">${cat.categoryName}</span>
                        <i class="icon icon-chevron-right extra-small" style="font-size:0.6rem;"></i>
                    </a>
                </li>
            `);
        });
    },

    filterByCategory: async (categoryId, categoryName) => {
        $("#category-title").text(categoryName || "Danh mục");
        $(".category-link").removeClass("active fw-bold text-dark").addClass("text-muted");
        $(event.currentTarget).addClass("active fw-bold text-dark").removeClass("text-muted");

        const grid = $("#books-grid");
        grid.html('<div class="col-12 text-center py-5"><div class="spinner-border text-accent"></div></div>');

        try {
            const res = await api.get(`/books?categoryName=${encodeURIComponent(categoryName)}`);
            books.renderGrid("#books-grid", res.data || []);
        } catch (e) {
            grid.html('<div class="col-12 text-center py-5 text-danger">Không thể lọc theo danh mục này.</div>');
        }
    },

    loadByPriceRange: async (min, max) => {
        const grid = $("#books-grid");
        grid.html('<div class="col-12 text-center py-5"><div class="spinner-border text-accent"></div></div>');
        try {
            const res = await api.get(`/books?minPrice=${min}&maxPrice=${max}`);
            books.renderGrid("#books-grid", res.data || []);
        } catch (e) {
            grid.html('<div class="col-12 text-center py-5 text-danger">Lỗi khi lọc theo giá.</div>');
        }
    },

    handleSort: (criteria) => {
        let sorted = [...books._allBooksCache];
        if (criteria === 'price-asc') {
            sorted.sort((a, b) => a.price - b.price);
        } else if (criteria === 'price-desc') {
            sorted.sort((a, b) => b.price - a.price);
        } else if (criteria === 'newest') {
            sorted.sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));
        }
        books.renderGrid("#books-grid", sorted);
    },

    loadDetail: async (isbn) => {
        // Implementation for details page initialization if needed via layout-manager
        // (Details are often handled by a separate call but good to have a placeholder)
    },


    // 1b. Render best seller section
    renderBestSeller: (book) => {
        if (!book) return;
        $('#best-seller-author').text(book.authorName || book.author || 'Tác giả');
        $('#best-seller-title').text(book.title || '---');
        $('#best-seller-desc').text(book.description ? book.description.substring(0, 150) + '...' : 'Miêu tả chưa có.');
        $('#best-seller-price').text(api.formatCurrency(book.price));
        $('#best-seller-link').attr('onclick', `layout.render('Books','Details','${book.isbn}')`);
    },

    loadCategoriesSidebar: async () => {
        const container = $("#categories-filter-list");
        if (!container.length) return;
        
        // Inject Compact Styles for Sidebar
        if (!document.getElementById('sidebar-compact-style')) {
            $('head').append(`
                <style id="sidebar-compact-style">
                    .cat-link { font-size: 0.85rem; border-radius: 6px; position: relative; transition: all 0.2s ease; }
                    .cat-parent-link { color: #444 !important; padding-left: 10px !important; }
                    .cat-parent-link:hover { color: #C5A992 !important; background: rgba(197, 169, 146, 0.05); padding-left: 15px !important; }
                    .cat-parent-link::before { content: "›"; position: absolute; left: 2px; opacity: 0; transition: all 0.2s; }
                    .cat-parent-link:hover::before { opacity: 1; left: 5px; }
                    
                    .cat-child-link { color: #777 !important; padding-left: 22px !important; }
                    .cat-child-link:hover { color: #C5A992 !important; background: rgba(0,0,0,0.02); padding-left: 27px !important; }
                    .cat-child-link::before { content: "•"; position: absolute; left: 10px; color: #dee2e6; transition: all 0.2s; }
                    .cat-child-link:hover::before { color: #C5A992; }
                    
                    .cat-active { background: #C5A992 !important; color: #fff !important; font-weight: 600 !important; box-shadow: 0 4px 10px rgba(197, 169, 146, 0.2); }
                    .cat-active::before { color: #fff !important; opacity: 1 !important; }
                    .sidebar-group-title { font-size: 0.65rem; color: #bbb; margin-bottom: 5px; margin-top: 15px; padding-left: 10px; }
                    .cat-main-all { background: #fdfdfd; border: 1px solid #f0f0f0; }
                </style>
            `);
        }

        try {
            const res = await api.get('/categories');
            const data = api.normalizeList(res) || [];
            container.empty();
            
            // 1. Compact Header
            container.append(`
                <li class="mb-3">
                    <a href="javascript:void(0)" onclick="books.loadStore(); $('.cat-link').removeClass('cat-active'); $(this).addClass('cat-active');" 
                       class="cat-link cat-main-all cat-active text-decoration-none d-flex align-items-center justify-content-between py-2 px-3 transition-all border shadow-sm">
                        <span class="fw-bold fw-serif small"><i class="icon icon-layers me-2"></i>Tất cả</span>
                        <span class="badge bg-light text-muted rounded-pill px-2" style="font-size:0.6rem">ALL</span>
                    </a>
                </li>
            `);

            // 2. Render Tree Compact
            const roots = data.filter(c => !c.parentId);
            roots.forEach(parent => {
                const parentId = parent.categoryId || parent.id;
                const parentName = parent.categoryName || parent.name;
                if (!parentId) return;
                
                // Parent acts as the 'All' link for this group
                container.append(`
                    <li class="mb-0 mt-2">
                        <a href="javascript:void(0)" id="cat-item-${parentId}" onclick="books.loadStore('${parentId}'); $('.cat-link').removeClass('cat-active'); $(this).addClass('cat-active');" 
                           class="cat-link cat-parent-link text-decoration-none d-block py-1 px-3 fw-bold text-uppercase" style="font-size: 0.75rem; letter-spacing: 0.5px;">
                            ${parentName}
                        </a>
                    </li>
                `);

                const subCats = parent.subCategories || [];
                if (Array.isArray(subCats)) {
                    subCats.forEach(child => {
                        const childId = child.categoryId || child.id;
                        const childName = child.categoryName || child.name;
                        container.append(`
                            <li class="mb-0">
                                <a href="javascript:void(0)" id="cat-item-${childId}" onclick="books.loadStore('${childId}'); $('.cat-link').removeClass('cat-active'); $(this).addClass('cat-active');" 
                                   class="cat-link cat-child-link text-decoration-none d-block py-1 pr-3">
                                    ${childName}
                                </a>
                            </li>
                        `);
                    });
                }
            });
        } catch (e) { 
            container.html('<div class="p-2 text-center text-muted small italic">Lỗi nạp danh mục</div>');
        }
    },

    loadDetail: async (isbn) => {
        if (!isbn) return;
        console.log("Loading details for ISBN:", isbn);
        try {
            const res = await api.get(`/books/${isbn}`);
            const book = res.data;
            if (!book) return;

            // Populate text elements
            $('#book-title').text(book.title);
            $('#book-author').text(book.authorName || 'Đang cập nhật');
            $('#book-isbn').text(book.isbn);
            $('#book-price').text(api.formatCurrency(book.price));
            $('#book-price-old').text(api.formatCurrency(book.price * 1.2));
            $('#book-description').text(book.description || 'Sản phẩm đang được cập nhật mô tả chi tiết...');
            $('#breadcrumb-category').text(book.categoryName || 'Sách');

            const imagePath = `assets/images/${book.coverImage || 'product-item1.jpg'}`;
            $('#book-image').attr('src', imagePath);
            currentBookIsbn = book.isbn;

            // Load Related Books
            books.loadRelated(book.categoryId);
            
            // Re-init AOS
            if (typeof AOS !== 'undefined') AOS.refresh();
        } catch (error) {
            console.error("Failed to load book details", error);
            api.showToast("Không thể tải thông tin chi tiết sách", "error");
        }
    },

    loadRelated: async (categoryId) => {
        if (!categoryId) return;
        try {
            const res = await api.get(`/books/search?categoryId=${categoryId}&status=ACTIVE`);
            const list = api.normalizeList(res) || [];
            // Hide current book if possible, filter by ISBN later
            books.renderGrid("#related-books-grid", list.slice(0, 4));
        } catch (e) {}
    },

    // --- RENDER HELPERS ---

    renderGrid: (selector, list) => {
        const container = $(selector);
        if (!container.length) return;
        container.empty();
        
        if (!list || list.length === 0) {
            container.html('<div class="col-12 text-center py-5 text-muted shadow-sm rounded-4 bg-light">Không tìm thấy sản phẩm nào phù hợp trong danh mục này.</div>');
            return;
        }

        list.forEach(book => {
            const imagePath = `assets/images/${book.coverImage || 'product-item1.jpg'}`;
            container.append(`
                <div class="col-xl-3 col-lg-4 col-sm-6 mb-4" data-aos="fade-up">
                    <div class="product-item bg-white p-3 rounded-4 shadow-sm h-100 text-center transition-all hvr-float border-0 shadow-hover position-relative">
                        <a href="javascript:void(0)" onclick="layout.render('Books','Details','${book.isbn}')" class="d-block image-holder mb-3 overflow-hidden rounded-3 bg-light p-2 shadow-sm" style="height:220px; display:flex; align-items:center; justify-content:center;">
                            <img src="${imagePath}" alt="${book.title}" class="img-fluid" style="max-height:100%; object-fit:contain; transition: transform 0.3s ease;">
                        </a>
                        <h6 class="fw-bold mb-1 font-serif text-dark truncate-2" style="min-height: 2.4em;">${book.title}</h6>
                        <div class="fw-bold text-accent mb-3 fs-5">${api.formatCurrency(book.price)}</div>
                        <button class="btn btn-outline-dark btn-sm rounded-pill px-3 py-2 w-100 fw-bold border-2" onclick="cart.add('${book.isbn}',1)">
                            <i class="icon icon-plus me-1"></i>Thêm vào giỏ
                        </button>
                    </div>
                </div>
            `);
        });
    },

    renderPopular: (selector, items) => {
        books.renderGrid(selector, items);
    },

    renderBestSeller: (book) => {
        if (!book) return;
        $('#best-seller-author').text(book.authorName || 'Tác giả');
        $('#best-seller-title').text(book.title || '---');
        $('#best-seller-desc').text(book.description ? book.description.substring(0, 150) + '...' : 'Miêu tả chưa có.');
        $('#best-seller-price').text(api.formatCurrency(book.price));
        $('#best-seller-link').attr('onclick', `layout.render('Books','Details','${book.isbn}')`);
    },

    // --- PUBLIC FILTERS ---

    loadByPriceRange: async (minPrice, maxPrice) => {
        api.showToast("Đang lọc theo giá...", "info");
        const grid = $("#books-grid");
        grid.html(`
            <div class="col-12 text-center py-5">
                <div class="spinner-border text-accent mb-3" style="width:2.5rem;height:2.5rem;"></div>
                <p class="text-muted">AI đang tìm kiếm: <strong>"${$('<div>').text(query).html()}"</strong></p>
            </div>
        `);

        try {
            const res = await api.get(`/books/search?minPrice=${minPrice}&maxPrice=${maxPrice}&status=ACTIVE`);
            const list = api.normalizeList(res) || [];
            books.renderGrid("#books-grid", list);
            $("#product-count").text(list.length);
            $("#category-title").text(`Sách từ ${api.formatCurrency(minPrice)}`);
        } catch (e) { api.showToast("Lọc theo giá thất bại", "error"); }
    },

    handleSort: async (sortBy) => {
        api.showToast("Tính năng sắp xếp đang được cập nhật", "info");
    },

    // --- ADMIN MANAGEMENT ---

    loadAdminList: async () => {
        try {
            const res = await api.get('/books');
            const data = api.parseResponse(res);
            books.renderAdminList(data);
        } catch (e) { console.error("Admin list error", e); }
    },

    renderAdminList: (data) => {
        try {
            const tbody = $("#books-admin-list");
            if (!tbody.length) return;
            tbody.empty();
            if (!data || data.length === 0) {
                tbody.html('<tr><td colspan="5" class="text-center py-5 text-muted">Không tìm thấy sách nào.</td></tr>');
                return;
            }
            data.forEach(book => {
                const typeBadge = book.bookType === 'EBOOK'
                    ? '<span class="badge bg-info text-dark rounded-pill px-2">EBOOK</span>'
                    : '<span class="badge bg-secondary rounded-pill px-2">PHYSICAL</span>';
                
                const statusOptions = [
                    { val: 'ACTIVE', label: 'Đang hoạt động' },
                    { val: 'INACTIVE', label: 'Ngừng kinh doanh' },
                    { val: 'DRAFT', label: 'Bản nháp' },
                    { val: 'OUT_OF_STOCK', label: 'Hết hàng' }
                ];
                let opts = '';
                statusOptions.forEach(o => {
                    const sel = book.status === o.val ? 'selected' : '';
                    opts += `<option value="${o.val}" ${sel}>${o.label}</option>`;
                });
                const sSelect = `<select class="form-select form-select-sm rounded-pill py-0 px-2" style="width:140px; font-size:0.75rem" onchange="books.quickUpdateStatus('${book.isbn}', this.value)">${opts}</select>`;
                

                tbody.append(`
                    <tr>
                        <td class="ps-4 py-4">
                            <div class="d-flex align-items-center gap-3">
                                <img src="assets/images/${book.coverImage || 'product-item1.jpg'}" class="rounded shadow-sm" width="50" onerror="this.src='../../assets/images/product-item1.jpg'">
                                <div><h6 class="fw-bold mb-0">${book.title}</h6><div class="d-flex align-items-center gap-2 mt-1"><small class="text-muted">ISBN: ${book.isbn}</small>${typeBadge}${sSelect}</div></div>
                            </div>
                        </td>
                        <td>${book.categoryName || '---'}</td>
                        <td class="fw-bold text-accent">${api.formatCurrency(book.price)}</td>
                        <td>${book.stockQuantity || 0}</td>
                        <td class="text-end pe-4"><div class="btn-group gap-2"><button class="btn btn-sm btn-light rounded-pill px-3" onclick="layout.render('Books/Admin', 'Edit', '${book.isbn}')">Edit</button><button class="btn btn-sm btn-outline-danger rounded-pill" onclick="books.delete('${book.isbn}')"><i class="icon icon-close"></i></button></div></td>
                    </tr>
                `);
            });
        } catch (e) {}
    },

    quickUpdateStatus: async (isbn, newStatus) => {
        api.showToast(`Đang cập nhật...`, "info");
        try {
            await api.request(`/admin/books/${isbn}/status?status=${newStatus}`, { method: 'PATCH' });
            api.showToast("Thành công!", "success");
        } catch (e) {
            api.showToast("Lỗi: " + e.message, "error");
            books.loadAdminList();
        }
    },

    toggleAdvancedFilters: () => {
        $("#advanced-filter-panel").slideToggle(300);
        if ($("#filter-category").children().length <= 1) books.loadFilterCategories();
    },

    applyFilters: async () => {
        const q = $("#book-search-input").val(), c = $("#filter-category").val(), s = $("#filter-status").val(), min = $("#filter-min-price").val(), max = $("#filter-max-price").val();
        api.showToast("Đang lọc...", "info");
        try {
            const p = new URLSearchParams();
            if (q) p.append('query', q);
            if (c && c !== "undefined") p.append('categoryId', c);
            if (s && s !== "undefined") p.append('status', s);
            if (min) p.append('minPrice', min);
            if (max) p.append('maxPrice', max);
            const res = await api.get(`/books/search?${p.toString()}`);
            books.renderAdminList(api.normalizeList(res));
            api.hideToast();
        } catch (e) { api.showToast("Lỗi lọc", "error"); }
    },

    loadFilterCategories: async () => {
    update: async (isbn) => {
        const form = $("#edit-book-form") || $("#create-book-form");
        if (!form[0].checkValidity()) { form[0].reportValidity(); return; }

        const raw = {};
        form.serializeArray().forEach(item => { raw[item.name] = item.value; });

        const payload = {
            title: raw.tenSach || raw.title,
            price: parseFloat(raw.giaNiemYet || raw.price) || 0,
            categoryId: raw.maDanhMuc ? parseInt(raw.maDanhMuc) : (raw.categoryId ? parseInt(raw.categoryId) : null),
            publisherId: raw.maNxb ? parseInt(raw.maNxb) : (raw.publisherId ? parseInt(raw.publisherId) : null),
            description: raw.moTa || raw.description || '',
            coverImage: raw.anhBia || raw.coverImage || '',
            coverAlt: raw.coverAlt || '',
            authorIds: raw.maTacGia ? [parseInt(raw.maTacGia)] : (raw.authorId ? [parseInt(raw.authorId)] : []),
            bookType: raw.bookType,
            weight: raw.weight ? parseFloat(raw.weight) : null,
            fileSize: raw.fileSize ? parseFloat(raw.fileSize) : null,
            downloadUrl: raw.downloadUrl || null
        };

        console.log("Update Book Payload:", payload);
        api.showToast("Đang cập nhật thông tin sách...", "info");
        try {
            const cats = api.normalizeList(await api.get('/categories')) || [];
            const sel = $("#filter-category");
            cats.forEach(c => {
                const id = c.id || c.categoryId, name = c.name || c.categoryName;
                if (id) sel.append(`<option value="${id}">${name}</option>`);
            });
        } catch (e) {}
    },

    adminSearch: (v) => {
        if (books._st) clearTimeout(books._st);
        books._st = setTimeout(() => books.applyFilters(), 400);
    },

    delete: async (isbn) => {
        if (!await api.confirm("Bạn có chắc chắn muốn xóa không?", "Xác nhận")) return;
        try {
            await api.delete(`/admin/books/${isbn}`);
            api.showToast("Đã xóa!", "success");
            books.loadAdminList();
        } catch (e) { api.showToast("Lỗi xóa", "error"); }
    },

    create: async () => {
        const f = $("#create-book-form"); if (!f[0].checkValidity()) { f[0].reportValidity(); return; }
        const r = {}; f.serializeArray().forEach(i => { r[i.name] = i.value; });
        const p = {
            isbn: r.isbn, title: r.tenSach, price: parseFloat(r.giaNiemYet),
            categoryId: parseInt(r.maDanhMuc), publisherId: parseInt(r.maNxb),
            description: r.moTa, coverImage: r.anhBia, authorIds: [parseInt(r.maTacGia)], bookType: r.bookType, status: r.status || 'DRAFT'
        };
        api.showToast("Đang tạo...", "info");
        try {
            await api.post('/admin/books', p);
            api.showToast("Thành công!", "success");
            setTimeout(() => { layout.render('Books/Admin', 'Index'); }, 800);
        } catch (e) { api.showToast("Lỗi", "error"); }
    },

    update: async (isbn) => {
        const f = $("#edit-book-form"); if (!f[0].checkValidity()) { f[0].reportValidity(); return; }
        const r = {}; f.serializeArray().forEach(i => { r[i.name] = i.value; });
        const p = {
            title: r.tenSach, price: parseFloat(r.giaNiemYet), categoryId: parseInt(r.maDanhMuc),
            publisherId: parseInt(r.maNxb), description: r.moTa, coverImage: r.anhBia,
            authorIds: [parseInt(r.maTacGia)], bookType: r.bookType, status: r.status
        };
        api.showToast("Đang lưu...", "info");
        try {
            await api.put(`/admin/books/${isbn}`, p);
            api.showToast("Thành công!", "success");
            setTimeout(() => { layout.render('Books/Admin', 'Index'); }, 800);
        } catch (e) { api.showToast("Lỗi", "error"); }
    },

    handleImageUpload: async (i) => {
        const file = i.files[0]; if (!file) return;
        try {
            const name = await books.uploadImage(file);
            $('#anhBia').val(name);
            $('#book-preview-img').attr('src', `assets/images/${name}`).removeClass('d-none').show();
            $('#preview-placeholder').addClass('d-none');
            api.showToast("Đã tải ảnh!", "success");
        } catch (e) { api.showToast("Lỗi tải ảnh", "error"); }
    },

    uploadImage: async (f) => {
        const fd = new FormData(); fd.append('file', f);
        const r = await api.request('/admin/upload-image', { method: 'POST', body: fd });
        return r.data?.fileName || f.name;
            const result = await response.json();
            if (result.status === 200 || result.status === 201) {
                const fileName = result.data.fileName;
                // Update hidden inputs (system fileName)
                $('#anhBia').val(fileName).trigger('input');
                $('#edit-anhBia').val(fileName).trigger('input');

                api.showToast("Tải ảnh lên thành công!", "success");
            } else {
                api.showToast("Lỗi: " + result.message, "error");
            }
        } catch (e) {
            console.error("Upload error:", e);
            api.showToast("Không thể kết nối đến máy chủ để tải ảnh", "error");
        } finally {
            overlay.addClass('d-none').removeClass('d-flex');
            // Cleanup object URL to avoid memory leaks
            // URL.revokeObjectURL(objectUrl); // Don't revoke yet as user might want to see it
            input.value = "";
        }
    },

    loadCategoriesIntoForm: async () => {
        try {
            const data = api.normalizeList(await api.get('/categories')) || [];
            const sel = $('#maDanhMuc'); if (!sel.length) return;
            sel.empty().append('<option value="">-- Chọn danh mục --</option>');
            data.forEach(c => { sel.append(`<option value="${c.id || c.categoryId}">${c.name || c.categoryName}</option>`); });
        } catch (e) {}

            const options = [];
            const walk = (nodes, depth = 0) => {
                (Array.isArray(nodes) ? nodes : []).forEach(n => {
                    options.push({ id: n.id || n.categoryId, name: n.name || n.categoryName, depth: depth });
                    if (n.subCategories) walk(n.subCategories, depth + 1);
                });
            };
            walk(Array.isArray(data) ? data : []);

            options.forEach(c => {
                const indent = "&nbsp;".repeat(c.depth * 4);
                sel.append(`<option value="${c.id}">${indent}${c.name}</option>`);
            });
        } catch (e) { console.warn('loadCategoriesIntoForm failed:', e.message); }
    },

    loadAuthors: async () => {
        try {
            const data = api.normalizeList(await api.get('/authors')) || [];
            const sel = $('#maTacGia'); if (sel.length) {
                sel.empty().append('<option value="">-- Chọn tác giả --</option>');
                data.forEach(a => { sel.append(`<option value="${a.authorId}">${a.authorName}</option>`); });
            }
        } catch (e) {}
    },

    loadPublishers: async () => {
        try {
            const data = api.normalizeList(await api.get('/publishers')) || [];
            const sel = $('#maNxb'); if (sel.length) {
                sel.empty().append('<option value="">-- Chọn NXB --</option>');
                data.forEach(p => { sel.append(`<option value="${p.publisherId}">${p.publisherName}</option>`); });
            const res = await api.get('/publishers');
            const data = res.data || res;
            const sel = $('#maNxb').length ? $('#maNxb') : $('#publisherId');
            if (!sel.length) return;
            sel.empty().append('<option value="">-- Chọn NXB --</option>');
            (Array.isArray(data) ? data : []).forEach(p => {
                sel.append(`<option value="${p.publisherId}">${p.publisherName}</option>`);
            });
        } catch (e) { console.error("loadPublishers error:", e); }
    },

    adminSearch: (query) => {
        const grid = $("#books-admin-list");
        if (!grid.length) return;

        const rows = grid.find("tr");
        rows.each(function () {
            const row = $(this);
            const text = row.text().toLowerCase();
            if (text.includes(query.toLowerCase())) {
                row.show();
            } else {
                row.hide();
            }
        } catch (e) {}
    },

    loadForEdit: async (isbn) => {
        if (!isbn) return;
        try {
            const res = await api.get(`/books/${isbn}`);
            const b = api.parseResponse(res); if (!b) return;
            const f = document.getElementById('edit-book-form'); if (!f) return;
            f.querySelector('input[name="tenSach"]').value = b.title || '';
            f.querySelector('input[name="isbn"]').value = b.isbn || isbn;
            f.querySelector('textarea[name="moTa"]').value = b.description || '';
            f.querySelector('input[name="giaNiemYet"]').value = b.price || '';
            f.querySelector('input[name="anhBia"]').value = b.coverImage || '';
            f.querySelector('select[name="status"]').value = b.status || 'ACTIVE';
            if (b.coverImage) {
                const img = document.getElementById('book-preview-img');
                if (img) { img.src = `assets/images/${b.coverImage}`; img.classList.remove('d-none'); document.getElementById('preview-placeholder')?.classList.add('d-none'); }
            }
            await books.loadCategoriesIntoForm(); await books.loadAuthors(); await books.loadPublishers();
            setTimeout(() => {
                f.querySelector('select[name="maDanhMuc"]').value = b.categoryId || "";
                f.querySelector('select[name="maTacGia"]').value = (b.authorIds && b.authorIds.length) ? b.authorIds[0] : "";
                f.querySelector('select[name="maNxb"]').value = b.publisherId || "";
            }, 500);
        } catch (e) { api.showToast("Lỗi", "error"); }
    }
};

$(document).on('click', '#btn-save-book', () => books.create());
