-- ============================================
-- INSERT DỮ LIỆU MẪU (SAMPLE DATA)
-- ============================================

-- Sample Users
INSERT INTO users (username, email, password_hash, full_name, role) VALUES
('admin', 'admin@trello.com', '$2a$12$pNvceqsKyEbn/H41071JQOTGAclrV9Kw/XpOnA35M2gFEJjQMikCq', 'Admin User', 'ADMIN'),
('john_doe', 'john@example.com', '$2a$12$pNvceqsKyEbn/H41071JQOTGAclrV9Kw/XpOnA35M2gFEJjQMikCq', 'John Doe', 'USER'),
('jane_smith', 'jane@example.com', '$2a$12$pNvceqsKyEbn/H41071JQOTGAclrV9Kw/XpOnA35M2gFEJjQMikCq', 'Jane Smith', 'USER');

-- Sample Workspace
INSERT INTO workspaces (name, slug, description, created_by) VALUES
('My Workspace', 'my-workspace', 'Workspace chính của tôi', 1);

-- Sample Board
INSERT INTO boards (workspace_id, name, slug, description, created_by) VALUES
(1, 'Project Management', 'project-management', 'Quản lý dự án chính', 1);

-- Sample Lists
INSERT INTO lists (board_id, name, position) VALUES
(1, 'To Do', 1),
(1, 'In Progress', 2),
(1, 'Done', 3);

-- Sample Cards
INSERT INTO cards (list_id, title, description, position, created_by) VALUES
(1, 'Thiết kế database', 'Thiết kế schema cho ứng dụng', 1, 1),
(1, 'Tạo API endpoints', 'Xây dựng REST API', 2, 1),
(2, 'Phát triển frontend', 'Dùng React để xây giao diện', 1, 2);

-- Sample Labels
INSERT INTO labels (board_id, name, color) VALUES
(1, 'Bug', '#EB5A46'),
(1, 'Feature', '#61BD4F'),
(1, 'Urgent', '#FF9F1A');