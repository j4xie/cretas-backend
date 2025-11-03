-- ========================================
-- 密码重置脚本 - 重置所有密码为 123456
-- 密码哈希: $2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW
-- ========================================

USE cretas;

-- 更新平台管理员密码
UPDATE platform_admins 
SET password_hash = '$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW',
    updated_at = NOW()
WHERE username IN ('admin', 'developer', 'platform_admin');

-- 更新工厂用户密码
UPDATE users 
SET password_hash = '$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW',
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

