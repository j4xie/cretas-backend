-- ========================================
-- 最终用户初始化脚本
-- 所有密码统一为: 123456
-- ========================================

USE cretas;

-- 临时禁用安全模式和外键检查
SET SQL_SAFE_UPDATES = 0;
SET FOREIGN_KEY_CHECKS = 0;

-- ========================================
-- 1. 清理现有测试数据
-- ========================================

-- 删除会话（依赖users和platform_admins）
DELETE FROM sessions WHERE user_id IN (
    SELECT id FROM users WHERE username IN ('perm_admin', 'proc_admin', 'farm_admin', 'logi_admin', 'proc_user', 'testuser', 'testadmin', 'testoperator')
);

DELETE FROM sessions WHERE user_id IN (
    SELECT id FROM platform_admins WHERE username IN ('admin', 'developer', 'platform_admin', 'system_admin', 'test_admin')
);

-- 删除白名单（依赖users）
DELETE FROM whitelist WHERE factory_id = 'F001';

-- 删除工厂用户
DELETE FROM users WHERE factory_id = 'F001';

-- 删除平台管理员
DELETE FROM platform_admins WHERE username IN ('admin', 'developer', 'platform_admin', 'system_admin', 'test_admin');

-- 删除工厂
DELETE FROM factories WHERE id = 'F001';

-- ========================================
-- 2. 创建测试工厂
-- ========================================

INSERT INTO factories (id, name, address, contact_name, contact_phone, is_active, ai_weekly_quota, manually_verified, created_at, updated_at)
VALUES ('F001', '测试工厂', '北京市朝阳区建国路XX号', '张经理', '010-12345678', TRUE, 20, FALSE, NOW(), NOW());

-- ========================================
-- 3. 创建平台管理员
-- 密码: 123456
-- BCrypt Hash: $2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW
-- ========================================

INSERT INTO platform_admins (username, password_hash, real_name, email, phone_number, platform_role, status, remarks, created_at, updated_at) VALUES
('admin', '$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW', '超级管理员', 'admin@cretas.com', '18800000001', 'super_admin', 'active', '系统超级管理员，拥有所有权限', NOW(), NOW()),
('developer', '$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW', '开发者', 'developer@cretas.com', '18800000002', 'developer', 'active', '系统开发者账号', NOW(), NOW()),
('platform_admin', '$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW', '平台管理员', 'platform@cretas.com', '18800000003', 'platform_admin', 'active', '平台管理员账号', NOW(), NOW());

-- ========================================
-- 4. 创建工厂用户
-- 工厂ID: F001
-- 密码: 123456
-- BCrypt Hash: $2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW
-- ========================================

INSERT INTO users (factory_id, username, password_hash, email, phone, full_name, is_active, role_code, department, position, created_at, updated_at) VALUES
('F001', 'perm_admin', '$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW', 'perm_admin@f001.com', '13900000001', '权限管理员', TRUE, 'permission_admin', 'management', '权限管理', NOW(), NOW()),
('F001', 'proc_admin', '$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW', 'proc_admin@f001.com', '13900000002', '加工管理员', TRUE, 'department_admin', 'processing', '加工部主管', NOW(), NOW()),
('F001', 'farm_admin', '$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW', 'farm_admin@f001.com', '13900000003', '养殖管理员', TRUE, 'department_admin', 'farming', '养殖部主管', NOW(), NOW()),
('F001', 'logi_admin', '$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW', 'logi_admin@f001.com', '13900000004', '物流管理员', TRUE, 'department_admin', 'logistics', '物流部主管', NOW(), NOW()),
('F001', 'proc_user', '$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW', 'proc_user@f001.com', '13900000005', '加工操作员', TRUE, 'operator', 'processing', '加工操作员', NOW(), NOW());

-- ========================================
-- 5. 创建白名单（用于注册测试）
-- ========================================

-- 使用perm_admin的ID作为added_by
INSERT INTO whitelist (factory_id, phone_number, name, department, position, status, expires_at, usage_count, max_usage_count, added_by, notes, created_at, updated_at)
SELECT
    'F001',
    '13800138000',
    '张三',
    '技术部',
    '软件工程师',
    'ACTIVE',
    DATE_ADD(NOW(), INTERVAL 1 YEAR),
    0,
    NULL,
    u.id,
    '测试账号 - 用于注册测试',
    NOW(),
    NOW()
