# 数据库初始化指南

## 📋 概述

本指南将帮助您在新服务器 `139.196.165.140` 上初始化 Cretas 系统数据库。

---

## ⚠️ 重要说明

**数据库表结构由 JPA 自动创建**

由于应用配置了 `ddl-auto: validate`，表结构需要预先存在。有两种方式初始化表结构：

### 方式 1: 临时改为 create（推荐）✅
1. 第一次启动时临时改为 `ddl-auto: create`
2. 让 JPA 自动创建所有表
3. 然后改回 `ddl-auto: validate`

### 方式 2: 手动执行 DDL
1. 从其他环境导出表结构
2. 在新服务器上手动执行

**本指南采用方式 1**

---

## 🎯 初始化步骤

### 步骤 1: 确认数据库存在

在服务器上执行：

```bash
# 登录 MySQL
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA

# 检查数据库
SHOW DATABASES LIKE 'cretas';

# 如果不存在，创建数据库
CREATE DATABASE IF NOT EXISTS cretas CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 退出
EXIT;
```

---

### 步骤 2: 临时修改配置让 JPA 创建表

**在服务器上**:

```bash
cd /www/wwwroot/project

# 备份当前 JAR
cp cretas-backend-system-1.0.0.jar cretas-backend-system-1.0.0.jar.backup

# 停止应用
ps aux | grep cretas-backend-system | grep -v grep | awk '{print $2}' | xargs -r kill -9
```

**在本地**:

修改 `application.yml`:

```yaml
jpa:
  hibernate:
    ddl-auto: create  # 临时改为 create，让 JPA 创建表
```

重新编译：

```bash
cd /Users/jietaoxie/Downloads/cretas-backend-system-main
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.0.1.jdk/Contents/Home
mvn clean package -DskipTests
```

---

### 步骤 3: 上传并启动（创建表结构）

1. 上传新编译的 JAR 到服务器（通过宝塔面板）
2. 启动应用：

```bash
cd /www/wwwroot/project
bash restart.sh
```

3. 等待启动完成（约 20 秒）
4. 检查日志，确认表已创建：

```bash
tail -f cretas-backend.log
# 查找 "Hibernate: create table" 等日志
```

---

### 步骤 4: 停止应用，改回 validate

**在本地**:

改回 `application.yml`:

```yaml
jpa:
  hibernate:
    ddl-auto: validate  # 改回 validate
```

重新编译：

```bash
mvn clean package -DskipTests
```

---

### 步骤 5: 初始化测试数据

**上传初始化 SQL 文件到服务器**:

通过宝塔面板上传以下文件到 `/www/wwwroot/project`:
- `fix-document/init-final-users.sql`

**在服务器上执行 SQL**:

```bash
cd /www/wwwroot/project

# 执行初始化脚本
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas < init-final-users.sql
```

---

### 步骤 6: 上传最终版本并重启

1. 上传步骤 4 编译的 JAR（ddl-auto: validate）
2. 重启应用：

```bash
cd /www/wwwroot/project
bash restart.sh
```

---

## 🔑 初始化的测试账号

### 平台管理员账号

所有密码均为: `123456`

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | 123456 | super_admin | 超级管理员 |
| developer | 123456 | developer | 开发者账号 |
| platform_admin | 123456 | platform_admin | 平台管理员 |

### 工厂用户账号

工厂ID: `F001` (测试工厂)
所有密码均为: `123456`

| 用户名 | 密码 | 角色 | 部门 | 说明 |
|--------|------|------|------|------|
| perm_admin | 123456 | permission_admin | management | 权限管理员 |
| proc_admin | 123456 | department_admin | processing | 加工部主管 |
| farm_admin | 123456 | department_admin | farming | 养殖部主管 |
| logi_admin | 123456 | department_admin | logistics | 物流部主管 |
| proc_user | 123456 | operator | processing | 加工操作员 |

### 白名单手机号（用于注册测试）

| 手机号 | 姓名 | 部门 | 工厂 |
|--------|------|------|------|
| 13800138000 | 张三 | 技术部 | F001 |
| 13800138001 | 李四 | 产品部 | F001 |
| 13800138002 | 王五 | 运营部 | F001 |

---

## ✅ 验证数据初始化

在服务器上执行：

```bash
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas << 'EOF'

-- 检查平台管理员
SELECT '=== 平台管理员 ===' AS '';
SELECT username, real_name, platform_role, status FROM platform_admins;

-- 检查工厂
SELECT '=== 工厂 ===' AS '';
SELECT id, name, contact_name, is_active FROM factories;

-- 检查工厂用户
SELECT '=== 工厂用户 ===' AS '';
SELECT username, full_name, role_code, department FROM users WHERE factory_id='F001';

-- 检查白名单
SELECT '=== 白名单 ===' AS '';
SELECT phone_number, name, department, status FROM whitelist WHERE factory_id='F001';

EOF
```

**预期输出**:

