-- =====================================================
-- 质检与返工流程完善迁移脚本
-- Version: 2.1
-- Date: 2025-11-05
-- Author: Cretas Backend Optimization Team
-- Description: 添加返工记录表和报废记录表，完善质检流程
-- =====================================================

-- =====================================================
-- Part 1: 创建返工记录表
-- =====================================================

CREATE TABLE IF NOT EXISTS rework_records (
    -- 主键
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '返工记录ID',

    -- 基础信息
    factory_id VARCHAR(50) NOT NULL COMMENT '工厂ID',
    quality_inspection_id BIGINT COMMENT '质检记录ID',
    production_batch_id BIGINT COMMENT '生产批次ID',
    material_batch_id INT COMMENT '原材料批次ID',

    -- 返工信息
    rework_quantity DECIMAL(12,2) NOT NULL COMMENT '返工数量',
    rework_type VARCHAR(30) NOT NULL COMMENT '返工类型: PRODUCTION_REWORK, MATERIAL_REWORK, QUALITY_REWORK等',
    rework_reason TEXT COMMENT '返工原因详细描述',
    rework_method TEXT COMMENT '返工方法/步骤',

    -- 时间信息
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    labor_duration_minutes INT COMMENT '人工时长（分钟）',

    -- 状态信息
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '返工状态: PENDING, IN_PROGRESS, COMPLETED, FAILED, CANCELLED',

    -- 结果信息
    success_quantity DECIMAL(12,2) COMMENT '成功数量（返工后合格）',
    failed_quantity DECIMAL(12,2) COMMENT '失败数量（返工后仍不合格）',
    salvage_quantity DECIMAL(12,2) COMMENT '挽救数量',
    rework_cost DECIMAL(12,2) COMMENT '返工成本',

    -- 负责人信息
    supervisor_id INT COMMENT '负责人ID',
    supervisor_name VARCHAR(100) COMMENT '负责人姓名',

    -- 备注
    notes TEXT COMMENT '备注',

    -- 时间戳（继承自BaseEntity）
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    deleted_at DATETIME COMMENT '软删除时间戳',

    -- 索引
    INDEX idx_rework_factory (factory_id),
    INDEX idx_rework_status (status),
    INDEX idx_rework_quality (quality_inspection_id),
    INDEX idx_rework_batch (production_batch_id),
    INDEX idx_rework_material (material_batch_id),
    INDEX idx_rework_date (start_time),
    INDEX idx_rework_deleted (deleted_at),

    -- 外键约束
    CONSTRAINT fk_rework_quality FOREIGN KEY (quality_inspection_id)
        REFERENCES quality_inspections(id) ON DELETE SET NULL,
    CONSTRAINT fk_rework_production FOREIGN KEY (production_batch_id)
        REFERENCES production_batches(id) ON DELETE SET NULL,
    CONSTRAINT fk_rework_material FOREIGN KEY (material_batch_id)
        REFERENCES material_batches(id) ON DELETE SET NULL,
    CONSTRAINT fk_rework_supervisor FOREIGN KEY (supervisor_id)
        REFERENCES users(id) ON DELETE SET NULL

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='返工记录表 - 追踪不合格品的返工处理全流程';

-- =====================================================
-- Part 2: 创建报废记录表
-- =====================================================

CREATE TABLE IF NOT EXISTS disposal_records (
    -- 主键
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '报废记录ID',

    -- 基础信息
    factory_id VARCHAR(50) NOT NULL COMMENT '工厂ID',
    quality_inspection_id BIGINT COMMENT '质检记录ID（质检后直接报废）',
    rework_record_id BIGINT COMMENT '返工记录ID（返工失败后报废）',
    production_batch_id BIGINT COMMENT '生产批次ID',
    material_batch_id INT COMMENT '原材料批次ID',

    -- 报废信息
    disposal_quantity DECIMAL(12,2) NOT NULL COMMENT '报废数量',
    disposal_type VARCHAR(30) NOT NULL COMMENT '报废类型: SCRAP, RECYCLE, RETURN, DONATE, DESTROY',
    disposal_reason TEXT COMMENT '报废原因详细描述',
    disposal_date DATETIME NOT NULL COMMENT '报废日期',
    disposal_method TEXT COMMENT '处理方式说明',

    -- 审批信息
    is_approved BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已审批',
    approved_by INT COMMENT '审批人ID',
    approved_by_name VARCHAR(100) COMMENT '审批人姓名',
    approval_date DATETIME COMMENT '审批日期',

    -- 成本信息
    estimated_loss DECIMAL(12,2) COMMENT '预估损失金额',
    actual_loss DECIMAL(12,2) COMMENT '实际损失金额',
    recovery_value DECIMAL(12,2) COMMENT '回收价值（如果可回收）',

    -- 备注
    notes TEXT COMMENT '备注',

    -- 时间戳（继承自BaseEntity）
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    deleted_at DATETIME COMMENT '软删除时间戳',

    -- 索引
    INDEX idx_disposal_factory (factory_id),
    INDEX idx_disposal_type (disposal_type),
    INDEX idx_disposal_date (disposal_date),
    INDEX idx_disposal_quality (quality_inspection_id),
    INDEX idx_disposal_rework (rework_record_id),
    INDEX idx_disposal_approved (is_approved),
    INDEX idx_disposal_deleted (deleted_at),

    -- 外键约束
    CONSTRAINT fk_disposal_quality FOREIGN KEY (quality_inspection_id)
        REFERENCES quality_inspections(id) ON DELETE SET NULL,
    CONSTRAINT fk_disposal_rework FOREIGN KEY (rework_record_id)
        REFERENCES rework_records(id) ON DELETE SET NULL,
    CONSTRAINT fk_disposal_production FOREIGN KEY (production_batch_id)
        REFERENCES production_batches(id) ON DELETE SET NULL,
    CONSTRAINT fk_disposal_material FOREIGN KEY (material_batch_id)
        REFERENCES material_batches(id) ON DELETE SET NULL,
    CONSTRAINT fk_disposal_approver FOREIGN KEY (approved_by)
        REFERENCES users(id) ON DELETE SET NULL

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='报废记录表 - 追踪不合格品的报废处理';

-- =====================================================
-- Part 3: 修改现有表结构 - 增强质检功能
-- =====================================================

-- 3.1 扩展 quality_inspections 表
ALTER TABLE quality_inspections
    ADD COLUMN IF NOT EXISTS defect_category VARCHAR(100) COMMENT '不合格类别' AFTER result,
    ADD COLUMN IF NOT EXISTS rework_suggestion TEXT COMMENT '返工建议' AFTER defect_category,
    ADD COLUMN IF NOT EXISTS inspector_name VARCHAR(100) COMMENT '质检员姓名' AFTER inspector_id,
    ADD COLUMN IF NOT EXISTS material_batch_id INT COMMENT '原材料批次ID（用于原料质检）' AFTER production_batch_id,
    ADD INDEX idx_quality_material (material_batch_id);

-- 3.2 添加外键约束（如果不存在）
ALTER TABLE quality_inspections
    ADD CONSTRAINT IF NOT EXISTS fk_quality_material
    FOREIGN KEY (material_batch_id)
    REFERENCES material_batches(id) ON DELETE SET NULL;

-- 3.3 扩展 production_batches 表 - 添加返工相关字段
ALTER TABLE production_batches
    ADD COLUMN IF NOT EXISTS rework_quantity DECIMAL(12,2) COMMENT '返工数量' AFTER defect_quantity,
    ADD COLUMN IF NOT EXISTS rework_status VARCHAR(30) COMMENT '返工状态' AFTER quality_status,
    ADD COLUMN IF NOT EXISTS rework_notes TEXT COMMENT '返工备注' AFTER notes,
    ADD INDEX idx_batch_rework_status (rework_status);

-- 3.4 扩展 material_batches 表 - 添加质检相关字段
ALTER TABLE material_batches
    ADD COLUMN IF NOT EXISTS quality_inspection_id BIGINT COMMENT '质检记录ID' AFTER status,
    ADD COLUMN IF NOT EXISTS quality_status VARCHAR(50) COMMENT '质量状态' AFTER quality_inspection_id,
    ADD INDEX idx_material_quality_status (quality_status),
    ADD INDEX idx_material_quality_inspection (quality_inspection_id);

-- 3.5 修改 production_batches 表的 quality_status 列长度（支持新枚举值）
ALTER TABLE production_batches
    MODIFY COLUMN quality_status VARCHAR(30) COMMENT '质量状态: PENDING_INSPECTION, INSPECTING, PASSED, FAILED, PARTIAL_PASS, REWORK_REQUIRED等';

-- =====================================================
-- Part 4: 创建触发器 - 自动更新相关统计
-- =====================================================

-- 4.1 返工完成后自动更新生产批次统计
DELIMITER //
CREATE TRIGGER IF NOT EXISTS trg_rework_update_batch
AFTER UPDATE ON rework_records
FOR EACH ROW
BEGIN
    IF NEW.status = 'COMPLETED' AND OLD.status != 'COMPLETED' THEN
        -- 更新生产批次的返工数量
        IF NEW.production_batch_id IS NOT NULL THEN
            UPDATE production_batches
            SET rework_quantity = COALESCE(rework_quantity, 0) + NEW.success_quantity
            WHERE id = NEW.production_batch_id;
        END IF;
    END IF;
END//
DELIMITER ;

-- 4.2 报废审批后自动更新批次状态
DELIMITER //
CREATE TRIGGER IF NOT EXISTS trg_disposal_update_batch
AFTER UPDATE ON disposal_records
FOR EACH ROW
BEGIN
    IF NEW.is_approved = TRUE AND OLD.is_approved = FALSE THEN
        -- 如果是生产批次报废，更新批次状态
        IF NEW.production_batch_id IS NOT NULL THEN
            UPDATE production_batches
            SET quality_status = 'SCRAPPED'
            WHERE id = NEW.production_batch_id
            AND quality_status NOT IN ('PASSED', 'PARTIAL_PASS');
        END IF;

        -- 如果是原材料报废，更新批次状态
        IF NEW.material_batch_id IS NOT NULL THEN
            UPDATE material_batches
            SET status = 'SCRAPPED'
            WHERE id = NEW.material_batch_id
            AND status NOT IN ('AVAILABLE', 'DEPLETED');
        END IF;
    END IF;
END//
DELIMITER ;

-- =====================================================
-- Part 5: 插入初始数据和统计视图
-- =====================================================

-- 5.1 创建返工统计视图
CREATE OR REPLACE VIEW v_rework_statistics AS
SELECT
    factory_id,
    DATE(start_time) as rework_date,
    rework_type,
    status,
    COUNT(*) as record_count,
    SUM(rework_quantity) as total_rework_quantity,
    SUM(success_quantity) as total_success_quantity,
    SUM(failed_quantity) as total_failed_quantity,
    AVG(CASE WHEN rework_quantity > 0
        THEN (success_quantity / rework_quantity) * 100
        ELSE 0 END) as avg_success_rate,
    SUM(rework_cost) as total_rework_cost,
    SUM(labor_duration_minutes) as total_labor_minutes
FROM rework_records
WHERE deleted_at IS NULL
GROUP BY factory_id, DATE(start_time), rework_type, status;

-- 5.2 创建报废统计视图
CREATE OR REPLACE VIEW v_disposal_statistics AS
SELECT
    factory_id,
    DATE(disposal_date) as disposal_date,
    disposal_type,
    is_approved,
    COUNT(*) as record_count,
    SUM(disposal_quantity) as total_disposal_quantity,
    SUM(estimated_loss) as total_estimated_loss,
    SUM(actual_loss) as total_actual_loss,
    SUM(recovery_value) as total_recovery_value,
    SUM(COALESCE(actual_loss, estimated_loss) - COALESCE(recovery_value, 0)) as net_loss
FROM disposal_records
WHERE deleted_at IS NULL
GROUP BY factory_id, DATE(disposal_date), disposal_type, is_approved;

-- 5.3 创建质检完整流程视图
CREATE OR REPLACE VIEW v_quality_full_process AS
SELECT
    qi.id as inspection_id,
    qi.factory_id,
    qi.production_batch_id,
    qi.material_batch_id,
    qi.inspection_date,
    qi.result as inspection_result,
    qi.defect_category,

    -- 返工信息
    rw.id as rework_id,
    rw.rework_type,
    rw.rework_quantity,
    rw.status as rework_status,
    rw.success_quantity as rework_success_qty,

    -- 报废信息
    dp.id as disposal_id,
    dp.disposal_type,
    dp.disposal_quantity,
    dp.is_approved as disposal_approved,
    dp.estimated_loss,

    -- 最终状态
    CASE
        WHEN qi.result = 'PASS' THEN 'PASSED'
        WHEN rw.status = 'COMPLETED' THEN 'REWORKED'
        WHEN dp.is_approved = TRUE THEN 'DISPOSED'
        WHEN qi.result = 'FAIL' THEN 'PENDING_ACTION'
        ELSE 'UNDER_INSPECTION'
    END as final_status

FROM quality_inspections qi
LEFT JOIN rework_records rw ON qi.id = rw.quality_inspection_id AND rw.deleted_at IS NULL
LEFT JOIN disposal_records dp ON qi.id = dp.quality_inspection_id AND dp.deleted_at IS NULL
WHERE qi.deleted_at IS NULL;

-- =====================================================
-- Part 6: 添加注释和文档
-- =====================================================

-- 为新表添加详细注释
ALTER TABLE rework_records COMMENT = '返工记录表 - 追踪质检不合格品的返工处理全流程，包括返工原因、方法、结果统计等';
ALTER TABLE disposal_records COMMENT = '报废记录表 - 记录不合格品的报废处理，包括报废类型、审批流程、成本损失等';

-- =====================================================
-- 迁移完成说明
-- =====================================================

/*
本迁移脚本完成了以下功能：

1. 新增表（2个）：
   - rework_records: 返工记录表，支持生产批次和原材料批次的返工追踪
   - disposal_records: 报废记录表，支持审批流程和成本核算

2. 扩展表（3个）：
   - quality_inspections: 添加缺陷类别、返工建议、原材料质检支持
   - production_batches: 添加返工数量和状态字段
   - material_batches: 添加质检ID和质量状态字段

3. 数据库对象：
   - 2个触发器：自动更新批次统计和状态
   - 3个视图：返工统计、报废统计、质检全流程

4. 索引优化：
   - 为所有外键添加索引
   - 为常用查询字段（status, type, date）添加索引
   - 为软删除字段添加索引

5. 完整性约束：
   - 外键约束确保数据一致性
   - 级联设置为SET NULL（软删除友好）

使用建议：
- 返工流程：质检不合格 -> 创建返工记录 -> 执行返工 -> 更新结果 -> 复检
- 报废流程：质检/返工失败 -> 创建报废记录 -> 审批 -> 执行报废 -> 记录损失
- 统计查询：使用提供的视图进行数据分析和报表生成
*/

-- =====================================================
-- 回滚脚本（如需回滚）
-- =====================================================

/*
-- 删除视图
DROP VIEW IF EXISTS v_quality_full_process;
DROP VIEW IF EXISTS v_disposal_statistics;
DROP VIEW IF EXISTS v_rework_statistics;

-- 删除触发器
DROP TRIGGER IF EXISTS trg_disposal_update_batch;
DROP TRIGGER IF EXISTS trg_rework_update_batch;

-- 删除新增的列
ALTER TABLE material_batches
    DROP COLUMN quality_inspection_id,
    DROP COLUMN quality_status;

ALTER TABLE production_batches
    DROP COLUMN rework_quantity,
    DROP COLUMN rework_status,
    DROP COLUMN rework_notes;

ALTER TABLE quality_inspections
    DROP COLUMN defect_category,
    DROP COLUMN rework_suggestion,
    DROP COLUMN inspector_name,
    DROP COLUMN material_batch_id;

-- 删除新表
DROP TABLE IF EXISTS disposal_records;
DROP TABLE IF EXISTS rework_records;
*/
