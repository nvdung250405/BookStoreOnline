/**
 * layout-manager.js - Centralized Layout Engine
 * Similar to .NET MVC RenderBody logic
 */
const layout = {
    // Current loaded area and view
    current: { area: '', view: '', id: null },

    /**
     * Render a view into the master layout
     * @param {string} area - e.g., 'Auth', 'Books', 'Orders'
     * @param {string} view - e.g., 'Login', 'Details', 'Index'
     */
    render: async (area, view, id, targetId) => {
        console.log(`Rendering View: ${area}/${view}`);
        
        // 1. Manage Global Layout (Admin vs Customer)
        const isAdmin = area.includes('Admin') || view.includes('Admin');
        const sidebar = $("#admin-sidebar-global");

        if (isAdmin) {
            $("#header-area, #footer-area").hide();
            sidebar.show();
            // Load sidebar only once if empty
            if (sidebar.is(':empty')) {
                await sidebar.load("Shared/AdminSidebar.html", () => {
                    layout.updateActiveAdminNav(area, view);
                });
            } else {
                layout.updateActiveAdminNav(area, view);
            }
        } else {
            $("#header-area, #footer-area").show();
            sidebar.hide();
        }

        // 2. Load Header/Footer (Customer only)
        if (!isAdmin) {
            if ($("#header-area").is(':empty')) {
                await $("#header-area").load("Shared/Header.html", () => {
                    layout.updateUserHeader();
                    categories.loadCategoryDropdown();
                });
            } else {
                layout.updateUserHeader();
            }
            
            if ($("#footer-area").is(':empty')) {
                await $("#footer-area").load("Shared/Footer.html");
            }
        }
        
        // 3. Load the main view content
        const contentPath = area === '' ? `${view}.html` : `${area}/${view}.html`;
        const cacheBust = "?v=" + new Date().getTime();
        
        // Update state BEFORE loading so that scripts in the view can access it
        layout.current = { area, view, id: id || null };
        if (id) sessionStorage.setItem('current_render_id', id);
        console.log(`Layout state updated: area=${area}, view=${view}, id=${id}`);

        return new Promise((resolve) => {
            $("#render-body").load(contentPath + cacheBust, function(response, status, xhr) {
                if (status == "error") {
                    $("#render-body").html(`<div class="container py-5 text-center"><h3>404 - Không tìm thấy nội dung</h3><p>${contentPath}</p></div>`);
                    resolve();
                    return;
                }

                layout.current = { area, view, id: id || null };

                // Persist current view state for reload recovery
                sessionStorage.setItem('last_area', area || '');
                sessionStorage.setItem('last_view', view || '');
                if (id) sessionStorage.setItem('last_id', id);
                else sessionStorage.removeItem('last_id');

                layout.initViewLogic(area, view, id);

                if (typeof initBooksawTheme === 'function') {
                    initBooksawTheme();
                }

                layout.updateActiveNav();

                // Global Trigger for profile reminder (if not already in profile page)
                if (view !== 'Profile' && typeof users !== 'undefined' && users.checkIncompleteProfile) {
                    users.checkIncompleteProfile();
                }

                // Scroll handling
                if (targetId) {
                    setTimeout(() => {
                        const el = document.getElementById(targetId);
                        if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
                    }, 50);
                } else {
                    window.scrollTo({ top: 0, behavior: 'auto' });
                }

                resolve();
            });
        });
    },

    /**
     * Highlight the active nav item in Admin Sidebar
     */
    updateActiveAdminNav: (area, view) => {
        const currentPath = `${area}/${view}`;
        $("#admin-nav .nav-link").removeClass("active-admin");
        $(`#admin-nav .nav-link[data-view="${currentPath}"]`).addClass("active-admin");
    },

    /**
     * Update Header UI — compact dark top-bar style
     */
    updateUserHeader: () => {
        const wrapper = document.getElementById('user-account-wrapper');
        if (!wrapper) return;

        const user = api.getUser();

        // Helper: close menu after clicking a nav item
        const closeMenu = `document.getElementById('userDropdownMenu') && (document.getElementById('userDropdownMenu').style.display='none');`;

        // ── GUEST ──
        if (!user) {
            wrapper.innerHTML = `
                <a href="javascript:void(0)" onclick="layout.render('Auth','Login')"
                   style="color:rgba(255,255,255,0.7); text-decoration:none; display:flex; align-items:center; gap:5px; font-size:0.8rem;">
                    <i class="icon icon-user" style="font-size:0.85rem;"></i>
                    <span>Đăng nhập</span>
                </a>`;
            return;
        }

        // ── LOGGED IN ──
        const isAdmin = user.role === 'ADMIN' || user.role === 'STAFF';
        const menuItems = isAdmin ? `
            <li>
                <a class="dropdown-item py-2 px-3 d-flex align-items-center gap-2"
                   style="font-size:0.85rem;"
                   href="javascript:void(0)"
                   onclick="${closeMenu} layout.render('Dashboard','Admin/Index')">
                   🛠️ Trang quản trị
                </a>
            </li>
            <li><hr class="dropdown-divider my-1"></li>` : `
            <li>
                <a class="dropdown-item py-2 px-3 d-flex align-items-center gap-2"
                   style="font-size:0.85rem;"
                   href="javascript:void(0)"
                   onclick="${closeMenu} layout.render('Users','Profile')">
                   👤 Thông tin cá nhân
                </a>
            </li>
            <li>
                <a class="dropdown-item py-2 px-3 d-flex align-items-center gap-2"
                   style="font-size:0.85rem;"
                   href="javascript:void(0)"
                   onclick="${closeMenu} layout.render('Orders','History')">
                   📋 Lịch sử đơn hàng
                </a>
            </li>
            <li><hr class="dropdown-divider my-1"></li>`;

        // Build avatar initial from username or fullName
        const displayName = user.fullName || user.username || 'U';
        const initials    = displayName.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);
        const roleColor   = { ADMIN: '#e74c3c', STAFF: '#3498db', STOREKEEPER: '#f39c12', CUSTOMER: '#C5A992' };
        const avatarBg    = roleColor[user.role] || '#C5A992';

        wrapper.innerHTML = `
            <a href="javascript:void(0)" id="userDropdownBtn" style="
                display:flex; align-items:center; gap:4px;
                background:none; border:none; padding:0; cursor:pointer;
                color:rgba(255,255,255,0.7); font-size:0.8rem;
                text-decoration:none; transition: color 0.2s; white-space:nowrap;
                line-height: 1;
            ">
                <i class="icon icon-user" style="font-size:0.85rem; line-height: 1;"></i>
                <span style="max-width:90px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; line-height: 1;">
                    ${user.username}
                </span>
                <span style="font-size:0.5rem; opacity:0.5; margin-left: -1px; line-height: 1;">▼</span>
            </a>
            <ul id="userDropdownMenu" style="display:none; position:absolute; right:0; top:calc(100% + 15px);
                min-width:180px; background:#fff; border-radius:12px; z-index:9999;
                box-shadow:0 8px 25px rgba(0,0,0,0.13); padding:5px 0; list-style:none; margin:0;">
                <li style="padding:10px 14px; border-bottom:1px solid #f3f0ec; margin-bottom:3px;">
                    <div style="display:flex; align-items:center; gap:8px;">
                        <div style="width:28px; height:28px; border-radius:50%; background:${avatarBg}; color:#fff; display:flex; align-items:center; justify-content:center; font-size:0.68rem; font-weight:800; flex-shrink:0;">
                            ${initials}
                        </div>
                        <div>
                            <div style="font-weight:700; font-size:0.82rem; color:#2F2F2F; line-height:1.2;">${displayName}</div>
                            <span style="background:${avatarBg}20; color:${avatarBg}; font-size:0.58rem; border-radius:20px; padding:1px 6px; display:inline-block; margin-top:2px; font-weight:700;">
                                ${user.role || 'CUSTOMER'}
                            </span>
                        </div>
                    </div>
                </li>
                ${menuItems}
                <li>
                    <a class="dropdown-item py-2 px-3 d-flex align-items-center gap-2 fw-bold"
                       style="font-size:0.82rem; color:#dc3545;"
                       href="javascript:void(0)" onclick="auth.logout()">
                       🚪 Đăng xuất
                    </a>
                </li>
            </ul>`;

        // Wire toggle
        const btn  = document.getElementById('userDropdownBtn');
        const menu = document.getElementById('userDropdownMenu');
        if (btn && menu) {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                menu.style.display = menu.style.display === 'block' ? 'none' : 'block';
            });
        }
    },


    /**
     * Dispatcher for page-specific logic
     */
    initViewLogic: (area, view, id) => {
        // Shared logic
        cart.updateCounter();

        const path = area ? `${area}/${view}` : view;
        console.log("Initializing Logic for:", path);

        switch(path) {
            case "Home/Index":
                books.loadFeatured();
                break;
            case "Auth/Login":
            case "Auth/Register":
                initAuthForms();
                break;
            case "Books/Index":
                if (!layout.current.isSearching) {
                    books.loadAll();
                }
                layout.current.isSearching = false;
                break;
            case "Books/Details":
                if (id) {
                    books.loadDetail(id);
                    review.loadForBook(id);
                }
                break;
            case "Orders/Checkout":
                orders.initCheckout();
                break;
            case "Orders/History":
                orders.loadHistory();
                break;
            case "Orders/Admin/Index":
                orders.loadAdminOrders();
                break;
            case "Orders/Details":
                if (id) orders.loadDetail(id);
                break;
            case "Orders/Cart":
                cart.load();
                break;
            case "Shipping/Tracking":
                if (id) {
                    // Load tracking details for the order
                    const trackingData = sessionStorage.getItem('tracking_' + id);
                    if (trackingData) {
                        const tracking = JSON.parse(trackingData);
                        $("#tracking-display-id").text(tracking.maVanChuyen);
                        $("#tracking-status-badge").text(tracking.trangThaiTracking);
                    }
                }
                break;
            case "Support/Index":
                support.loadUserTickets();
                break;
            case "Users/Admin/Index":
                users.loadAdminList();
                break;
            case "Users/Admin/Create":
                // Form ready, no preload needed
                break;
            case "Users/Profile":
                users.loadProfile();
                break;
            case "Categories/Admin/Index":
                categories.loadAdminList();
                break;
            case "Categories/Index":
                categories.loadPublicList();
                break;
            case "Dashboard/Admin/Index":
                dashboard.init();
                break;
            case "Vouchers/Admin/Index":
                vouchers.loadAdminList();
                break;
            case "Reviews/Admin/Index":
                review.loadAdminList();
                break;
            case 'Books/Admin/Index':
                books.loadAdminList();
                break;
            case 'Books/Admin/Create':
                books.loadCategoriesIntoForm && books.loadCategoriesIntoForm();
                books.loadAuthors && books.loadAuthors();
                books.loadPublishers && books.loadPublishers();
                break;
            case 'Books/Admin/Edit':
                // Edit.html handles its own initialization via IIFE in script tag
                // It will load dropdowns and book data automatically
                break;
            case 'Inventory/Admin/Index':
                inventory.loadList();
                break;
            case "Inventory/Admin/Import":
                inventory.initImportForm();
                break;
            case "Inventory/Admin/Export":
                // Ready for export logic form
                break;
            case "Suppliers/Admin/Index":
                suppliers.loadList();
                break;
            case "Suppliers/Admin/Edit":
            case "Suppliers/Admin/Details":
            case "Suppliers/Admin/Delete":
                if (id) suppliers.loadDetail(id);
                break;
            case "Shipping/Tracking":
                // Ready for tracking ID input
                break;
            case "Support/Chat":
                support.initChat();
                break;
            case "Authors/Admin/Index":
                if (typeof authors !== 'undefined') authors.loadAdminList();
                break;
            case "Publishers/Admin/Index":
                if (typeof publishers !== 'undefined') publishers.loadAdminList();
                break;
            case "Reviews/Admin/Index":
                if (typeof review !== 'undefined') review.loadAdminList();
                break;
            case "Payments/Admin/Index":
                if (typeof payments !== 'undefined') payments.loadAdminList();
                break;
            case "Shipping/Admin/Index":
                if (typeof shipping !== 'undefined') shipping.loadAdminList();
                break;
            case "AuditLogs/Admin/Index":
                if (typeof auditlog !== 'undefined') auditlog.loadList();
                break;
            case "Vouchers/Admin/Create":
                // form ready, no preload
                break;
            case "Vouchers/Admin/Edit":
                if (id && typeof vouchers !== 'undefined') vouchers.loadForEdit(id);
                break;
            case "Home/About":
            case "Home/Contact":
                // Static pages, no special init needed
                break;
        }
    },

    updateActiveNav: () => {
        // Basic active state logic
    }
};

$(document).ready(() => {
    // One-time global handler: close user dropdown when clicking outside
    document.addEventListener('click', (e) => {
        const menu = document.getElementById('userDropdownMenu');
        const btn  = document.getElementById('userDropdownBtn');
        if (menu && btn && !btn.contains(e.target)) {
            menu.style.display = 'none';
        }
    });
});
