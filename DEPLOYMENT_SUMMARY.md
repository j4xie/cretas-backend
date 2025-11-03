# 部署总结 - Cretas Backend System

## 📅 部署日期
2025-11-02

## 🎯 部署状态
✅ **成功部署并运行**

---

## 📋 服务器信息

| 配置项 | 值 |
|--------|---|
| 服务器地址 | 139.196.165.140 |
| 应用端口 | 10010 |
| 部署路径 | /www/wwwroot/project |
| 宝塔面板 | https://139.196.165.140:17400 |
| 操作系统 | Alibaba Cloud Linux 4 |

---

## 🔧 已解决的问题

### 问题 1: Java 未安装
**现象**: `nohup: failed to run command 'java': No such file or directory`

**解决方案**:
- 创建并上传 `install-java-simple.sh` 脚本
- 在服务器上执行安装 Java 17
- 配置 JAVA_HOME 环境变量

**状态**: ✅ 已解决

---

### 问题 2: 数据库连接失败
**现象**: `Communications link failure`, `Connect timed out`

**原因分析**:
- 应用配置使用 `139.196.165.140:3306` (远程连接)
- MySQL 实际在本地运行 (localhost)
- 防火墙未开放 3306 端口

**解决方案**:
- 修改 `application.yml`:
  ```yaml
  datasource:
    url: jdbc:mysql://localhost:3306/cretas
  redis:
    host: localhost
  ```
- 使用本地连接，无需开放 3306 端口
- 更安全、更快速

**状态**: ✅ 已解决

---

### 问题 3: CacheManager 冲突
**现象**: `No qualifying bean of type 'CacheManager' available: expected single matching bean but found 2`

**原因分析**:
- 同时定义了两个 CacheManager bean:
  - `cacheManager` (Redis)
  - `fallbackCacheManager` (内存)
- Spring 不知道选择哪个

**解决方案**:
- 在主要的 `cacheManager` 上添加 `@Primary` 注解
- 优先使用 Redis 缓存管理器

**状态**: ✅ 已解决

---

### 问题 4: 数据库表结构警告
**现象**: `Can't create table 'cretas.#sql-6953_1c' (errno: 150)`

**原因分析**:
- Hibernate `ddl-auto: update` 尝试自动修改表结构
- 外键约束冲突

**解决方案**:
- 修改为 `ddl-auto: validate`
- 只验证表结构，不自动修改
- 适合生产环境

**状态**: ✅ 已解决

---

## 📦 最终配置

### application.yml 关键配置

```yaml
server:
  port: 10010

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cretas
    username: cretas
    password: sYyS6Jp3pyFMwLdA

  redis:
    host: localhost
    port: 6379
    password: 123456

  jpa:
    hibernate:
      ddl-auto: validate  # 生产环境：只验证不修改
```

### CacheConfig.java 关键修改

```java
@Primary  // 标记为主要的 CacheManager
@Bean
public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    // Redis 缓存管理器配置
    ...
}
```

---

## 🚀 部署文件清单

### 已上传到服务器的脚本

| 文件名 | 用途 | 路径 |
|--------|------|------|
| restart.sh | 重启应用 | /www/wwwroot/project/restart.sh |
| install-java-simple.sh | 安装 Java 17 | /www/wwwroot/project/install-java-simple.sh |
| open-port-10010.sh | 开放端口 10010 | /www/wwwroot/project/open-port-10010.sh |
| diagnose-mysql.sh | MySQL 诊断 | /www/wwwroot/project/diagnose-mysql.sh |

### 本地文档

| 文件名 | 说明 |
|--------|------|
| DEPLOYMENT_CHECKLIST.md | 部署检查清单 |
| PORT_CONFIGURATION_GUIDE.md | 端口配置指南 |
| BAOTA_SETUP_GUIDE.md | 宝塔面板配置指南 |
| bt-api-guide.md | 宝塔 API 使用指南 |

---

## ✅ 验证结果

### 应用状态

```bash
$ curl http://139.196.165.140:10010/api/auth/test
HTTP 403  # Spring Security 正常工作
```

### 日志确认

```
2025-11-03 09:20:33 - Tomcat started on port(s): 10010 (http)
2025-11-03 09:20:33 - Started CretasApplication in 19.258 seconds
========================================
     白垩纪食品溯源系统启动成功！
     Cretas Backend System Started!
========================================
```

