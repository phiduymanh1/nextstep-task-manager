# nextstep-task-manager
## 🏗️ Cấu trúc Database
```
Workspace → Board → List → Card
```

## 📦 Nhóm bảng chính

### 👥 Users & Auth
- `users`, `user_profiles`, `auth_tokens`

### 🏢 Workspace
- `workspaces`, `workspace_members`

### 📋 Boards
- `boards`, `board_members`, `board_stars`, `lists`, `cards`

### ✅ Task Management
- `card_members`, `checklists`, `checklist_items`
- `labels`, `card_labels`, `attachments`, `comments`

### 🔔 Activity
- `activities`, `notifications`

### 🎨 Advanced
- `custom_fields`, `card_custom_field_values`

## ⚡ Điểm nổi bật

### 1. ENUM cho validation
```sql
role ENUM('owner', 'admin', 'member', 'guest')
```
✅ Tiết kiệm storage, validate tự động

### 2. DECIMAL cho Position
```sql
position DECIMAL(10,2)
```
✅ Drag & drop: `1.0` → `1.5` → `2.0` (không cần update hàng loạt)

### 3. JSON cho metadata
```sql
metadata JSON
```
✅ Lưu data linh hoạt: `{"from": "To Do", "to": "Done"}`

### 4. Soft Delete
```sql
is_archived BOOLEAN
```
✅ Không xóa thật → Có thể khôi phục

### 5. Slug SEO
```sql
slug VARCHAR(100)
```
✅ URL đẹp: `/board/my-project`

## 🛠️ Tech Stack
- MySQL 8.0+ / InnoDB / utf8mb4
- TIMESTAMP (auto timezone)
- Indexes trên FK, status, dates

## ✨ Features
Core: Workspace, Board, List, Card, Members, Labels, Checklists, Comments, Attachments, Activity Log, Notifications

Advanced: Custom Fields, Stars, Cover Images, Soft Delete, Role Permissions
