-- =====================================================
-- 软删除功能迁移脚本
-- Version: 2.0
-- Date: 2025-11-05
-- Author: Cretas Backend Optimization Team
-- Description: 为所有表添加软删除支持（deleted_at字段）
-- =====================================================

-- =====================================================
-- Phase 1: 核心实体表
-- =====================================================

-- 用户和工厂相关
ALTER TABLE users ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_users_deleted_at ON users(deleted_at);

ALTER TABLE factories ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_factories_deleted_at ON factories(deleted_at);

ALTER TABLE platform_admins ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_platform_admins_deleted_at ON platform_admins(deleted_at);

ALTER TABLE whitelist ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_whitelist_deleted_at ON whitelist(deleted_at);

ALTER TABLE factory_settings ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_factory_settings_deleted_at ON factory_settings(deleted_at);

-- =====================================================
-- Phase 2: 生产管理表
-- =====================================================

-- 生产批次
ALTER TABLE processing_batches ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_processing_batches_deleted_at ON processing_batches(deleted_at);

ALTER TABLE production_batches ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_production_batches_deleted_at ON production_batches(deleted_at);

ALTER TABLE production_plans ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_production_plans_deleted_at ON production_plans(deleted_at);

-- =====================================================
-- Phase 3: 质检相关表
-- =====================================================

ALTER TABLE quality_inspections ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_quality_inspections_deleted_at ON quality_inspections(deleted_at);

-- =====================================================
-- Phase 4: 物料管理表
-- =====================================================

ALTER TABLE material_batches ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_material_batches_deleted_at ON material_batches(deleted_at);

ALTER TABLE material_consumptions ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_material_consumptions_deleted_at ON material_consumptions(deleted_at);

ALTER TABLE material_batch_adjustments ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_material_batch_adjustments_deleted_at ON material_batch_adjustments(deleted_at);

ALTER TABLE raw_material_types ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_raw_material_types_deleted_at ON raw_material_types(deleted_at);

ALTER TABLE material_spec_config ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_material_spec_config_deleted_at ON material_spec_config(deleted_at);

-- =====================================================
-- Phase 5: 转化率和产品类型表
-- =====================================================

ALTER TABLE material_product_conversions ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_material_product_conversions_deleted_at ON material_product_conversions(deleted_at);

ALTER TABLE product_types ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_product_types_deleted_at ON product_types(deleted_at);

-- =====================================================
-- Phase 6: 考勤和工时表
-- =====================================================

ALTER TABLE time_clock_records ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_time_clock_records_deleted_at ON time_clock_records(deleted_at);

ALTER TABLE employee_work_sessions ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_employee_work_sessions_deleted_at ON employee_work_sessions(deleted_at);

ALTER TABLE batch_work_sessions ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_batch_work_sessions_deleted_at ON batch_work_sessions(deleted_at);

ALTER TABLE work_types ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_work_types_deleted_at ON work_types(deleted_at);

-- =====================================================
-- Phase 7: 设备管理表
-- =====================================================

ALTER TABLE equipment ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_equipment_deleted_at ON equipment(deleted_at);

ALTER TABLE factory_equipment ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_factory_equipment_deleted_at ON factory_equipment(deleted_at);

ALTER TABLE equipment_usages ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_equipment_usages_deleted_at ON equipment_usages(deleted_at);

ALTER TABLE equipment_maintenance ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_equipment_maintenance_deleted_at ON equipment_maintenance(deleted_at);

ALTER TABLE batch_equipment_usage ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_batch_equipment_usage_deleted_at ON batch_equipment_usage(deleted_at);

-- =====================================================
-- Phase 8: 合作伙伴表
-- =====================================================

ALTER TABLE suppliers ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_suppliers_deleted_at ON suppliers(deleted_at);

ALTER TABLE customers ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_customers_deleted_at ON customers(deleted_at);

ALTER TABLE shipment_records ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_shipment_records_deleted_at ON shipment_records(deleted_at);

-- =====================================================
-- Phase 9: AI和分析表
-- =====================================================

ALTER TABLE ai_analysis_results ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_ai_analysis_results_deleted_at ON ai_analysis_results(deleted_at);

ALTER TABLE ai_usage_log ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_ai_usage_log_deleted_at ON ai_usage_log(deleted_at);

ALTER TABLE ai_audit_logs ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_ai_audit_logs_deleted_at ON ai_audit_logs(deleted_at);

