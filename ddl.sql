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

-- Bảng Groups (nhóm)
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

-- Bảng task statuses 
CREATE TABLE task_statuses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL, -- mã hệ thống: todo, in_progress, done
    label VARCHAR(100) NOT NULL,      -- nhãn hiển thị: "To Do", "In Progress", "Done"
    color VARCHAR(20),                -- màu nhãn cho UI (vd: #FF0000)
    sort_order INT DEFAULT 0          -- thứ tự hiển thị
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
    create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);


CREATE TABLE password_change_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    changed_by INT NULL, -- nếu admin đổi hộ
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE SET NULL
);