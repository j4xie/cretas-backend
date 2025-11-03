# 数据库初始化步骤

## 🎯 当前状态

- ❌ 数据库表结构不存在
- ✅ SQL 初始化文件已上传
- ✅ 新 JAR 已编译（ddl-auto: create）

---

## 📋 操作步骤

### 步骤 1: 上传新 JAR 并启动（创建表结构）

**在本地**:

新的 JAR 文件位置:
```
/Users/jietaoxie/Downloads/cretas-backend-system-main/target/cretas-backend-system-1.0.0.jar
```

**请通过宝塔面板上传到服务器**:
- 登录: https://139.196.165.140:17400
- 文件管理 → /www/wwwroot/project
- 上传 `cretas-backend-system-1.0.0.jar`（覆盖旧的）

**在服务器上**:

```bash
cd /www/wwwroot/project
bash restart.sh
```

**重要**: 这次启动会自动创建所有表结构（ddl-auto: create）

**等待约 20 秒，然后查看日志**:

```bash
tail -50 cretas-backend.log
```

查找这些日志确认表已创建:
```
Hibernate: create table factories (...)
Hibernate: create table users (...)
Hibernate: create table platform_admins (...)
Hibernate: create table sessions (...)
Hibernate: create table whitelist (...)
...
Started CretasApplication
```

---

### 步骤 2: 停止应用

```bash
cd /www/wwwroot/project
ps aux | grep cretas-backend-system | grep -v grep | awk '{print $2}' | xargs -r kill -9
```

---

### 步骤 3: 执行数据初始化 SQL

```bash
cd /www/wwwroot/project
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas < init-final-users.sql
```

**预期输出**: 无错误信息

---

### 步骤 4: 验证数据

```bash
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas -e "SELECT username, real_name FROM platform_admins;"
```

**预期输出**:
```
+-----------------+------------------+
| username        | real_name        |
+-----------------+------------------+
| admin           | 超级管理员       |
| developer       | 开发者           |
| platform_admin  | 平台管理员       |
+-----------------+------------------+
```

---

### 步骤 5: 上传最终版本 JAR（改回 validate）

**我会在本地重新编译 JAR（ddl-auto: validate）**

等我编译完成后，您需要：
1. 再次上传新的 JAR（这次是 validate 模式）
2. 重启应用

---

## ⚠️ 为什么需要两次部署？

1. **第一次（create 模式）**:
   - 让 Hibernate 创建所有表结构
   - 启动后立即停止

2. **第二次（validate 模式）**:
   - 只验证表结构，不会修改
   - 这是生产环境的安全模式

---

## 📝 测试账号

所有密码: `123456`

### 平台管理员
- admin
- developer
- platform_admin

### 工厂用户（工厂ID: F001）
- perm_admin
- proc_admin
- farm_admin
- logi_admin
- proc_user

---

**现在请执行步骤 1：上传 JAR 并启动！**
