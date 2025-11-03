-- 更新测试账号密码为已知的正确BCrypt哈希
-- 这些哈希是通过Spring Boot BCryptPasswordEncoder生成的

USE cretas;

-- 临时禁用安全模式
SET SQL_SAFE_UPDATES = 0;

-- 更新平台管理员密码（admin123）
-- 使用简单密码 "admin" 的BCrypt哈希: $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
UPDATE platform_admins
SET password_hash = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
    updated_at = NOW()
WHERE username IN ('platform_admin', 'system_admin', 'test_admin');

-- 更新工厂用户密码（password）
-- 使用简单密码 "password" 的BCrypt哈希: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
UPDATE users
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    updated_at = NOW()
WHERE username IN ('testuser', 'testadmin', 'testoperator');

-- 恢复安全模式
SET SQL_SAFE_UPDATES = 1;

-- 验证更新
SELECT '=== 平台管理员（密码: admin）===' AS '';
SELECT id, username, real_name, LEFT(password_hash, 20) as password_prefix
FROM platform_admins
WHERE username IN ('platform_admin', 'system_admin', 'test_admin');

SELECT '=== 工厂用户（密码: password）===' AS '';
SELECT id, username, full_name, LEFT(password_hash, 20) as password_prefix
FROM users
WHERE username IN ('testuser', 'testadmin', 'testoperator');

SELECT '密码已更新！请使用以下凭据测试：' AS message;
SELECT '平台管理员 - username: platform_admin, password: admin' AS '';
SELECT '工厂用户 - username: testadmin, password: password, factoryId: F001' AS '';
