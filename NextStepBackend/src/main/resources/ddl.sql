-- Create database
CREATE DATABASE IF NOT EXISTS nextstep
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Select database
USE nextstep;

-- 1. BẢNG USERS - Quản lý người dùng
CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(100),
                       avatar_url VARCHAR(500),
                       phone VARCHAR(15) UNIQUE,
                       role ENUM('USER', 'ADMIN') DEFAULT 'USER',
                       is_active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       INDEX idx_email (email),
                       INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. BẢNG USER_PROFILES - Thông tin chi tiết người dùng
CREATE TABLE user_profiles (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               user_id INT UNIQUE NOT NULL,
                               bio TEXT,
                               date_of_birth DATE,
                               address VARCHAR(255),
                               timezone VARCHAR(50) DEFAULT 'Asia/Ho_Chi_Minh',
                               language VARCHAR(10) DEFAULT 'vi',
                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. BẢNG WORKSPACES - Không gian làm việc
CREATE TABLE workspaces (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(100) NOT NULL,
                            slug VARCHAR(100) UNIQUE NOT NULL, -- URL-friendly name
                            description TEXT,
                            visibility ENUM('PRIVATE', 'WORKSPACE', 'PUBLIC') DEFAULT 'PRIVATE',
                            created_by INT NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
                            INDEX idx_slug (slug),
                            INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. BẢNG WORKSPACE_MEMBERS - Thành viên trong workspace
CREATE TABLE workspace_members (
                                   id INT AUTO_INCREMENT PRIMARY KEY,
                                   workspace_id INT NOT NULL,
                                   user_id INT NOT NULL,
                                   role ENUM('OWNER', 'ADMIN', 'MEMBER', 'GUEST') DEFAULT 'MEMBER',
                                   joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
                                   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                   UNIQUE KEY unique_workspace_member (workspace_id, user_id),
                                   INDEX idx_workspace_id (workspace_id),
                                   INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. BẢNG BOARDS - Bảng làm việc
CREATE TABLE boards (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        workspace_id INT NOT NULL,
                        name VARCHAR(100) NOT NULL,
                        slug VARCHAR(100) NOT NULL,
                        description TEXT,
                        background_color VARCHAR(20) DEFAULT '#0079BF',
                        background_image_url VARCHAR(500),
                        visibility ENUM('PRIVATE', 'WORKSPACE', 'PUBLIC') DEFAULT 'WORKSPACE',
                        is_closed BOOLEAN DEFAULT FALSE,
                        created_by INT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
                        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
                        UNIQUE KEY unique_board_slug (workspace_id, slug),
                        INDEX idx_workspace_id (workspace_id),
                        INDEX idx_is_closed (is_closed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. BẢNG BOARD_MEMBERS - Thành viên có quyền truy cập board
CREATE TABLE board_members (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               board_id INT NOT NULL,
                               user_id INT NOT NULL,
                               role ENUM('ADMIN', 'MEMBER', 'OBSERVER') DEFAULT 'MEMBER',
                               joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                               UNIQUE KEY unique_board_member (board_id, user_id),
                               INDEX idx_board_id (board_id),
                               INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. BẢNG LISTS - Danh sách/Cột trong board (To Do, In Progress, Done)
CREATE TABLE lists (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       board_id INT NOT NULL,
                       name VARCHAR(100) NOT NULL,
                       position DECIMAL(10,2) NOT NULL, -- Dùng DECIMAL để dễ sắp xếp khi kéo thả
                       is_archived BOOLEAN DEFAULT FALSE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
                       INDEX idx_board_id (board_id),
                       INDEX idx_position (board_id, position),
                       INDEX idx_archived (is_archived)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. BẢNG CARDS - Thẻ công việc (Card/Task trong Trello)
CREATE TABLE cards (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       list_id INT NOT NULL,
                       title VARCHAR(255) NOT NULL,
                       description TEXT,
                       position DECIMAL(10,2) NOT NULL,
                       due_date DATETIME NULL,
                       due_reminder BOOLEAN DEFAULT FALSE,
                       is_completed BOOLEAN DEFAULT FALSE,
                       completed_at TIMESTAMP NULL,
                       cover_color VARCHAR(20),
                       cover_image_url VARCHAR(500),
                       is_archived BOOLEAN DEFAULT FALSE,
                       created_by INT NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       FOREIGN KEY (list_id) REFERENCES lists(id) ON DELETE CASCADE,
                       FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
                       INDEX idx_list_id (list_id),
                       INDEX idx_position (list_id, position),
                       INDEX idx_due_date (due_date),
                       INDEX idx_archived (is_archived),
                       INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. BẢNG CARD_MEMBERS - Người được giao thẻ
CREATE TABLE card_members (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              card_id INT NOT NULL,
                              user_id INT NOT NULL,
                              assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
                              FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                              UNIQUE KEY unique_card_member (card_id, user_id),
                              INDEX idx_card_id (card_id),
                              INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. BẢNG LABELS - Nhãn cho board
CREATE TABLE labels (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        board_id INT NOT NULL,
                        name VARCHAR(50) NOT NULL,
                        color VARCHAR(20) NOT NULL, -- #61BD4F, #F2D600, #FF9F1A, etc.
                        FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
                        INDEX idx_board_id (board_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. BẢNG CARD_LABELS - Gắn nhãn cho thẻ
CREATE TABLE card_labels (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             card_id INT NOT NULL,
                             label_id INT NOT NULL,
                             FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
                             FOREIGN KEY (label_id) REFERENCES labels(id) ON DELETE CASCADE,
                             UNIQUE KEY unique_card_label (card_id, label_id),
                             INDEX idx_card_id (card_id),
                             INDEX idx_label_id (label_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. BẢNG CHECKLISTS - Danh sách checklist trong thẻ
CREATE TABLE checklists (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            card_id INT NOT NULL,
                            title VARCHAR(255) NOT NULL,
                            position DECIMAL(10,2) NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
                            INDEX idx_card_id (card_id),
                            INDEX idx_position (card_id, position)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. BẢNG CHECKLIST_ITEMS - Các item trong checklist
CREATE TABLE checklist_items (
                                 id INT AUTO_INCREMENT PRIMARY KEY,
                                 checklist_id INT NOT NULL,
                                 content TEXT NOT NULL,
                                 is_completed BOOLEAN DEFAULT FALSE,
                                 completed_by INT NULL,
                                 completed_at TIMESTAMP NULL,
                                 position DECIMAL(10,2) NOT NULL,
                                 due_date DATETIME NULL,
                                 FOREIGN KEY (checklist_id) REFERENCES checklists(id) ON DELETE CASCADE,
                                 FOREIGN KEY (completed_by) REFERENCES users(id) ON DELETE SET NULL,
                                 INDEX idx_checklist_id (checklist_id),
                                 INDEX idx_position (checklist_id, position)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. BẢNG ATTACHMENTS - File đính kèm
CREATE TABLE attachments (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             card_id INT NOT NULL,
                             uploaded_by INT NOT NULL,
                             file_name VARCHAR(255) NOT NULL,
                             file_url VARCHAR(500) NOT NULL,
                             file_size BIGINT, -- bytes
                             mime_type VARCHAR(100),
                             is_cover BOOLEAN DEFAULT FALSE, -- Đặt làm ảnh bìa
                             uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
                             FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE CASCADE,
                             INDEX idx_card_id (card_id),
                             INDEX idx_uploaded_by (uploaded_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. BẢNG COMMENTS - Bình luận trong thẻ
CREATE TABLE comments (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          card_id INT NOT NULL,
                          user_id INT NOT NULL,
                          content TEXT NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
                          FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                          INDEX idx_card_id (card_id),
                          INDEX idx_user_id (user_id),
                          INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. BẢNG ACTIVITIES - Lịch sử hoạt động (Activity Log)
CREATE TABLE activities (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            card_id INT NULL,
                            board_id INT NULL,
                            user_id INT NOT NULL,
                            action_type VARCHAR(50) NOT NULL, -- created, updated, moved, archived, commented, etc.
                            entity_type VARCHAR(50) NOT NULL, -- card, list, board, comment, etc.
                            entity_id INT,
                            message TEXT, -- "Đã di chuyển thẻ từ 'To Do' sang 'In Progress'"
                            metadata JSON, -- Lưu data chi tiết dạng JSON
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
                            FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            INDEX idx_card_id (card_id),
                            INDEX idx_board_id (board_id),
                            INDEX idx_user_id (user_id),
                            INDEX idx_created_at (created_at),
                            INDEX idx_action_type (action_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. BẢNG NOTIFICATIONS - Thông báo
CREATE TABLE notifications (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               user_id INT NOT NULL,
                               type VARCHAR(50) NOT NULL, -- card_assigned, card_due, comment_mention, etc.
                               title VARCHAR(255) NOT NULL,
                               message TEXT,
                               entity_type VARCHAR(50), -- card, board, comment
                               entity_id INT,
                               link_url VARCHAR(500), -- URL để click vào
                               is_read BOOLEAN DEFAULT FALSE,
                               read_at TIMESTAMP NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                               INDEX idx_user_id (user_id),
                               INDEX idx_is_read (user_id, is_read),
                               INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18. BẢNG CUSTOM_FIELDS - Trường tùy chỉnh cho board (Premium feature)
CREATE TABLE custom_fields (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               board_id INT NOT NULL,
                               name VARCHAR(100) NOT NULL,
                               type ENUM('TEXT', 'NUMBER', 'DATE', 'CHECKBOX', 'DROPDOWN') NOT NULL,
                               options JSON, -- Cho dropdown: ["Option 1", "Option 2"]
                               position DECIMAL(10,2) NOT NULL,
                               FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
                               INDEX idx_board_id (board_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 19. BẢNG CARD_CUSTOM_FIELD_VALUES - Giá trị custom field của thẻ
CREATE TABLE card_custom_field_values (
                                          id INT AUTO_INCREMENT PRIMARY KEY,
                                          card_id INT NOT NULL,
                                          custom_field_id INT NOT NULL,
                                          value TEXT,
                                          FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
                                          FOREIGN KEY (custom_field_id) REFERENCES custom_fields(id) ON DELETE CASCADE,
                                          UNIQUE KEY unique_card_field (card_id, custom_field_id),
                                          INDEX idx_card_id (card_id),
                                          INDEX idx_custom_field_id (custom_field_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20. BẢNG AUTH_TOKENS - Token xác thực
CREATE TABLE auth_tokens (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             user_id INT NOT NULL,
                             token VARCHAR(500) UNIQUE NOT NULL,
                             refresh_token VARCHAR(500) UNIQUE,
                             device_info VARCHAR(255), -- User agent, device name
                             ip_address VARCHAR(45),
                             expires_at TIMESTAMP NOT NULL,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                             INDEX idx_user_id (user_id),
                             INDEX idx_token (token),
                             INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 21. BẢNG BOARD_STARS - Board được đánh dấu sao (yêu thích)
CREATE TABLE board_stars (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             board_id INT NOT NULL,
                             user_id INT NOT NULL,
                             position DECIMAL(10,2) NOT NULL, -- Thứ tự hiển thị
                             starred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
                             FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                             UNIQUE KEY unique_board_star (board_id, user_id),
                             INDEX idx_user_id (user_id),
                             INDEX idx_position (user_id, position)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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