```
=== 平台管理员 ===
admin          | 超级管理员 | super_admin    | active
developer      | 开发者     | developer      | active
platform_admin | 平台管理员 | platform_admin | active

=== 工厂 ===
F001 | 测试工厂 | 张经理 | 1

=== 工厂用户 ===
perm_admin | 权限管理员 | permission_admin  | management
proc_admin | 加工管理员 | department_admin  | processing
farm_admin | 养殖管理员 | department_admin  | farming
logi_admin | 物流管理员 | department_admin  | logistics
proc_user  | 加工操作员 | operator          | processing

=== 白名单 ===
13800138000 | 张三 | 技术部 | ACTIVE
13800138001 | 李四 | 产品部 | ACTIVE
13800138002 | 王五 | 运营部 | ACTIVE
```

---

## 🧪 测试登录

### 测试平台管理员登录

```bash
curl -X POST http://139.196.165.140:10010/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

### 测试工厂用户登录

```bash
curl -X POST http://139.196.165.140:10010/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "perm_admin",
    "password": "123456",
    "factoryId": "F001"
  }'
```

**预期响应**:

```json
{
  "success": true,
  "data": {
    "token": "eyJhbGc...",
    "user": {
      "username": "admin",
      "fullName": "超级管理员",
      ...
    }
  }
}
```

---

## 🔄 快速初始化脚本

为了简化操作，我创建了一个一键初始化脚本：

**create-database-init-script.sh** (在本地运行):

```bash
#!/bin/bash

echo "============================================================"
echo "数据库初始化准备"
echo "============================================================"
echo ""

# 1. 检查 SQL 文件
if [ ! -f "fix-document/init-final-users.sql" ]; then
    echo "❌ SQL 文件不存在"
    exit 1
fi

echo "✅ SQL 文件已找到"
echo ""

# 2. 通过宝塔 API 上传 SQL 文件
echo "📤 上传 SQL 初始化脚本..."

BT_PANEL="https://139.196.165.140:17400"
API_KEY="Fw3rqkRqAashK9uNDsFxvst31YSbBmUb"
TARGET_PATH="/www/wwwroot/project"

# 生成签名
TIME_TOKEN=$(python3 << 'PYTHON_EOF'
import hashlib
import time

api_sk = "Fw3rqkRqAashK9uNDsFxvst31YSbBmUb"
request_time = str(int(time.time()))
md5_api_sk = hashlib.md5(api_sk.encode()).hexdigest()
request_token = hashlib.md5((request_time + md5_api_sk).encode()).hexdigest()
print(f"{request_time}|{request_token}")
PYTHON_EOF
)

REQUEST_TIME=$(echo $TIME_TOKEN | cut -d'|' -f1)
REQUEST_TOKEN=$(echo $TIME_TOKEN | cut -d'|' -f2)

FILE_SIZE=$(stat -f%z "fix-document/init-final-users.sql" 2>/dev/null || stat -c%s "fix-document/init-final-users.sql" 2>/dev/null)

# 上传文件
UPLOAD_RESPONSE=$(curl -k -s -X POST "${BT_PANEL}/files?action=upload" \
  -F "request_time=$REQUEST_TIME" \
  -F "request_token=$REQUEST_TOKEN" \
  -F "f_path=$TARGET_PATH" \
  -F "f_name=init-final-users.sql" \
  -F "f_size=$FILE_SIZE" \
  -F "f_start=0" \
  -F "blob=@fix-document/init-final-users.sql")

if echo "$UPLOAD_RESPONSE" | grep -q '"status":\s*true'; then
    echo "✅ SQL 文件上传成功"
else
    echo "❌ SQL 文件上传失败"
    exit 1
fi

echo ""
echo "============================================================"
echo "下一步操作"
echo "============================================================"
echo ""
echo "请在服务器上执行:"
echo ""
echo "  cd /www/wwwroot/project"
echo "  mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas < init-final-users.sql"
echo ""
echo "然后重启应用:"
echo "  bash restart.sh"
echo ""
```

---

## ❗常见问题

### Q1: 表已存在怎么办？

如果表已经存在但数据不对，可以：

```bash
# 删除所有表（谨慎！）
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas << 'EOF'
SET FOREIGN_KEY_CHECKS = 0;
DROP DATABASE cretas;
CREATE DATABASE cretas CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 1;
EOF
```

然后重新执行初始化步骤。

### Q2: BCrypt 密码哈希是什么？

`$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW` 是密码 `123456` 的 BCrypt 哈希值。

### Q3: 如何创建新用户？

使用相同的密码哈希：

```sql
INSERT INTO users (factory_id, username, password_hash, email, phone, full_name, is_active, role_code, department, position, created_at, updated_at)
VALUES ('F001', 'newuser', '$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW', 'newuser@test.com', '13900000099', '新用户', TRUE, 'operator', '生产部', '操作员', NOW(), NOW());
```

密码是: `123456`

---

## 📝 总结

**完整流程**:

1. ✅ 确认数据库 `cretas` 存在
2. ✅ 临时改为 `ddl-auto: create`，启动应用创建表
3. ✅ 改回 `ddl-auto: validate`
4. ✅ 执行 `init-final-users.sql` 初始化数据
5. ✅ 重启应用
6. ✅ 测试登录验证

**时间估计**: 约 15-20 分钟

---

**准备好了吗？让我们开始吧！** 🚀
