# 快速开始 - 数据库初始化

## ✅ 已完成

1. ✅ SQL 初始化文件已上传到服务器 `/www/wwwroot/project/init-final-users.sql`

---

## 🎯 现在请在服务器上执行

### 步骤 1: 执行 SQL 初始化

```bash
cd /www/wwwroot/project
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas < init-final-users.sql
```

**这个脚本会**:
- 清理旧数据
- 创建测试工厂 (F001)
- 创建 3 个平台管理员账号
- 创建 5 个工厂用户账号
- 创建 3 个白名单手机号

---

### 步骤 2: 验证数据

```bash
# 检查平台管理员
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas -e "SELECT username, real_name, platform_role FROM platform_admins;"

# 检查工厂用户
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas -e "SELECT username, full_name, role_code FROM users WHERE factory_id='F001';"

# 检查白名单
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas -e "SELECT phone_number, name, status FROM whitelist WHERE factory_id='F001';"
```

**预期输出**:

```
# 平台管理员
username        | real_name     | platform_role
admin           | 超级管理员    | super_admin
developer       | 开发者        | developer
platform_admin  | 平台管理员    | platform_admin

# 工厂用户
username    | full_name    | role_code
perm_admin  | 权限管理员   | permission_admin
proc_admin  | 加工管理员   | department_admin
farm_admin  | 养殖管理员   | department_admin
logi_admin  | 物流管理员   | department_admin
proc_user   | 加工操作员   | operator

# 白名单
phone_number  | name | status
13800138000   | 张三 | ACTIVE
13800138001   | 李四 | ACTIVE
13800138002   | 王五 | ACTIVE
```

---

### 步骤 3: 测试登录

**测试平台管理员登录**:

```bash
curl -X POST http://139.196.165.140:10010/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

**测试工厂用户登录**:

```bash
curl -X POST http://139.196.165.140:10010/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"perm_admin","password":"123456","factoryId":"F001"}'
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

## 🔑 测试账号清单

### 平台管理员（所有密码: 123456）

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | 123456 | super_admin | 超级管理员 |
| developer | 123456 | developer | 开发者 |
| platform_admin | 123456 | platform_admin | 平台管理员 |

### 工厂用户（工厂ID: F001，所有密码: 123456）

| 用户名 | 密码 | 角色 | 部门 |
|--------|------|------|------|
| perm_admin | 123456 | permission_admin | management |
| proc_admin | 123456 | department_admin | processing |
| farm_admin | 123456 | department_admin | farming |
| logi_admin | 123456 | department_admin | logistics |
| proc_user | 123456 | operator | processing |

### 白名单手机号（用于注册）

| 手机号 | 姓名 | 部门 |
|--------|------|------|
| 13800138000 | 张三 | 技术部 |
| 13800138001 | 李四 | 产品部 |
| 13800138002 | 王五 | 运营部 |

---

## ❓ 如果遇到问题

### 问题 1: 外键约束错误

**错误信息**: `Cannot add or update a child row: a foreign key constraint fails`

**解决方案**:

```bash
# 临时禁用外键检查
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas << 'EOF'
SET FOREIGN_KEY_CHECKS = 0;
SOURCE /www/wwwroot/project/init-final-users.sql;
SET FOREIGN_KEY_CHECKS = 1;
EOF
```

---

### 问题 2: 表不存在

**错误信息**: `Table 'cretas.platform_admins' doesn't exist`

**原因**: JPA 还没有创建表结构

**解决方案**: 参考 `DATABASE_INIT_GUIDE.md` 的完整步骤，先让 JPA 创建表

---

### 问题 3: 密码登录失败

**检查密码哈希**:

```bash
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas -e "SELECT username, password_hash FROM platform_admins WHERE username='admin';"
```

**正确的密码哈希**: `$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW` (密码: 123456)

---

## 📝 总结

**已完成**:
- ✅ SQL 文件已上传到服务器

**需要您做**:
1. 在服务器上执行 SQL: `mysql ... < init-final-users.sql`
2. 验证数据是否正确
3. 测试登录接口

**时间**: 约 5 分钟

---

**准备好了吗？现在去服务器上执行第 1 步吧！** 🚀
