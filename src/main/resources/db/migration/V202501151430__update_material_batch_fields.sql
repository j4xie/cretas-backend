-- =====================================================
-- 原材料批次表字段修改
-- 版本: V2025.01.15.1430
-- 说明: 更新原材料入库相关字段，支持单位转换和价值管理
-- 作者: Cretas Team
-- 日期: 2025-01-15
-- =====================================================

-- 1. 重命名字段
ALTER TABLE material_batches
  CHANGE COLUMN purchase_date receipt_date DATE NOT NULL COMMENT '入库日期（原采购日期）';

ALTER TABLE material_batches
  CHANGE COLUMN initial_quantity receipt_quantity DECIMAL(10,2) NOT NULL COMMENT '入库数量';

-- 2. 新增字段
ALTER TABLE material_batches
  ADD COLUMN quantity_unit VARCHAR(20) NOT NULL DEFAULT 'KG' COMMENT '数量单位（箱、袋、件、KG等）' AFTER receipt_quantity;

ALTER TABLE material_batches
  ADD COLUMN weight_per_unit DECIMAL(10,3) NULL COMMENT '每单位重量(kg)，如10.5kg/箱' AFTER quantity_unit;

ALTER TABLE material_batches
  ADD COLUMN total_weight DECIMAL(10,3) NOT NULL DEFAULT 0 COMMENT '入库总重量(kg)' AFTER weight_per_unit;

ALTER TABLE material_batches
  ADD COLUMN total_value DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '入库总价值(元)' AFTER reserved_quantity;

-- 3. 修改字段属性（单价改为可选）
ALTER TABLE material_batches
  MODIFY COLUMN unit_price DECIMAL(10,2) NULL COMMENT '单价(元/kg)，可选，不填则自动计算';

-- 4. 数据迁移（为已存在的数据设置默认值）
-- 假设旧数据的数量单位都是KG
UPDATE material_batches
SET
  quantity_unit = 'KG',
  total_weight = current_quantity,
  total_value = COALESCE(total_price, current_quantity * COALESCE(unit_price, 0))
WHERE quantity_unit IS NULL OR quantity_unit = '';

-- 5. 添加索引（可选，提高查询性能）
CREATE INDEX idx_material_batch_receipt_date ON material_batches(receipt_date);
CREATE INDEX idx_material_batch_quantity_unit ON material_batches(quantity_unit);

-- 6. 添加检查约束（确保数据完整性）
ALTER TABLE material_batches
  ADD CONSTRAINT chk_total_weight_positive CHECK (total_weight > 0);

ALTER TABLE material_batches
  ADD CONSTRAINT chk_total_value_positive CHECK (total_value >= 0);

-- =====================================================
-- Migration完成
-- =====================================================
-- 注意事项:
-- 1. 执行前请备份数据库
-- 2. 建议在测试环境先执行验证
-- 3. 执行后需要重启应用服务
-- 4. 如有回滚需求，请使用对应的回滚脚本
-- =====================================================
