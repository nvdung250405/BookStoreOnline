# 📚 BookStore Online - Hệ thống Quản trị Nhà sách

Dự án quản lý nhà sách toàn diện bao gồm Backend API, Tài liệu thiết kế và Cơ sở dữ liệu.

---

## 📂 Cấu trúc thư mục
- 📁 **`bookstoreonline/`**: Mã nguồn Backend (Java 21, Spring Boot 3.3).
- 📁 **`tailieu/`**: Danh sách 49 API, Kế hoạch thực hiện, Phân chia nhiệm vụ [MB A-E].
- 📁 **`database/`**: Script khởi tạo SQL Server (`bookstore_db.sql`).

---

## 🛠 Công nghệ sử dụng
- **Ngôn ngữ:** Java 21 LTS
- **Framework:** Spring Boot 3.3.x
- **Cơ sở dữ liệu:** Microsoft SQL Server
- **ORM:** Spring Data JPA / Hibernate
- **API Documentation:** Swagger UI / OpenAPI 3.0

---

## 🚀 Hướng dẫn khởi động

### 1. Cấu hình Database
- Tạo database `nha_sach_online` trong SQL Server.
- Chạy script SQL: `database/bookstore_db.sql`.

### 2. Chạy ứng dụng
- Truy cập thư mục backend: `cd bookstoreonline`
- Chạy lệnh: `mvn spring-boot:run`

### 3. Kiểm thử API
- Truy xuất Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 🏗 Quy chuẩn lập trình nhóm
- Sử dụng mô hình: **Controller - Service - Repository**.
- Mọi kết quả trả về sử dụng lớp: **`ApiResponse<T>`**.
- Phân chia công việc chi tiết được quy định tại tệp: `tailieu/api.txt`.

---
**Dự án đang trong giai đoạn Sprint hoàn thiện.**
