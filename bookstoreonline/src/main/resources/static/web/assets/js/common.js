/**
 * common.js - Shared utilities for Bookstore Online
 */

const common = {
    /**
     * Xác định tiền tố đường dẫn dựa trên vị trí file hiện tại
     * Giúp nạp component từ đúng thư mục gốc (static/web/)
     */
    getBaseURL() {
        const path = window.location.pathname;
        if (path.includes('/Books/') || path.includes('/Auth/') || path.includes('/Orders/')) {
            return '../';
        }
        return '';
    },

    /**
     * Get URL query parameter
     */
    getQueryParam(name) {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get(name);
    },

    /**
     * Format number as VNĐ currency
     */
    formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(amount);
    }
};

/**
 * common.js - Global Utilities and API configurations
 */
const api = {
    baseUrl: 'http://localhost:8080/api', // Backend base URL (Absolute for cross-origin dev)
    
    // Auth Token Management
    getToken: () => localStorage.getItem('access_token'),
    setToken: (token) => localStorage.setItem('access_token', token),
    clearToken: () => localStorage.removeItem('access_token'),
    
    // User Management
    getUser: () => JSON.parse(localStorage.getItem('user_info')),
    setUser: (user) => localStorage.setItem('user_info', JSON.stringify(user)),
    clearUser: () => localStorage.removeItem('user_info'),

    /**
     * Core fetch wrapper
     */
    request: async (endpoint, options = {}) => {
        const token = api.getToken();
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        try {
            const response = await fetch(`${api.baseUrl}${endpoint}`, {
                ...options,
                headers
            });

            const result = await response.json();
            
            if (!response.ok) {
                if (response.status === 401) {
                    api.clearToken();
                    api.clearUser();
                    // Optional: redirect to login
                }
                throw new Error(result.message || 'Có lỗi xảy ra từ máy chủ');
            }

            return result; // Usually { success: true, message: "...", data: ... }
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    },

    get: (endpoint) => api.request(endpoint, { method: 'GET' }),
    
    post: (endpoint, data) => api.request(endpoint, {
        method: 'POST',
        body: JSON.stringify(data)
    }),
    
    put: (endpoint, data) => api.request(endpoint, {
        method: 'PUT',
        body: JSON.stringify(data)
    }),
    
    delete: (endpoint) => api.request(endpoint, { method: 'DELETE' }),

    // Toast Notification Helper
    showToast: (message, type = 'success') => {
        const toastEl = document.getElementById('appToast');
        if (!toastEl) return;
        
        const toastBody = document.getElementById('toastMessage');
        const toastHeader = toastEl.querySelector('.toast-header');
        
        toastBody.innerText = message;
        
        // Coloring
        if (type === 'error') {
            toastHeader.classList.replace('bg-accent', 'bg-danger');
        } else {
            toastHeader.classList.replace('bg-danger', 'bg-accent');
        }
        
        const toast = new bootstrap.Toast(toastEl);
        toast.show();
    }
};

// Global formatters
const formatPrice = (price) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
};

// Global utilities for Bookstore
