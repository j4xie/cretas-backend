# 原材料入库接口字段修改总结

## 修改日期
2025-01-15

## 修改原因
根据业务需求调整原材料入库字段，更准确地反映实际业务流程。

---

## 📋 字段修改清单

### 1. CreateMaterialBatchRequest.java（入库请求DTO）

| 原字段 | 新字段 | 类型 | 必填 | 说明 |
|-------|-------|------|------|------|
| ~~purchaseDate~~ | **receiptDate** | LocalDate | ✅ | 采购日期 → 入库日期 |
| ~~initialQuantity~~ | **receiptQuantity** | BigDecimal | ✅ | 初始数量 → 入库数量 |
| - | **quantityUnit** | String | ✅ | 数量单位（新增，如"箱"、"袋"） |
| - | **weightPerUnit** | BigDecimal | ❌ | 每单位重量kg（新增） |
| - | **totalWeight** | BigDecimal | ✅ | 入库总重量kg（新增） |
| - | **totalValue** | BigDecimal | ✅ | 入库总价值（新增） |
| unitPrice | unitPrice | BigDecimal | ❌ | 单价（改为可选） |
| supplierId | supplierId | Integer | ✅ | 供应商ID（改为必填） |
| expireDate | expireDate | LocalDate | ❌ | 到期日期（保持可选） |

### 2. MaterialBatch.java（数据库实体）

| 原字段名 | 新字段名 | 数据库字段 | 类型 |
|---------|---------|-----------|------|
| purchaseDate | receiptDate | receipt_date | LocalDate |
| initialQuantity | receiptQuantity | receipt_quantity | BigDecimal(10,2) |
| - | quantityUnit | quantity_unit | String(20) |
| - | weightPerUnit | weight_per_unit | BigDecimal(10,3) |
| - | totalWeight | total_weight | BigDecimal(10,3) |
| - | totalValue | total_value | BigDecimal(10,2) |
| unitPrice | unitPrice | unit_price | BigDecimal(10,2) - 改为可空 |

### 3. MaterialBatchDTO.java（响应DTO）

新增字段：
- `receiptDate` - 入库日期
- `receiptQuantity` - 入库数量
- `quantityUnit` - 数量单位
- `weightPerUnit` - 每单位重量
- `totalWeight` - 总重量
- `totalValue` - 总价值
- `ifRunout` - 是否消耗完（计算字段）

---

## 🔧 业务逻辑修改

### 1. 单价自动计算
```java
// 如果用户未填单价，自动计算
if (unitPrice == null) {
    unitPrice = totalValue ÷ totalWeight
}
```

### 2. 到期日期自动计算
```java
// 优先使用用户填写的到期日期
if (expireDate != null) {
    return expireDate;
}

// 否则根据原材料类型的保质期自动计算
if (materialType.shelfLifeDays != null) {
    return receiptDate + shelfLifeDays;
}
```

### 3. 单位转换系统
- 支持常用单位：KG、G、T（重量单位）
- 支持自定义单位：箱、袋、件、瓶、罐等
- weightPerUnit字段记录每单位对应的kg重量

### 4. ifRunout计算逻辑
```java
ifRunout = (status == USED_UP || status == EXPIRED || status == SCRAPPED)
```

---

## 📊 数据库Migration脚本

```sql
ALTER TABLE material_batches
  -- 重命名字段
  CHANGE COLUMN purchase_date receipt_date DATE NOT NULL,
  CHANGE COLUMN initial_quantity receipt_quantity DECIMAL(10,2) NOT NULL,

  -- 新增字段
  ADD COLUMN quantity_unit VARCHAR(20) NOT NULL AFTER receipt_quantity,
  ADD COLUMN weight_per_unit DECIMAL(10,3) AFTER quantity_unit,
  ADD COLUMN total_weight DECIMAL(10,3) NOT NULL AFTER weight_per_unit,
  ADD COLUMN total_value DECIMAL(10,2) NOT NULL AFTER reserved_quantity,

  -- 修改字段属性
  MODIFY COLUMN unit_price DECIMAL(10,2) NULL;
```

---

## ✅ 修改验证清单

- [x] CreateMaterialBatchRequest字段修改
- [x] MaterialBatch实体类字段修改
- [x] MaterialBatchDTO字段修改
- [x] MaterialBatchMapper映射逻辑更新
- [x] MaterialBatchController默认天数改为3
- [x] 添加ifRunout计算字段
- [ ] MaterialBatchServiceImpl业务逻辑修复
- [ ] 数据库Migration脚本生成
- [ ] 接口测试验证

---

## 🎯 后续工作

1. **修复Service层语法错误** - 多个方法缺少return语句和右括号
2. **添加保质期自动计算** - 在MaterialBatchServiceImpl中实现
3. **添加单价验证逻辑** - 验证用户填写单价与计算单价的差异
4. **生成并执行Migration脚本** - 更新生产数据库
5. **编写集成测试** - 验证完整流程

---

## 📝 注意事项

1. ⚠️ **数据库字段重命名需要谨慎** - 确保所有引用都已更新
2. ⚠️ **保持向后兼容** - 考虑已有数据的迁移
3. ⚠️ **单价可选逻辑** - 确保系统能够正确处理未填单价的情况
4. ⚠️ **单位转换** - 需要完善单位管理系统
5. ⚠️ **权限控制** - 确保只有authorized用户可以修改批次

---

## 🔗 相关文件

- `/src/main/java/com/cretas/aims/dto/material/CreateMaterialBatchRequest.java`
- `/src/main/java/com/cretas/aims/entity/MaterialBatch.java`
- `/src/main/java/com/cretas/aims/dto/material/MaterialBatchDTO.java`
- `/src/main/java/com/cretas/aims/mapper/MaterialBatchMapper.java`
- `/src/main/java/com/cretas/aims/controller/MaterialBatchController.java`
- `/src/main/java/com/cretas/aims/service/impl/MaterialBatchServiceImpl.java` (待修复)