**结论**: ✅ 应用成功启动并正常运行

---

## 🔐 安全配置

### 端口配置

| 端口 | 服务 | 开放范围 | 状态 |
|------|------|----------|------|
| 10010 | Spring Boot API | 外部访问 | ✅ 已开放 |
| 3306 | MySQL | 仅本地 | ✅ 未开放 (更安全) |
| 6379 | Redis | 仅本地 | ✅ 未开放 (更安全) |
| 17400 | 宝塔面板 | 管理员 | ✅ 已开放 |

### 防火墙规则

```bash
firewall-cmd --list-ports
# 输出: 20/tcp 21/tcp 22/tcp 80/tcp 443/tcp 888/tcp 3001/tcp 10010/tcp 16435/tcp 39000-40000/tcp
```

**安全优势**:
- ✅ MySQL 和 Redis 不对外开放
- ✅ 减少攻击面
- ✅ 使用本地连接更快速

---

## 📊 性能指标

- **启动时间**: 19.258 秒
- **内存使用**: 正常
- **数据库连接池**:
  - 最小空闲连接: 5
  - 最大连接数: 20
- **Redis 连接池**:
  - 最大活动连接: 8
  - 最小空闲连接: 0

---

## 🔄 后续操作指南

### 更新应用

1. **本地编译新版本**:
   ```bash
   cd /Users/jietaoxie/Downloads/cretas-backend-system-main
   mvn clean package -DskipTests
   ```

2. **上传 JAR 文件**:
   - 登录宝塔面板: https://139.196.165.140:17400
   - 文件管理 → /www/wwwroot/project
   - 上传 `target/cretas-backend-system-1.0.0.jar`

3. **重启应用**:
   ```bash
   cd /www/wwwroot/project
   bash restart.sh
   ```

4. **验证部署**:
   ```bash
   tail -f /www/wwwroot/project/cretas-backend.log
   curl http://139.196.165.140:10010/api/auth/test
   ```

---

## 🛠️ 故障排查

### 应用无法启动

**检查步骤**:
```bash
# 1. 查看日志
cat /www/wwwroot/project/cretas-backend.log

# 2. 检查 Java 版本
java -version  # 应该是 17+

# 3. 检查进程
ps aux | grep java

# 4. 检查端口
netstat -tulpn | grep 10010
```

### 数据库连接问题

**检查步骤**:
```bash
# 1. 测试 MySQL 连接
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA -e "SELECT 1;"

# 2. 检查 MySQL 服务
systemctl status mysqld

# 3. 运行诊断脚本
bash /www/wwwroot/project/diagnose-mysql.sh
```

### Redis 连接问题

**检查步骤**:
```bash
# 测试 Redis 连接
redis-cli -h localhost -p 6379 -a 123456 ping
# 预期输出: PONG
```

---

## 📝 配置变更历史

### 版本 1.0.0 (2025-11-02)

1. **数据库连接**:
   - 从远程连接改为本地连接
   - MySQL: `139.196.165.140:3306` → `localhost:3306`
   - Redis: `139.196.165.140:6379` → `localhost:6379`

2. **缓存配置**:
   - 添加 `@Primary` 注解到主 CacheManager
   - 保留 fallbackCacheManager 作为降级方案

3. **JPA 配置**:
   - `ddl-auto`: `update` → `validate`
   - 避免生产环境自动修改表结构

---

## 🎉 部署成功确认

- ✅ Java 17 安装成功
- ✅ MySQL 本地连接正常
- ✅ Redis 本地连接正常
- ✅ 端口 10010 已开放
- ✅ 应用成功启动
- ✅ API 响应正常
- ✅ Spring Security 工作正常
- ✅ 无严重错误或警告

**系统现在可以正常使用！**

---

## 📞 技术支持

- 宝塔面板: https://139.196.165.140:17400
- 应用 API: http://139.196.165.140:10010
- 部署路径: /www/wwwroot/project
- 日志文件: /www/wwwroot/project/cretas-backend.log

---

**部署完成时间**: 2025-11-02 20:25
**部署人员**: Claude AI Assistant
**部署状态**: ✅ 成功
