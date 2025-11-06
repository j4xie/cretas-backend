-- 原材料规格配置表
-- 用于存储每个工厂的原材料类别对应的规格选项
-- 版本: 1.1
-- 日期: 2025-11-04

CREATE TABLE IF NOT EXISTS material_spec_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    factory_id VARCHAR(50) NOT NULL COMMENT '工厂ID',
    category VARCHAR(50) NOT NULL COMMENT '原材料类别（海鲜、肉类等）',
    specifications TEXT NOT NULL COMMENT '规格选项列表JSON ["切片", "整条", "去骨"]',
    is_system_default BOOLEAN DEFAULT FALSE COMMENT '是否系统默认配置',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_factory_category (factory_id, category) COMMENT '工厂+类别唯一索引',
    INDEX idx_spec_factory (factory_id) COMMENT '工厂ID索引',
    INDEX idx_spec_category (category) COMMENT '类别索引',
    CONSTRAINT fk_spec_factory FOREIGN KEY (factory_id) REFERENCES factories(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='原材料规格配置表';

-- 插入系统默认配置（为已存在的工厂）
-- 注意：新工厂创建时，应通过 MaterialSpecConfigService.initializeDefaultConfigs() 方法自动初始化

INSERT IGNORE INTO material_spec_config (factory_id, category, specifications, is_system_default)
SELECT
    f.id AS factory_id,
    '海鲜' AS category,
    JSON_ARRAY('整条', '切片', '去骨切片', '鱼块', '鱼排', '虾仁', '去壳') AS specifications,
    TRUE AS is_system_default
FROM factories f;

INSERT IGNORE INTO material_spec_config (factory_id, category, specifications, is_system_default)
SELECT
    f.id AS factory_id,
    '肉类' AS category,
    JSON_ARRAY('整块', '切片', '切丁', '绞肉', '排骨', '带骨', '去骨') AS specifications,
    TRUE AS is_system_default
FROM factories f;

INSERT IGNORE INTO material_spec_config (factory_id, category, specifications, is_system_default)
SELECT
    f.id AS factory_id,
    '蔬菜' AS category,
    JSON_ARRAY('整颗', '切段', '切丝', '切块', '切片') AS specifications,
    TRUE AS is_system_default
FROM factories f;

INSERT IGNORE INTO material_spec_config (factory_id, category, specifications, is_system_default)
SELECT
    f.id AS factory_id,
    '水果' AS category,
    JSON_ARRAY('整个', '切片', '切块', '去皮', '带皮') AS specifications,
    TRUE AS is_system_default
FROM factories f;

INSERT IGNORE INTO material_spec_config (factory_id, category, specifications, is_system_default)
SELECT
    f.id AS factory_id,
    '粉类' AS category,
    JSON_ARRAY('袋装', '散装', '桶装') AS specifications,
    TRUE AS is_system_default
FROM factories f;

INSERT IGNORE INTO material_spec_config (factory_id, category, specifications, is_system_default)
SELECT
    f.id AS factory_id,
    '米面' AS category,
    JSON_ARRAY('袋装', '散装', '包装') AS specifications,
    TRUE AS is_system_default
FROM factories f;

INSERT IGNORE INTO material_spec_config (factory_id, category, specifications, is_system_default)
SELECT
    f.id AS factory_id,
    '油类' AS category,
    JSON_ARRAY('瓶装', '桶装', '散装', '大桶', '小瓶') AS specifications,
    TRUE AS is_system_default
FROM factories f;

INSERT IGNORE INTO material_spec_config (factory_id, category, specifications, is_system_default)
SELECT
    f.id AS factory_id,
    '调料' AS category,
    JSON_ARRAY('瓶装', '袋装', '罐装', '散装', '盒装') AS specifications,
    TRUE AS is_system_default
FROM factories f;

INSERT IGNORE INTO material_spec_config (factory_id, category, specifications, is_system_default)
SELECT
    f.id AS factory_id,
    '其他' AS category,
    JSON_ARRAY('原装', '分装', '定制') AS specifications,
    TRUE AS is_system_default
FROM factories f;
