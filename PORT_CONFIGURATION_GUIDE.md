# 端口配置说明

## 📋 端口和服务关系

您的系统涉及**三个不同的端口**，它们各有不同的用途：

### 1. 端口 10010 - Spring Boot 应用服务 ✅

- **用途**: HTTP API 服务（您的 Java 后端应用）
- **服务**: Spring Boot Application
- **协议**: HTTP
- **配置文件**: `application.yml` 中的 `server.port: 10010`
- **访问**: `http://139.196.165.140:10010`
- **状态**: ✅ 已开放（通过 `open-port-10010.sh`）

**客户端访问**:
- React Native 前端
- 移动应用
- 外部 API 调用

---

### 2. 端口 3306 - MySQL 数据库服务 ⏳

- **用途**: 数据库连接
- **服务**: MySQL Database
- **协议**: MySQL Protocol
- **配置文件**: `application.yml` 中的 `datasource.url`
- **当前配置**: `jdbc:mysql://139.196.165.140:3306/cretas`
- **状态**: ⏳ 待诊断（应用无法连接）

**客户端访问**:
- Spring Boot 应用（通过 JDBC）
- 数据库管理工具
- 备份脚本

---

### 3. 端口 17400 - 宝塔面板 ✅

- **用途**: 服务器管理面板
- **服务**: Baota Panel
- **协议**: HTTPS
- **访问**: `https://139.196.165.140:17400`
- **状态**: ✅ 正常运行

**客户端访问**:
- 管理员浏览器
- API 自动化脚本

---

## 🔍 当前问题分析

### 问题现象

Spring Boot 应用启动失败，日志显示：

```
Caused by: java.net.SocketTimeoutException: Connect timed out
Communications link failure
```

### 问题原因

应用无法连接到 MySQL 数据库 `139.196.165.140:3306`

### 可能的原因

有两种情况：

#### 情况 A: MySQL 在同一台服务器上（本地连接）

如果 MySQL 和 Spring Boot 应用在同一台服务器 `139.196.165.140` 上：

**推荐方案**: 使用本地连接（localhost）

**优点**:
- ✅ 不需要开放 3306 端口
- ✅ 更安全
- ✅ 连接速度更快
- ✅ 无需配置防火墙

**需要修改**:

修改 `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cretas?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    # 或使用
    url: jdbc:mysql://127.0.0.1:3306/cretas?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
```

**验证方法**:
```bash
# 在服务器上执行
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA -e "SELECT 1;"
```

---

#### 情况 B: MySQL 在不同的服务器上（远程连接）

如果 MySQL 在另一台服务器上，需要配置远程访问：

**需要的步骤**:

1. **开放 MySQL 服务器的 3306 端口**

   使用防火墙开放端口：
   ```bash
   # firewalld
   firewall-cmd --permanent --add-port=3306/tcp
   firewall-cmd --reload

   # 或 iptables
   iptables -I INPUT -p tcp --dport 3306 -j ACCEPT
   iptables-save > /etc/sysconfig/iptables
   ```

2. **配置云服务商安全组**

   在阿里云/腾讯云/华为云控制台：
   - 找到 MySQL 服务器的安全组设置
   - 添加入站规则: TCP 3306 端口
   - 授权对象: 应用服务器 IP 或 0.0.0.0/0

3. **配置 MySQL 允许远程连接**

   ```bash
   # 登录 MySQL
   mysql -u root -p

   # 查看当前用户权限
   SELECT host, user FROM mysql.user WHERE user='cretas';

   # 如果 host 是 'localhost'，需要添加远程访问权限
   GRANT ALL PRIVILEGES ON cretas.* TO 'cretas'@'%' IDENTIFIED BY 'sYyS6Jp3pyFMwLdA';
   FLUSH PRIVILEGES;

   # 或者针对特定 IP
   GRANT ALL PRIVILEGES ON cretas.* TO 'cretas'@'139.196.165.140' IDENTIFIED BY 'sYyS6Jp3pyFMwLdA';
   FLUSH PRIVILEGES;
   ```

4. **修改 MySQL 配置监听所有接口**

   编辑 `/etc/my.cnf`:
   ```ini
   [mysqld]
   bind-address = 0.0.0.0
   ```

   重启 MySQL:
   ```bash
   systemctl restart mysqld
   ```

**验证方法**:
```bash
# 从应用服务器测试连接到 MySQL 服务器
mysql -h 139.196.165.140 -u cretas -psYyS6Jp3pyFMwLdA -e "SELECT 1;"
```

---

## 🎯 推荐方案

### 方案选择流程图

