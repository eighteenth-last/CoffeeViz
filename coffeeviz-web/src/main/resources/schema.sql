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

 Date: 07/02/2026 19:48:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for biz_project
-- ----------------------------
DROP TABLE IF EXISTS `biz_project`;
CREATE TABLE `biz_project`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `project_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '项目描述',
  `source_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '来源类型（SQL/JDBC/AI）',
  `db_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '数据库类型（mysql/postgres）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'DRAFT' COMMENT '状态（DRAFT/PUBLISHED）',
  `mermaid_code` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'Mermaid源码',
  `svg_content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'SVG内容',
  `table_count` int NULL DEFAULT 0 COMMENT '表数量',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_user_status`(`user_id` ASC, `status` ASC) USING BTREE,
  CONSTRAINT `fk_project_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '项目表' ROW_FORMAT = Dynamic;

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
  UNIQUE INDEX `uk_project_id`(`project_id` ASC) USING BTREE,
  CONSTRAINT `fk_config_project` FOREIGN KEY (`project_id`) REFERENCES `biz_project` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '项目配置表' ROW_FORMAT = Dynamic;

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
  INDEX `idx_project_id`(`project_id` ASC) USING BTREE,
  INDEX `idx_project_version`(`project_id` ASC, `version_no` ASC) USING BTREE,
  CONSTRAINT `fk_version_project` FOREIGN KEY (`project_id`) REFERENCES `biz_project` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '项目版本表' ROW_FORMAT = Dynamic;

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

SET FOREIGN_KEY_CHECKS = 1;
