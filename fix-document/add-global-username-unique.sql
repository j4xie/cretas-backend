-- ========================================
-- 添加用户名全局唯一约束
-- ========================================
-- 目的：让工厂用户登录时无需提供factoryId
-- 
-- 修改时间：2025-11-03
-- 影响：users表
-- ========================================

USE cretas;

-- 1. 检查是否有重复的用户名
SELECT username, COUNT(*) as count, GROUP_CONCAT(factory_id) as factories
FROM users
GROUP BY username
HAVING COUNT(*) > 1;

-- 如果上面的查询有结果，说明存在重复用户名
-- 需要手动处理重复项（给重复的用户名加后缀）

-- 2. 删除旧的组合唯一约束 (factory_id + username)
ALTER TABLE users 
DROP INDEX IF EXISTS factory_id;  -- 旧约束名称可能不同

-- 或者尝试这个（根据实际约束名称）
-- SHOW CREATE TABLE users;  -- 先查看实际约束名称
-- ALTER TABLE users DROP INDEX <实际约束名称>;

-- 3. 添加新的全局唯一约束（只针对username）
ALTER TABLE users
ADD UNIQUE INDEX idx_username_unique (username);

-- 4. 验证约束已添加
SHOW CREATE TABLE users;

-- 5. 测试：尝试插入重复用户名（应该失败）
-- INSERT INTO users (factory_id, username, password_hash, full_name, is_active, created_at, updated_at)
-- VALUES ('F002', 'proc_admin', 'test_hash', '测试用户', true, NOW(), NOW());
-- 这个应该报错：Duplicate entry 'proc_admin' for key 'idx_username_unique'

