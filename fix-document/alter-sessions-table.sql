-- ========================================
-- 修改sessions表，允许factory_id为NULL
-- 用于支持平台管理员登录
-- ========================================

USE cretas;

-- 1. 先删除外键约束
ALTER TABLE sessions DROP FOREIGN KEY FK3ssg794muk337sso6ungy976j;

-- 2. 修改factory_id字段，允许为NULL
ALTER TABLE sessions MODIFY COLUMN factory_id VARCHAR(50) NULL;

-- 3. 重新添加外键约束（允许NULL）
ALTER TABLE sessions
ADD CONSTRAINT FK3ssg794muk337sso6ungy976j
FOREIGN KEY (factory_id) REFERENCES factories(id);

-- 验证修改
DESCRIBE sessions;

SELECT '✅ sessions表已更新，factory_id现在可以为NULL' AS message;
SELECT 'Platform admins can now login successfully' AS note;
