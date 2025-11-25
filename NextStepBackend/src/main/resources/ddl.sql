CREATE DATABASE nextstep CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- CHARACTER SET utf8mb4: hỗ trợ đầy đủ ký tự Unicode, kể cả emoji
-- COLLATE utf8mb4_unicode_ci: kiểu so sánh chuỗi, không phân biệt hoa thường

USE nextstep;

-- Bảng Users (người dùng)
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(50),
    user_email VARCHAR(100) UNIQUE NOT NULL,
    user_role VARCHAR(20),
    user_phone VARCHAR(10) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Bảng User Profiles ( chứa thông tin về lý lịch - ảnh đại diện - ngày sinh, .. ) của user
CREATE TABLE user_profiles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNIQUE,
    biography TEXT,         -- lý lịch / mô tả chi tiết
    date_of_birth DATE,
    address VARCHAR(255),
    avatar_url VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Bảng task statuses (phải tạo trước vì tasks phụ thuộc vào bảng này)
CREATE TABLE task_statuses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL, -- mã hệ thống: todo, in_progress, done
    label VARCHAR(100) NOT NULL,      -- nhãn hiển thị: "To Do", "In Progress", "Done"
    color VARCHAR(20),                -- màu nhãn cho UI (vd: #FF0000)
    sort_order INT DEFAULT 0          -- thứ tự hiển thị
);

-- Bảng Groups (nhóm) - đã sửa tên bảng
CREATE TABLE `groups` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_name VARCHAR(100),
    description TEXT,
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Bảng Group Members (thành viên trong nhóm, có role)
CREATE TABLE group_members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    group_id INT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'member',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES `groups`(id) ON DELETE CASCADE,
    UNIQUE KEY unique_member (user_id, group_id) -- 1 user chỉ có 1 vai trò trong group
);

-- Bảng Tasks (nhiệm vụ/công việc)
CREATE TABLE tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT NULL, -- nếu NULL thì là task cá nhân
    created_by INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status_id INT NOT NULL DEFAULT 1,
    due_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES `groups`(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (status_id) REFERENCES task_statuses(id) ON DELETE RESTRICT
);

-- Bảng Task Assignees (ai được giao task nào)
CREATE TABLE task_assignees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    task_id INT NOT NULL,
    user_id INT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_assignee (task_id, user_id) -- tránh giao trùng user cho 1 task
);

-- Bảng Task Activity Log (lịch sử của task)
CREATE TABLE task_activity_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    task_id INT NOT NULL,
    user_id INT NOT NULL, -- ai thực hiện hành động
    action_type VARCHAR(50) NOT NULL,
    message TEXT, -- mô tả hành động: "Phí Duy Mạnh đã hoàn tất Tạo class..."
    old_value TEXT NULL,
    new_value TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Bảng Task Comments (comment của task)
CREATE TABLE task_comments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    task_id INT NOT NULL,
    user_id INT NOT NULL,
    comment TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- (Tuỳ chọn) Bảng Auth Tokens - nếu bạn dùng JWT/session
CREATE TABLE auth_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token VARCHAR(500) NOT NULL,
    expired_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Bảng lịch sử thay đổi mật khẩu
CREATE TABLE password_change_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    changed_by INT NULL, -- nếu admin đổi hộ
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Thêm dữ liệu mẫu cho task_statuses
INSERT INTO task_statuses (code, label, color, sort_order) VALUES
('todo', 'To Do', '#6B7280', 1),
('in_progress', 'In Progress', '#3B82F6', 2),
('done', 'Done', '#10B981', 3);

INSERT INTO users (user_name, user_email, user_role, user_phone, password_hash)
VALUES
('Phí Duy Mạnh', 'manh@example.com', 'admin', '0912345678', '$2b$12$j8LHgMfQZGEEIOhDFmoEr.lk9MSK44hbGK1OBU2EPQhNGctaU3mQy'),
('Nguyễn Văn A', 'vana@example.com', 'user', '0987654321', '$2b$12$j8LHgMfQZGEEIOhDFmoEr.lk9MSK44hbGK1OBU2EPQhNGctaU3mQy'),
('Trần Thị B', 'thib@example.com', 'user', '0933666999', '$2b$12$j8LHgMfQZGEEIOhDFmoEr.lk9MSK44hbGK1OBU2EPQhNGctaU3mQy');


INSERT INTO user_profiles (user_id, biography, date_of_birth, address, avatar_url)
VALUES
(1, 'Founder of NextStep', '2000-01-01', 'Hà Nội', 'https://example.com/avatar1.jpg'),
(2, 'Front-end developer', '1999-05-20', 'TP.HCM', 'https://example.com/avatar2.jpg'),
(3, 'Mobile developer', '2001-11-11', 'Đà Nẵng', 'https://example.com/avatar3.jpg');


INSERT INTO `groups` (group_name, description, created_by)
VALUES
('Team Backend', 'Nhóm phát triển backend', 1),
('Team Mobile', 'Nhóm phát triển mobile', 2);


INSERT INTO group_members (user_id, group_id, role)
VALUES
(1, 1, 'leader'),
(2, 1, 'member'),
(3, 2, 'leader'),
(1, 2, 'member');


INSERT INTO tasks (group_id, created_by, title, description, status_id, due_date)
VALUES
(1, 1, 'Thiết kế API Login', 'Tạo API đăng nhập với JWT', 1, '2025-01-30'),
(1, 2, 'Viết API Task CRUD', 'Hoàn thiện CRUD task cho hệ thống', 2, '2025-02-05'),
(2, 3, 'Tạo màn hình Dashboard', 'UI dashboard cho mobile app', 1, '2025-02-10'),
(NULL, 1, 'Việc cá nhân: tập gym', 'Đi tập 1 tiếng mỗi ngày', 1, '2025-01-25');


INSERT INTO task_assignees (task_id, user_id)
VALUES
(1, 1),
(1, 2),
(2, 2),
(3, 3),
(4, 1);


INSERT INTO task_activity_log (task_id, user_id, action_type, message, old_value, new_value)
VALUES
(1, 1, 'create', 'Mạnh tạo task Thiết kế API Login', NULL, 'Task created'),
(1, 2, 'update', 'Văn A cập nhật mô tả task', 'Tạo API đăng nhập', 'Tạo API đăng nhập với JWT'),
(2, 2, 'status_change', 'Văn A chuyển trạng thái sang In Progress', 'todo', 'in_progress');


INSERT INTO task_comments (task_id, user_id, comment)
VALUES
(1, 2, 'API login này để hôm nay tôi làm nốt'),
(1, 1, 'Nhớ thêm refresh token nhé'),
(3, 3, 'UI màn hình chính tôi làm xong phần layout');


INSERT INTO auth_tokens (user_id, token, expired_at)
VALUES
(1, 'jwt-token-demo-1', '2025-12-31 23:59:59'),
(2, 'jwt-token-demo-2', '2025-12-31 23:59:59');


INSERT INTO password_change_history (user_id, changed_by)
VALUES
(1, NULL),
(2, 1),
(3, 1);


SHOW CREATE TABLE auth_tokens;