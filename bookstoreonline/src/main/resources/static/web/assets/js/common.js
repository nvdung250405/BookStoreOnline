/**
 * common.js - Shared utilities for Bookstore Online
 * Refactored for Full English Standardization (Hybrid Layer)
 */

const common = {
    getBaseURL() {
        const path = window.location.pathname;
        if (path.includes('/Books/') || path.includes('/Auth/') || path.includes('/Orders/')) {
            return '../';
        }
        return '';
    },
    getQueryParam(name) {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get(name);
    },
    subscribe(email) {
        if (!email || !email.includes('@')) {
            api.showToast('Vui lòng nhập email hợp lệ', 'warning');
            return;
        }
        // In a real scenario, call an API endpoint
        api.showToast('Cảm ơn! Email ' + email + ' đã được đăng ký bản tin thành công.', 'success');
        $('#sub-email').val('');
    },
    formatDate(timestamp, includeTime = false) {
        if (!timestamp) return "---";
        const date = new Date(timestamp);
        if (isNaN(date)) return timestamp;
        
        const options = {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        };
        if (includeTime) {
            options.hour = '2-digit';
            options.minute = '2-digit';
            options.second = '2-digit';
        }
        return new Intl.DateTimeFormat('vi-VN', options).format(date);
    },
    copyToClipboard(text) {
        if (!text) return;
        navigator.clipboard.writeText(text).then(() => {
            api.showToast("Đã sao chép vào bộ nhớ tạm", "success");
        }).catch(err => {
            console.error('Lỗi sao chép:', err);
            // Fallback for older browsers
            const textArea = document.createElement("textarea");
            textArea.value = text;
            document.body.appendChild(textArea);
            textArea.select();
            document.execCommand("copy");
            document.body.removeChild(textArea);
            api.showToast("Đã sao chép vào bộ nhớ tạm", "success");
        });
    }
};

const api = {
    baseUrl: 'http://localhost:8080/api', 
    
    getToken: () => localStorage.getItem('access_token'),
    setToken: (token) => localStorage.setItem('access_token', token),
    clearToken: () => localStorage.removeItem('access_token'),
    
    getUser: () => JSON.parse(localStorage.getItem('user_info')),
    setUser: (user) => localStorage.setItem('user_info', JSON.stringify(user)),
    clearUser: () => localStorage.removeItem('user_info'),

    /**
     * Standardized API Request Handler
     * Handles ApiResponse<T> { status, message, data }
     */
    request: async (endpoint, options = {}) => {
        const token = api.getToken();
        const headers = { 'Content-Type': 'application/json', ...options.headers };
        if (token) headers['Authorization'] = `Bearer ${token}`;

        const method = options.method || 'GET';
        
        // Debug group for tracking requests during refactor
        console.groupCollapsed(`%c API ${method} %c ${endpoint}`, "color:white; background:#C5A992; padding:2px 5px; border-radius:3px;", "color:#2F2F2F; font-weight:bold;");
        if (options.body) {
            try { console.log("Payload:", JSON.parse(options.body)); } catch(e) { console.log("Payload (raw):", options.body); }
        }

        try {
            const response = await fetch(`${api.baseUrl}${endpoint}`, { ...options, headers });
            
            if (response.status === 204) {
                console.log("Response: No Content (204)");
                console.groupEnd();
                return { status: 200, data: true };
            }

            const result = await response.json();
            console.log("Response:", result);

            if (!response.ok) {
                if (response.status === 401) { api.clearToken(); api.clearUser(); }
                throw new Error(result.message || 'Error from server');
            }

            console.groupEnd();
            return result;
        } catch (error) {
            console.error("API Call Failed:", error);
            console.groupEnd();
            throw error;
        }
    },

    get: (endpoint) => api.request(endpoint, { method: 'GET' }),
    post: (endpoint, data = {}) => api.request(endpoint, { method: 'POST', body: JSON.stringify(data || {}) }),
    put: (endpoint, data = {}) => api.request(endpoint, { method: 'PUT', body: JSON.stringify(data || {}) }),
    delete: (endpoint) => api.request(endpoint, { method: 'DELETE' }),

    showToast: (message, type = 'success') => {
        const toastEl = document.getElementById('appToast');
        if (!toastEl) { console.warn('[Toast]', message); return; }

        const configs = {
            success: { icon: '✓', title: 'Thành công', iconColor: '#C5A992', iconBg: '#f5f0eb' },
            error:   { icon: '✕', title: 'Lỗi',        iconColor: '#a07060', iconBg: '#f5eeeb' },
            warning: { icon: '!', title: 'Cảnh báo',   iconColor: '#b08050', iconBg: '#f5eedf' },
            info:    { icon: 'i', title: 'Thông tin',  iconColor: '#7090a0', iconBg: '#eaf0f5' },
        };
        const cfg = configs[type] || configs.success;

        // Update elements
        const set = (id, prop, val) => { const el = document.getElementById(id); if (el) el[prop] = val; };
        const style = (id, prop, val) => { const el = document.getElementById(id); if (el) el.style[prop] = val; };

        set('toastIcon',    'textContent', cfg.icon);
        set('toastTitle',   'textContent', cfg.title);
        set('toastMessage', 'textContent', message);
        style('toastTitle',    'color',      '#2f2f2f');
        style('toastIcon',     'color',      cfg.iconColor);
        style('toastIcon',     'fontWeight', '700');
        style('toastIconWrap', 'background', cfg.iconBg);
        style('toastProgress', 'background', cfg.iconColor);
        style('toastProgress', 'width',      '100%');
        style('toastProgress', 'transition', 'none');

        // Slide in from left
        toastEl.style.display    = 'block';
        toastEl.style.opacity    = '0';
        toastEl.style.transform  = 'translateX(-24px) scale(0.97)';
        toastEl.style.transition = 'opacity 0.28s cubic-bezier(0.22,1,0.36,1), transform 0.28s cubic-bezier(0.22,1,0.36,1)';
        requestAnimationFrame(() => requestAnimationFrame(() => {
            toastEl.style.opacity   = '1';
            toastEl.style.transform = 'translateX(0) scale(1)';
        }));

        // Shrink progress bar
        const duration = 3800;
        setTimeout(() => {
            style('toastProgress', 'transition', `width ${duration}ms linear`);
            style('toastProgress', 'width', '0%');
        }, 80);

        if (api._toastTimer) clearTimeout(api._toastTimer);
        api._toastTimer = setTimeout(() => api.hideToast(), duration + 300);
    },

    hideToast: () => {
        const toastEl = document.getElementById('appToast');
        if (!toastEl) return;
        toastEl.style.transition = 'opacity 0.22s ease, transform 0.22s ease';
        toastEl.style.opacity   = '0';
        toastEl.style.transform = 'translateX(-20px) scale(0.97)';
        setTimeout(() => { toastEl.style.display = 'none'; }, 240);
        if (api._toastTimer) { clearTimeout(api._toastTimer); api._toastTimer = null; }
    },

    formatCurrency: (amount) => {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
    },

    /**
     * Standardized response parser for ApiResponse<T> handling
     * Handles both wrapped { data: T } and direct T responses
     */
    parseResponse: (response) => {
        if (!response) return null;
        if (response.data !== undefined) return response.data;
        if (Array.isArray(response)) return response;
        return response;
    }
};

const formatPrice = (price) => api.formatCurrency(price);
