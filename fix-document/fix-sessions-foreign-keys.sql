-- ========================================
-- 修复sessions表外键约束
-- 问题：user_id可能来自users表或platform_admins表
-- 解决：删除user_id的外键约束
-- ========================================

USE cretas;

-- 删除user_id的外键约束
ALTER TABLE sessions DROP FOREIGN KEY FKruie73rneumyyd1bgo6qw8vjt;

-- 验证修改
SHOW CREATE TABLE sessions;

SELECT '✅ sessions表外键约束已修复' AS message;
SELECT 'user_id现在可以引用users或platform_admins表' AS note;
