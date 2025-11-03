# 🚀 快速开始指南

## 📁 当前目录文件

你现在在 `~/Downloads/cretas-backend-system-main/fix-document/` 目录中，包含以下重要文件：

- ✅ **init-final-users.sql** - 数据库初始化脚本（217行，完整）
- 📖 **EXECUTE_SQL_GUIDE.md** - 详细执行指南
- 📖 **README_QUICK_START.md** - 本文档

---

## ⚡ 30秒快速执行

### 1. 上传SQL文件到服务器

```bash
# 方式1: 使用scp
scp ~/Downloads/cretas-backend-system-main/fix-document/init-final-users.sql root@106.14.165.234:/root/

# 方式2: 使用SFTP工具（FileZilla, Cyberduck等）
# 上传到: /root/init-final-users.sql
```

### 2. SSH登录并执行

```bash
# SSH登录
ssh root@106.14.165.234

# 执行SQL（会提示输入MySQL root密码）
mysql -u root -p cretas < /root/init-final-users.sql
```

### 3. 验证结果

```bash
# 查看创建的用户
mysql -u root -p cretas -e "SELECT username, role_code FROM users WHERE factory_id='F001';"
```

---

## 📊 这个SQL做了什么？

### 创建的测试账号（密码都是 `123456`）

**工厂用户 (F001)**:
- `proc_admin` - 加工管理员 ⭐ **推荐测试**
- `perm_admin` - 权限管理员
- `farm_admin` - 养殖管理员  
- `logi_admin` - 物流管理员
- `proc_user` - 加工操作员

**平台管理员**:
- `admin` - 超级管理员
- `developer` - 开发者
- `platform_admin` - 平台管理员

---

## ✅ 验证执行成功

### 测试登录API

```bash
curl -X POST "http://106.14.165.234:10010/api/mobile/auth/unified-login" \
  -H "Content-Type: application/json" \
  -d '{"username":"proc_admin","password":"123456","factoryId":"F001"}'
```

**预期结果**: 返回 `{"code":200,"success":true,...}` 并包含 `accessToken`

### 测试Dashboard API

在项目根目录执行：
```bash
bash test_server_106.sh
```

---

## 🎯 下一步

### 1. 重启React Native应用

```bash
cd ~/my-prototype-logistics/frontend/CretasFoodTrace
npx expo start --clear
```

### 2. 登录测试

- 用户名: `proc_admin`
- 密码: `123456`
- 工厂ID: 自动使用 `F001`

### 3. 检查Dashboard

- 打开应用首页
- 查看Dashboard数据
- 确认不再显示全0
- 验证没有403错误

---

## 🔧 如果遇到问题

### 问题1: 上传失败

```bash
# 检查SSH连接
ssh root@106.14.165.234

# 检查文件路径
ls -la ~/Downloads/cretas-backend-system-main/fix-document/init-final-users.sql
```

### 问题2: MySQL密码错误

```bash
# 在服务器上重置MySQL root密码
# 详见: https://dev.mysql.com/doc/refman/8.0/en/resetting-permissions.html
```

### 问题3: 执行后仍无法登录

**检查清单**:
1. SQL是否执行成功（无错误）？
2. 用户是否创建成功？
   ```sql
   SELECT * FROM users WHERE username='proc_admin';
   ```
3. 前端配置是否正确（使用F001）？
   - 文件: `frontend/CretasFoodTrace/src/constants/config.ts`
   - 应该是: `DEFAULT_FACTORY_ID = 'F001'`
4. 后端是否重启？
   ```bash
   cd /www/wwwroot/cretas
   bash restart.sh
   ```

---

## 📚 详细文档

需要更多信息？查看：

- **完整执行指南**: [EXECUTE_SQL_GUIDE.md](./EXECUTE_SQL_GUIDE.md)
- **测试账号列表**: `/my-prototype-logistics/TEST_ACCOUNTS.md`
- **Dashboard API文档**: `/my-prototype-logistics/DASHBOARD_API_ALREADY_IMPLEMENTED.md`

---

## 🎉 成功标志

执行成功后，你会看到：

✅ SQL执行无错误
✅ 测试登录API返回200
✅ React Native应用可以登录
✅ Dashboard显示数据
✅ 没有403权限错误

---

**文件位置**: `~/Downloads/cretas-backend-system-main/fix-document/init-final-users.sql`
**文件大小**: 217行
**工厂ID**: F001
**统一密码**: 123456

---

**最后更新**: 2025-11-02
**需要帮助**: 查看 EXECUTE_SQL_GUIDE.md 或联系开发团队
