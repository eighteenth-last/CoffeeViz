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

 Date: 08/02/2026 18:57:22
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
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_user_status`(`user_id` ASC, `status` ASC) USING BTREE,
  CONSTRAINT `fk_project_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '项目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_repository_config
-- ----------------------------
DROP TABLE IF EXISTS `biz_repository_config`;
CREATE TABLE `biz_repository_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `repository_id` bigint NOT NULL COMMENT '架构库ID',
  `config_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置类型（SQL/JDBC）',
  `sql_content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'SQL内容',
  `jdbc_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'JDBC URL',
  `jdbc_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '数据库用户名',
  `jdbc_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '数据库密码（AES加密）',
  `schema_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Schema名称',
  `render_options` json NULL COMMENT '渲染选项（JSON格式）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_project_id`(`repository_id` ASC) USING BTREE,
  CONSTRAINT `fk_config_repository` FOREIGN KEY (`repository_id`) REFERENCES `biz_repository` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '项目配置表' ROW_FORMAT = Dynamic;

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

INSERT INTO `coffeeviz`.`biz_subscription_plan` (`id`, `plan_code`, `plan_name`, `plan_name_en`, `description`, `price_monthly`, `price_yearly`, `max_repositories`, `max_diagrams_per_repo`, `max_sql_size_mb`, `support_jdbc`, `support_ai`, `support_export`, `support_team`, `priority_support`, `features`, `sort_order`, `status`, `create_time`, `update_time`) VALUES (1, 'FREE', '社区版', 'Community', '适合个人开发者和小型项目使用', 0.00, 0.00, 3, 10, 5, 0, 0, 1, 0, 0, '[\"3个架构库\", \"10次架构图/月\", \"50次SQL解析/天\", \"基础ER图生成\", \"MySQL支持\", \"社区支持\"]', 1, 'active', '2026-02-08 14:16:32', '2026-02-08 18:56:01');
INSERT INTO `coffeeviz`.`biz_subscription_plan` (`id`, `plan_code`, `plan_name`, `plan_name_en`, `description`, `price_monthly`, `price_yearly`, `max_repositories`, `max_diagrams_per_repo`, `max_sql_size_mb`, `support_jdbc`, `support_ai`, `support_export`, `support_team`, `priority_support`, `features`, `sort_order`, `status`, `create_time`, `update_time`) VALUES (2, 'PRO', '专业版', 'Professional', '适合开发者和生产力工具使用', 29.00, 290.00, 20, 500, 50, 1, 1, 1, 0, 0, '[\"20个架构库\", \"500次架构图/月\", \"1000次SQL解析/天\", \"100次AI生成/月\", \"多数据库支持\", \"JDBC实时连接\", \"高清导出\", \"优先支持\"]', 2, 'active', '2026-02-08 14:16:32', '2026-02-08 18:56:01');
INSERT INTO `coffeeviz`.`biz_subscription_plan` (`id`, `plan_code`, `plan_name`, `plan_name_en`, `description`, `price_monthly`, `price_yearly`, `max_repositories`, `max_diagrams_per_repo`, `max_sql_size_mb`, `support_jdbc`, `support_ai`, `support_export`, `support_team`, `priority_support`, `features`, `sort_order`, `status`, `create_time`, `update_time`) VALUES (3, 'TEAM', '团队版', 'Team', '适合企业团队协作使用', 99.00, 990.00, -1, -1, 200, 1, 1, 1, 1, 1, '[\"无限架构库\", \"无限架构图\", \"无限SQL解析\", \"1000次AI生成/月\", \"团队协作\", \"版本控制\", \"API集成\", \"私有部署\", \"专属支持\"]', 3, 'active', '2026-02-08 14:16:32', '2026-02-08 18:56:01');

-- ----------------------------
-- Table structure for biz_usage_quota
-- ----------------------------
DROP TABLE IF EXISTS `biz_usage_quota`;
CREATE TABLE `biz_usage_quota`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配额ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `quota_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配额类型（repository/diagram/sql_parse/ai_generate）',
  `quota_limit` int NOT NULL COMMENT '配额限制',
  `quota_used` int NULL DEFAULT 0 COMMENT '已使用配额',
  `reset_cycle` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'monthly' COMMENT '重置周期（daily/monthly/yearly/never）',
  `last_reset_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上次重置时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_quota_type`(`user_id` ASC, `quota_type` ASC) USING BTREE,
  CONSTRAINT `fk_quota_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户使用配额表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for biz_user_subscription
-- ----------------------------
DROP TABLE IF EXISTS `biz_user_subscription`;
CREATE TABLE `biz_user_subscription`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订阅ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `plan_id` bigint NOT NULL COMMENT '计划ID',
  `plan_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '计划代码',
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
  CONSTRAINT `fk_subscription_plan` FOREIGN KEY (`plan_id`) REFERENCES `biz_subscription_plan` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_subscription_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户订阅表' ROW_FORMAT = DYNAMIC;

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
