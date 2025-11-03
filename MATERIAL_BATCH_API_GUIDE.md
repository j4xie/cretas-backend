# 原材料入库API完整使用指南

## 📅 更新日期
2025-01-15

## ✅ 已完成的修改

### 1. 核心字段修改
- ✅ CreateMaterialBatchRequest - 入库请求DTO
- ✅ MaterialBatch - 数据库实体
- ✅ MaterialBatchDTO - 响应DTO
- ✅ MaterialBatchMapper - 实体映射器
- ✅ MaterialBatchController - 控制器

### 2. 新增业务逻辑
- ✅ 单价自动计算（totalValue ÷ totalWeight）
- ✅ 单价验证警告（5%误差容忍，以总价值为准）
- ✅ 保质期自动计算（receiptDate + shelfLifeDays）
- ✅ ifRunout计算字段（status-based）

---

## 📦 API接口详细说明

### 1. 创建原材料批次（入库核心接口）

**接口**: `POST /api/mobile/{factoryId}/material-batches`

**请求头**:
```
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**:
```json
{
  "materialTypeId": 5,              // 必填 - 原材料类型ID
  "supplierId": 12,                 // 必填 - 供应商ID
  "receiptDate": "2025-01-15",      // 必填 - 入库日期
  "receiptQuantity": 10.0,          // 必填 - 入库数量（如：10箱）
  "quantityUnit": "箱",             // 必填 - 数量单位
  "weightPerUnit": 10.5,            // 可选 - 每单位重量(kg)，如10.5kg/箱
  "totalWeight": 105.0,             // 必填 - 入库总重量(kg)
  "totalValue": 4725.0,             // 必填 - 入库总价值(元)
  "unitPrice": 45.0,                // 可选 - 单价(元/kg)
  "expireDate": "2025-01-18",       // 可选 - 到期日期
  "storageLocation": "冷库A区-03",   // 可选 - 存储位置
  "qualityCertificate": "QC20250115-001", // 可选 - 质量证书
  "notes": "新鲜鲜虾，冷链运输"        // 可选 - 备注
}
```

**字段说明**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| materialTypeId | Integer | ✅ | 原材料类型ID，从原材料类型列表获取 |
| supplierId | Integer | ✅ | 供应商ID，从供应商列表获取 |
| receiptDate | String(Date) | ✅ | 入库日期，格式：YYYY-MM-DD |
| receiptQuantity | Number | ✅ | 入库数量，如10表示10箱/袋/件 |
| quantityUnit | String | ✅ | 数量单位，如"箱"、"袋"、"件"、"KG" |
| weightPerUnit | Number | ❌ | 每单位重量(kg)，当单位不是KG时建议填写 |
| totalWeight | Number | ✅ | 入库总重量(kg)，用于成本计算 |
| totalValue | Number | ✅ | 入库总价值(元)，实际支付金额 |
| unitPrice | Number | ❌ | 单价(元/kg)，不填则自动计算 |
| expireDate | String(Date) | ❌ | 到期日期，不填则根据保质期自动计算 |
| storageLocation | String | ❌ | 存储位置，如"冷库A区-03" |
| qualityCertificate | String | ❌ | 质量证书编号 |
| notes | String | ❌ | 备注信息，最多500字符 |

**自动计算规则**:

1. **单价自动计算**:
   ```
   if (未填unitPrice) {
       unitPrice = totalValue ÷ totalWeight
   }
   ```

2. **单价验证警告**（如果用户填写了单价）:
   ```
   calculatedUnitPrice = totalValue ÷ totalWeight
   if (|userUnitPrice - calculatedUnitPrice| > 5% of calculatedUnitPrice) {
       记录警告日志
       在notes中添加提示
       最终使用calculatedUnitPrice（以总价值为准）
   }
   ```

3. **到期日期自动计算**:
   ```
   if (未填expireDate && 原材料类型有shelfLifeDays) {
       expireDate = receiptDate + shelfLifeDays天
   }
   ```

4. **批次号自动生成**:
   ```
   格式: MAT-YYYYMMDD-HHMMSS
   示例: MAT-20250115-143022
   ```

**成功响应** (200 OK):
```json
{
  "success": true,
  "message": "原材料批次创建成功",
  "data": {
    "id": 123,
    "factoryId": "TEST_FACTORY_001",
    "batchNumber": "MAT-20250115-143022",
    "materialTypeId": 5,
    "materialName": "鲜虾",
    "materialCode": "RM-SHRIMP-001",
    "materialCategory": "海鲜类",
    "supplierId": 12,
    "supplierName": "XX海鲜供应商",
    "receiptDate": "2025-01-15",
    "expireDate": "2025-01-18",
    "receiptQuantity": 10.0,
    "quantityUnit": "箱",
    "weightPerUnit": 10.5,
    "totalWeight": 105.0,
    "currentQuantity": 105.0,
    "unit": "kg",
    "totalValue": 4725.0,
    "unitPrice": 45.0,
    "totalPrice": 4725.0,
    "status": "AVAILABLE",
    "statusDisplayName": "可用",
    "ifRunout": false,
    "storageLocation": "冷库A区-03",
    "qualityCertificate": "QC20250115-001",
    "notes": "新鲜鲜虾，冷链运输",
    "remainingDays": 3,
    "usageRate": 0.00,
    "createdBy": 1,
    "createdByName": "张三",
    "createdAt": "2025-01-15T14:30:22",
    "updatedAt": "2025-01-15T14:30:22"
  }
}
```

**错误响应示例**:

```json
// 400 - 参数验证失败
{
  "success": false,
  "message": "供应商不能为空",
  "code": "VALIDATION_ERROR"
}

