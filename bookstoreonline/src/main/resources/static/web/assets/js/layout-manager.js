/**
 * layout-manager.js - Centralized Layout Engine
 * Similar to .NET MVC RenderBody logic
 */
const layout = {
    // Current loaded area and view
    current: { area: '', view: '' },

    /**
     * Render a view into the master layout
     * @param {string} area - e.g., 'Auth', 'Books', 'Orders'
     * @param {string} view - e.g., 'Login', 'Details', 'Index'
     */
    render: async (area, view, id) => {
        console.log(`Rendering View: ${area}/${view}`);
        
        // 1. Manage Area-specific Layout Visibility
        const isAdmin = area.includes('Admin') || view.includes('Admin');
        if (isAdmin) {
            $("#header-area, #footer-area").hide();
        } else {
            $("#header-area, #footer-area").show();
        }

        // 2. Load Header/Footer if not already present
        if ($("#header-area").is(':empty')) {
            await $("#header-area").load("Shared/Header.html", () => layout.updateUserHeader());
        } else {
            layout.updateUserHeader();
        }
        
        if ($("#footer-area").is(':empty')) {
            await $("#footer-area").load("Shared/Footer.html");
        }
        
        // 3. Load the main view content
        const contentPath = area === '' ? `${view}.html` : `${area}/${view}.html`;
        
        $("#render-body").load(contentPath, function(response, status, xhr) {
            if (status == "error") {
                $("#render-body").html(`<div class="container py-5 text-center"><h3>404 - Không tìm thấy nội dung</h3><p>${contentPath}</p></div>`);
                return;
            }

            layout.initViewLogic(area, view, id);

            if (typeof initBooksawTheme === 'function') {
                initBooksawTheme();
            }
            
            layout.updateActiveNav();
        });
    },

    /**
     * Update Header UI based on login status
     */
    updateUserHeader: () => {
        const user = api.getUser();
        const headerAction = $(".user-account");
        
        if (user && headerAction.length) {
            headerAction.html(`
                <i class="icon icon-user text-accent fs-5"></i>
                <span class="d-none d-sm-inline fw-bold text-dark">${user.username}</span>
                <span class="ms-2 small text-muted text-decoration-underline" onclick="auth.logout()" style="cursor:pointer; font-size: 0.7rem;">(Thoát)</span>
            `);
            headerAction.attr("onclick", ""); 
        }
    },

    /**
     * Dispatcher for page-specific logic
     */
    initViewLogic: (area, view, id) => {
        // Shared logic
        cart.updateCounter();

        // Specific views
        if (view === 'Home/Index') {
            books.loadFeatured();
        } 
        else if (area === 'Auth') {
            initAuthForms();
        } 
        else if (area === 'Books' && view === 'Index') {
            // books.loadAll();
        } 
        else if (area === 'Books' && view === 'Details') {
            if (id) books.loadDetail(id);
        } 
        else if (area === 'Orders' && view === 'Cart') {
            cart.load();
        }
    },

    updateActiveNav: () => {
        // Basic active state logic
    }
};

$(document).ready(() => {
    // Initial Load handled in index.html
});