```
MySQL 是否在同一台服务器？
├─ 是 → 使用 localhost 连接（方案 A）✅ 推荐
│         - 修改 application.yml
│         - 无需开放端口
│         - 重新编译部署
│
└─ 否 → 配置远程连接（方案 B）
          - 开放 3306 端口
          - 配置 MySQL 远程访问
          - 配置云安全组
          - 修改 application.yml（如果需要）
```

---

## 📝 诊断步骤

我已经为您创建了诊断脚本，请在服务器上执行：

```bash
cd /www/wwwroot/project
bash diagnose-mysql.sh
```

这个脚本会自动检查：

1. ✓ MySQL 服务状态
2. ✓ MySQL 监听端口和接口
3. ✓ 防火墙配置
4. ✓ 本地连接测试（localhost, 127.0.0.1）
5. ✓ 远程连接测试（139.196.165.140）
6. ✓ 给出具体的修复建议

**输出示例**:

```
======================================"
  MySQL 配置诊断
======================================"

🔍 检查 MySQL 服务...

1. 检查本地 MySQL 服务状态:
✅ MySQL 正在运行

2. 检查 MySQL 监听端口:
✅ MySQL 正在监听:
tcp  0  0  127.0.0.1:3306  0.0.0.0:*  LISTEN

⚠️  MySQL 只监听 localhost (127.0.0.1)
   建议: 应用配置使用 localhost 连接

3. 检查防火墙配置:
...

4. 测试本地 MySQL 连接:
   ✅ localhost 连接成功
   ✅ 127.0.0.1 连接成功
   ❌ 139.196.165.140 连接失败

======================================"
  诊断建议
======================================"

📋 建议方案 A: 使用本地连接（推荐）

修改 application.yml:
  datasource:
    url: jdbc:mysql://localhost:3306/cretas?...

优点: 无需开放端口，更安全
```

---

## 🔧 修复步骤（假设 MySQL 在本地）

### 步骤 1: 运行诊断脚本

```bash
cd /www/wwwroot/project
bash diagnose-mysql.sh
```

### 步骤 2: 根据诊断结果修改配置

如果诊断显示 MySQL 在本地（127.0.0.1），在本地开发环境修改 `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cretas?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: cretas
    password: sYyS6Jp3pyFMwLdA
```

### 步骤 3: 重新编译 JAR

```bash
# 在本地开发环境
cd /Users/jietaoxie/Downloads/cretas-backend-system-main
mvn clean package -DskipTests
```

### 步骤 4: 上传新的 JAR 到服务器

使用宝塔面板文件管理器上传：
- 登录: https://139.196.165.140:16435
- 文件 → /www/wwwroot/project
- 上传 `target/cretas-backend-system-1.0.0.jar`

### 步骤 5: 重启应用

```bash
cd /www/wwwroot/project
bash restart.sh
```

### 步骤 6: 验证应用

```bash
# 检查应用日志
tail -f /www/wwwroot/project/cretas-backend.log

# 测试健康检查
curl http://139.196.165.140:10010/actuator/health
```

---

## 📊 端口使用总结

| 端口 | 服务 | 协议 | 开放范围 | 状态 | 用途 |
|------|------|------|----------|------|------|
| 10010 | Spring Boot | HTTP | 外部访问 | ✅ 已开放 | API 服务 |
| 3306 | MySQL | MySQL | 本地/远程 | ⏳ 待配置 | 数据库连接 |
| 17400 | 宝塔面板 | HTTPS | 管理员 | ✅ 已开放 | 服务器管理 |

---

## ⚠️ 安全建议

### MySQL 端口安全

如果必须开放 3306 端口：

1. **限制访问 IP**: 只允许应用服务器 IP 访问
   ```bash
   # 只允许特定 IP
   firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="应用服务器IP" port protocol="tcp" port="3306" accept'
   ```

2. **使用强密码**: 数据库用户密码应足够复杂

3. **定期审计**: 检查 MySQL 用户权限和访问日志

4. **使用 SSL 连接**: 在 JDBC URL 中启用 SSL
   ```
   useSSL=true&requireSSL=true
   ```

### 推荐配置

对于单服务器部署：
- ✅ **推荐**: MySQL 只监听 localhost，应用使用本地连接
- ❌ **不推荐**: 开放 3306 给公网

对于多服务器部署：
- ✅ **推荐**: 使用 VPC 内网连接，不开放公网端口
- ⚠️ **可接受**: 开放 3306，但限制特定 IP + 使用 SSL

---

## 📞 下一步操作

请在服务器上执行诊断脚本：

```bash
cd /www/wwwroot/project
bash diagnose-mysql.sh
```

然后将诊断结果反馈，我会根据实际情况给出具体的解决方案。