// 404 - 原材料类型不存在
{
  "success": false,
  "message": "原材料类型不存在",
  "code": "RESOURCE_NOT_FOUND"
}
```

---

### 2. 完整使用示例

#### 场景1：标准入库（填写单价）

```bash
curl -X POST "https://api.example.com/api/mobile/TEST_FACTORY_001/material-batches" \
  -H "Authorization: Bearer eyJhbGc..." \
  -H "Content-Type: application/json" \
  -d '{
    "materialTypeId": 5,
    "supplierId": 12,
    "receiptDate": "2025-01-15",
    "receiptQuantity": 10,
    "quantityUnit": "箱",
    "weightPerUnit": 10.5,
    "totalWeight": 105.0,
    "totalValue": 4725.0,
    "unitPrice": 45.0,
    "storageLocation": "冷库A区-03"
  }'
```

#### 场景2：简化入库（不填单价，自动计算）

```bash
curl -X POST "https://api.example.com/api/mobile/TEST_FACTORY_001/material-batches" \
  -H "Authorization: Bearer eyJhbGc..." \
  -H "Content-Type: application/json" \
  -d '{
    "materialTypeId": 5,
    "supplierId": 12,
    "receiptDate": "2025-01-15",
    "receiptQuantity": 10,
    "quantityUnit": "箱",
    "totalWeight": 105.0,
    "totalValue": 4725.0
  }'