FROM users u WHERE u.username = 'perm_admin' AND u.factory_id = 'F001';

INSERT INTO whitelist (factory_id, phone_number, name, department, position, status, expires_at, usage_count, max_usage_count, added_by, notes, created_at, updated_at)
SELECT
    'F001',
    '13800138001',
    '李四',
    '产品部',
    '产品经理',
    'ACTIVE',
    DATE_ADD(NOW(), INTERVAL 1 YEAR),
    0,
    NULL,
    u.id,
    '测试账号 - 用于移动端测试',
    NOW(),
    NOW()
FROM users u WHERE u.username = 'perm_admin' AND u.factory_id = 'F001';

INSERT INTO whitelist (factory_id, phone_number, name, department, position, status, expires_at, usage_count, max_usage_count, added_by, notes, created_at, updated_at)
SELECT
    'F001',
    '13800138002',
    '王五',
    '运营部',
    '运营专员',
    'ACTIVE',
    DATE_ADD(NOW(), INTERVAL 1 YEAR),
    0,
    3,
    u.id,
    '测试账号 - 限制3次使用',
    NOW(),
    NOW()
FROM users u WHERE u.username = 'perm_admin' AND u.factory_id = 'F001';

-- ========================================
-- 6. 恢复安全模式和外键检查
-- ========================================

SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;

-- ========================================
-- 7. 验证数据
-- ========================================

SELECT '========================================' AS '';
SELECT '✅ 数据初始化完成！' AS '';
SELECT '========================================' AS '';
SELECT '' AS '';

SELECT '=== 平台管理员（密码: 123456）===' AS '';
SELECT
    id,
    username,
    real_name,
    platform_role,
    status,
    LEFT(password_hash, 30) AS password_prefix
FROM platform_admins
WHERE username IN ('admin', 'developer', 'platform_admin')
ORDER BY id;

SELECT '' AS '';
SELECT '=== 工厂用户（工厂: F001, 密码: 123456）===' AS '';
SELECT
    id,
    username,
    full_name,
    role_code,
    department,
    is_active,
    LEFT(password_hash, 30) AS password_prefix
FROM users
WHERE factory_id = 'F001'
ORDER BY id;

SELECT '' AS '';
SELECT '=== 测试工厂 ===' AS '';
SELECT
    id,
    name,
    contact_name,
    is_active
FROM factories
WHERE id = 'F001';

SELECT '' AS '';
SELECT '=== 白名单（用于注册测试）===' AS '';
SELECT
    id,
    factory_id,
    phone_number,
    name,
    status,
    DATE_FORMAT(expires_at, '%Y-%m-%d') AS expires_date
FROM whitelist
WHERE factory_id = 'F001'
ORDER BY id;

-- ========================================
-- 8. 账号说明
-- ========================================

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT '📋 测试账号列表（所有密码: 123456）' AS '';
SELECT '========================================' AS '';
SELECT '' AS '';
SELECT '🔷 平台管理员:' AS '';
SELECT '  1. admin          - 超级管理员' AS '';
SELECT '  2. developer      - 开发者' AS '';
SELECT '  3. platform_admin - 平台管理员' AS '';
SELECT '' AS '';
SELECT '🔷 工厂用户 (工厂F001):' AS '';
SELECT '  1. perm_admin - 权限管理员' AS '';
SELECT '  2. proc_admin - 加工管理员' AS '';
SELECT '  3. farm_admin - 养殖管理员' AS '';
SELECT '  4. logi_admin - 物流管理员' AS '';
SELECT '  5. proc_user  - 加工操作员' AS '';
SELECT '' AS '';
SELECT '🔷 测试手机号（用于注册）:' AS '';
SELECT '  1. 13800138000 - 张三（无限制）' AS '';
SELECT '  2. 13800138001 - 李四（无限制）' AS '';
SELECT '  3. 13800138002 - 王五（限3次）' AS '';
SELECT '' AS '';
SELECT '✅ 初始化完成！请使用上述账号测试' AS '';
SELECT '========================================' AS '';
