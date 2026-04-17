/**
 * books.js - Product Management & AI Search
 */

let currentBookIsbn = null;

const books = {
    // === PRIVATE STATE ===
    _allBooksCache: [],
    _categoriesCache: [],

    // === HELPERS ===
    _createBookCardHtml: (book, options = {}) => {
        const imagePath = book.coverImage ? `assets/images/${book.coverImage}` : 'assets/images/product-item1.jpg';
        const isPopular = options.isPopular || false;
        const columnClass = options.columnClass || "col-xl-3 col-lg-4 col-sm-6 mb-4";
        
        // Dynamic overlay text/style
        const overlayText = isPopular ? 'Xem nhanh' : 'Xem chi tiết';
        
        return `
            <div class="${columnClass}">
                <div class="product-item bg-white p-3 rounded-4 shadow-sm h-100 text-center transition-all hvr-float border-0">
                    <a href="javascript:void(0)" onclick="layout.render('Books','Details','${book.isbn}')" 
                       class="d-block image-holder position-relative mb-3 overflow-hidden rounded-3 bg-light p-2" style="cursor:pointer;">
                        <img src="${imagePath}" alt="${book.title}" class="img-fluid book-cover-img" 
                             style="height:${isPopular ? '200px' : '200px'}; object-fit:contain; transition:transform 0.35s ease;">
                        <div class="book-img-overlay position-absolute top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center" 
                             style="background:rgba(47,47,47,0); transition:background 0.3s ease; border-radius:inherit;">
                            <span class="text-white fw-bold small" style="opacity:0; transition:opacity 0.3s ease; letter-spacing:0.05em;">${overlayText}</span>
                        </div>
                    </a>
                    <h6 class="fw-bold mb-1">
                        <a href="javascript:void(0)" onclick="layout.render('Books','Details','${book.isbn}')" class="text-decoration-none text-dark">${book.title}</a>
                    </h6>
                    <div class="fw-bold text-accent mb-2">${api.formatCurrency(book.price)}</div>
                    <button class="btn btn-outline-dark btn-sm rounded-pill px-3" onclick="cart.add('${book.isbn}',1)">
                        <i class="icon icon-plus me-1"></i>Thêm vào giỏ
                    </button>
                </div>
            </div>
        `;
    },

    // 1. Fetch and display featured books on homepage
    loadFeatured: async () => {
        try {
            if (books._allBooksCache.length === 0) {
                const res = await api.get('/books');
                books._allBooksCache = res.data || [];
            }
            const itemList = books._allBooksCache;
            if (Array.isArray(itemList)) {
                books.renderGrid("#featured-books-container", itemList.slice(0, 8));
                books.renderPopular("#popular-books-container", itemList.slice(0, 8));
                if (itemList.length > 0) books.renderBestSeller(itemList[0]);
            }
        } catch (error) {
            console.error("Failed to load featured books", error);
        }
    },
    
    // 1b. Load books by category for Homepage Tabs (Optimized to use cache)
    loadByCategory: async (categoryName) => {
        const container = $("#popular-books-container");
        if (!container.length) return;
        container.html('<div class="col-12 text-center py-5"><div class="spinner-border text-accent spinner-border-sm"></div></div>');
        
        try {
            // Use cache if available for faster, more accurate switching
            if (books._allBooksCache.length === 0) {
                const res = await api.get('/books');
                books._allBooksCache = res.data || [];
            }

            let filtered = [];
            if (categoryName === 'ALL') {
                filtered = books._allBooksCache;
            } else {
                filtered = books._allBooksCache.filter(b => 
                    b.categoryName && b.categoryName.trim().toLowerCase() === categoryName.trim().toLowerCase()
                );
            }
            books.renderPopular("#popular-books-container", filtered.slice(0, 8));
        } catch (error) {
            console.error("loadByCategory error", error);
            container.html('<div class="col-12 text-center py-4 text-muted">Không tải được dữ liệu.</div>');
        }
    },

    // 1d. Main store loader
    loadAll: async () => {
        const grid = $("#books-grid");
        try {
            // 1. Categories
            const catRes = await api.get('/categories');
            books._categoriesCache = catRes.data || [];
            books.renderCategories(books._categoriesCache);

            // 2. Books
            grid.html('<div class="col-12 text-center py-5"><div class="spinner-border text-accent mb-3"></div><div class="text-muted">Đang tải danh sách sách...</div></div>');

            if (layout.pendingCategory) {
                const p = layout.pendingCategory;
                layout.pendingCategory = null;
                await books.filterByCategory(p.id, p.name);
                return;
            }

            const res = await api.get('/books');
            books._allBooksCache = res.data || [];
            books.renderGrid("#books-grid", books._allBooksCache);

        } catch (error) {
            console.error("Failed to load books index", error);
            grid.html('<div class="col-12 text-center py-5 text-danger">Lỗi khi tải dữ liệu.</div>');
        }
    },

    renderCategories: (data) => {
        const container = $("#categories-filter-list");
        if (!container.length) return;
        container.empty();
        container.append(`
            <li class="mb-1">
                <a href="javascript:void(0)" onclick="books.loadAll()" class="text-decoration-none text-dark d-flex justify-content-between align-items-center py-2 px-3 rounded-3 category-link active bg-light shadow-sm">
                    <span class="small fw-bold"><i class="icon icon-grid me-2"></i>Tất cả sản phẩm</span>
                </a>
            </li>
        `);

        const renderLevel = (items, parentElement, depth = 0) => {
            (items || []).forEach(cat => {
                const hasChildren = cat.subCategories && cat.subCategories.length > 0;
                const subListId = `sub-cat-list-${cat.categoryId}`;
                const itemHtml = `
                    <li class="category-item-wrapper" style="margin-bottom: 2px;">
                        <a href="javascript:void(0)" 
                           onclick="layout.pendingCategory = { id: ${cat.categoryId}, name: '${cat.categoryName.replace(/'/g, "\\'")}' }; layout.render('Books','Index');" 
                           data-id="${cat.categoryId}"
                           class="text-decoration-none text-muted d-flex justify-content-between align-items-center py-2 px-3 rounded-2 category-link"
                           style="padding-left: ${depth * 15 + 16}px !important; border-left: ${depth > 0 ? '1px solid #eee' : 'none'};">
                            <span class="small ${depth === 0 ? 'fw-bold text-dark' : ''}">${depth > 0 ? '└ ' : ''}${cat.categoryName}</span>
                            ${hasChildren ? '<i class="icon icon-chevron-down extra-small opacity-50" style="font-size:0.5rem;"></i>' : ''}
                        </a>
                        <ul class="list-unstyled sub-category-list" id="${subListId}"></ul>
                    </li>
                `;
                const $item = $(itemHtml);
                parentElement.append($item);
                if (hasChildren) renderLevel(cat.subCategories, $item.find(`#${subListId}`), depth + 1);
            });
        };
        renderLevel(data, container);
    },

    _getAllChildIds: (categoryId, nodes = null) => {
        const validId = parseInt(categoryId);
        if (isNaN(validId)) return [];
        if (!nodes) nodes = books._categoriesCache;
        
        let ids = [validId];
        const findAndCollect = (list) => {
            for (const node of list) {
                const nodeId = parseInt(node.categoryId || node.id); 
                if (nodeId === validId) {
                    const collect = (children) => {
                        children.forEach(c => {
                            ids.push(parseInt(c.categoryId || c.id));
                            if (c.subCategories) collect(c.subCategories);
                        });
                    };
                    if (node.subCategories) collect(node.subCategories);
                    return true;
                }
                if (node.subCategories && findAndCollect(node.subCategories)) return true;
            }
            return false;
        };
        findAndCollect(nodes || []);
        return ids;
    },

    filterByCategory: async (categoryId, categoryName, element) => {
        const validCatId = parseInt(categoryId);
        const isIdValid = !isNaN(validCatId);
        $("#category-title").text(categoryName || "Danh mục");
        $(".category-link").removeClass("active fw-bold text-dark bg-light shadow-sm").addClass("text-muted");
        
        let target = element || (isIdValid ? $(`.category-link[data-id="${validCatId}"]`) : null);
        if (target) $(target).addClass("active fw-bold text-dark bg-light shadow-sm").removeClass("text-muted");

        const grid = $("#books-grid");
        grid.html('<div class="col-12 text-center py-5"><div class="spinner-border text-accent"></div></div>');

        try {
            if (books._allBooksCache.length === 0) {
                const res = await api.get('/books');
                books._allBooksCache = res.data || [];
            }
            const targetIds = isIdValid ? books._getAllChildIds(validCatId) : [];
            const filtered = books._allBooksCache.filter(book => {
                const bookCatId = parseInt(book.categoryId || book.category_id || book.id);
                const matchById = isIdValid && !isNaN(bookCatId) && targetIds.includes(bookCatId);
                const matchByName = categoryName && book.categoryName && (book.categoryName.trim().toLowerCase() === categoryName.trim().toLowerCase());
                return matchById || matchByName;
            });
            books.renderGrid("#books-grid", filtered);
        } catch (e) {
            grid.html('<div class="col-12 text-center py-5 text-danger">Lỗi lọc danh mục.</div>');
        }
    },

    // 2. Search & Sort
    handleSort: (criteria) => {
        let sorted = [...books._allBooksCache];
        if (criteria === 'price-asc') sorted.sort((a, b) => a.price - b.price);
        else if (criteria === 'price-desc') sorted.sort((a, b) => b.price - a.price);
        else if (criteria === 'newest') sorted.sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));
        books.renderGrid("#books-grid", sorted);
    },

    loadByPriceRange: async (min, max) => {
        const grid = $("#books-grid");
        grid.html('<div class="col-12 text-center py-5"><div class="spinner-border text-accent"></div></div>');
        try {
            const res = await api.get(`/books?minPrice=${min}&maxPrice=${max}`);
            books._allBooksCache = res.data || []; // Refresh cache with filtered results if desired, or just render
            books.renderGrid("#books-grid", books._allBooksCache);
            $("#category-title").text(`Sách giá từ ${api.formatCurrency(min)} đến ${api.formatCurrency(max)}`);
        } catch (e) {
            grid.html('<div class="col-12 text-center py-5 text-danger">Lỗi lọc giá.</div>');
        }
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

    // 1c. Popular books grid
    renderPopular: (selector, items) => {
        const container = $(selector);
        if (!container.length) return;
        container.empty();
        (items || []).forEach(book => {
            container.append(books._createBookCardHtml(book, { isPopular: true }));
        });
    },

    // 2. AI Search
    searchAI: async (query) => {
        if (!query || !query.trim()) return;
        layout.current.isSearching = true;
        if (layout.current.area !== 'Books' || layout.current.view !== 'Index') {
            await layout.render('Books', 'Index');
        }
        const grid = $("#books-grid");
        grid.html(`<div class="col-12 text-center py-5"><div class="spinner-border text-accent mb-3"></div><p class="text-muted">AI đang tìm: <strong>"${$('<div>').text(query).html()}"</strong></p></div>`);

        try {
            const res = await api.post('/books/ai-search', { query });
            books.renderGrid("#books-grid", res.data || []);
        } catch (error) { console.error("AI Search error:", error); }
    },

    // 3. Admin Views
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
                                    <div class="d-flex align-items-center gap-2 mt-1"><small class="text-muted">ISBN: ${book.isbn}</small>${typeBadge}</div>
                                </div>
                            </div>
                        </td>
                        <td>${book.categoryName || '---'}</td>
                        <td class="fw-bold text-accent">${api.formatCurrency(book.price)}</td>
                        <td>${book.stockQuantity || 0}</td>
                        <td class="text-end pe-4">
                            <div class="btn-group gap-2">
                                <button class="btn btn-sm btn-light rounded-pill shadow-sm py-1 px-3" onclick="layout.render('Books/Admin', 'Edit', '${book.isbn}')">Sửa</button>
                                <button class="btn btn-sm btn-outline-danger rounded-pill shadow-sm" onclick="books.delete('${book.isbn}')"><i class="icon icon-close"></i></button>
                            </div>
                        </td>
                    </tr>
                `);
            });
        } catch (e) { api.showToast("Lỗi tải danh mục admin", "error"); }
    },

    // 4. Detail & Related
    loadDetail: async (isbn) => {
        try {
            const res = await api.get(`/books/${isbn}`);
            const book = res.data || res;
            if (!book) return api.showToast("Không tìm thấy sách", "error");

            currentBookIsbn = book.isbn;
            $('#book-title').text(book.title);
            $('#book-author').text(book.authorNames ? Array.from(book.authorNames).join(', ') : 'Chưa cập nhật');
            $('#book-isbn').text(book.isbn);
            $('#book-price').text(api.formatCurrency(book.price));
            $('#book-price-old').text(api.formatCurrency(book.price * 1.2));
            $('#book-description').text(book.description || 'Không có mô tả.');
            $('#breadcrumb-category').text(book.categoryName || 'Sách');
            $('#book-image').attr('src', book.coverImage ? `assets/images/${book.coverImage}` : 'assets/images/main-banner2.jpg');

            if (book.categoryName) books.loadRelated(book.categoryName, book.isbn);
        } catch (error) { api.showToast("Lỗi tải chi tiết", "error"); }
    },

    loadRelated: async (categoryName, excludeIsbn) => {
        try {
            const res = await api.get(`/books?categoryName=${encodeURIComponent(categoryName)}`);
            const data = (res.data || res).filter(b => b.isbn !== excludeIsbn).slice(0, 4);
            const container = $("#related-books-grid");
            if (!container.length) return;
            container.empty();
            if (data.length === 0) return container.html('<div class="col-12 text-center py-4 text-muted">Không có sản phẩm liên quan.</div>');
            data.forEach(book => {
                container.append(books._createBookCardHtml(book, { columnClass: "col-md-3" }));
            });
        } catch (e) { console.error("loadRelated failed", e); }
    },

    // 5. Form Helpers
    loadForEdit: (isbn) => {
        if (window.loadBookForEdit) {
            window.loadBookForEdit(isbn);
        } else {
            // If window function not yet available (race condition), retry shortly
            setTimeout(() => books.loadForEdit(isbn), 100);
        }
    },

    toggleTypeFields: () => {
        const type = $('input[name="bookType"]:checked').val();
        $('#physical-fields-section, #ebook-fields-section').addClass('d-none').find('input').removeAttr('required');
        if (type === 'PHYSICAL') {
            $('#physical-fields-section').removeClass('d-none').find('input').attr('required', true);
        } else {
            $('#ebook-fields-section').removeClass('d-none').find('input').attr('required', true);
        }
    },

    create: async () => {
        const form = $("#create-book-form");
        if (!form[0].checkValidity()) { form[0].reportValidity(); return; }
        const raw = {};
        form.serializeArray().forEach(item => { raw[item.name] = item.value; });
        const payload = {
            isbn: raw.isbn, 
            title: raw.title || raw.tenSach, 
            price: parseFloat(raw.price || raw.giaNiemYet) || 0,
            categoryId: raw.categoryId ? parseInt(raw.categoryId) : (raw.maDanhMuc ? parseInt(raw.maDanhMuc) : null),
            publisherId: raw.publisherId ? parseInt(raw.publisherId) : (raw.maNxb ? parseInt(raw.maNxb) : null),
            description: raw.description || raw.moTa || '', 
            coverImage: raw.coverImage || raw.anhBia || '',
            authorIds: raw.authorId ? [parseInt(raw.authorId)] : (raw.maTacGia ? [parseInt(raw.maTacGia)] : []),
            bookType: raw.bookType, 
            weight: parseFloat(raw.weight) || null,
            fileSize: parseFloat(raw.fileSize) || null, 
            downloadUrl: raw.downloadUrl || null
        };
        try {
            await api.post('/admin/books', payload);
            api.showToast("Đã thêm sách!");
            layout.render('Books', 'Admin/Index');
        } catch (e) { api.showToast("Lỗi: " + e.message, "error"); }
    },

    update: async (isbn) => {
        const form = $("#edit-book-form") || $("#create-book-form");
        if (!form[0].checkValidity()) { form[0].reportValidity(); return; }
        const raw = {};
        form.serializeArray().forEach(item => { raw[item.name] = item.value; });
        const payload = {
            title: raw.title || raw.tenSach, 
            price: parseFloat(raw.price || raw.giaNiemYet) || 0,
            categoryId: raw.categoryId ? parseInt(raw.categoryId) : (raw.maDanhMuc ? parseInt(raw.maDanhMuc) : null),
            publisherId: raw.publisherId ? parseInt(raw.publisherId) : (raw.maNxb ? parseInt(raw.maNxb) : null),
            description: raw.description || raw.moTa || '', 
            coverImage: raw.coverImage || raw.anhBia || '',
            authorIds: raw.authorId ? [parseInt(raw.authorId)] : (raw.maTacGia ? [parseInt(raw.maTacGia)] : []),
            bookType: raw.bookType, 
            weight: parseFloat(raw.weight) || null,
            fileSize: parseFloat(raw.fileSize) || null, 
            downloadUrl: raw.downloadUrl || null
        };
        try {
            await api.put(`/admin/books/${isbn}`, payload);
            api.showToast("Đã cập nhật!");
            layout.render('Books', 'Admin/Index');
        } catch (e) { api.showToast("Lỗi: " + e.message, "error"); }
    },

    delete: async (isbn) => {
        if (!confirm("Xóa cuốn sách này?")) return;
        try {
            await api.delete(`/admin/books/${isbn}`);
            api.showToast("Đã xóa");
            books.loadAdminList();
        } catch (e) { api.showToast("Xóa thất bại", "error"); }
    },

    handleImageUpload: async (input) => {
        const file = input.files[0];
        if (!file) return;
        
        const objectUrl = URL.createObjectURL(file);
        $('#book-preview-img').attr('src', objectUrl).removeClass('d-none').show();
        $('#preview-placeholder').addClass('d-none');
        
        $('#upload-overlay').removeClass('d-none').addClass('d-flex');
        const formData = new FormData();
        formData.append('file', file);
        try {
            const res = await fetch('/api/admin/upload-image', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + api.getToken() },
                body: formData
            });
            const result = await res.json();
            if (result.status === 200 || result.status === 201) {
                $('#anhBia, #edit-anhBia').val(result.data.fileName);
                api.showToast("Tải ảnh thành công!");
            }
        } catch (e) { api.showToast("Lỗi upload", "error"); }
        finally { $('#upload-overlay').addClass('d-none'); }
    },

    loadCategoriesIntoForm: async () => {
        try {
            const res = await api.get('/categories');
            const data = res.data || res;
            const sel = $('#maDanhMuc, #categoryId');
            sel.empty().append('<option value="">-- Chọn danh mục --</option>');
            const walk = (nodes, depth = 0) => {
                nodes.forEach(n => {
                    sel.append(`<option value="${n.categoryId}">${"&nbsp;".repeat(depth*4)}${n.categoryName}</option>`);
                    if (n.subCategories) walk(n.subCategories, depth + 1);
                });
            };
            walk(data);
        } catch (e) {}
    },

    loadAuthors: async () => {
        try {
            const res = await api.get('/authors');
            const sel = $('#maTacGia, #authorId');
            sel.empty().append('<option value="">-- Chọn tác giả --</option>');
            (res.data || res).forEach(a => sel.append(`<option value="${a.authorId}">${a.authorName}</option>`));
        } catch (e) {}
    },

    loadPublishers: async () => {
        try {
            const res = await api.get('/publishers');
            const sel = $('#maNxb, #publisherId');
            sel.empty().append('<option value="">-- Chọn NXB --</option>');
            (res.data || res).forEach(p => sel.append(`<option value="${p.publisherId}">${p.publisherName}</option>`));
        } catch (e) {}
    },

    adminSearch: (query) => {
        $("#books-admin-list tr").each(function () {
            $(this).toggle($(this).text().toLowerCase().includes(query.toLowerCase()));
        });
    },

    renderGrid: (selector, itemList) => {
        const container = $(selector);
        if (!container.length) return;
        container.empty();
        $("#product-count").text(itemList ? itemList.length : 0);
        if (!itemList || itemList.length === 0) {
            return container.html('<div class="col-12 text-center py-5"><p class="text-muted">Không tìm thấy sản phẩm nào.</p></div>');
        }
        itemList.forEach(book => container.append(books._createBookCardHtml(book)));
    },

    toggleAdvancedFilters: () => {
        const pane = $('#advanced-filters-pane');
        if (pane.length) {
            pane.toggleClass('d-none');
        } else {
            console.log("Advanced filters pane not found.");
        }
    }
};

$(document).on('click', '#btn-save-book', () => books.create());
$(document).on('click', '#btn-update-book', function () {
    const isbn = $(this).data('isbn');
    books.update(isbn);
});
