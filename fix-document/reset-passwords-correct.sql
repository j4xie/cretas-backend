-- ========================================
-- 密码重置脚本 - 使用正确的BCrypt哈希
-- 密码: 123456
-- 正确的BCrypt哈希: $2b$12$KO2Euov0Mz3ZZx4BeoYkzO9r7nJHY9lZcQ3IcTXmQO1vhWYYLkF4y
-- ========================================

USE cretas;

-- 更新平台管理员密码
UPDATE platform_admins 
SET password_hash = '$2b$12$KO2Euov0Mz3ZZx4BeoYkzO9r7nJHY9lZcQ3IcTXmQO1vhWYYLkF4y',
    updated_at = NOW()
WHERE username IN ('admin', 'developer', 'platform_admin');

-- 更新工厂用户密码
UPDATE users 
SET password_hash = '$2b$12$KO2Euov0Mz3ZZx4BeoYkzO9r7nJHY9lZcQ3IcTXmQO1vhWYYLkF4y',
    updated_at = NOW()
WHERE factory_id = 'F001';

-- 验证更新结果
SELECT '=== 平台管理员密码重置结果 ===' AS '';
SELECT 
    username,
    real_name,
    LEFT(password_hash, 30) AS password_hash_prefix,
    updated_at
FROM platform_admins
WHERE username IN ('admin', 'developer', 'platform_admin');

SELECT '' AS '';
SELECT '=== 工厂用户密码重置结果 ===' AS '';
SELECT 
    username,
    full_name,
    LEFT(password_hash, 30) AS password_hash_prefix,
    updated_at
FROM users
WHERE factory_id = 'F001';

SELECT '' AS '';
SELECT '✅ 密码重置完成！所有密码已设置为: 123456' AS '';
SELECT '哈希值: $2b$12$KO2Euov0Mz3ZZx4BeoYkzO9r7nJHY9lZcQ3IcTXmQO1vhWYYLkF4y' AS '';