ALTER TABLE ai_quota_usage ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_ai_quota_usage_deleted_at ON ai_quota_usage(deleted_at);

-- =====================================================
-- Phase 10: 系统和设备激活表
-- =====================================================

ALTER TABLE system_logs ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_system_logs_deleted_at ON system_logs(deleted_at);

ALTER TABLE device_activations ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_device_activations_deleted_at ON device_activations(deleted_at);

ALTER TABLE production_plan_batch_usage ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_production_plan_batch_usage_deleted_at ON production_plan_batch_usage(deleted_at);

-- =====================================================
-- Phase 11: Sessions表 (特殊处理 - varchar主键)
-- =====================================================

ALTER TABLE sessions ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间戳';
CREATE INDEX idx_sessions_deleted_at ON sessions(deleted_at);

-- =====================================================
-- 迁移完成统计
-- =====================================================

-- 共36个表全部添加软删除支持
-- 表列表:
-- 1. users
-- 2. factories
-- 3. platform_admins
-- 4. whitelist
-- 5. factory_settings
-- 6. processing_batches
-- 7. production_batches
-- 8. production_plans
-- 9. quality_inspections
-- 10. material_batches
-- 11. material_consumptions
-- 12. material_batch_adjustments
-- 13. raw_material_types
-- 14. material_spec_config
-- 15. material_product_conversions
-- 16. product_types
-- 17. time_clock_records
-- 18. employee_work_sessions
-- 19. batch_work_sessions
-- 20. work_types
-- 21. equipment
-- 22. factory_equipment
-- 23. equipment_usages
-- 24. equipment_maintenance
-- 25. batch_equipment_usage
-- 26. suppliers
-- 27. customers
-- 28. shipment_records
-- 29. ai_analysis_results
-- 30. ai_usage_log
-- 31. ai_audit_logs
-- 32. ai_quota_usage
-- 33. system_logs
-- 34. device_activations
-- 35. production_plan_batch_usage
-- 36. sessions

-- =====================================================
-- 回滚脚本 (如需回滚，请执行以下语句)
-- =====================================================

/*
-- 删除所有deleted_at列和索引
ALTER TABLE users DROP INDEX idx_users_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE factories DROP INDEX idx_factories_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE platform_admins DROP INDEX idx_platform_admins_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE whitelist DROP INDEX idx_whitelist_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE factory_settings DROP INDEX idx_factory_settings_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE processing_batches DROP INDEX idx_processing_batches_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE production_batches DROP INDEX idx_production_batches_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE production_plans DROP INDEX idx_production_plans_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE quality_inspections DROP INDEX idx_quality_inspections_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE material_batches DROP INDEX idx_material_batches_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE material_consumptions DROP INDEX idx_material_consumptions_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE material_batch_adjustments DROP INDEX idx_material_batch_adjustments_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE raw_material_types DROP INDEX idx_raw_material_types_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE material_spec_config DROP INDEX idx_material_spec_config_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE material_product_conversions DROP INDEX idx_material_product_conversions_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE product_types DROP INDEX idx_product_types_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE time_clock_records DROP INDEX idx_time_clock_records_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE employee_work_sessions DROP INDEX idx_employee_work_sessions_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE batch_work_sessions DROP INDEX idx_batch_work_sessions_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE work_types DROP INDEX idx_work_types_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE equipment DROP INDEX idx_equipment_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE factory_equipment DROP INDEX idx_factory_equipment_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE equipment_usages DROP INDEX idx_equipment_usages_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE equipment_maintenance DROP INDEX idx_equipment_maintenance_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE batch_equipment_usage DROP INDEX idx_batch_equipment_usage_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE suppliers DROP INDEX idx_suppliers_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE customers DROP INDEX idx_customers_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE shipment_records DROP INDEX idx_shipment_records_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE ai_analysis_results DROP INDEX idx_ai_analysis_results_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE ai_usage_log DROP INDEX idx_ai_usage_log_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE ai_audit_logs DROP INDEX idx_ai_audit_logs_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE ai_quota_usage DROP INDEX idx_ai_quota_usage_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE system_logs DROP INDEX idx_system_logs_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE device_activations DROP INDEX idx_device_activations_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE production_plan_batch_usage DROP INDEX idx_production_plan_batch_usage_deleted_at, DROP COLUMN deleted_at;
ALTER TABLE sessions DROP INDEX idx_sessions_deleted_at, DROP COLUMN deleted_at;
*/
