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
    baseUrl: '/api', 
    
    getToken: () => localStorage.getItem('access_token'),
    setToken: (token) => localStorage.setItem('access_token', token),
    clearToken: () => localStorage.removeItem('access_token'),
    
    getUser: () => JSON.parse(localStorage.getItem('user_info')),
    setUser: (user) => localStorage.setItem('user_info', JSON.stringify(user)),
    clearUser: () => localStorage.removeItem('user_info'),

    /**
     * Standardized API Request Handler
     */
    request: async (endpoint, options = {}) => {
        const token = api.getToken();
        let cleanEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
        cleanEndpoint = cleanEndpoint.replace(/^\/api\/api\//, '/api/');

        const headers = { 'Content-Type': 'application/json', ...options.headers };
        if (token) headers['Authorization'] = `Bearer ${token}`;

        const fetchOptions = { ...options, headers };
        
        // Handle FormData (FileUpload)
        if (options.body instanceof FormData) {
            delete headers['Content-Type']; 
            fetchOptions.body = options.body;
        } else if (options.body && typeof options.body === 'string') {
            fetchOptions.body = options.body;
        } else if (options.body) {
            fetchOptions.body = JSON.stringify(options.body);
        }

        try {
            const response = await fetch(`${api.baseUrl}${cleanEndpoint}`, fetchOptions);
            if (response.status === 204) return { status: 200, data: true };

            const contentType = response.headers.get("content-type");
            let result;
            if (contentType && contentType.includes("application/json")) {
                result = await response.json();
            } else {
                const text = await response.text();
                try { result = JSON.parse(text); } catch(e) { result = { message: text }; }
            }

            if (!response.ok) {
                if (response.status === 401) { api.clearToken(); api.clearUser(); }
                const errMsg = result.message || result.error || (typeof result === 'string' ? result : 'Lỗi từ máy chủ');
                throw new Error(errMsg);
            }
            return result;
        } catch (error) {
            console.error("API Call Failed:", error);
            throw error;
        }
    },

    get: (endpoint) => api.request(endpoint, { method: 'GET' }),
    post: (endpoint, data) => api.request(endpoint, { method: 'POST', body: data }),
    put: (endpoint, data) => api.request(endpoint, { method: 'PUT', body: data }),
    post: (endpoint, data = {}) => api.request(endpoint, { method: 'POST', body: JSON.stringify(data || {}) }),
    put: (endpoint, data = {}) => api.request(endpoint, { method: 'PUT', body: JSON.stringify(data || {}) }),
    delete: (endpoint) => api.request(endpoint, { method: 'DELETE' }),

    /**
     * Global System Alerts
     */
    showToast: (message, type = 'success') => {
        console.log(`[ALERT] ${type.toUpperCase()}: ${message}`);
        const alertEl = document.getElementById('systemAlert');
        if (!alertEl) { 
            console.error('systemAlert not found!'); 
            if (type === 'error') alert(message); 
            return; 
        }

        const configs = {
            success: { icon: '✓', title: 'Thành công', color: '#C5A992', bg: '#f5f0eb' },
            error:   { icon: '✕', title: 'Lỗi',        color: '#d63031', bg: '#fab1a0' },
            warning: { icon: '!', title: 'Cảnh báo',   color: '#fdcb6e', bg: '#ffeaa7' },
            info:    { icon: 'i', title: 'Thông tin',  color: '#0984e3', bg: '#74b9ff' },
        };
        const cfg = configs[type] || configs.success;

        // Populate Content
        const el = (id) => document.getElementById(id);
        if (el('alertIcon')) { el('alertIcon').textContent = cfg.icon; el('alertIcon').style.color = cfg.color; }
        if (el('alertIconWrap')) el('alertIconWrap').style.background = cfg.bg;
        if (el('alertTitle')) el('alertTitle').textContent = cfg.title;
        if (el('alertMessage')) el('alertMessage').textContent = message;
        if (el('alertProgress')) { el('alertProgress').style.background = cfg.color; el('alertProgress').style.width = '100%'; }

        // Trigger Visibility
        alertEl.style.display = 'block';
        alertEl.style.opacity = '1';

        if (api._alertTimer) clearTimeout(api._alertTimer);
        api._alertTimer = setTimeout(() => api.hideToast(), 4000);
    },

    hideToast: () => {
        const alertEl = document.getElementById('systemAlert');
        if (alertEl) alertEl.style.display = 'none';
        if (api._alertTimer) { clearTimeout(api._alertTimer); api._alertTimer = null; }
    },

    /**
     * Modern Confirm Dialog (Promise based)
     */
    confirm: (message, title = 'Xác nhận xóa?') => {
        return new Promise((resolve) => {
            const modal = document.getElementById('systemConfirmModal');
            if (!modal) { resolve(confirm(message)); return; }

            document.getElementById('confirmTitle').textContent = title;
            document.getElementById('confirmMessage').textContent = message;
            modal.style.display = 'flex';

            const cleanup = (result) => {
                modal.style.display = 'none';
                document.getElementById('btn-confirm-proceed').onclick = null;
                document.getElementById('btn-confirm-cancel').onclick = null;
                resolve(result);
            };

            document.getElementById('btn-confirm-proceed').onclick = () => cleanup(true);
            document.getElementById('btn-confirm-cancel').onclick = () => cleanup(false);
        });
    },

    formatCurrency: (amount) => {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
    },

    parseResponse: (response) => {
        if (!response) return null;
        api.lastMeta = response.meta || null;
        let baseData = (response.data !== undefined) ? response.data : response;
        if (baseData && typeof baseData === 'object' && Array.isArray(baseData.content)) {
            if (!api.lastMeta) {
                api.lastMeta = {
                    totalElements: baseData.totalElements,
                    totalPages: baseData.totalPages,
                    pageNumber: baseData.number,
                    pageSize: baseData.size
                };
            }
            return baseData.content;
        }
        return baseData;
    },

    normalizeList: (response) => {
        const data = api.parseResponse(response);
        return Array.isArray(data) ? data : [];
    }
};

const formatPrice = (price) => api.formatCurrency(price);
