# 数据库结构更新指南

## 📋 概述

当需要更新数据库表结构时（添加字段、修改字段、创建新表等），使用以下安全流程，**避免覆盖现有数据**。

---

## 🎯 推荐方案：手动执行SQL迁移脚本（最安全）

### 流程步骤

#### 步骤 1: 编写迁移脚本

在 `src/main/resources/db/migration/` 目录下创建新的迁移脚本：

**命名规范**: `V{YYYYMMDDHHMMSS}__{描述}.sql`

**示例**:
```
V202501151430__update_material_batch_fields.sql
V202501161200__add_user_avatar_column.sql
```

#### 步骤 2: 备份数据库（重要！）

```bash
# 在服务器上执行
cd /www/wwwroot/project

# 备份整个数据库
mysqldump -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas > backup_$(date +%Y%m%d_%H%M%S).sql

# 或者只备份特定表
mysqldump -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas material_batches > backup_material_batches_$(date +%Y%m%d_%H%M%S).sql
```

#### 步骤 3: 测试迁移脚本（在测试环境或本地）

```bash
# 在测试环境执行
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas < src/main/resources/db/migration/V202501151430__update_material_batch_fields.sql

# 验证结果
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas -e "DESCRIBE material_batches;"
```

#### 步骤 4: 在生产环境执行迁移

```bash
# SSH登录服务器
ssh root@your-server-ip

# 切换到项目目录
cd /www/wwwroot/project

# 上传迁移脚本到服务器（如果还没有）
# 可以通过宝塔面板或 scp 上传

# 执行迁移脚本
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas < V202501151430__update_material_batch_fields.sql

# 验证迁移结果
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas -e "DESCRIBE material_batches;"
```

#### 步骤 5: 更新代码并重启应用

```bash
# 1. 在本地更新实体类（Entity）
# 2. 重新编译
cd /Users/jietaoxie/Downloads/cretas-backend-system-main
mvn clean package -DskipTests

# 3. 上传新的 JAR 到服务器
# 4. 重启应用（配置保持 ddl-auto: validate）
cd /www/wwwroot/project
bash restart.sh

# 5. 验证应用启动成功
tail -f logs/cretas-backend.log
```

---

## ⚠️ 方案2：临时使用 update（需要谨慎）

**⚠️ 警告**: 这种方式有风险，只建议在以下情况使用：
- 测试环境
- 确定不会丢失数据的情况下
- 无法手动执行SQL的情况

### 流程步骤

#### 步骤 1: 备份数据库

```bash
mysqldump -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas > backup_before_update_$(date +%Y%m%d_%H%M%S).sql
```

#### 步骤 2: 临时修改配置

修改 `application.yml`:
```yaml
jpa:
  hibernate:
    ddl-auto: update  # 临时改为 update
```

#### 步骤 3: 编译并部署

```bash
# 编译
mvn clean package -DskipTests

# 上传到服务器并重启
cd /www/wwwroot/project
bash restart.sh

# 等待启动完成（约20秒）
tail -f logs/cretas-backend.log
```

#### 步骤 4: 立即改回 validate

修改 `application.yml`:
```yaml
jpa:
  hibernate:
    ddl-auto: validate  # 改回 validate
```

重新编译并部署：
```bash
mvn clean package -DskipTests
# 上传新的 JAR 并重启
```

#### 步骤 5: 验证数据完整性

```bash
# 检查关键表的数据量
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas -e "SELECT COUNT(*) FROM users;"
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas -e "SELECT COUNT(*) FROM material_batches;"
```

---

## 📝 迁移脚本编写规范

### 基本结构

```sql
-- =====================================================
-- 迁移脚本标题
-- 版本: V{YYYYMMDDHHMMSS}
-- 说明: 详细说明本次迁移的目的和内容
-- 作者: 你的名字
-- 日期: YYYY-MM-DD
-- =====================================================

-- 1. 备份相关表（可选，如果是重要修改）
-- CREATE TABLE material_batches_backup AS SELECT * FROM material_batches;

-- 2. 执行表结构修改
ALTER TABLE table_name
  ADD COLUMN new_column VARCHAR(50) NULL COMMENT '新字段说明';

-- 3. 数据迁移（如果需要）
UPDATE table_name
SET new_column = 'default_value'
WHERE new_column IS NULL;

-- 4. 修改字段属性（如需要）
ALTER TABLE table_name
  MODIFY COLUMN new_column VARCHAR(50) NOT NULL DEFAULT 'default_value';

-- 5. 添加索引（如需要）
CREATE INDEX idx_table_column ON table_name(column_name);

-- 6. 添加外键约束（如需要）
ALTER TABLE table_name
  ADD CONSTRAINT fk_name FOREIGN KEY (column_name) REFERENCES other_table(id);

-- =====================================================
-- Migration完成
-- =====================================================
-- 注意事项:
-- 1. 执行前请备份数据库
-- 2. 建议在测试环境先执行验证
-- 3. 执行后需要重启应用服务
-- 4. 如有回滚需求，请使用对应的回滚脚本
-- =====================================================
```

