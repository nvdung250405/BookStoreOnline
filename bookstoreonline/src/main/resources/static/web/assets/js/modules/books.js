/**
 * books.js - Product Management & AI Search
 * Standardized for Full English Backend synchronization
 */

let currentBookIsbn = null;

const books = {
    // 1. Fetch and display featured books on homepage
    loadFeatured: async () => {
        try {
            const res = await api.get('/books');
            const itemList = res.data || res;
            if (Array.isArray(itemList)) {
                books.renderGrid("#featured-books-container", itemList.slice(0, 8));
                books.renderPopular("#popular-books-container", itemList.slice(0, 8));
                // Populate best seller
                if (itemList.length > 0) {
                    books.renderBestSeller(itemList[0]);
                }
            }
        } catch (error) {
            console.error("Failed to load featured books", error);
        }
    },

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

    // 1c. Render popular books grid (for Home page tabs)
    renderPopular: (selector, items) => {
        const container = $(selector);
        if (!container.length) return;
        container.empty();
        if (!items || items.length === 0) return;
        items.forEach(book => {
            const imagePath = book.coverImage ? `assets/images/${book.coverImage}` : 'assets/images/product-item1.jpg';
            container.append(`
                <div class="col-xl-3 col-lg-4 col-sm-6 mb-4">
                    <div class="product-item bg-white p-3 rounded-4 shadow-sm h-100 text-center transition-all hvr-float border-0">
                        <a href="javascript:void(0)" onclick="layout.render('Books','Details','${book.isbn}')" class="d-block image-holder position-relative mb-3 overflow-hidden rounded-3 bg-light p-2 book-img-link" style="cursor:pointer;">
                            <img src="${imagePath}" alt="${book.title}" class="img-fluid book-cover-img" style="height:200px;object-fit:contain;transition:transform 0.35s cubic-bezier(0.25,0.46,0.45,0.94);">
                            <div class="book-img-overlay position-absolute top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center" style="background:rgba(47,47,47,0);transition:background 0.3s ease;border-radius:inherit;">
                                <span class="text-white fw-bold small" style="opacity:0;transition:opacity 0.3s ease;letter-spacing:0.05em;">Xem chi tiết</span>
                            </div>
                        </a>
                        <h6 class="fw-bold mb-1"><a href="javascript:void(0)" onclick="layout.render('Books','Details','${book.isbn}')" class="text-decoration-none text-dark">${book.title}</a></h6>
                        <div class="fw-bold text-accent mb-2">${api.formatCurrency(book.price)}</div>
                        <button class="btn btn-outline-dark btn-sm rounded-pill px-3" onclick="cart.add('${book.isbn}',1)"><i class="icon icon-plus me-1"></i>Thêm vào giỏ</button>
                    </div>
                </div>
            `);
        });
    },

    // 2. AI Search Integration
    searchAI: async (query) => {
        if (!query || !query.trim()) return;
        query = query.trim();

        layout.current.isSearching = true;
        if (layout.current.area !== 'Books' || layout.current.view !== 'Index') {
            await layout.render('Books', 'Index');
        }

        const grid = $("#books-grid");
        grid.html(`
            <div class="col-12 text-center py-5">
                <div class="spinner-border text-accent mb-3" style="width:2.5rem;height:2.5rem;"></div>
                <p class="text-muted">AI đang tìm kiếm: <strong>"${$('<div>').text(query).html()}"</strong></p>
            </div>
        `);

        try {
            const res = await api.post('/books/ai-search', { query });
            const results = res.data || [];
            books.renderGrid("#books-grid", results);
        } catch (error) {
            console.error("AI Search error:", error);
        }
    },

    // 3. Admin Management
    loadAdminList: async () => {
        try {
            const res = await api.get('/books');
            const data = res.data || res;
            const tbody = $("#books-admin-list");
            if (!tbody.length) return;
            tbody.empty();

            data.forEach(book => {
                const typeBadge = book.bookType === 'EBOOK'
                    ? '<span class="badge bg-info text-dark rounded-pill px-2">EBOOK</span>'
                    : '<span class="badge bg-secondary rounded-pill px-2">PHYSICAL</span>';

                tbody.append(`
                    <tr>
                        <td class="ps-4 py-4">
                            <div class="d-flex align-items-center gap-3">
                                <img src="${book.coverImage ? 'assets/images/' + book.coverImage : 'assets/images/product-item1.jpg'}" class="rounded shadow-sm" width="50">
                                <div>
                                    <h6 class="fw-bold mb-0">${book.title}</h6>
                                    <div class="d-flex align-items-center gap-2 mt-1">
                                        <small class="text-muted">ISBN: ${book.isbn}</small>
                                        ${typeBadge}
                                    </div>
                                </div>
                            </div>
                        </td>
                        <td>${book.categoryName || '---'}</td>
                        <td class="fw-bold text-accent">${api.formatCurrency(book.price)}</td>
                        <td>${book.stockQuantity || 0}</td>
                        <td class="text-end pe-4">
                            <div class="btn-group gap-2">
                                <button class="btn btn-sm btn-light rounded-pill shadow-sm py-1 px-3" onclick="layout.render('Books/Admin', 'Edit', '${book.isbn}')">Edit</button>
                                <button class="btn btn-sm btn-outline-danger rounded-pill shadow-sm" onclick="books.delete('${book.isbn}')"><i class="icon icon-close"></i></button>
                            </div>
                        </td>
                    </tr>
                `);
            });
        } catch (e) {
            api.showToast("Không thể tải danh sách sách quản trị", "error");
        }
    },

    // 4. Load specialized detail view
    loadDetail: async (isbn) => {
        try {
            const res = await api.get(`/books/${isbn}`);
            const book = res.data || res;
            if (!book) {
                api.showToast("Không tìm thấy thông tin sách", "error");
                return;
            }

            currentBookIsbn = book.isbn;
            
            // Populate DOM
            $('#book-title').text(book.title);
            $('#book-author').text(book.authorNames ? Array.from(book.authorNames).join(', ') : 'Chưa cập nhật');
            $('#book-isbn').text(book.isbn);
            $('#book-price').text(api.formatCurrency(book.price));
            $('#book-price-old').text(api.formatCurrency(book.price * 1.2)); // Fake old price for UI
            $('#book-description').text(book.description || 'Không có mô tả cho sản phẩm này.');
            $('#breadcrumb-category').text(book.categoryName || 'Sách');
            
            const imagePath = book.coverImage ? `assets/images/${book.coverImage}` : 'assets/images/main-banner2.jpg';
            $('#book-image').attr('src', imagePath);

            // Load related books based on category
            if (book.categoryName) {
                books.loadRelated(book.categoryName, book.isbn);
            }
        } catch (error) {
            console.error("Failed to load book details", error);
            api.showToast("Lỗi khi tải chi tiết sách", "error");
        }
    },

    // 5. Load all books (Store Index)
    loadAll: async () => {
        try {
            const res = await api.get('/books');
            const data = res.data || res;
            books.renderGrid("#books-grid", data);
        } catch (error) {
            api.showToast("Không thể tải danh sách sản phẩm", "error");
        }
    },

    // 6. Load related products
    loadRelated: async (categoryName, excludeIsbn) => {
        try {
            const res = await api.get(`/books?categoryName=${encodeURIComponent(categoryName)}`);
            const data = res.data || res;
            const filtered = data.filter(b => b.isbn !== excludeIsbn).slice(0, 4);
            
            const container = $("#related-books-grid");
            if (!container.length) return;
            container.empty();
            
            if (filtered.length === 0) {
                container.html('<div class="col-12 text-center py-4 text-muted">Không có sản phẩm liên quan.</div>');
                return;
            }

            filtered.forEach(book => {
                const imagePath = book.coverImage ? `assets/images/${book.coverImage}` : 'assets/images/product-item1.jpg';
                container.append(`
                    <div class="col-md-3">
                        <div class="product-item bg-white p-3 rounded-4 shadow-sm h-100 text-center transition-all hvr-float border-0">
                            <a href="javascript:void(0)" onclick="layout.render('Books','Details','${book.isbn}')" class="d-block image-holder mb-3 overflow-hidden rounded-3 bg-light p-2">
                                <img src="${imagePath}" class="img-fluid" style="height:180px;object-fit:contain;">
                            </a>
                            <h6 class="fw-bold mb-1 small">${book.title}</h6>
                            <div class="text-accent fw-bold mb-2">${api.formatCurrency(book.price)}</div>
                            <button class="btn btn-outline-dark btn-sm rounded-pill px-3" style="font-size: 0.7rem;" onclick="cart.add('${book.isbn}',1)">Thêm vào giỏ</button>
                        </div>
                    </div>
                `);
            });
        } catch (e) { console.error("Load related books failed", e); }
    },

    // 7. Filtering and Sorting
    handleSort: (criteria) => {
        const grid = $("#books-grid");
        const items = grid.children('.col-xl-3').get();
        if (items.length === 0) return;

        items.sort((a, b) => {
            const priceA = parseFloat($(a).find('.text-accent').first().text().replace(/\D/g,'')) || 0;
            const priceB = parseFloat($(b).find('.text-accent').first().text().replace(/\D/g,'')) || 0;
            
            if (criteria === 'price-asc') return priceA - priceB;
            if (criteria === 'price-desc') return priceB - priceA;
            return 0;
        });
        
        grid.empty().append(items);
    },

    loadByPriceRange: async (min, max) => {
        try {
            const res = await api.get(`/books?minPrice=${min}&maxPrice=${max}`);
            const data = res.data || res;
            books.renderGrid("#books-grid", data);
            $("#category-title").text(`Sách giá từ ${api.formatCurrency(min)} đến ${api.formatCurrency(max)}`);
        } catch (e) { 
            console.error("Price filter error:", e);
            api.showToast("Không thể lọc sản phẩm theo giá", "error"); 
        }
    },

    toggleTypeFields: () => {
        const type = $('input[name="bookType"]:checked').val();
        if (type === 'PHYSICAL') {
            $('#physical-fields-section').removeClass('d-none');
            $('#ebook-fields-section').addClass('d-none');
            $('#physical-fields-section input').attr('required', true);
            $('#ebook-fields-section input').removeAttr('required');
        } else {
            $('#physical-fields-section').addClass('d-none');
            $('#ebook-fields-section').removeClass('d-none');
            $('#ebook-fields-section input').attr('required', true);
            $('#physical-fields-section input').removeAttr('required');
        }
    },

    create: async () => {
        const form = $("#create-book-form");
        if (!form[0].checkValidity()) { form[0].reportValidity(); return; }

        const raw = {};
        form.serializeArray().forEach(item => { raw[item.name] = item.value; });

        const payload = {
            isbn: raw.isbn,
            title: raw.tenSach || raw.title,
            price: parseFloat(raw.giaNiemYet || raw.price) || 0,
            categoryId: raw.maDanhMuc ? parseInt(raw.maDanhMuc) : (raw.categoryId ? parseInt(raw.categoryId) : null),
            publisherId: raw.maNxb ? parseInt(raw.maNxb) : (raw.publisherId ? parseInt(raw.publisherId) : null),
            description: raw.moTa || raw.description || '',
            coverImage: ($('#anhBia').val() || $('#edit-anhBia').val() || raw.anhBia || ''),
            coverAlt: ($('#coverAlt').val() || $('#edit-coverAlt').val() || raw.coverAlt || ''),
            authorIds: raw.maTacGia ? [parseInt(raw.maTacGia)] : (raw.authorId ? [parseInt(raw.authorId)] : []),
            bookType: raw.bookType,
            weight: raw.weight ? parseFloat(raw.weight) : null,
            fileSize: raw.fileSize ? parseFloat(raw.fileSize) : null,
            downloadUrl: raw.downloadUrl || null
        };

        console.log("Create Book Payload:", payload);
        api.showToast("Đang lưu thông tin sách...", "info");
        try {
            await api.post('/admin/books', payload);
            api.showToast("Đã thêm sách mới thành công!", "success");
            layout.render('Books', 'Admin/Index');
        } catch (e) { api.showToast("Lỗi khi thêm sách: " + e.message, "error"); }
    },

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
            await api.put(`/admin/books/${isbn}`, payload);
            api.showToast("Đã cập nhật sách thành công!", "success");
            layout.render('Books', 'Admin/Index');
        } catch (e) { api.showToast("Lỗi khi cập nhật: " + e.message, "error"); }
    },

    delete: async (isbn) => {
        if (!confirm("Bạn có chắc chắn muốn xóa cuốn sách này không?")) return;
        try {
            await api.delete(`/admin/books/${isbn}`);
            api.showToast("Đã xóa sản phẩm thành công");
            books.loadAdminList();
        } catch (e) {
            api.showToast("Xóa sản phẩm thất bại", "error");
        }
    },

    handleImageUpload: async (input) => {
        const file = input.files[0];
        if (!file) return;

        console.log("Starting upload for file:", file.name);

        // Instant Local Preview
        const objectUrl = URL.createObjectURL(file);
        const previewImg = $('#book-preview-img');
        const placeholder = $('#preview-placeholder');

        if (previewImg.length) {
            console.log("Updating preview image src");
            previewImg.attr('src', objectUrl).removeClass('d-none').show();
            if (placeholder.length) placeholder.addClass('d-none');
        } else {
            console.warn("Element #book-preview-img not found!");
        }

        // Show original filename info
        const originalInfo = $('#original-filename-info');
        if (originalInfo.length) {
            originalInfo.text(file.name);
            $('#file-info').removeClass('d-none');
        }

        // Show loading state
        const overlay = $('#upload-overlay');
        overlay.removeClass('d-none').addClass('d-flex');

        const formData = new FormData();
        formData.append('file', file);

        try {
            const response = await fetch('/api/admin/upload-image', {
                method: 'POST',
                headers: {
                    'Authorization': localStorage.getItem('token') ? 'Bearer ' + localStorage.getItem('token') : ''
                },
                body: formData
            });

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
            const res = await api.get('/categories');
            const data = res.data || res;
            const sel = $('#maDanhMuc') || $('#categoryId');
            if (!sel.length) return;
            sel.empty().append('<option value="">-- Chọn danh mục --</option>');

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
            const res = await api.get('/authors');
            const data = res.data || res;
            const sel = $('#maTacGia').length ? $('#maTacGia') : $('#authorId');
            if (!sel.length) return;
            sel.empty().append('<option value="">-- Chọn tác giả --</option>');
            (Array.isArray(data) ? data : []).forEach(a => {
                sel.append(`<option value="${a.authorId}">${a.authorName}</option>`);
            });
        } catch (e) { console.error("loadAuthors error:", e); }
    },

    loadPublishers: async () => {
        try {
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
        });
    },

    toggleAdvancedFilters: () => {
        api.showToast("Tính năng lọc nâng cao đang được phát triển", "info");
    },

    renderGrid: (selector, itemList) => {
        const container = $(selector);
        if (!container.length) return;
        container.empty();
        $("#product-count").text(itemList ? itemList.length : 0);
        if (!itemList || itemList.length === 0) {
            container.html('<div class="col-12 text-center py-5"><p class="text-muted">Không tìm thấy sản phẩm nào.</p></div>');
            return;
        }
        itemList.forEach(book => {
            const imagePath = book.coverImage ? `assets/images/${book.coverImage}` : 'assets/images/product-item1.jpg';
            container.append(`
                <div class="col-xl-3 col-lg-4 col-sm-6 mb-4">
                    <div class="product-item bg-white p-3 rounded-4 shadow-sm h-100 text-center transition-all hvr-float">
                        <a href="javascript:void(0)" onclick="layout.render('Books', 'Details', '${book.isbn}')" class="d-block image-holder mb-3 overflow-hidden rounded-3 bg-light p-2">
                            <img src="${imagePath}" class="img-fluid" style="height:200px;object-fit:contain;">
                        </a>
                        <h6 class="fw-bold mb-1">${book.title}</h6>
                        <div class="text-accent mb-2">${api.formatCurrency(book.price)}</div>
                        <button class="btn btn-outline-dark btn-sm rounded-pill" onclick="cart.add('${book.isbn}', 1)">+ Giỏ hàng</button>
                    </div>
                </div>
            `);
        });
    }
};

$(document).on('click', '#btn-save-book', () => books.create());
$(document).on('click', '#btn-update-book', function () {
    const isbn = $(this).data('isbn');
    books.update(isbn);
});
