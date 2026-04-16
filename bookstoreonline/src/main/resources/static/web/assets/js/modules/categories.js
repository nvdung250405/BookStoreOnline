/**
 * categories.js - Category Management Logic
 * Standardized for Full English Backend synchronization (Hybrid Layer: Admin English)
 */
const categories = {

    _flatList: [],   
    _treeData: [],   


    // ─── ICONS for categories ────────────────────────────────────────────────
    _catIcons: ['📚','📖','✍️','💡','🔬','🌍','🎭','🏛️','💼','🎨','🖥️','🎵','🌱','⚽','👶','🍳','🧠','🔭','📐','🗺️'],

    // ─── PUBLIC LIST (Customer view) ────────────────────────────────────────
    loadPublicList: async () => {
        const grid    = $('#categories-grid');
        const loading = $('#categories-loading');
        const empty   = $('#categories-empty');

        loading.show(); grid.hide(); empty.hide();

        try {
            const res = await api.get('/categories');
            const list = res.data || [];

            loading.hide();

            if (list.length === 0) {
                empty.show();
                return;
            }

            grid.empty();
            list.forEach((cat, idx) => {
                const icon = categories._catIcons[idx % categories._catIcons.length];
                const subCount = (cat.subCategories || []).length;
                const delay = (idx % 3) * 100;
                grid.append(`
                    <div class="col-lg-4 col-md-6" data-aos="fade-up" data-aos-delay="${delay}">
                        <a href="javascript:void(0)"
                           onclick="layout.render('Books','Index'); books.filterByCategory && books.filterByCategory(${cat.categoryId})"
                           class="text-decoration-none">
                            <div class="category-card bg-white p-5 rounded-5 shadow-sm">
                                <span class="cat-icon">${icon}</span>
                                <h3 class="fw-bold font-serif mb-3" style="font-size:1.25rem;">${cat.categoryName}</h3>
                                <p class="text-muted small mb-3">
                                    ${subCount > 0
                                        ? `Bao gồm ${subCount} danh mục con`
                                        : 'Khám phá các đầu sách trong danh mục này'}
                                </p>
                                <span class="badge rounded-pill px-3 py-2"
                                      style="background:#f5f2eb; color:#888; border:1px solid #e8e4dc; font-size:0.75rem;">
                                    Xem sách
                                </span>
                            </div>
                        </a>
                    </div>
                `);
            });

            grid.show();
        } catch (e) {
            loading.hide();
            empty.show();
            console.error('loadPublicList error', e);
        }
    },

    // ─── DROPDOWN for Header nav ─────────────────────────────────────────────
    loadCategoryDropdown: async () => {
        const container = $('#header-category-items');
        if (!container.length) return;
        try {
            const res = await api.get('/categories');
            const list = res.data || [];
            container.empty();
            if (list.length === 0) return;
            list.slice(0, 8).forEach(cat => {
                container.append(`
                    <li><a class="dropdown-item small py-2" href="javascript:void(0)"
                           onclick="layout.render('Categories','Index')">
                        ${cat.categoryName}
                    </a></li>
                `);
            });
        } catch (e) {
            container.empty();
        }
    },

    // ─── LOAD ────────────────────────────────────────────────────────────────
    loadAdminList: async () => {
        try {
            const res = await api.get('/categories');
            categories._treeData = Array.isArray(res) ? res : (res.data || []);

            categories._flatList = [];
            categories._walkFlat(categories._treeData, 0, null);

            categories._fillParentSelects(null);
            categories._renderTable();

        } catch (e) {
            console.error('loadAdminList error', e);
            api.showToast('Không thể tải danh sách danh mục', 'error');
        }
    },

    // ─── FLATTEN TREE ────────────────────────────────────────────
    _walkFlat: (nodes, depth, parentId) => {
        (nodes || []).forEach(node => {
            categories._flatList.push({
                categoryId:    node.categoryId,
                categoryName:  node.categoryName,
                parentId:      node.parentId || null,
                subCategories: node.subCategories || [],
                _depth:        depth,
                _parentId:     parentId
            });
            if (node.subCategories && node.subCategories.length > 0) {
                categories._walkFlat(node.subCategories, depth + 1, node.categoryId);
            }
        });
    },

    // ─── RENDER FLAT TABLE ───────────────────────────────────────────────────
    _renderTable: () => {
        const tbody = $('#categories-admin-list');
        if (!tbody.length) return;
        tbody.empty();

        if (categories._flatList.length === 0) {
            tbody.html(`
                <tr><td colspan="4" class="text-center py-5">
                    <div style="color:#ccc; font-size:2rem;">📂</div>
                    <div class="text-muted mt-2">Không tìm thấy danh mục nào.</div>
                </td></tr>`);
            return;
        }

        categories._flatList.forEach(cat => {
            const depth       = cat._depth;
            const hasChildren = cat.subCategories.length > 0;
            const childCount  = cat.subCategories.length;
            const isRoot      = depth === 0;

            const parentName = cat.parentId
                ? (categories._flatList.find(x => x.categoryId === cat.parentId)?.categoryName || `#${cat.parentId}`)
                : null;

            const indentPx = depth * 32;
            const levelBadge = isRoot
                ? `<span class="cat-badge-root">Gốc</span>`
                : `<span class="cat-badge-child">Cấp ${depth}</span>`;

            const toggleBtn = hasChildren
                ? `<button class="cat-expand-btn" id="toggle-${cat.categoryId}"
                           onclick="categories._toggle(${cat.categoryId})" title="Thu gọn/Mở rộng danh mục con">
                       <svg width="10" height="10" viewBox="0 0 10 10" fill="currentColor">
                           <path d="M2 3l3 4 3-4H2z"/>
                       </svg>
                   </button>`
                : `<span style="width:20px;display:inline-block;"></span>`;

            tbody.append(`
                <tr class="cat-item-row ${isRoot ? 'cat-root-row' : 'cat-child-row'}"
                    data-id="${cat.categoryId}"
                    data-parent="${cat.parentId || ''}"
                    data-depth="${depth}"
                    style="${!isRoot ? 'display:none;' : ''}">
                    <td class="cat-cell-id">${cat.categoryId}</td>
                    <td class="cat-cell-name">
                        <div style="display:flex; align-items:center; gap:8px; padding-left:${indentPx}px;">
                            ${toggleBtn}
                            <span class="cat-name-text" style="font-weight:${isRoot ? '700' : '500'};">
                                ${cat.categoryName}
                            </span>
                            ${levelBadge}
                            ${hasChildren ? `<span class="cat-child-count">${childCount} mục con</span>` : ''}
                        </div>
                    </td>
                    <td class="cat-cell-parent">
                        ${parentName
                            ? `<div class="cat-parent-chip">${parentName}</div>`
                            : `<span class="cat-no-parent">—</span>`}
                    </td>
                    <td class="cat-cell-actions">
                        <button class="cat-btn-edit"
                                onclick="categories._editById(${cat.categoryId})">
                            Sửa
                        </button>
                        <button class="cat-btn-delete"
                                onclick="categories._deleteById(${cat.categoryId})">
                            Xóa
                        </button>
                    </td>
                </tr>
            `);
        });

        $('#cat-total-badge').text(`${categories._flatList.length} tổng cộng · ${categories._flatList.filter(c => c._depth === 0).length} mục gốc`);
    },

    _toggle: (parentId) => {
        const btn = $(`#toggle-${parentId}`);
        const isExpanded = btn.hasClass('expanded');
        if (isExpanded) {
            categories._hideDescendants(parentId);
            btn.removeClass('expanded');
        } else {
            $(`[data-parent="${parentId}"]`).show();
            btn.addClass('expanded');
        }
    },

    _hideDescendants: (parentId) => {
        $(`[data-parent="${parentId}"]`).each(function() {
            const childId = $(this).attr('data-id');
            categories._hideDescendants(childId);
            $(`#toggle-${childId}`).removeClass('expanded');
            $(this).hide();
        });
    },

    _editById: (id) => {
        const cat = categories._flatList.find(c => c.categoryId === id);
        if (!cat) return;
        categories.openEdit(cat.categoryId, cat.categoryName, cat.parentId);
    },

    _deleteById: (id) => {
        const cat = categories._flatList.find(c => c.categoryId === id);
        if (!cat) return;
        categories.deleteCat(cat.categoryId, cat.categoryName, cat.subCategories.length);
    },

    _fillParentSelects: (excludeId) => {
        ['#cat-parent-select', '#cat-edit-parent-select'].forEach(sel => {
            const el = $(sel);
            if (!el.length) return;
            const prev = el.val();
            el.empty().append('<option value="">— Danh mục gốc —</option>');
            categories._flatList.forEach(cat => {
                if (excludeId && (cat.categoryId === excludeId || cat._parentId === excludeId)) return;
                const prefix = '\u00a0\u00a0\u00a0\u00a0'.repeat(cat._depth);
                el.append(`<option value="${cat.categoryId}">${prefix}${cat._depth > 0 ? '└ ' : ''}${cat.categoryName}</option>`);
            });
            if (prev) el.val(prev);
        });
    },

    create: async () => {
        const categoryName = $('#cat-name').val().trim();
        const parentId = $('#cat-parent-select').val() || null;

        if (!categoryName) { api.showToast('Vui lòng nhập tên danh mục', 'warning'); return; }

        try {
            await api.post('/admin/categories', {
                categoryName,
                parentId: parentId ? parseInt(parentId) : null
            });
            api.showToast('Đã tạo danh mục thành công!');
            $('#cat-name').val('');
            $('#cat-parent-select').val('');
            $('#cat-create-section').slideUp();
            categories.loadAdminList();
        } catch (e) {
            api.showToast('Lỗi: ' + e.message, 'error');
        }
    },

    openEdit: (id, categoryName, parentId) => {
        $('#cat-edit-id').val(id);
        $('#cat-edit-name').val(categoryName);
        categories._fillParentSelects(id);
        $('#cat-edit-parent-select').val(parentId || '');
        $('#cat-create-section').slideUp();
        $('#cat-edit-section').slideDown();
        document.getElementById('cat-edit-section')?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    },

    update: async () => {
        const id = $('#cat-edit-id').val();
        const categoryName = $('#cat-edit-name').val().trim();
        const parentId = $('#cat-edit-parent-select').val() || null;

        if (!id || !categoryName) { api.showToast('Thiếu thông tin bắt buộc', 'warning'); return; }

        try {
            await api.put(`/admin/categories/${id}`, {
                categoryName,
                parentId: parentId ? parseInt(parentId) : null
            });
            api.showToast('Cập nhật thành công!');
            $('#cat-edit-section').slideUp();
            categories.loadAdminList();
        } catch (e) {
            api.showToast('Cập nhật thất bại: ' + e.message, 'error');
        }
    },

    deleteCat: async (id, categoryName, childCount) => {
        if (!confirm(`Bạn có chắc chắn muốn xóa "${categoryName}" không?${childCount > 0 ? '\nCảnh báo: Tất cả danh mục con cũng sẽ bị xóa.' : ''}`)) return;
        
        try {
            await api.delete(`/admin/categories/${id}`);
            api.showToast(`Đã xóa "${categoryName}"`);
            categories.loadAdminList();
        } catch (e) {
            api.showToast('Xóa thất bại: ' + e.message, 'error');
        }
    }
};
