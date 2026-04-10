# 📚 BookStore Online - Hệ thống Quản trị Nhà sách (Backend Web API)

Dự án Web API chuyên sâu phục vụ quản lý nhà sách, được xây dựng trên nền tảng **Java 21** và **Spring Boot 3.3**.

---

## 🛠 Bộ Công Nghệ (Tech Stack)
- **Cốt lõi:** Java 21 LTS (Virtual Threads support)
- **Framework:** Spring Boot 3.3.x
- **Bảo mật:** Spring Security 6 & JWT (Stateless)
- **Cơ sở dữ liệu:** Microsoft SQL Server
- **ORM:** Spring Data JPA (Hibernate 6)
- **Tài liệu hóa:** Swagger / OpenAPI 3.0

---

## 🚀 Hướng dẫn Cài đặt cho Thành viên (Setup)

### 1. Chuẩn bị môi trường
- Cài đặt **JDK 21**.
- Cài đặt **SQL Server** và tạo database có tên: `nha_sach_online`.
- Chạy script trong thư mục `database/bookstore_db.sql` để khởi tạo cấu trúc bảng.

### 2. Thiết lập dự án
- Clone dự án về máy: `git clone <url_hop_le>`
- Mở bằng IntelliJ/VS Code và đợi Maven tải các thư viện.
- Kiểm tra tệp `src/main/resources/application.yml` để cấu hình lại Username/Password SQL Server của bạn (nếu cần).

---

## 🏗 Quy trình Lập trình (Workflow)

Nhóm áp dụng mô hình **Controller - Service - Repository**.

1.  **Entity**: Đã được tạo sẵn trong package `.entity`. Không tự ý sửa nếu chưa thảo luận nhóm.
2.  **DTO**: Tạo các lớp hứng dữ liệu trong package `.dto`.
3.  **ApiResponse**: Mọi API **BẮT BUỘC** phải trả về kiểu `ApiResponse<T>`.
    - Thành công: `return ResponseEntity.ok(ApiResponse.success(data));`
    - Thất bại: Sử dụng `throw new SomeException("Message");` (Sẽ được xử lý bởi GlobalExceptionHandler).

---

## 👥 Phân chia Nhiệm vụ (Assignments)
*Xem chi tiết định danh [MB X] cho 49 API trong tệp `tailieu/api.txt`.*

- **Member A (Leader):** Infrastructure, Security, Authentication, Statistics.
- **Member B:** Module Books, Category, Authors (Catalog).
- **Member C:** Module Inventory, Warehouse, Barcode, Suppliers.
- **Member D:** Module Order, Checkout, VNPay Payment.
- **Member E:** Module Shopping Cart, Vouchers, Logistics Tracking, Reviews.

---

## 🌿 Quy chế Git (Git Flow)
1. **Không code trực tiếp trên nhánh `main`**.
2. **Tạo nhánh tính năng**: `feature/[ten-module]` (Ví dụ: `feature/cart-logic`).
3. **Commit message chuẩn**: `feat: thêm chức năng lọc sách`, `fix: sửa lỗi trừ tồn kho`.
4. **Pull Request**: Sau khi xong module, đẩy lên Git và tạo Pull Request để Trưởng nhóm Review trước khi Merge vào `main`.

---

## 📝 Tài liệu API (Swagger UI)
Khởi động ứng dụng và truy cập:
`http://localhost:8080/swagger-ui.html`

---
**Dự án được thực hiện với mục tiêu Hoàn thiện trong 07 ngày thần tốc.**
