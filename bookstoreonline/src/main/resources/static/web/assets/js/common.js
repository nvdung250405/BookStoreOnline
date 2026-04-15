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
    post: (endpoint, data) => api.request(endpoint, { method: 'POST', body: JSON.stringify(data) }),
    put: (endpoint, data) => api.request(endpoint, { method: 'PUT', body: JSON.stringify(data) }),
    delete: (endpoint) => api.request(endpoint, { method: 'DELETE' }),

    showToast: (message, type = 'success') => {
        const toastEl = document.getElementById('appToast');
        if (!toastEl) {
            alert(message);
            return;
        }
        const toastBody = document.getElementById('toastMessage');
        const toastHeader = toastEl.querySelector('.toast-header');
        toastBody.innerText = message;
        
        if (type === 'error') {
            toastHeader.classList.add('bg-danger');
            toastHeader.classList.remove('bg-accent');
        } else {
            toastHeader.classList.add('bg-accent');
            toastHeader.classList.remove('bg-danger');
        }
        
        const toast = new bootstrap.Toast(toastEl);
        toast.show();
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
