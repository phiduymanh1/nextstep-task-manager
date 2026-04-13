# 🔐 Board Permission Service Design

## 1. 🎯 Mục tiêu

Thiết kế hệ thống kiểm tra quyền truy cập (authorization) cho Board đảm bảo:

- Phân quyền rõ ràng theo nhiều cấp:
  - Workspace
  - Board
- Dễ mở rộng và maintain
- Tách biệt giữa:
  - Data access
  - Business rule

---

## 2. 🧠 Ý tưởng thiết kế

Quyền truy cập vào Board không chỉ phụ thuộc vào:

- Role trong Board
- Mà còn phụ thuộc vào Role trong Workspace

👉 Vì vậy cần kiểm tra **2 lớp permission**:
Workspace Permission → Board Permission → Action

---

## 3. 🏗️ Kiến trúc

### 3.1 RoleBoardService

Đóng vai trò:

- Entry point cho việc kiểm tra quyền
- Lấy dữ liệu từ database
- Điều phối flow kiểm tra

---

### 3.2 PermissionService

Đóng vai trò:

- Chứa toàn bộ business rule về permission
- Không phụ thuộc database
- Có thể tái sử dụng ở nhiều module

---

## 4. 🔄 Luồng xử lý

### Bước 1: Xác định user có thuộc Board không

- Nếu không:
  → từ chối truy cập

---

### Bước 2: Xác định user có thuộc Workspace không

- Nếu không:
  → từ chối truy cập

---

### Bước 3: Kiểm tra quyền theo hành động

Các hành động chính:

- CREATE
- UPDATE
- DELETE

---

## 5. 🔐 Nguyên tắc phân quyền

### 5.1 Workspace Level

| Role   | Quyền        |
| ------ | ------------ |
| OWNER  | Full quyền   |
| ADMIN  | Gần như full |
| MEMBER | Hạn chế      |
| GUEST  | Chỉ đọc      |

---

### 5.2 Board Level

| Role     | Quyền            |
| -------- | ---------------- |
| MEMBER   | Có thể chỉnh sửa |
| OBSERVER | Chỉ xem          |

---

## 6. ⚖️ Rule kết hợp

Một action hợp lệ khi:

Workspace Role hợp lệ
AND
Board Role hợp lệ

---

### Ví dụ:

#### ❌ Không hợp lệ

- Workspace: GUEST
- Board: MEMBER  
  → Không được edit

---

#### ❌ Không hợp lệ

- Workspace: MEMBER
- Board: OBSERVER  
  → Không được edit

---

#### ✅ Hợp lệ

- Workspace: ADMIN
- Board: MEMBER  
  → Được edit

---

## 7. 🧩 Phân loại hành động

### 7.1 Edit / Update

Yêu cầu:

- Không phải GUEST (workspace)
- Không phải OBSERVER (board)

---

### 7.2 Delete

- Áp dụng cùng rule với Edit

---

### 7.3 Create (liên quan workspace)

- Chỉ:
  - OWNER
  - ADMIN

---

## 8. 💡 Lý do tách 2 service

### RoleBoardService

- Xử lý:
  - Lấy dữ liệu
  - Flow logic
- Phụ thuộc repository

---

### PermissionService

- Xử lý:
  - Rule thuần (pure logic)
- Không phụ thuộc DB
- Dễ test unit

---

## 9. 🚀 Ưu điểm thiết kế

- Tách biệt rõ trách nhiệm (SRP)
- Dễ mở rộng thêm role/action
- Tái sử dụng permission logic
- Dễ test (mock data)

---

## 10. 📌 Kết luận

Thiết kế này đảm bảo:

- Authorization đa cấp (workspace + board)
- Logic rõ ràng, dễ maintain
- Sẵn sàng mở rộng cho các module khác (List, Card, etc.)
