/*
 Navicat Premium Dump SQL

 Source Server         : WSL_MySQL
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45-0ubuntu0.24.04.1)
 Source Host           : 127.0.0.1:3306
 Source Schema         : coffeeviz

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45-0ubuntu0.24.04.1)
 File Encoding         : 65001

 Date: 10/02/2026 18:46:47
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for biz_diagram
-- ----------------------------
DROP TABLE IF EXISTS `biz_diagram`;
CREATE TABLE `biz_diagram`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '架构图ID',
  `repository_id` bigint NOT NULL COMMENT '架构库ID',
  `save_source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'personal' COMMENT '保存来源: personal-个人, team-团队',
  `save_by_user_id` bigint NULL DEFAULT NULL COMMENT '保存人ID（团队成员保存时记录）',
  `diagram_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '架构图名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '架构图描述',
  `source_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '来源类型（SQL/JDBC/AI）',
  `db_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '数据库类型（mysql/postgres）',
  `mermaid_code` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'Mermaid源码',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片URL（MinIO存储）',
  `table_count` int NULL DEFAULT 0 COMMENT '表数量',
  `relation_count` int NULL DEFAULT 0 COMMENT '关系数量',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_repository_id`(`repository_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `fk_diagram_repository` FOREIGN KEY (`repository_id`) REFERENCES `biz_repository` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '架构图表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of biz_diagram
-- ----------------------------
INSERT INTO `biz_diagram` VALUES (1, 7, 'personal', NULL, '测试', '', 'SQL', 'mysql', 'erDiagram\n    biz_diagram {\n        bigint id PK \"架构图ID\"\n        bigint repository_id FK \"NOT NULL, 架构库ID\"\n        varchar(100) diagram_name \"NOT NULL, 架构图名称\"\n        text description \"架构图描述\"\n        varchar(20) source_type \"NOT NULL, 来源类型（SQL/JDBC/AI）\"\n        varchar(20) db_type \"数据库类型（mysql/postgres）\"\n        text mermaid_code \"Mermaid源码\"\n        varchar(500) image_url \"图片URL（MinIO存储）\"\n        int table_count \"表数量\"\n        int relation_count \"关系数量\"\n        datetime create_time \"创建时间\"\n        datetime update_time \"更新时间\"\n    }\n    biz_repository {\n        bigint id PK \"项目ID\"\n        bigint user_id FK \"NOT NULL, 用户ID\"\n        varchar(100) repository_name \"NOT NULL, 架构库名称\"\n        text description \"项目描述\"\n        int diagram_count \"架构图数量\"\n        varchar(20) status \"状态（DRAFT/PUBLISHED）\"\n        datetime create_time \"创建时间\"\n        datetime update_time \"更新时间\"\n    }\n    biz_repository_config {\n        bigint id PK \"配置ID\"\n        bigint repository_id FK \"NOT NULL, 架构库ID\"\n        varchar(20) config_type \"NOT NULL, 配置类型（SQL/JDBC）\"\n        mediumtext sql_content \"SQL内容\"\n        varchar(500) jdbc_url \"JDBC URL\"\n        varchar(100) jdbc_username \"数据库用户名\"\n        varchar(255) jdbc_password \"数据库密码（AES加密）\"\n        varchar(100) schema_name \"Schema名称\"\n        json render_options \"渲染选项（JSON格式）\"\n        datetime create_time \"创建时间\"\n    }\n    sys_config {\n        bigint id PK \"配置ID\"\n        varchar(100) config_key \"NOT NULL, 配置键（唯一）\"\n        text config_value \"配置值\"\n        varchar(500) description \"描述\"\n        datetime create_time \"创建时间\"\n        datetime update_time \"更新时间\"\n    }\n    sys_user {\n        bigint id PK \"用户ID\"\n        varchar(50) username \"NOT NULL, 用户名（唯一）\"\n        varchar(255) password \"NOT NULL, 密码（BCrypt加密）\"\n        varchar(100) email \"邮箱\"\n        varchar(20) phone \"手机号\"\n        varchar(50) display_name \"显示名称\"\n        varchar(50) job_title \"职位头衔\"\n        varchar(500) avatar_url \"头像URL\"\n        tinyint status \"状态（1=正常，0=禁用）\"\n        datetime create_time \"创建时间\"\n        datetime update_time \"更新时间\"\n    }\n    biz_repository ||--o{ biz_diagram : \"repository_id\"\n    sys_user ||--o{ biz_repository : \"user_id\"\n    biz_repository ||--o{ biz_repository_config : \"repository_id\"\n', 'http://localhost:8080/api/image/diagrams/7/1770528744064_测试.png', 5, 3, '2026-02-08 13:32:24', '2026-02-08 13:32:24');

-- ----------------------------
-- Table structure for biz_payment_order
-- ----------------------------
DROP TABLE IF EXISTS `biz_payment_order`;
CREATE TABLE `biz_payment_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `subscription_id` bigint NULL DEFAULT NULL COMMENT '订阅ID',
  `plan_id` bigint NOT NULL COMMENT '计划ID',
  `plan_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '计划代码',
  `billing_cycle` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'monthly' COMMENT '计费周期',
  `amount` decimal(10, 2) NOT NULL COMMENT '订单金额',
  `currency` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'CNY' COMMENT '货币类型',
  `payment_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付方式',
  `payment_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '支付状态（pending/paid/failed/refunded）',
  `transaction_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '第三方交易ID',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `refund_time` datetime NULL DEFAULT NULL COMMENT '退款时间',
  `refund_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '退款原因',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_subscription_id`(`subscription_id` ASC) USING BTREE,
  INDEX `idx_payment_status`(`payment_status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '支付订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of biz_payment_order
-- ----------------------------
INSERT INTO `biz_payment_order` VALUES (1, 'PAY20260208145150509045', 1, NULL, 2, 'PRO', 'monthly', 29.00, 'CNY', 'ALIPAY', 'pending', NULL, NULL, NULL, NULL, NULL, '2026-02-08 14:51:51', '2026-02-08 14:51:51');
INSERT INTO `biz_payment_order` VALUES (2, 'PAY20260208174645871407', 1, NULL, 2, 'PRO', 'monthly', 29.00, 'CNY', 'ALIPAY', 'pending', NULL, NULL, NULL, NULL, NULL, '2026-02-08 17:46:45', '2026-02-08 17:46:45');
INSERT INTO `biz_payment_order` VALUES (3, 'PAY20260208175209919583', 1, NULL, 2, 'PRO', 'monthly', 29.00, 'CNY', 'ALIPAY', 'pending', NULL, NULL, NULL, NULL, NULL, '2026-02-08 17:52:10', '2026-02-08 17:52:10');
INSERT INTO `biz_payment_order` VALUES (4, 'PAY20260208175818157234', 1, NULL, 2, 'PRO', 'monthly', 29.00, 'CNY', 'ALIPAY', 'pending', NULL, NULL, NULL, NULL, NULL, '2026-02-08 17:58:18', '2026-02-08 17:58:18');

-- ----------------------------
-- Table structure for biz_plan_quota
-- ----------------------------
DROP TABLE IF EXISTS `biz_plan_quota`;
CREATE TABLE `biz_plan_quota`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配额ID',
  `plan_id` bigint NOT NULL COMMENT '订阅计划ID',
  `quota_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配额类型（repository/diagram/sql_parse/ai_generate）',
  `quota_limit` int NOT NULL COMMENT '配额限制（-1表示无限制）',
  `reset_cycle` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'monthly' COMMENT '重置周期（daily/monthly/yearly/never）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '配额描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_plan_quota_type`(`plan_id` ASC, `quota_type` ASC) USING BTREE,
  INDEX `idx_plan_id`(`plan_id` ASC) USING BTREE,
  CONSTRAINT `fk_plan_quota_plan` FOREIGN KEY (`plan_id`) REFERENCES `biz_subscription_plan` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '计划配额配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_plan_quota
-- ----------------------------
INSERT INTO `biz_plan_quota` VALUES (1, 1, 'ai_generate', 0, 'never', 'AI生成不可用', '2026-02-10 17:31:37', '2026-02-10 17:35:37');
INSERT INTO `biz_plan_quota` VALUES (2, 1, 'diagram', 10, 'monthly', '每月10次架构图保存', '2026-02-10 17:31:37', '2026-02-10 17:43:01');
INSERT INTO `biz_plan_quota` VALUES (3, 1, 'repository', 3, 'never', '最多3个归档库', '2026-02-10 17:31:37', '2026-02-10 17:35:37');
INSERT INTO `biz_plan_quota` VALUES (4, 1, 'sql_parse', 50, 'monthly', '每月50次SQL解析操作', '2026-02-10 17:31:37', '2026-02-10 17:35:37');
INSERT INTO `biz_plan_quota` VALUES (5, 3, 'ai_generate', 1000, 'monthly', '每月1000次AI生成', '2026-02-10 17:31:37', '2026-02-10 17:43:01');
INSERT INTO `biz_plan_quota` VALUES (6, 3, 'diagram', -1, 'never', '无限架构图保存', '2026-02-10 17:31:37', '2026-02-10 17:43:01');
INSERT INTO `biz_plan_quota` VALUES (7, 3, 'repository', -1, 'never', '无限归档库', '2026-02-10 17:31:37', '2026-02-10 17:43:01');
INSERT INTO `biz_plan_quota` VALUES (8, 3, 'sql_parse', -1, 'never', '无限SQL解析', '2026-02-10 17:31:37', '2026-02-10 17:43:01');
INSERT INTO `biz_plan_quota` VALUES (20, 2, 'repository', 20, 'never', '最多20个归档库', '2026-02-10 17:35:37', '2026-02-10 17:43:01');
INSERT INTO `biz_plan_quota` VALUES (21, 2, 'diagram', 500, 'monthly', '每月500次架构图保存', '2026-02-10 17:35:37', '2026-02-10 17:43:01');
INSERT INTO `biz_plan_quota` VALUES (22, 2, 'sql_parse', 1000, 'monthly', '每月1000次SQL解析', '2026-02-10 17:35:37', '2026-02-10 17:43:01');
INSERT INTO `biz_plan_quota` VALUES (23, 2, 'ai_generate', 100, 'monthly', '每月100次AI生成', '2026-02-10 17:35:37', '2026-02-10 17:43:01');

-- ----------------------------
-- Table structure for biz_project_config
-- ----------------------------
DROP TABLE IF EXISTS `biz_project_config`;
CREATE TABLE `biz_project_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `config_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置类型（SQL/JDBC）',
  `sql_content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'SQL内容',
  `jdbc_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'JDBC URL',
  `jdbc_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '数据库用户名',
  `jdbc_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '数据库密码（AES加密）',
  `schema_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Schema名称',
  `render_options` json NULL COMMENT '渲染选项（JSON格式）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_project_id`(`project_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '项目配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of biz_project_config
-- ----------------------------

-- ----------------------------
-- Table structure for biz_project_version
-- ----------------------------
DROP TABLE IF EXISTS `biz_project_version`;
CREATE TABLE `biz_project_version`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '版本ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `version_no` int NOT NULL COMMENT '版本号',
  `mermaid_code` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'Mermaid源码',
  `change_log` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '变更日志',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project_id`(`project_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '项目版本表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of biz_project_version
-- ----------------------------

-- ----------------------------
-- Table structure for biz_repository
-- ----------------------------
DROP TABLE IF EXISTS `biz_repository`;
CREATE TABLE `biz_repository`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `repository_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '架构库名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '项目描述',
  `diagram_count` int NULL DEFAULT 0 COMMENT '架构图数量',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'DRAFT' COMMENT '状态（DRAFT/PUBLISHED）',
  `is_team_repository` tinyint(1) NULL DEFAULT 0 COMMENT '是否为团队归档库',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_user_status`(`user_id` ASC, `status` ASC) USING BTREE,
  CONSTRAINT `fk_project_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '项目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_repository
-- ----------------------------
INSERT INTO `biz_repository` VALUES (7, 1, '架构可视化E-R图', '', 1, 'active', 1, '2026-02-08 13:32:24', '2026-02-08 13:32:23');
INSERT INTO `biz_repository` VALUES (13, 1, 'd', '', 0, 'active', 0, '2026-02-10 11:52:03', '2026-02-10 11:52:03');

-- ----------------------------
-- Table structure for biz_subscription_plan
-- ----------------------------
DROP TABLE IF EXISTS `biz_subscription_plan`;
CREATE TABLE `biz_subscription_plan`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '计划ID',
  `plan_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '计划代码（FREE/PRO/TEAM）',
  `plan_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '计划名称',
  `plan_name_en` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '计划英文名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '计划描述',
  `price_monthly` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '月付价格',
  `price_yearly` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '年付价格',
  `max_repositories` int NULL DEFAULT -1 COMMENT '最大架构库数量（-1表示无限制）',
  `max_diagrams_per_repo` int NULL DEFAULT -1 COMMENT '每个库最大架构图数量（-1表示无限制）',
  `max_sql_size_mb` int NULL DEFAULT 10 COMMENT '最大SQL文件大小（MB）',
  `support_jdbc` tinyint NULL DEFAULT 0 COMMENT '是否支持JDBC连接（0=否，1=是）',
  `support_ai` tinyint NULL DEFAULT 0 COMMENT '是否支持AI功能（0=否，1=是）',
  `support_export` tinyint NULL DEFAULT 1 COMMENT '是否支持导出（0=否，1=是）',
  `support_team` tinyint NULL DEFAULT 0 COMMENT '是否支持团队协作（0=否，1=是）',
  `max_teams` int NULL DEFAULT 0 COMMENT '最大团队数量',
  `max_team_members` int NULL DEFAULT 0 COMMENT '每个团队最大成员数',
  `priority_support` tinyint NULL DEFAULT 0 COMMENT '是否优先支持（0=否，1=是）',
  `features` json NULL COMMENT '功能特性列表（JSON格式）',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序顺序',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active' COMMENT '状态（active/inactive）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_plan_code`(`plan_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订阅计划表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of biz_subscription_plan
-- ----------------------------
INSERT INTO `biz_subscription_plan` VALUES (1, 'FREE', '社区版', 'Community', '适合个人开发者和小型项目使用', 0.00, 0.00, 3, 10, 5, 0, 0, 1, 0, 0, 0, 0, '[\"3个架构库\", \"10次架构图/月\", \"50次SQL解析/月\", \"基础ER图生成\", \"MySQL支持\", \"社区支持\"]', 1, 'active', '2026-02-08 14:16:32', '2026-02-10 13:07:08');
INSERT INTO `biz_subscription_plan` VALUES (2, 'PRO', '专业版', 'Professional', '适合开发者和生产力工具使用', 29.00, 290.00, 20, 500, 50, 1, 1, 1, 0, 0, 0, 0, '[\"20个架构库\", \"500次架构图/月\", \"1000次SQL解析/月\", \"100次AI生成/月\", \"多数据库支持\", \"JDBC实时连接\", \"高清导出\", \"优先支持\"]', 2, 'active', '2026-02-08 14:16:32', '2026-02-10 13:06:45');
INSERT INTO `biz_subscription_plan` VALUES (3, 'TEAM', '团队版', 'Team', '适合企业团队协作使用', 99.00, 990.00, -1, -1, 200, 1, 1, 1, 1, 1, 10, 1, '[\"无限架构库\", \"无限架构图\", \"无限SQL解析\", \"1000次AI生成/月\", \"团队协作\", \"版本控制\", \"API集成\", \"私有部署\", \"专属支持\"]', 3, 'active', '2026-02-08 14:16:32', '2026-02-10 12:19:31');

-- ----------------------------
-- Table structure for biz_team
-- ----------------------------
DROP TABLE IF EXISTS `biz_team`;
CREATE TABLE `biz_team`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '团队ID',
  `team_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '团队名称',
  `team_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '团队唯一标识',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '团队描述',
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '团队头像',
  `owner_id` bigint NOT NULL COMMENT '所有者ID',
  `repository_id` bigint NOT NULL COMMENT '绑定的架构归档库ID',
  `member_count` int NULL DEFAULT 1 COMMENT '成员数量',
  `max_members` int NULL DEFAULT 10 COMMENT '最大成员数（根据订阅套餐）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active' COMMENT '状态: active-活跃, suspended-暂停, deleted-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_team_code`(`team_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_repository_id`(`repository_id` ASC) USING BTREE,
  INDEX `idx_owner_id`(`owner_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '团队表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_team
-- ----------------------------
INSERT INTO `biz_team` VALUES (1, '测试', 'TEAM_1770700953662_3685', '', NULL, 1, 7, 1, 10, 'active', '2026-02-10 13:22:34', '2026-02-10 13:22:34');

-- ----------------------------
-- Table structure for biz_team_invitation
-- ----------------------------
DROP TABLE IF EXISTS `biz_team_invitation`;
CREATE TABLE `biz_team_invitation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '邀请ID',
  `team_id` bigint NOT NULL COMMENT '团队ID',
  `invite_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邀请码（UUID）',
  `invite_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '完整邀请链接',
  `creator_id` bigint NOT NULL COMMENT '创建人ID',
  `max_uses` int NULL DEFAULT 0 COMMENT '最大使用次数（0=无限制）',
  `used_count` int NULL DEFAULT 0 COMMENT '已使用次数',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active' COMMENT '状态: active-有效, disabled-禁用, expired-已过期',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间（NULL=永久有效）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_invite_code`(`invite_code` ASC) USING BTREE,
  INDEX `idx_team_id`(`team_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '团队邀请链接表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_team_invitation
-- ----------------------------
INSERT INTO `biz_team_invitation` VALUES (1, 1, '2238df34b218468ab55d205c23fbe8e9', 'https://coffeeviz.com/team/join/2238df34b218468ab55d205c23fbe8e9', 1, 10, 0, 'disabled', NULL, '2026-02-10 13:24:53', '2026-02-10 18:17:33');

-- ----------------------------
-- Table structure for biz_team_invitation_log
-- ----------------------------
DROP TABLE IF EXISTS `biz_team_invitation_log`;
CREATE TABLE `biz_team_invitation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `team_id` bigint NOT NULL COMMENT '团队ID',
  `invite_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邀请码',
  `user_id` bigint NOT NULL COMMENT '加入用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `is_new_user` tinyint(1) NULL DEFAULT 0 COMMENT '是否新注册用户',
  `join_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户代理',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_team_id`(`team_id` ASC) USING BTREE,
  INDEX `idx_invite_code`(`invite_code` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '邀请使用记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_team_invitation_log
-- ----------------------------

-- ----------------------------
-- Table structure for biz_team_log
-- ----------------------------
DROP TABLE IF EXISTS `biz_team_log`;
CREATE TABLE `biz_team_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `team_id` bigint NOT NULL COMMENT '团队ID',
  `user_id` bigint NOT NULL COMMENT '操作人ID',
  `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型',
  `target_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标类型: member-成员, diagram-架构图, team-团队',
  `target_id` bigint NULL DEFAULT NULL COMMENT '目标ID',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '操作描述',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户代理',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_team_id`(`team_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '团队操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_team_log
-- ----------------------------
INSERT INTO `biz_team_log` VALUES (1, 1, 1, 'create_team', 'team', 1, '创建团队: 测试', NULL, NULL, '2026-02-10 13:22:34');
INSERT INTO `biz_team_log` VALUES (2, 1, 1, 'create_invite', 'invitation', 1, '创建邀请链接', NULL, NULL, '2026-02-10 13:24:53');
INSERT INTO `biz_team_log` VALUES (3, 1, 1, 'disable_invite', 'invitation', 1, '禁用邀请链接', NULL, NULL, '2026-02-10 18:17:33');

-- ----------------------------
-- Table structure for biz_team_member
-- ----------------------------
DROP TABLE IF EXISTS `biz_team_member`;
CREATE TABLE `biz_team_member`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '成员ID',
  `team_id` bigint NOT NULL COMMENT '团队ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'member' COMMENT '角色: owner-所有者, member-成员',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '团队内昵称',
  `join_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `join_source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'invite' COMMENT '加入方式: create-创建, invite-邀请',
  `invite_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '使用的邀请码',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active' COMMENT '状态: active-活跃, inactive-停用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_team_user`(`team_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_team_id`(`team_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '团队成员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_team_member
-- ----------------------------
INSERT INTO `biz_team_member` VALUES (1, 1, 1, 'owner', NULL, '2026-02-10 13:22:34', 'create', NULL, 'active', '2026-02-10 13:22:34', '2026-02-10 13:22:34');

-- ----------------------------
-- Table structure for biz_user_quota_tracking
-- ----------------------------
DROP TABLE IF EXISTS `biz_user_quota_tracking`;
CREATE TABLE `biz_user_quota_tracking`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '跟踪ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `plan_quota_id` bigint NOT NULL COMMENT '计划配额ID',
  `quota_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配额类型',
  `quota_limit` int NOT NULL COMMENT '当前配额限制',
  `quota_used` int NULL DEFAULT 0 COMMENT '已使用配额',
  `last_reset_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上次重置时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_quota_type`(`user_id` ASC, `quota_type` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_plan_quota_id`(`plan_quota_id` ASC) USING BTREE,
  CONSTRAINT `fk_user_quota_plan_quota` FOREIGN KEY (`plan_quota_id`) REFERENCES `biz_plan_quota` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_quota_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户配额使用跟踪表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_user_quota_tracking
-- ----------------------------
INSERT INTO `biz_user_quota_tracking` VALUES (1, 1, 5, 'ai_generate', 100, 19, '2026-02-09 15:30:34', '2026-02-10 17:31:37', '2026-02-10 17:31:37');
INSERT INTO `biz_user_quota_tracking` VALUES (2, 1, 6, 'diagram', 500, 0, '2026-02-09 15:30:34', '2026-02-10 17:31:37', '2026-02-10 17:31:37');
INSERT INTO `biz_user_quota_tracking` VALUES (3, 1, 7, 'repository', 20, 5, '2026-02-09 15:30:34', '2026-02-10 17:31:37', '2026-02-10 17:31:37');
INSERT INTO `biz_user_quota_tracking` VALUES (4, 1, 8, 'sql_parse', 1000, 2, '2026-02-09 15:30:34', '2026-02-10 17:31:37', '2026-02-10 17:31:37');
INSERT INTO `biz_user_quota_tracking` VALUES (5, 2, 1, 'ai_generate', 0, 0, '2026-02-08 17:47:27', '2026-02-10 17:31:37', '2026-02-10 17:31:37');
INSERT INTO `biz_user_quota_tracking` VALUES (6, 2, 2, 'diagram', 10, 0, '2026-02-08 14:16:32', '2026-02-10 17:31:37', '2026-02-10 17:31:37');
INSERT INTO `biz_user_quota_tracking` VALUES (7, 2, 3, 'repository', 3, 0, '2026-02-08 14:16:32', '2026-02-10 17:31:37', '2026-02-10 17:31:37');
INSERT INTO `biz_user_quota_tracking` VALUES (8, 2, 4, 'sql_parse', 50, 0, '2026-02-08 17:47:27', '2026-02-10 17:31:37', '2026-02-10 17:31:37');
INSERT INTO `biz_user_quota_tracking` VALUES (9, 3, 1, 'ai_generate', 0, 0, '2026-02-08 17:47:27', '2026-02-10 17:31:37', '2026-02-10 17:31:37');
INSERT INTO `biz_user_quota_tracking` VALUES (10, 3, 2, 'diagram', 10, 0, '2026-02-08 14:16:32', '2026-02-10 17:31:37', '2026-02-10 17:31:37');
INSERT INTO `biz_user_quota_tracking` VALUES (11, 3, 3, 'repository', 3, 0, '2026-02-08 14:16:32', '2026-02-10 17:31:37', '2026-02-10 17:31:37');
INSERT INTO `biz_user_quota_tracking` VALUES (12, 3, 4, 'sql_parse', 50, 0, '2026-02-08 17:47:27', '2026-02-10 17:31:37', '2026-02-10 17:31:37');
INSERT INTO `biz_user_quota_tracking` VALUES (13, 4, 1, 'ai_generate', 0, 0, '2026-02-08 17:47:27', '2026-02-10 17:31:37', '2026-02-10 17:31:37');
INSERT INTO `biz_user_quota_tracking` VALUES (14, 4, 2, 'diagram', 10, 0, '2026-02-08 14:16:32', '2026-02-10 17:31:37', '2026-02-10 17:31:37');
INSERT INTO `biz_user_quota_tracking` VALUES (15, 4, 3, 'repository', 3, 0, '2026-02-08 14:16:32', '2026-02-10 17:31:37', '2026-02-10 17:31:37');
INSERT INTO `biz_user_quota_tracking` VALUES (16, 4, 4, 'sql_parse', 50, 0, '2026-02-08 17:47:27', '2026-02-10 17:31:37', '2026-02-10 17:31:37');

-- ----------------------------
-- Table structure for biz_user_subscription
-- ----------------------------
DROP TABLE IF EXISTS `biz_user_subscription`;
CREATE TABLE `biz_user_subscription`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订阅ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `plan_id` bigint NOT NULL COMMENT '计划ID',
  `plan_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '计划代码',
  `payment_order_id` bigint NULL DEFAULT NULL COMMENT '支付订单ID',
  `billing_cycle` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'monthly' COMMENT '计费周期（monthly/yearly）',
  `price` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '订阅价格',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `auto_renew` tinyint NULL DEFAULT 0 COMMENT '是否自动续费（0=否，1=是）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active' COMMENT '状态（active/expired/cancelled）',
  `payment_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付方式（wechat/alipay/stripe）',
  `transaction_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '交易ID',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '取消原因',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_plan_id`(`plan_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_end_time`(`end_time` ASC) USING BTREE,
  INDEX `idx_payment_order_id`(`payment_order_id` ASC) USING BTREE,
  CONSTRAINT `fk_subscription_plan` FOREIGN KEY (`plan_id`) REFERENCES `biz_subscription_plan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_subscription_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_subscription_payment` FOREIGN KEY (`payment_order_id`) REFERENCES `biz_payment_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户订阅表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of biz_user_subscription
-- ----------------------------
INSERT INTO `biz_user_subscription` VALUES (1, 1, 3, 'TEAM', NULL, 'monthly', 99.00, '2026-02-08 14:16:32', '2027-02-09 15:30:34', 0, 'active', NULL, NULL, NULL, NULL, '2026-02-08 14:16:32', '2026-02-10 13:21:53');
INSERT INTO `biz_user_subscription` VALUES (2, 2, 1, 'FREE', NULL, 'monthly', 0.00, '2026-02-08 14:16:32', '2026-03-08 14:16:32', 0, 'active', NULL, NULL, NULL, NULL, '2026-02-08 14:16:32', '2026-02-08 18:42:16');
INSERT INTO `biz_user_subscription` VALUES (3, 3, 1, 'FREE', NULL, 'monthly', 0.00, '2026-02-08 14:16:32', '2026-03-08 14:16:32', 0, 'active', NULL, NULL, NULL, NULL, '2026-02-08 14:16:32', '2026-02-08 18:42:19');
INSERT INTO `biz_user_subscription` VALUES (4, 4, 1, 'FREE', NULL, 'monthly', 0.00, '2026-02-08 14:16:32', '2026-03-08 14:16:32', 0, 'active', NULL, NULL, NULL, NULL, '2026-02-08 14:16:32', '2026-02-08 18:42:21');

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键（唯一）',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '配置值',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `config_key`(`config_key` ASC) USING BTREE,
  INDEX `idx_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, 'openai.api.key', 'ms-78cb0e54-1793-4ad4-8502-fce765c14498', 'OpenAI API密钥', '2026-02-07 11:42:16', '2026-02-09 15:49:32');
INSERT INTO `sys_config` VALUES (2, 'openai.api.base_url', 'https://api-inference.modelscope.cn/v1', 'OpenAI API基础地址', '2026-02-07 11:42:16', '2026-02-07 11:42:16');
INSERT INTO `sys_config` VALUES (3, 'openai.model.name', 'moonshotai/Kimi-K2.5', 'OpenAI模型名称', '2026-02-07 11:42:16', '2026-02-07 11:42:16');
INSERT INTO `sys_config` VALUES (4, 'mermaid.cli.path', 'mmdc', 'Mermaid CLI路径', '2026-02-07 11:42:16', '2026-02-07 11:42:16');
INSERT INTO `sys_config` VALUES (5, 'mermaid.cli.timeout', '10000', 'Mermaid CLI超时时间（毫秒）', '2026-02-07 11:42:16', '2026-02-07 11:42:16');
INSERT INTO `sys_config` VALUES (6, 'export.max.size', '10485760', '导出文件最大大小（字节，默认10MB）', '2026-02-07 11:42:16', '2026-02-07 11:42:16');
INSERT INTO `sys_config` VALUES (7, 'export.temp.dir', '/tmp/coffeeviz', '临时文件目录', '2026-02-07 11:42:16', '2026-02-07 11:42:16');
INSERT INTO `sys_config` VALUES (8, 'sql.max.length', '1000000', 'SQL文本最大长度（字符）', '2026-02-07 11:42:16', '2026-02-07 11:42:16');
INSERT INTO `sys_config` VALUES (9, 'jdbc.connection.timeout', '10', 'JDBC连接超时时间（秒）', '2026-02-07 11:42:16', '2026-02-07 11:42:16');
INSERT INTO `sys_config` VALUES (10, 'jdbc.query.timeout', '30', 'JDBC查询超时时间（秒）', '2026-02-07 11:42:16', '2026-02-07 11:42:16');
INSERT INTO `sys_config` VALUES (11, 'rate.limit.per.second', '10', 'API限流：每秒请求数', '2026-02-07 11:42:16', '2026-02-07 11:42:16');
INSERT INTO `sys_config` VALUES (12, 'cache.expire.seconds', '3600', '缓存过期时间（秒）', '2026-02-07 11:42:16', '2026-02-07 11:42:16');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名（唯一）',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `display_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '显示名称',
  `job_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '职位头衔',
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态（1=正常，0=禁用）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$rJrh3nvpyBQ1QDDsTkmIG.nAXiwjTF0lwRHuEQz/JY/DDRpMhO9j.', 'admin@coffeeviz.com', NULL, '系统管理员', 'Administrator', NULL, 1, '2026-02-07 11:42:16', '2026-02-07 13:51:38');
INSERT INTO `sys_user` VALUES (2, '3273495516@qq.com', '$2a$12$Wgmk1lSoAJWlwSDwNMXGQu6ux2k/IUiiEjoGsj9gLvnHuSRJ3Yke.', NULL, NULL, '神阁绘8d67833604', NULL, NULL, 1, '2026-02-07 19:23:26', '2026-02-07 19:45:51');
INSERT INTO `sys_user` VALUES (3, 'user_6154', '$2a$12$Fc2luHllb37KJeAaGTFwy.QcW8ABaD5TKSXmSiy2c4wtmjRVk41qm', NULL, '13272796154', '用户6154', NULL, NULL, 1, '2026-02-07 19:30:56', '2026-02-07 19:30:56');
INSERT INTO `sys_user` VALUES (4, '2703772950@qq.com', '$2a$12$4bnoZcppTnPR3eeaTLG2c.cbkl1UsIYh/2DG5fp0eT.JhCm9j6.c6', 'eighteenthstuai@gmail.com', '13245456565', '神阁绘9e3981fd55', NULL, NULL, 1, '2026-02-07 19:39:43', '2026-02-07 19:39:43');

-- ----------------------------
-- Triggers structure for table biz_diagram
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_diagram_insert`;
delimiter ;;
CREATE TRIGGER `trg_diagram_insert` AFTER INSERT ON `biz_diagram` FOR EACH ROW BEGIN
  UPDATE `biz_repository` 
  SET `diagram_count` = (SELECT COUNT(*) FROM `biz_diagram` WHERE `repository_id` = NEW.repository_id)
  WHERE `id` = NEW.repository_id;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table biz_diagram
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_diagram_delete`;
delimiter ;;
CREATE TRIGGER `trg_diagram_delete` AFTER DELETE ON `biz_diagram` FOR EACH ROW BEGIN
  UPDATE `biz_repository` 
  SET `diagram_count` = (SELECT COUNT(*) FROM `biz_diagram` WHERE `repository_id` = OLD.repository_id)
  WHERE `id` = OLD.repository_id;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
