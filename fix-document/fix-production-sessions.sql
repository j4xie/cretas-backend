-- 修复生产环境 sessions 表，允许 factory_id 为 NULL
-- 使用方法: mysql -h 47.251.121.76 -P 3307 -u cretas_user -pCretas@2024 < fix-production-sessions.sql

USE cretas;

-- 1. 先删除 factory_id 的外键约束（如果存在）
SET @query = (
    SELECT CONCAT('ALTER TABLE sessions DROP FOREIGN KEY ', CONSTRAINT_NAME, ';')
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = 'cretas'
      AND TABLE_NAME = 'sessions'
      AND COLUMN_NAME = 'factory_id'
      AND CONSTRAINT_NAME != 'PRIMARY'
    LIMIT 1
);

SET @query = IFNULL(@query, 'SELECT "No foreign key constraint found on factory_id" AS info;');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 修改 factory_id 字段，允许为 NULL
ALTER TABLE sessions MODIFY COLUMN factory_id VARCHAR(50) NULL;

-- 3. 重新添加外键约束（允许 NULL）
ALTER TABLE sessions
ADD CONSTRAINT FK_sessions_factory_id
FOREIGN KEY (factory_id) REFERENCES factories(id);

-- 4. 验证修改结果
SELECT
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_KEY,
    COLUMN_DEFAULT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'cretas'
  AND TABLE_NAME = 'sessions'
  AND COLUMN_NAME = 'factory_id';

SELECT '✅ Sessions 表修复完成！' AS status;
