-- ========================================
-- 添加工厂名称全局唯一约束
-- ========================================
-- 目的：防止创建重复名称的工厂
-- 
-- 修改时间：2025-11-03
-- 影响：factories表
-- ========================================

USE cretas;

-- 1. 检查是否有重复的工厂名称
SELECT name, COUNT(*) as count, GROUP_CONCAT(id) as factory_ids
FROM factories
GROUP BY name
HAVING COUNT(*) > 1;

-- 如果上面的查询有结果，说明存在重复工厂名称
-- 需要手动处理重复项（给重复的工厂名称加后缀）

-- 2. 添加工厂名称全局唯一约束
ALTER TABLE factories
ADD UNIQUE INDEX idx_factory_name_unique (name);

-- 3. 验证约束已添加
SHOW CREATE TABLE factories;

-- 4. 测试：尝试插入重复工厂名称（应该失败）
-- INSERT INTO factories (id, name, is_active, created_at, updated_at, ai_weekly_quota, manually_verified)
-- VALUES ('F999', '测试工厂', true, NOW(), NOW(), 100, false);
-- 这个应该报错：Duplicate entry '测试工厂' for key 'idx_factory_name_unique'

