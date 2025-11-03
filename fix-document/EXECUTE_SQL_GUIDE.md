# 数据库初始化执行指南

## 📋 概述

本指南帮助你在服务器上执行 `init-final-users.sql` 来初始化测试账号。

---

## 🎯 这个SQL文件会做什么？

执行 `init-final-users.sql` 会：

1. **清理旧数据**
   - 删除工厂ID为 `F001` 的所有测试数据
   - 删除测试账号的会话
   - 删除测试白名单

2. **创建测试工厂**
   - 工厂ID: `F001`
   - 工厂名称: 测试工厂
   - 状态: 已激活

3. **创建5个工厂用户**（密码都是 `123456`）
   - `perm_admin` - 权限管理员
   - `proc_admin` - 加工管理员 ⭐ **推荐测试用**
   - `farm_admin` - 养殖管理员
   - `logi_admin` - 物流管理员
   - `proc_user` - 加工操作员

4. **创建3个平台管理员**（密码都是 `123456`）
   - `admin` - 超级管理员
   - `developer` - 开发者
   - `platform_admin` - 平台管理员

5. **创建测试白名单**
   - 用于测试注册功能

---

## 🚀 执行方法

### 方法1: SSH命令行执行（推荐）

```bash
# 1. SSH登录到服务器
ssh root@106.14.165.234

# 2. 上传SQL文件（如果还没上传）
# 在本地执行：
scp ~/Downloads/cretas-backend-system-main/fix-document/init-final-users.sql root@106.14.165.234:/root/

# 3. 在服务器上执行SQL
mysql -u root -p cretas < /root/init-final-users.sql

# 或者交互式执行：
mysql -u root -p cretas
source /root/init-final-users.sql;
exit;
```

### 方法2: 使用MySQL客户端

如果你使用MySQL Workbench或其他GUI工具：

1. 连接到数据库 `cretas`
2. 打开 `init-final-users.sql` 文件
3. 执行整个脚本

---

## ✅ 验证执行结果

执行SQL后，验证数据是否正确创建：

```sql
-- 1. 检查工厂是否创建
SELECT id, name, is_active FROM factories WHERE id = 'F001';
-- 应该返回: F001 | 测试工厂 | 1

-- 2. 检查工厂用户是否创建
SELECT username, role_code, department, is_active 
FROM users 
WHERE factory_id = 'F001';
-- 应该返回5个用户

-- 3. 检查平台管理员是否创建
SELECT username, platform_role, status 
FROM platform_admins 
WHERE username IN ('admin', 'developer', 'platform_admin');
-- 应该返回3个管理员

-- 4. 检查白名单是否创建
SELECT phone_number, name, status 
FROM whitelist 
WHERE factory_id = 'F001';
-- 应该返回2个白名单条目
```

---

## 🧪 执行后测试

### 1. 测试登录API

```bash
# 测试加工管理员登录
curl -X POST "http://106.14.165.234:10010/api/mobile/auth/unified-login" \
  -H "Content-Type: application/json" \
  -d '{"username":"proc_admin","password":"123456","factoryId":"F001"}'

# 应该返回: {"code":200,"success":true,"data":{...,"accessToken":"..."}}
```

### 2. 测试Dashboard API

执行测试脚本：
```bash
bash test_server_106.sh
```

### 3. 测试React Native应用

1. 重启应用：
   ```bash
   cd frontend/CretasFoodTrace
   npx expo start --clear
   ```

2. 使用账号登录：
   - 用户名: `proc_admin`
   - 密码: `123456`

3. 查看首页Dashboard数据

---

## ⚠️ 注意事项

1. **数据会被清理**
   - 执行此SQL会**删除**工厂ID为 `F001` 的所有数据
   - 如果你已经有重要的 `F001` 数据，请先备份

2. **密码Hash**
   - 所有账号的密码都是 `123456`
   - BCrypt Hash: `$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW`

3. **外键约束**
   - SQL脚本会临时禁用外键检查
   - 执行完成后会自动恢复

4. **工厂ID**
   - 必须使用 `F001` 而不是 `FISH_2025_001`
   - 前端配置已经修改为使用 `F001`

---

## 🔧 常见问题

### Q1: 执行SQL时报错 "Access denied"

**解决方案**:
```bash
# 确认MySQL root密码
mysql -u root -p
# 如果忘记密码，需要重置MySQL root密码
```

### Q2: 执行后登录仍然失败

**检查清单**:
1. 确认SQL执行成功（无错误输出）
2. 验证用户是否创建成功（使用上面的验证SQL）
3. 检查工厂ID是否正确（必须是 `F001`）
4. 查看后端日志：`tail -f /www/wwwroot/cretas/cretas-backend.log`

### Q3: 想使用不同的工厂ID

**解决方案**:
修改SQL文件中的所有 `F001` 为你想要的工厂ID：
```bash
# 在本地修改SQL文件
sed -i '' 's/F001/YOUR_FACTORY_ID/g' init-final-users.sql
```

---

## 📝 执行记录

**执行时间**: _____________
**执行人**: _____________
**执行状态**: [ ] 成功  [ ] 失败
**备注**: _____________

---

## 🎉 完成后

执行成功后，你应该能够：

✅ 使用 `proc_admin/123456` 登录React Native应用
✅ 看到Dashboard显示数据（不再是全0）
✅ 没有403错误
✅ 所有Dashboard API正常工作

---

**最后更新**: 2025-11-02
**相关文件**: 
- `init-final-users.sql` - SQL脚本
- `TEST_ACCOUNTS.md` - 测试账号文档
- `test_server_106.sh` - API测试脚本
