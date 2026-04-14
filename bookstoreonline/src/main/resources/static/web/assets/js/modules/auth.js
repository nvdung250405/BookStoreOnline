/**
 * auth.js - Authentication Logic
 */
const auth = {
    // Perform Login
    login: async (username, password) => {
        try {
            const result = await api.post('/auth/login', { username, password });
            
            // Backend ApiResponse uses 'status: 200' instead of 'success: true'
            if (result.status === 200) {
                const loginData = result.data;
                api.setToken(loginData.token);
                // Save essential user info
                api.setUser({
                    username: loginData.username,
                    role: loginData.role
                });
                
                api.showToast("Đăng nhập thành công!", "success");
                
                // Redirect based on role
                if (loginData.role === 'ADMIN') {
                    setTimeout(() => layout.render('Dashboard/Admin', 'Index'), 500);
                } else {
                    setTimeout(() => layout.render('', 'Home/Index'), 500);
                }
            }
        } catch (error) {
            api.showToast(error.message, "error");
        }
    },

    // Perform Registration
    register: async (userData) => {
        try {
            const result = await api.post('/auth/register', userData);
            if (result.success) {
                api.showToast("Đăng ký thành công! Hãy đăng nhập.", "success");
                layout.render('Auth', 'Login');
            }
        } catch (error) {
            api.showToast(error.message, "error");
        }
    },

    // Logout
    logout: () => {
        api.clearToken();
        api.clearUser();
        api.showToast("Đã đăng xuất");
        layout.render('', 'Home/Index');
    }
};

// Form Binding Logic (to be called after view render)
function initAuthForms() {
    $("#loginForm").on('submit', function(e) {
        e.preventDefault();
        const user = $("#username").val();
        const pass = $("#password").val();
        auth.login(user, pass);
    });

    $("#registerForm").on('submit', function(e) {
        e.preventDefault();
        const pass = $("#reg-password").val();
        const confirm = $("#reg-confirm-password").val();
        
        if (pass !== confirm) {
            api.showToast("Mật khẩu xác nhận không khớp", "error");
            return;
        }

        const data = {
            username: $("#reg-username").val(),
            email: $("#reg-email").val(),
            password: pass
        };
        auth.register(data);
    });
}
