# Authentication Improvements & Roadmap

## 1. Mục tiêu

Cải thiện hệ thống authentication hiện tại để:

- Tăng bảo mật
- Đạt mức production-ready
- Hỗ trợ phân quyền (RBAC)
- Quản lý token hiệu quả

---

## 2. Token Management

### 2.1 Token Blacklist

- Lưu token vào database hoặc Redis
- Khi logout → đưa token vào blacklist
- JwtFilter cần check blacklist

---

### 2.2 Revoke Token

- Hỗ trợ revoke token trước khi hết hạn
- Áp dụng cho:
  - Logout
  - Reset password
  - Security event

---

## 3. Access Token & Refresh Token

### 3.1 Tách token

- Access Token:
  - Short-lived (15–30 phút)
- Refresh Token:
  - Long-lived (7–30 ngày)

---

### 3.2 Lưu Refresh Token

- Lưu trong DB
- Gắn với user
- Có expiration riêng

---

### 3.3 Rotate Refresh Token

- Mỗi lần refresh → cấp token mới
- Token cũ bị invalid

---

## 4. JWT Payload Improvement

- Thêm:
  - `userId`
  - `role`
- Có thể bổ sung:
  - `iss` (issuer)
  - `aud` (audience)
  - `jti` (token id)

---

## 5. Authorization (RBAC)

- Áp dụng role-based access control:
  - OWNER
  - ADMIN
  - MEMBER
  - GUEST

- Sử dụng:
  - `@PreAuthorize`
  - hoặc check trong service

---

## 6. Security Hardening

### 6.1 Login Protection

- Rate limiting
- Account lock khi login sai nhiều lần

---

### 6.2 Input Validation

- Validate email format
- Validate password strength

---

### 6.3 Email Verification

- Xác thực email khi đăng ký

---

## 7. Password Reset

- Tạo token reset riêng
- Có expiration
- Validate token khi reset

---

## 8. Future Enhancements

- OAuth2 (Google login)
- Multi-device session management
- Audit log (login history)

---

## 9. Notes

Tài liệu này mô tả các hướng cải tiến từ hệ thống hiện tại (AS-IS) lên mức production-ready (TO-BE).