```
**系统自动计算**: unitPrice = 4725 ÷ 105 = 45.0元/kg

#### 场景3：单价不一致的警告示例

**请求**:
```json
{
  "materialTypeId": 5,
  "supplierId": 12,
  "receiptDate": "2025-01-15",
  "receiptQuantity": 10,
  "quantityUnit": "箱",
  "totalWeight": 105.0,
  "totalValue": 4725.0,
  "unitPrice": 50.0  // 用户填写50，但计算值为45
}
```

**系统处理**:
- 计算: 4725 ÷ 105 = 45.0元/kg
- 差异: |50 - 45| = 5元，差异率 = 5/45 = 11.1% > 5%
- 记录警告日志 ⚠️
- 在notes中添加: `[系统提示] 单价已按总价值自动计算为45.00元/kg (用户填写50.00元/kg存在11.11%差异)`
- 最终使用45.0作为单价

**响应**:
```json
{
  "data": {
    "unitPrice": 45.0,  // 使用计算值
    "notes": "[系统提示] 单价已按总价值自动计算为45.00元/kg (用户填写50.00元/kg存在11.11%差异)"
  }
}
```

---

### 3. 其他相关接口

#### 3.1 查询即将过期批次
```
GET /api/mobile/{factoryId}/material-batches/expiring?days=3
```
默认查询3天内过期的批次（已修改）

#### 3.2 查询库存汇总
```
GET /api/mobile/{factoryId}/material-batches/inventory/statistics
```

响应包含:
- 总批次数、可用批次数
- 入库总量、剩余总量、已用总量、预留总量
- 库存总价值
- 按原材料类型分组统计

---

## 🔄 与旧版API的区别

| 项目 | 旧版 | 新版 |
|------|------|------|
| 日期字段 | purchaseDate | receiptDate |
| 数量字段 | initialQuantity | receiptQuantity |
| 单位字段 | unit（单一） | quantityUnit + weightPerUnit |
| 重量字段 | - | totalWeight（新增） |
| 价值字段 | - | totalValue（新增） |
| 单价必填 | ✅ | ❌（可选） |
| 供应商必填 | ❌ | ✅ |
| 过期预警默认天数 | 7天 | 3天 |
| ifRunout字段 | - | ✅（新增计算字段） |

---

## ⚠️ 注意事项

1. **单位转换系统**
   - 如果`quantityUnit`是"KG"、"G"、"T"，可以不填`weightPerUnit`
   - 如果是"箱"、"袋"等非重量单位，建议填写`weightPerUnit`便于追踪

2. **单价验证**
   - 系统允许5%的误差范围
   - 超过5%会记录警告但不会阻止入库
   - 最终单价以`totalValue ÷ totalWeight`为准

3. **保质期管理**
   - 优先使用用户填写的`expireDate`
   - 如果未填写且原材料类型配置了`shelfLifeDays`，则自动计算
   - 如果都没有，`expireDate`为null（表示永久有效或不限制）

4. **批次号**
   - 系统自动生成，格式：`MAT-YYYYMMDD-HHMMSS`
   - 保证唯一性，同一秒内多次入库会自动避免冲突

---

## 📊 数据库Migration（待执行）

```sql
-- 注意：此脚本尚未执行，需要DBA审核后执行
ALTER TABLE material_batches
  -- 重命名字段
  CHANGE COLUMN purchase_date receipt_date DATE NOT NULL COMMENT '入库日期',
  CHANGE COLUMN initial_quantity receipt_quantity DECIMAL(10,2) NOT NULL COMMENT '入库数量',

  -- 新增字段
  ADD COLUMN quantity_unit VARCHAR(20) NOT NULL COMMENT '数量单位' AFTER receipt_quantity,
  ADD COLUMN weight_per_unit DECIMAL(10,3) COMMENT '每单位重量(kg)' AFTER quantity_unit,
  ADD COLUMN total_weight DECIMAL(10,3) NOT NULL COMMENT '入库总重量(kg)' AFTER weight_per_unit,
  ADD COLUMN total_value DECIMAL(10,2) NOT NULL COMMENT '入库总价值(元)' AFTER reserved_quantity,

  -- 修改字段属性
  MODIFY COLUMN unit_price DECIMAL(10,2) NULL COMMENT '单价(元/kg，可选)';
```

---

## ✅ 测试清单

- [ ] 创建批次（填写单价）
- [ ] 创建批次（不填单价，自动计算）
- [ ] 创建批次（单价不一致，触发警告）
- [ ] 创建批次（不填到期日期，自动计算）
- [ ] 创建批次（填写到期日期）
- [ ] 创建批次（供应商不填，验证必填检查）
- [ ] 查询即将过期批次（默认3天）
- [ ] 查询库存统计
- [ ] 验证ifRunout计算字段
- [ ] 验证批次号生成

---

## 🐛 已知问题

1. ⚠️ **MaterialBatchServiceImpl存在大量语法错误**
   - 多个方法缺少return语句和右括号
   - 需要完整重构或逐个修复（约80+个错误）

2. ⚠️ **数据库Migration脚本未执行**
   - 当前代码与数据库结构不匹配
   - 需要先执行migration才能运行

3. ⚠️ **单位管理系统未完善**
   - 缺少单位枚举或单位配置表
   - 建议添加`material_units`表管理自定义单位

---

## 📞 技术支持

如有问题，请联系后端开发团队。
