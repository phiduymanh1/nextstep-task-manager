-- 1. BẢNG USERS - Quản lý người dùng
CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL,
                       email VARCHAR(100) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(100),
                       avatar_url VARCHAR(500),
                       avatar_public_id VARCHAR(255),
                       phone VARCHAR(15),
                       role VARCHAR(20) DEFAULT 'USER',
                       is_active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT uq_users_username UNIQUE (username),
                       CONSTRAINT uq_users_email UNIQUE (email),
                       CONSTRAINT uq_users_phone UNIQUE (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);

-- 2. BẢNG USER_PROFILES - Thông tin chi tiết người dùng
CREATE TABLE user_profiles (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               user_id INT NOT NULL,
                               bio TEXT,
                               date_of_birth DATE,
                               address VARCHAR(255),
                               timezone VARCHAR(50) DEFAULT 'Asia/Ho_Chi_Minh',
                               language VARCHAR(10) DEFAULT 'vi',

                               CONSTRAINT uq_user_profiles_user UNIQUE (user_id),
                               CONSTRAINT fk_user_profiles_user
                                   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. BẢNG WORKSPACES - Không gian làm việc
CREATE TABLE workspaces (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(100) NOT NULL,
                            slug VARCHAR(100) NOT NULL, -- URL-friendly name
                            description TEXT,
                            visibility VARCHAR(20) DEFAULT 'PRIVATE',
                            created_by INT NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            deleted BOOLEAN DEFAULT FALSE,
                            CONSTRAINT uq_workspace_slug_delete UNIQUE (slug, deleted),
                            CONSTRAINT fk_workspace_creator
                                FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_workspace_created_by ON workspaces(created_by);
CREATE INDEX idx_workspace_slug ON workspaces(slug);
-- 4. BẢNG WORKSPACE_MEMBERS - Thành viên trong workspace
CREATE TABLE workspace_members (
                                   id INT AUTO_INCREMENT PRIMARY KEY,
                                   workspace_id INT NOT NULL,
                                   user_id INT NOT NULL,
                                   role VARCHAR(20) DEFAULT 'MEMBER',
                                   joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT uq_workspace_member UNIQUE (workspace_id, user_id),
                                   CONSTRAINT fk_wm_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
                                   CONSTRAINT fk_wm_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_wm_workspace_id ON workspace_members(workspace_id);
CREATE INDEX idx_wm_user_id ON workspace_members(user_id);

-- 5. BẢNG BOARDS - Bảng làm việc
CREATE TABLE boards (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        workspace_id INT NOT NULL,
                        name VARCHAR(100) NOT NULL,
                        slug VARCHAR(100) NOT NULL,
                        description TEXT,
                        background_color VARCHAR(20) DEFAULT '#0079BF',
                        background_image_url VARCHAR(500),
                        visibility VARCHAR(20) DEFAULT 'WORKSPACE',
                        is_closed BOOLEAN DEFAULT FALSE,
                        created_by INT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                        CONSTRAINT uq_board_slug UNIQUE (workspace_id, slug),
                        CONSTRAINT fk_board_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
                        CONSTRAINT fk_board_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_board_workspace_id ON boards(workspace_id);
CREATE INDEX idx_board_is_closed ON boards(is_closed);

-- 6. BẢNG BOARD_MEMBERS - Thành viên có quyền truy cập board
CREATE TABLE board_members (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               board_id INT NOT NULL,
                               user_id INT NOT NULL,
                               role VARCHAR(20) DEFAULT 'MEMBER',
                               joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT uq_board_member UNIQUE (board_id, user_id),
                               CONSTRAINT fk_board_members_board FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
                               CONSTRAINT fk_board_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_board_members_board_id ON board_members(board_id);
CREATE INDEX idx_board_members_user_id ON board_members(user_id);

-- 7. BẢNG LISTS - Danh sách/Cột trong board (To Do, In Progress, Done)
CREATE TABLE lists (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       board_id INT NOT NULL,
                       name VARCHAR(100) NOT NULL,
                       position DECIMAL(10,2) NOT NULL, -- Dùng DECIMAL để dễ sắp xếp khi kéo thả
                       is_archived BOOLEAN DEFAULT FALSE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                       CONSTRAINT fk_list_board FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_list_board_id ON lists(board_id);
CREATE INDEX idx_list_board_position ON lists(board_id, position);
CREATE INDEX idx_list_archived ON lists(is_archived);

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

                       CONSTRAINT fk_card_list FOREIGN KEY (list_id) REFERENCES lists(id) ON DELETE CASCADE,
                       CONSTRAINT fk_card_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_card_list_position ON cards(list_id, position);
CREATE INDEX idx_card_archived ON cards(is_archived);
CREATE INDEX idx_card_created_by ON cards(created_by);
CREATE INDEX idx_card_list_id ON cards(list_id);
CREATE INDEX idx_card_due_date ON cards(due_date);

-- 9. BẢNG CARD_MEMBERS - Người được giao thẻ
CREATE TABLE card_members (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              card_id INT NOT NULL,
                              user_id INT NOT NULL,
                              assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT uq_card_member UNIQUE (card_id, user_id),
                              CONSTRAINT fk_card_members_card FOREIGN KEY (card_id) REFERENCES cards(id),
                              CONSTRAINT fk_card_members_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_card_members_card_id ON card_members(card_id);
CREATE INDEX idx_card_members_user_id ON card_members(user_id);

-- 10. BẢNG LABELS - Nhãn cho board
CREATE TABLE labels (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        board_id INT NOT NULL,
                        name VARCHAR(50) NOT NULL,
                        color VARCHAR(20) NOT NULL, -- #61BD4F, #F2D600, #FF9F1A, etc.

                        CONSTRAINT fk_labels_board FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_labels_board_id ON labels(board_id);

-- 11. BẢNG CARD_LABELS - Gắn nhãn cho thẻ
CREATE TABLE card_labels (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             card_id INT NOT NULL,
                             label_id INT NOT NULL,

                             CONSTRAINT uq_card_label UNIQUE (card_id, label_id),
                             CONSTRAINT fk_card_labels_card FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
                             CONSTRAINT fk_card_labels_label FOREIGN KEY (label_id) REFERENCES labels(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_card_labels_card_id ON card_labels(card_id);
CREATE INDEX idx_card_labels_label_id ON card_labels(label_id);

-- 12. BẢNG CHECKLISTS - Danh sách checklist trong thẻ
CREATE TABLE checklists (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            card_id INT NOT NULL,
                            title VARCHAR(255) NOT NULL,
                            position DECIMAL(10,2) NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT fk_checklists_card FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_checklists_card_position ON checklists(card_id, position);
CREATE INDEX idx_checklists_card_id ON checklists(card_id);

-- 13. BẢNG CHECKLIST_ITEMS - Các item trong checklist
CREATE TABLE checklist_items (
                                 id INT AUTO_INCREMENT PRIMARY KEY,
                                 checklist_id INT NOT NULL,
                                 content TEXT NOT NULL,
                                 is_completed BOOLEAN DEFAULT FALSE,
                                 completed_by INT,
                                 completed_at TIMESTAMP,
                                 position DECIMAL(10,2) NOT NULL,
                                 due_date DATETIME,

                                 CONSTRAINT fk_checklist_items_checklist FOREIGN KEY (checklist_id) REFERENCES checklists(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_checklist_items_completed_by FOREIGN KEY (completed_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_checklist_items_checklist_position ON checklist_items(checklist_id, position);
CREATE INDEX idx_checklist_items_checklist_id ON checklist_items(checklist_id);

-- 14. BẢNG ATTACHMENTS - File đính kèm
CREATE TABLE attachments (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             card_id INT NOT NULL,
                             uploaded_by INT NOT NULL,
                             file_name VARCHAR(255) NOT NULL,
                             file_url VARCHAR(500) NOT NULL,
                             public_id VARCHAR(255),
                             file_size BIGINT, -- bytes
                             mime_type VARCHAR(100),
                             is_cover BOOLEAN DEFAULT FALSE, -- Đặt làm ảnh bìa
                             uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_attachments_card FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
                             CONSTRAINT fk_attachments_uploader FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_attachments_card_id ON attachments(card_id);
CREATE INDEX idx_attachments_uploaded_by ON attachments(uploaded_by);

-- 15. BẢNG COMMENTS - Bình luận trong thẻ
CREATE TABLE comments (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          card_id INT NOT NULL,
                          user_id INT NOT NULL,
                          content TEXT NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                          CONSTRAINT fk_comments_card FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
                          CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_comments_card_id ON comments(card_id);
CREATE INDEX idx_comments_user_id ON comments(user_id);
CREATE INDEX idx_comments_created_at ON comments(created_at);

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

                            CONSTRAINT fk_activities_card FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
                            CONSTRAINT fk_activities_board FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
                            CONSTRAINT fk_activities_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_activities_card_id ON activities(card_id);
CREATE INDEX idx_activities_board_id ON activities(board_id);
CREATE INDEX idx_activities_user_id ON activities(user_id);
CREATE INDEX idx_activities_created_at ON activities(created_at);
CREATE INDEX idx_activities_action_type ON activities(action_type);

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
                               CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_user_id ON notifications(entity_type, entity_id);

-- 18. BẢNG CUSTOM_FIELDS - Trường tùy chỉnh cho board (Premium feature)
CREATE TABLE custom_fields (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               board_id INT NOT NULL,
                               name VARCHAR(100) NOT NULL,
                               type VARCHAR(20) NOT NULL,
                               options JSON, -- Cho dropdown: ["Option 1", "Option 2"]
                               position DECIMAL(10,2) NOT NULL,
                               CONSTRAINT fk_custom_fields_board FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_custom_fields_board_id ON custom_fields(board_id);

-- 19. BẢNG CARD_CUSTOM_FIELD_VALUES - Giá trị custom field của thẻ
CREATE TABLE card_custom_field_values (
                                          id INT AUTO_INCREMENT PRIMARY KEY,
                                          card_id INT NOT NULL,
                                          custom_field_id INT NOT NULL,
                                          field_value  TEXT,

                                          CONSTRAINT uq_card_custom_field UNIQUE (card_id, custom_field_id),
                                          CONSTRAINT fk_ccfv_card FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
                                          CONSTRAINT fk_ccfv_custom_field FOREIGN KEY (custom_field_id) REFERENCES custom_fields(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_ccfv_card_id ON card_custom_field_values(card_id);
CREATE INDEX idx_ccfv_custom_field_id ON card_custom_field_values(custom_field_id);

-- 20. BẢNG AUTH_TOKENS - Token xác thực
CREATE TABLE auth_tokens (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             user_id INT NOT NULL,
                             token VARCHAR(255) NOT NULL,
                             refresh_token VARCHAR(255),
                             device_info VARCHAR(255), -- User agent, device name
                             ip_address VARCHAR(45),
                             expires_at TIMESTAMP NOT NULL,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT uq_auth_token UNIQUE (token,refresh_token),
                             CONSTRAINT fk_auth_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_auth_tokens_user_id ON auth_tokens(user_id);
CREATE INDEX idx_auth_tokens_expires_at ON auth_tokens(expires_at);
CREATE INDEX idx_auth_tokens_token ON auth_tokens(token);

-- 21. BẢNG BOARD_STARS - Board được đánh dấu sao (yêu thích)
CREATE TABLE board_stars (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             board_id INT NOT NULL,
                             user_id INT NOT NULL,
                             position DECIMAL(10,2) NOT NULL, -- Thứ tự hiển thị
                             starred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT uq_board_star UNIQUE (board_id, user_id),
                             CONSTRAINT fk_board_stars_board FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
                             CONSTRAINT fk_board_stars_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_board_stars_user_position ON board_stars(user_id, position);
CREATE INDEX idx_board_stars_user_id ON board_stars(user_id);

CREATE TABLE password_reset_tokens (
                                       id INT AUTO_INCREMENT PRIMARY KEY,
                                       user_id INT NOT NULL,
                                       token VARCHAR(255) NOT NULL,
                                       expires_at TIMESTAMP NOT NULL,
                                       used BOOLEAN NOT NULL DEFAULT FALSE,
                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                       CONSTRAINT uq_password_reset_token UNIQUE (token),
                                       CONSTRAINT fk_password_reset_user
                                           FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_tokens_expires_at ON password_reset_tokens(expires_at);
CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);