### 常见操作示例

#### 添加新字段

```sql
-- 添加新字段（允许NULL，后面再填充数据）
ALTER TABLE users
  ADD COLUMN avatar_url VARCHAR(255) NULL COMMENT '头像URL' AFTER email;

-- 为现有数据设置默认值
UPDATE users
SET avatar_url = 'https://default-avatar.com/default.png'
WHERE avatar_url IS NULL;

-- 修改为必填（数据填充后）
ALTER TABLE users
  MODIFY COLUMN avatar_url VARCHAR(255) NOT NULL DEFAULT 'https://default-avatar.com/default.png';
```

#### 重命名字段

```sql
-- MySQL重命名字段
ALTER TABLE material_batches
  CHANGE COLUMN old_name new_name VARCHAR(50) NOT NULL COMMENT '新字段说明';
```

#### 删除字段（谨慎！）

```sql
-- 先备份数据（如果需要）
CREATE TABLE material_batches_backup AS SELECT id, old_column FROM material_batches;

-- 删除字段
ALTER TABLE material_batches
  DROP COLUMN old_column;
```

#### 修改字段类型

```sql
-- 先检查是否有数据冲突
SELECT COUNT(*) FROM table_name WHERE column_name NOT REGEXP '^[0-9]+$';

-- 修改字段类型
ALTER TABLE table_name
  MODIFY COLUMN column_name INT NOT NULL;
```

---

## 🔄 回滚方案

如果迁移出现问题，需要回滚：

### 方法1: 使用备份恢复

```bash
# 恢复整个数据库
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas < backup_20250115_120000.sql

# 或恢复单个表
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas < backup_material_batches_20250115_120000.sql
```

### 方法2: 编写回滚脚本

创建回滚脚本 `R202501151430__rollback_material_batch_fields.sql`:

```sql
-- 回滚操作
ALTER TABLE material_batches
  CHANGE COLUMN receipt_date purchase_date DATE NOT NULL COMMENT '采购日期',
  CHANGE COLUMN receipt_quantity initial_quantity DECIMAL(10,2) NOT NULL COMMENT '初始数量',
  DROP COLUMN quantity_unit,
  DROP COLUMN weight_per_unit,
  DROP COLUMN total_weight,
  DROP COLUMN total_value,
  MODIFY COLUMN unit_price DECIMAL(10,2) NOT NULL COMMENT '单价';
```

---

## ✅ 迁移检查清单

执行迁移前，请确认：

- [ ] 数据库已备份
- [ ] 迁移脚本已在测试环境验证
- [ ] 迁移脚本包含详细的注释说明
- [ ] 检查了现有数据是否会影响迁移
- [ ] 准备了回滚方案
- [ ] 通知了团队成员（如果是生产环境）
- [ ] 选择了合适的维护窗口（如果是生产环境）

执行迁移后，请确认：

- [ ] 表结构已正确更新
- [ ] 现有数据完整
- [ ] 应用可以正常启动（ddl-auto: validate）
- [ ] 相关功能测试通过
- [ ] 记录迁移日志

---

## 📚 参考示例

项目中已有的迁移脚本示例：
- `src/main/resources/db/migration/V202501151430__update_material_batch_fields.sql`

---

## 🆘 常见问题

### Q1: 执行ALTER TABLE时提示"表不存在"

**原因**: 表还没有创建

**解决**: 
1. 检查表是否真的存在: `SHOW TABLES LIKE 'table_name';`
2. 如果不存在，可能需要先创建表（使用 `ddl-auto: create` 临时创建）

### Q2: 字段已存在，执行ADD COLUMN失败

**解决**: 使用 `IF NOT EXISTS` 或先检查：
```sql
-- MySQL 8.0+ 支持
ALTER TABLE table_name
  ADD COLUMN IF NOT EXISTS column_name VARCHAR(50);

-- 或先检查
SELECT COUNT(*) FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = 'cretas' 
  AND TABLE_NAME = 'table_name' 
  AND COLUMN_NAME = 'column_name';
```

### Q3: 迁移后应用启动失败，提示表结构不匹配

**原因**: 实体类与数据库表结构不一致

**解决**:
1. 检查实体类是否正确更新
2. 检查数据库表结构: `DESCRIBE table_name;`
3. 对比差异，补充迁移脚本或更新实体类

---

**最后更新**: 2025-02-02

