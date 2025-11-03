-- 最终密码修复脚本
-- 使用在线BCrypt生成器验证过的哈希值

USE cretas;
SET SQL_SAFE_UPDATES = 0;

-- 使用 "password" 的标准BCrypt哈希
-- 这个哈希已通过多个在线工具验证
UPDATE platform_admins
SET password_hash = '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE username = 'platform_admin';

UPDATE users
SET password_hash = '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE username = 'testadmin';

SET SQL_SAFE_UPDATES = 1;

SELECT 'Password updated to: password' AS message;
SELECT username, LEFT(password_hash, 60) as full_hash FROM platform_admins WHERE username='platform_admin';
SELECT username, LEFT(password_hash, 60) as full_hash FROM users WHERE username='testadmin';
