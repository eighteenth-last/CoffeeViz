-- ============================================================================
-- CoffeeViz 数据库初始化脚本
-- 版本: 1.0.0
-- 描述: 创建 CoffeeViz 架构可视化生成器所需的数据库表结构
-- ============================================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS coffeeviz
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE coffeeviz;

-- ============================================================================
-- 1. 用户表 (sys_user)
-- 描述: 存储系统用户信息，包括认证和个人资料
-- ============================================================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名（唯一）',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    display_name VARCHAR(50) COMMENT '显示名称',
    job_title VARCHAR(50) COMMENT '职位头衔',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态（1=正常，0=禁用）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================================
-- 2. 项目表 (biz_project)
-- 描述: 存储用户创建的架构可视化项目
-- ============================================================================
DROP TABLE IF EXISTS biz_project;
CREATE TABLE biz_project (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '项目ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    project_name VARCHAR(100) NOT NULL COMMENT '项目名称',
    description TEXT COMMENT '项目描述',
    source_type VARCHAR(20) NOT NULL COMMENT '来源类型（SQL/JDBC/AI）',
    db_type VARCHAR(20) COMMENT '数据库类型（mysql/postgres）',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态（DRAFT/PUBLISHED）',
    mermaid_code TEXT COMMENT 'Mermaid源码',
    svg_content MEDIUMTEXT COMMENT 'SVG内容',
    table_count INT DEFAULT 0 COMMENT '表数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    INDEX idx_user_status (user_id, status),
    CONSTRAINT fk_project_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

-- ============================================================================
-- 3. 项目配置表 (biz_project_config)
-- 描述: 存储项目的详细配置信息（SQL内容或JDBC连接信息）
-- ============================================================================
DROP TABLE IF EXISTS biz_project_config;
CREATE TABLE biz_project_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    config_type VARCHAR(20) NOT NULL COMMENT '配置类型（SQL/JDBC）',
    sql_content MEDIUMTEXT COMMENT 'SQL内容',
    jdbc_url VARCHAR(500) COMMENT 'JDBC URL',
    jdbc_username VARCHAR(100) COMMENT '数据库用户名',
    jdbc_password VARCHAR(255) COMMENT '数据库密码（AES加密）',
    schema_name VARCHAR(100) COMMENT 'Schema名称',
    render_options JSON COMMENT '渲染选项（JSON格式）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_project_id (project_id),
    CONSTRAINT fk_config_project FOREIGN KEY (project_id) REFERENCES biz_project(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目配置表';

-- ============================================================================
-- 4. 项目版本表 (biz_project_version)
-- 描述: 存储项目的历史版本记录
-- ============================================================================
DROP TABLE IF EXISTS biz_project_version;
CREATE TABLE biz_project_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '版本ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    version_no INT NOT NULL COMMENT '版本号',
    mermaid_code TEXT COMMENT 'Mermaid源码',
    change_log TEXT COMMENT '变更日志',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_project_id (project_id),
    INDEX idx_project_version (project_id, version_no),
    CONSTRAINT fk_version_project FOREIGN KEY (project_id) REFERENCES biz_project(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目版本表';

-- ============================================================================
-- 5. 系统配置表 (sys_config)
-- 描述: 存储系统级配置信息（如API密钥、系统参数等）
-- ============================================================================
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键（唯一）',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ============================================================================
-- 初始化系统配置数据
-- ============================================================================
INSERT INTO sys_config (config_key, config_value, description) VALUES
('openai.api.key', '', 'OpenAI API密钥'),
('openai.api.base_url', 'https://api.openai.com/v1', 'OpenAI API基础地址'),
('openai.model.name', 'gpt-4', 'OpenAI模型名称'),
('mermaid.cli.path', 'mmdc', 'Mermaid CLI路径'),
('mermaid.cli.timeout', '10000', 'Mermaid CLI超时时间（毫秒）'),
('export.max.size', '10485760', '导出文件最大大小（字节，默认10MB）'),
('export.temp.dir', '/tmp/coffeeviz', '临时文件目录'),
('sql.max.length', '1000000', 'SQL文本最大长度（字符）'),
('jdbc.connection.timeout', '10', 'JDBC连接超时时间（秒）'),
('jdbc.query.timeout', '30', 'JDBC查询超时时间（秒）'),
('rate.limit.per.second', '10', 'API限流：每秒请求数'),
('cache.expire.seconds', '3600', '缓存过期时间（秒）');

-- ============================================================================
-- 创建初始管理员用户（可选）
-- 用户名: admin
-- 密码: admin123 (BCrypt加密后的值)
-- 注意: 生产环境请修改密码！
-- ============================================================================
INSERT INTO sys_user (username, password, email, display_name, job_title, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@coffeeviz.com', '系统管理员', 'Administrator', 1);

-- ============================================================================
-- 脚本执行完成
-- ============================================================================
SELECT '数据库初始化完成！' AS message;
SELECT CONCAT('创建了 ', COUNT(*), ' 张表') AS table_count 
FROM information_schema.tables 
WHERE table_schema = 'coffeeviz';
