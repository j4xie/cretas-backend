# 宝塔面板数据库结构更新指南

## 📋 概述

本指南将帮助你在宝塔面板中安全地手动更新数据库表结构。

---

## 🎯 准备工作

### 1. 准备SQL迁移脚本

在本地创建或编辑SQL脚本，例如：
```sql
-- 示例：添加新字段
ALTER TABLE users
  ADD COLUMN avatar_url VARCHAR(255) NULL COMMENT '头像URL' AFTER email;

-- 示例：修改字段
ALTER TABLE material_batches
  ADD COLUMN quantity_unit VARCHAR(20) NOT NULL DEFAULT 'KG' COMMENT '数量单位' AFTER receipt_quantity;
```

保存为文件，例如：`update_users_table.sql`

---

## 🚀 操作步骤（宝塔面板）

### 步骤 1: 登录宝塔面板

1. 打开浏览器，访问：`https://139.196.165.140:17400`
2. 输入用户名和密码登录

### 步骤 2: 备份数据库（重要！）

#### 方法1: 通过宝塔面板备份（推荐）

1. 点击左侧菜单 **"数据库"**
2. 找到数据库 `cretas`
3. 点击右侧的 **"备份"** 按钮
4. 等待备份完成
5. 备份文件会保存在：`/www/backup/database/` 目录

#### 方法2: 通过SSH终端备份

1. 点击左侧菜单 **"文件"**
2. 点击右上角 **"终端"** 打开SSH终端
3. 执行备份命令：
```bash
cd /www/wwwroot/project
mysqldump -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas > backup_cretas_$(date +%Y%m%d_%H%M%S).sql
```

---

### 步骤 3: 上传SQL脚本到服务器

#### 方法1: 通过宝塔文件管理器上传

1. 点击左侧菜单 **"文件"**
2. 进入 `/www/wwwroot/project` 目录
3. 点击上方 **"上传"** 按钮
4. 选择你的SQL脚本文件（例如：`update_users_table.sql`）
5. 点击 **"开始上传"**
6. 等待上传完成

#### 方法2: 直接创建SQL文件

1. 点击左侧菜单 **"文件"**
2. 进入 `/www/wwwroot/project` 目录
3. 点击 **"新建文件"**
4. 文件名输入：`update_users_table.sql`
5. 点击 **"创建"**
6. 在编辑器中粘贴SQL代码
7. 点击 **"保存"**

---

### 步骤 4: 执行SQL脚本

#### 方法1: 通过宝塔数据库管理工具（phpMyAdmin）

1. 点击左侧菜单 **"数据库"**
2. 找到数据库 `cretas`
3. 点击右侧的 **"管理"** 按钮（会打开phpMyAdmin）
4. 在左侧选择数据库 `cretas`
5. 点击顶部菜单 **"SQL"**
6. 点击 **"导入文件"** 标签
7. 点击 **"选择文件"**，选择你上传的SQL脚本
8. 点击 **"执行"** 或 **"Go"**
9. 查看执行结果

#### 方法2: 通过SQL命令行（推荐，更安全）

1. 点击左侧菜单 **"文件"**
2. 点击右上角 **"终端"** 打开SSH终端
3. 执行以下命令：

```bash
# 切换到项目目录
cd /www/wwwroot/project

# 执行SQL脚本
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas < update_users_table.sql

# 如果执行成功，不会有任何输出
# 如果有错误，会显示错误信息
```

#### 方法3: 直接在phpMyAdmin中执行SQL

1. 打开phpMyAdmin（通过数据库 → 管理）
2. 选择数据库 `cretas`
3. 点击顶部 **"SQL"** 标签
4. 在SQL编辑框中粘贴你的SQL语句
5. 点击 **"执行"**
6. 查看执行结果

---

### 步骤 5: 验证表结构更新

#### 方法1: 通过phpMyAdmin查看

1. 在phpMyAdmin中选择数据库 `cretas`
2. 点击表名（例如：`users`）
3. 点击 **"结构"** 标签
4. 查看字段列表，确认新字段已添加

#### 方法2: 通过SQL命令查看

在SSH终端中执行：
```bash
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas -e "DESCRIBE users;"
```

或查看特定字段：
```bash
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas -e "SHOW COLUMNS FROM users LIKE 'avatar_url';"
```

---

### 步骤 6: 更新代码并重启应用

#### 6.1 更新本地代码

1. 在本地更新实体类（Entity），添加新字段
2. 重新编译：
```bash
cd /Users/jietaoxie/Downloads/cretas-backend-system-main
mvn clean package -DskipTests
```

#### 6.2 上传新的JAR到服务器

1. 在宝塔面板，点击 **"文件"**
2. 进入 `/www/wwwroot/project` 目录
3. 点击 **"上传"**
4. 选择编译好的 `cretas-backend-system-1.0.0.jar`
5. 上传并覆盖旧文件

#### 6.3 重启应用

**方法1: 通过宝塔计划任务（推荐）**

1. 点击左侧菜单 **"计划任务"**
2. 点击 **"添加计划任务"**
3. 任务类型选择：**"Shell脚本"**
4. 任务名称：`重启后端服务`
5. 执行周期：**"手动"**
6. 脚本内容：
```bash
cd /www/wwwroot/project
bash restart.sh
```
7. 点击 **"添加任务"**
8. 在任务列表中点击 **"执行"** 按钮

**方法2: 通过SSH终端**

在SSH终端中执行：
```bash
cd /www/wwwroot/project
bash restart.sh
```

**方法3: 使用宝塔终端**

1. 点击 **"文件"** → **"终端"**
2. 执行：
```bash
cd /www/wwwroot/project
bash restart.sh
```

