-- ========================================
-- AIMS系统数据库初始化脚本
-- 用于登录注册功能测试
-- ========================================

USE cretas;

-- ========================================
-- 1. 创建平台管理员表
-- ========================================

CREATE TABLE IF NOT EXISTS platform_admins (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    real_name VARCHAR(100) NOT NULL COMMENT '真实姓名',
    email VARCHAR(255) COMMENT '邮箱',
    phone_number VARCHAR(20) COMMENT '手机号',
    platform_role VARCHAR(50) NOT NULL COMMENT '平台角色',
    status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态',
    last_login_at DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(45) COMMENT '最后登录IP',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remarks TEXT COMMENT '备注',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台管理员表';

-- ========================================
-- 2. 插入测试平台管理员
-- ========================================

-- 删除已存在的测试数据
DELETE FROM platform_admins WHERE username IN ('platform_admin', 'system_admin', 'test_admin');

-- 插入测试管理员（密码均为: admin123）
INSERT INTO platform_admins (username, password_hash, real_name, email, phone_number, platform_role, status, remarks, created_at, updated_at) VALUES
('platform_admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2W5f8vGXOFoT1.QL31/8.W6', '超级管理员', 'super@aims.com', '18800000001', 'super_admin', 'active', '系统超级管理员，拥有所有权限', NOW(), NOW()),
('system_admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2W5f8vGXOFoT1.QL31/8.W6', '系统管理员', 'system@aims.com', '18800000002', 'system_admin', 'active', '系统管理员，管理系统配置', NOW(), NOW()),
('test_admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2W5f8vGXOFoT1.QL31/8.W6', '测试管理员', 'test@aims.com', '18800000003', 'auditor', 'active', '测试用审计员账号', NOW(), NOW());

-- ========================================
-- 3. 检查并创建测试工厂（必须在whitelist之前）
-- ========================================

-- 删除已存在的测试工厂数据
DELETE FROM factories WHERE id IN ('F001', 'F002');

-- 插入测试工厂
INSERT INTO factories (id, name, address, contact_name, contact_phone, is_active, ai_weekly_quota, manually_verified, created_at, updated_at)
VALUES
('F001', '测试工厂A', '北京市朝阳区建国路XX号', '张经理', '010-12345678', TRUE, 20, FALSE, NOW(), NOW()),
('F002', '测试工厂B', '上海市浦东新区XX路XX号', '李经理', '021-87654321', TRUE, 20, FALSE, NOW(), NOW());

-- ========================================
-- 4. 创建测试用户（必须在whitelist之前，因为whitelist.added_by引用users.id）
-- ========================================

-- 删除已存在的测试用户
DELETE FROM users WHERE username IN ('testuser', 'testadmin', 'testoperator');

-- 插入测试用户（密码: password123）
INSERT INTO users (factory_id, username, password_hash, email, phone, full_name, is_active, role_code, department, position, created_at, updated_at) VALUES
('F001', 'testuser', '$2a$10$YhKRbgXrOuJN9OMzHOczCuH3jZ0xH4KnoR6l2FqXQ7vZ9Yd7KzZ.S', 'testuser@test.com', '13900000001', '测试用户', TRUE, 'operator', NULL, '操作员', NOW(), NOW()),
('F001', 'testadmin', '$2a$10$YhKRbgXrOuJN9OMzHOczCuH3jZ0xH4KnoR6l2FqXQ7vZ9Yd7KzZ.S', 'testadmin@test.com', '13900000002', '测试管理员', TRUE, 'factory_super_admin', NULL, '工厂管理员', NOW(), NOW()),
('F001', 'testoperator', '$2a$10$YhKRbgXrOuJN9OMzHOczCuH3jZ0xH4KnoR6l2FqXQ7vZ9Yd7KzZ.S', 'operator@test.com', '13900000003', '测试操作员', TRUE, 'operator', NULL, '普通操作员', NOW(), NOW());

-- ========================================
-- 5. 插入测试白名单数据（依赖users表）
-- ========================================

-- 删除已存在的测试数据
DELETE FROM whitelist WHERE phone_number LIKE '138001380%';

-- 插入测试白名单（使用testadmin的ID作为added_by）
INSERT INTO whitelist (factory_id, phone_number, name, department, position, status, expires_at, usage_count, max_usage_count, added_by, notes, created_at, updated_at) VALUES
('F001', '13800138000', '张三', '技术部', '软件工程师', 'ACTIVE', DATE_ADD(NOW(), INTERVAL 1 YEAR), 0, NULL, (SELECT id FROM users WHERE username='testadmin'), '测试账号1 - 用于注册测试', NOW(), NOW()),
('F001', '13800138001', '李四', '产品部', '产品经理', 'ACTIVE', DATE_ADD(NOW(), INTERVAL 1 YEAR), 0, NULL, (SELECT id FROM users WHERE username='testadmin'), '测试账号2 - 用于移动端测试', NOW(), NOW()),
('F001', '13800138002', '王五', '运营部', '运营专员', 'ACTIVE', DATE_ADD(NOW(), INTERVAL 1 YEAR), 0, NULL, (SELECT id FROM users WHERE username='testadmin'), '测试账号3 - 备用测试', NOW(), NOW()),
('F001', '13800138003', '赵六', '技术部', '测试工程师', 'ACTIVE', DATE_ADD(NOW(), INTERVAL 1 YEAR), 0, 3, (SELECT id FROM users WHERE username='testadmin'), '测试账号4 - 限制使用次数', NOW(), NOW()),
('F002', '13800138010', '孙七', '生产部', '生产主管', 'ACTIVE', DATE_ADD(NOW(), INTERVAL 1 YEAR), 0, NULL, (SELECT id FROM users WHERE username='testadmin'), '测试账号5 - F002工厂', NOW(), NOW());

-- ========================================
-- 6. 验证数据
-- ========================================

-- 查看平台管理员
SELECT '=== 平台管理员 ===' AS '';
SELECT
    id, username, real_name, platform_role, status, created_at
FROM platform_admins
ORDER BY id;

-- 查看测试工厂
SELECT '=== 测试工厂 ===' AS '';
SELECT
    id, name, address, contact_name, is_active
FROM factories
WHERE id IN ('F001', 'F002');

-- 查看白名单
SELECT '=== 白名单 ===' AS '';
SELECT
    id, factory_id, phone_number, name, department, status, expires_at
FROM whitelist
WHERE phone_number LIKE '138001380%'
ORDER BY id;

-- 查看测试用户
SELECT '=== 测试用户 ===' AS '';
SELECT
    id, factory_id, username, full_name, is_active, role_code
FROM users
WHERE username LIKE 'test%'
ORDER BY id;

-- ========================================
-- 7. 测试账号说明
-- ========================================

/*
平台管理员账号:
  - username: platform_admin, password: admin123, role: super_admin
  - username: system_admin,   password: admin123, role: system_admin
  - username: test_admin,     password: admin123, role: auditor

工厂用户账号:
  - username: testuser,     password: password123, role: operator
  - username: testadmin,    password: password123, role: factory_super_admin
  - username: testoperator, password: password123, role: operator

白名单手机号（用于注册）:
  - 13800138000 (张三 - F001)
  - 13800138001 (李四 - F001)
  - 13800138002 (王五 - F001)
  - 13800138003 (赵六 - F001, 限制3次使用)
  - 13800138010 (孙七 - F002)

工厂ID:
  - F001 (测试工厂A)
  - F002 (测试工厂B)
*/

-- ========================================
-- 执行完成
-- ========================================

SELECT '✅ 数据库初始化完成！' AS message;
SELECT '请使用上述账号进行测试' AS note;
