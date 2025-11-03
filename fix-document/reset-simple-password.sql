-- 设置超级简单的密码进行测试
-- 使用 "123456" 作为测试密码
USE cretas;

SET SQL_SAFE_UPDATES = 0;

-- 密码: 123456 的BCrypt哈希
-- $2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW

UPDATE platform_admins
SET password_hash = '$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW'
WHERE username = 'platform_admin';

UPDATE users
SET password_hash = '$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW'
WHERE username = 'testadmin';

SET SQL_SAFE_UPDATES = 1;

SELECT '密码已重置为: 123456' AS message;
SELECT username, LEFT(password_hash, 30) as hash_prefix FROM platform_admins WHERE username='platform_admin';
SELECT username, LEFT(password_hash, 30) as hash_prefix FROM users WHERE username='testadmin';
