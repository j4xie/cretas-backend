-- =====================================================
-- AI成本分析系统 - 数据库表结构
-- 版本: 1.5
-- 创建日期: 2025-11-04
-- 描述: 创建AI成本分析相关的3个表
-- =====================================================

-- 1. AI分析结果表
-- 存储多层级AI成本分析报告（批次、周报、月报、历史综合报告）
CREATE TABLE IF NOT EXISTS `ai_analysis_results` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `factory_id` VARCHAR(50) NOT NULL COMMENT '工厂ID',
    `batch_id` VARCHAR(50) COMMENT '批次ID（聚合报告时可为空）',
    `report_type` VARCHAR(20) NOT NULL DEFAULT 'batch' COMMENT '报告类型：batch/weekly/monthly/historical',
    `analysis_text` TEXT COMMENT 'AI分析结果（Markdown格式）',
    `session_id` VARCHAR(100) COMMENT 'Python Session ID',
    `period_start` DATETIME COMMENT '报告期间开始时间',
    `period_end` DATETIME COMMENT '报告期间结束时间',
    `expires_at` DATETIME NOT NULL COMMENT '过期时间',
    `is_auto_generated` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否由定时任务自动生成',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 索引
    INDEX `idx_factory_type_expires` (`factory_id`, `report_type`, `expires_at`),
    INDEX `idx_batch_id` (`batch_id`),
    INDEX `idx_factory_batch` (`factory_id`, `batch_id`),
    INDEX `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI分析结果表';

-- 2. AI配额使用表
-- 管理工厂每周AI分析配额
CREATE TABLE IF NOT EXISTS `ai_quota_usage` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `factory_id` VARCHAR(50) NOT NULL COMMENT '工厂ID',
    `week_start` DATE NOT NULL COMMENT '周开始日期（周一）',
    `used_count` INT NOT NULL DEFAULT 0 COMMENT '已使用次数',
    `quota_limit` INT NOT NULL DEFAULT 100 COMMENT '配额上限',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 唯一约束：每个工厂每周只能有一条记录
    UNIQUE KEY `uk_factory_week` (`factory_id`, `week_start`),

    -- 索引
    INDEX `idx_factory_id` (`factory_id`),
    INDEX `idx_week_start` (`week_start`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI配额使用表';

-- 3. AI审计日志表
-- 记录所有AI分析请求用于合规和分析（保留3年）
CREATE TABLE IF NOT EXISTS `ai_audit_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `factory_id` VARCHAR(50) NOT NULL COMMENT '工厂ID',
    `user_id` BIGINT COMMENT '用户ID（定时任务时为NULL）',
    `batch_id` VARCHAR(50) COMMENT '批次ID',
    `question_type` VARCHAR(20) NOT NULL COMMENT '问题类型：default/followup/weekly/monthly/historical',
    `question` TEXT COMMENT '用户问题内容',
    `session_id` VARCHAR(100) COMMENT 'Python Session ID',
    `consumed_quota` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否消耗配额',
    `quota_cost` INT NOT NULL DEFAULT 0 COMMENT '配额消耗数量',
    `is_success` BOOLEAN NOT NULL COMMENT '请求是否成功',
    `error_message` VARCHAR(500) COMMENT '错误信息',
    `response_time_ms` BIGINT COMMENT 'AI响应时间（毫秒）',
    `cache_hit` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否命中缓存',
    `ip_address` VARCHAR(50) COMMENT '用户IP地址',
    `user_agent` VARCHAR(500) COMMENT '用户设备信息',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 索引
    INDEX `idx_factory_created` (`factory_id`, `created_at`),
    INDEX `idx_user_created` (`user_id`, `created_at`),
    INDEX `idx_batch_id` (`batch_id`),
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_question_type` (`question_type`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI审计日志表';

-- =====================================================
-- 数据初始化
-- =====================================================

-- 为现有工厂初始化配额记录（如果需要）
-- 注意：这只是示例，实际运行时根据需要决定是否执行
-- INSERT INTO ai_quota_usage (factory_id, week_start, used_count, quota_limit)
-- SELECT
--     id as factory_id,
--     DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY) as week_start,
--     0 as used_count,
--     100 as quota_limit
-- FROM factories
-- WHERE is_active = TRUE
-- ON DUPLICATE KEY UPDATE used_count = used_count;

-- =====================================================
-- 数据清理说明
-- =====================================================
-- 1. ai_analysis_results: 由定时任务定期清理过期报告
-- 2. ai_quota_usage: 保留最近26周（半年）的记录
-- 3. ai_audit_logs: 保留3年用于合规（ISO 27001）

-- =====================================================
-- 权限说明
-- =====================================================
-- 确保应用用户有以下权限：
-- GRANT SELECT, INSERT, UPDATE, DELETE ON cretas.ai_analysis_results TO 'cretas'@'%';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON cretas.ai_quota_usage TO 'cretas'@'%';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON cretas.ai_audit_logs TO 'cretas'@'%';