#### 6.4 查看启动日志

```bash
cd /www/wwwroot/project
tail -f logs/cretas-backend.log
```

按 `Ctrl+C` 退出日志查看

---

## 🔍 实际操作示例

### 示例：添加用户头像字段

#### 1. 创建SQL脚本

在宝塔文件管理器中创建 `/www/wwwroot/project/add_user_avatar.sql`：

```sql
-- 添加用户头像字段
ALTER TABLE users
  ADD COLUMN avatar_url VARCHAR(255) NULL COMMENT '头像URL' AFTER email;

-- 为现有数据设置默认值（可选）
UPDATE users
SET avatar_url = 'https://default-avatar.com/default.png'
WHERE avatar_url IS NULL;
```

#### 2. 备份数据库

- 数据库 → 找到 `cretas` → 点击 **"备份"**

#### 3. 执行SQL

在SSH终端执行：
```bash
cd /www/wwwroot/project
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas < add_user_avatar.sql
```

#### 4. 验证

```bash
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas -e "DESCRIBE users;"
```

应该能看到 `avatar_url` 字段。

#### 5. 更新代码并重启

- 更新 `User.java` 实体类
- 重新编译并上传JAR
- 重启应用

---

## ⚠️ 注意事项

### 1. 备份的重要性

**每次修改数据库前，必须先备份！**

- 宝塔面板备份最简单：数据库 → 备份
- 备份文件保存在：`/www/backup/database/`
- 备份文件名格式：`cretas_YYYYMMDD_HHMMSS.sql`

### 2. 测试环境验证

**建议先在测试环境或本地测试SQL脚本**

- 可以先在本地MySQL执行一遍
- 确认SQL语法正确
- 确认不会影响现有数据

### 3. 字段添加顺序

MySQL的 `AFTER column_name` 语法：
```sql
-- ✅ 正确：指定字段位置
ALTER TABLE users ADD COLUMN new_field VARCHAR(50) AFTER email;

-- ✅ 也可以：添加到末尾（不指定位置）
ALTER TABLE users ADD COLUMN new_field VARCHAR(50);

-- ❌ 错误：不能使用 BEFORE
-- ALTER TABLE users ADD COLUMN new_field VARCHAR(50) BEFORE email;
```

### 4. 默认值设置

**添加NOT NULL字段时，必须设置默认值：**
```sql
-- ✅ 正确：设置默认值
ALTER TABLE users
  ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'active';

-- ❌ 错误：会导致现有数据无法插入
-- ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL;
```

### 5. 字段删除要谨慎

**删除字段前，确认不再需要：**
```sql
-- 建议先备份数据
CREATE TABLE users_backup AS SELECT id, old_field FROM users;

-- 再删除字段
ALTER TABLE users DROP COLUMN old_field;
```

---

## 🆘 常见问题

### Q1: 执行SQL时提示"Access denied"

**原因**: 数据库用户权限不足

**解决**: 
- 检查数据库用户名密码是否正确
- 确认用户有 ALTER TABLE 权限
- 如果使用root用户，需要修改命令：
```bash
mysql -h localhost -u root -p你的root密码 cretas < update.sql
```

### Q2: 提示"Table doesn't exist"

**原因**: 表不存在

**解决**:
1. 检查表是否存在：
```bash
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas -e "SHOW TABLES;"
```

2. 如果表不存在，可能需要先创建（临时使用 `ddl-auto: create`）

### Q3: 执行SQL后，应用启动失败

**原因**: 表结构与实体类不匹配

**解决**:
1. 检查实体类是否正确更新
2. 检查数据库表结构：
```bash
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas -e "DESCRIBE table_name;"
```
3. 对比差异，补充迁移脚本或更新实体类

### Q4: 如何查看执行历史

**方法1: 查看MySQL日志**
```bash
# 查看MySQL错误日志（如果开启了）
tail -f /www/server/mysql/data/你的服务器名.err
```

**方法2: 检查phpMyAdmin执行历史**
- phpMyAdmin会显示最近执行的SQL语句

### Q5: 如何回滚操作

**如果SQL执行出错，可以恢复备份：**

1. 在宝塔面板，点击 **"数据库"**
2. 找到 `cretas` 数据库
3. 点击 **"导入"**
4. 选择之前的备份文件
5. 点击 **"导入"**

或使用命令行：
```bash
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas < /www/backup/database/cretas_20250115_120000.sql
```

---

## 📚 参考资源

### 宝塔面板相关

- 数据库管理：左侧菜单 → **"数据库"**
- 文件管理：左侧菜单 → **"文件"**
- SSH终端：文件管理 → 右上角 **"终端"**
- 计划任务：左侧菜单 → **"计划任务"**

### 项目相关

- 项目目录：`/www/wwwroot/project`
- 日志目录：`/www/wwwroot/project/logs/`
- 备份目录：`/www/backup/database/`
- 数据库配置：`src/main/resources/application.yml`

---

## ✅ 操作检查清单

执行前：
- [ ] SQL脚本已准备好
- [ ] 已在测试环境验证SQL脚本
- [ ] 数据库已备份
- [ ] 已通知团队成员（如果是生产环境）

执行中：
- [ ] SQL脚本已上传到服务器
- [ ] 已执行SQL脚本
- [ ] 已验证表结构更新成功
- [ ] 代码已更新并编译
- [ ] 新JAR已上传到服务器

执行后：
- [ ] 应用已重启
- [ ] 应用启动成功（检查日志）
- [ ] 功能测试通过
- [ ] 现有数据完整（检查数据量）

---

**最后更新**: 2025-02-02

