# Authentication & Authorization Design (AS-IS)

## 1. Overview

Hệ thống sử dụng **JWT (JSON Web Token)** để xác thực người dùng và bảo vệ API.

Mục tiêu hiện tại:

- Xác thực user thông qua token
- Cho phép truy cập API với token hợp lệ
- Hệ thống hoạt động theo mô hình stateless

---

## 2. Main Components

### AuthController

- Cung cấp các endpoint:
  - `/login`
  - `/register`
  - `/refresh`
  - `/logout`

---

### AuthService

- Xử lý:
  - Xác thực user
  - Tạo JWT token
- Sử dụng `PasswordEncoder` để kiểm tra mật khẩu

⚠️ Lưu ý:

- Chưa validate chặt chẽ email/password

---

### JwtUtil

- Tạo và validate JWT token
- Token chứa thông tin cơ bản và expiration

⚠️ Lưu ý:

- Chưa chứa role/permission trong payload

---

### JwtFilter

- Intercept request
- Lấy token từ header `Authorization`
- Validate:
  - Signature
  - Expiration
- Set authentication vào `SecurityContext`

⚠️ Lưu ý:

- Request không có token vẫn có thể đi tiếp (tùy config)
- Không có cơ chế revoke token

---

### SecurityConfig

- Cấu hình security toàn hệ thống
- Public:
  - `/auth/**`
- Các endpoint khác yêu cầu authentication
- Sử dụng cơ chế stateless

---

## 3. Processing Flows

### 3.1 Login Flow

1. Client gửi request `/login`
2. `AuthController` gọi `AuthService`
3. `AuthService` xác thực user
4. Nếu hợp lệ:
   - Tạo JWT token
5. Trả token về client

⚠️ Lưu ý:

- Chưa có rate limiting
- Chưa validate input rõ ràng

---

### 3.2 Authenticated Request Flow

1. Client gửi request:
   Authorization: Bearer <token>
2. `JwtFilter` intercept request
3. Extract token
4. Validate:

- Signature
- Expiration

5. Nếu hợp lệ:

- Set authentication vào `SecurityContext`

6. Request tiếp tục tới controller

⚠️ Lưu ý:

- Token không thể revoke
- Token hợp lệ cho đến khi hết hạn

---

### 3.3 Refresh Token Flow

- Endpoint: `/refresh`

Luồng hiện tại:

1. Client gọi `/refresh`
2. Server trả token mới

⚠️ Lưu ý:

- Chưa tách access token và refresh token
- Chưa lưu refresh token
- Không có rotate token

---

## 4. Current Behavior

Hệ thống hiện hỗ trợ:

- JWT authentication
- Password được mã hóa bằng `PasswordEncoder`
- `JwtFilter` validate token
- Security stateless
- Endpoint:
- `/login`
- `/register`
- `/refresh`
- `/logout`
- Token có expiration

---

## 5. Notes

Tài liệu này phản ánh trạng thái hiện tại của hệ thống (AS-IS) dựa trên code.
