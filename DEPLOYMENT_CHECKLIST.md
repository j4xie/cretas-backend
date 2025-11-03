# 部署检查清单

## 服务器配置信息

- **宝塔面板**: https://139.196.165.140:17400
- **API密钥**: Fw3rqkRqAashK9uNDsFxvst31YSbBmUb
- **应用服务器**: 139.196.165.140
- **部署路径**: /www/wwwroot/project
- **应用端口**: 10010
- **数据库**: 139.196.165.140:3306 (用户: cretas, 密码: sYyS6Jp3pyFMwLdA)
- **Redis**: 139.196.165.140:6379 (密码: 123456)

## 已上传的脚本文件

### 1. restart.sh ✅
**用途**: 重启 Spring Boot 应用
**状态**: 已上传
**位置**: /www/wwwroot/project/restart.sh
**权限**: 755

### 2. install-java-simple.sh ✅
**用途**: 安装 Java 17 (简化版，适用于 Alibaba Cloud Linux)
**状态**: 已上传
**位置**: /www/wwwroot/project/install-java-simple.sh
**权限**: 755

### 3. open-port-10010.sh ✅
**用途**: 开放防火墙端口 10010
**状态**: 已上传
**位置**: /www/wwwroot/project/open-port-10010.sh
**权限**: 755

## 部署步骤

### 步骤 1: 安装 Java 17 ⏳

**操作**:
```bash
cd /www/wwwroot/project
sudo bash install-java-simple.sh
```

**预期结果**:
```
✅ Java 安装成功!
java version "17.x.x"
建议 JAVA_HOME: /usr/lib/jvm/java-17-openjdk-xxx
```

**验证**:
```bash
java -version
echo $JAVA_HOME
```

**注意事项**:
- 安装完成后需要执行 `source /etc/profile` 或重新登录终端
- 确保 JAVA_HOME 环境变量已正确设置

---

### 步骤 2: 开放端口 10010 ⏳

**操作**:
```bash
cd /www/wwwroot/project
sudo bash open-port-10010.sh
```

**预期结果**:
```
✅ 端口开放成功!
📋 当前开放的端口:
10010/tcp
```

**验证**:
```bash
# 检查端口是否开放
netstat -tulpn | grep 10010

# 检查防火墙规则
firewall-cmd --list-ports
# 或
iptables -L -n | grep 10010
```

**注意事项**:
- 脚本自动检测系统使用 firewalld 还是 iptables
- 如果是云服务器，还需要在**云服务商控制台**配置安全组规则

---

### 步骤 3: 启动应用 ⏳

**操作**:
```bash
cd /www/wwwroot/project
bash restart.sh
```

**预期结果**:
```
✅ 应用启动成功!
   PID: [进程ID]
   端口: 10010
```

**验证**:
```bash
# 检查进程
ps aux | grep cretas-backend-system

# 检查端口监听
netstat -tulpn | grep 10010

# 检查日志
tail -f /www/wwwroot/project/cretas-backend.log
```

---

### 步骤 4: 测试应用 ⏳

**健康检查**:
```bash
curl http://139.196.165.140:10010/actuator/health
```

**预期响应**:
```json
{
  "status": "UP"
}
```

**测试登录 API**:
```bash
curl -X POST http://139.196.165.140:10010/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin@123456"
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
      ...
    }
  }
}
```

---

## 故障排查

### 问题 1: Java 未安装或版本不正确

**症状**:
```
nohup: failed to run command 'java': No such file or directory
```

**解决方案**:
1. 重新运行 `install-java-simple.sh`
2. 检查 Java 版本: `java -version` (需要 >= 17)
3. 检查 JAVA_HOME: `echo $JAVA_HOME`
4. 如果环境变量未生效，执行: `source /etc/profile`

---

### 问题 2: 端口未开放或被占用

**症状**:
```
Address already in use
```

**解决方案**:
```bash
# 检查端口占用
netstat -tulpn | grep 10010

# 如果被其他进程占用，停止该进程
kill -9 [PID]

# 确保防火墙开放端口
sudo bash open-port-10010.sh

# 云服务器还需检查安全组规则
```

---

### 问题 3: 数据库连接失败

**症状**:
```
Unable to connect to database
Communications link failure
```

**解决方案**:
1. 检查数据库服务状态:
   ```bash
   systemctl status mysqld
   ```

2. 检查数据库连接:
   ```bash
   mysql -h 139.196.165.140 -u cretas -p
   # 密码: sYyS6Jp3pyFMwLdA
   ```

3. 检查 application.yml 配置:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://139.196.165.140:3306/cretas
       username: cretas
       password: sYyS6Jp3pyFMwLdA
   ```

---

### 问题 4: Redis 连接失败

**症状**:
```
Unable to connect to Redis
Connection refused
```

**解决方案**:
1. 检查 Redis 服务状态:
   ```bash
   redis-cli -h 139.196.165.140 -p 6379 -a 123456 ping
   ```

2. 预期响应: `PONG`

3. 检查 application.yml 配置:
   ```yaml
   spring:
     redis:
       host: 139.196.165.140
       port: 6379
       password: 123456
   ```

---

### 问题 5: 应用启动后立即退出

**症状**:
```
❌ 应用启动失败!
```

**解决方案**:
1. 查看完整日志:
   ```bash
   cat /www/wwwroot/project/cretas-backend.log
   ```

2. 常见原因:
   - 配置文件错误 (application.yml)
   - 依赖服务不可用 (MySQL, Redis)
   - 端口冲突
   - JAR 文件损坏或不完整

3. 手动启动测试:
   ```bash
   cd /www/wwwroot/project
   java -jar cretas-backend-system-1.0.0.jar --server.port=10010
   ```

---

## 云服务商安全组配置

### 阿里云 ECS

1. 登录阿里云控制台
2. 进入 **云服务器 ECS** → **实例**
3. 找到服务器 139.196.165.140
4. 点击 **安全组** → **配置规则**
5. 添加入站规则:
   - **端口范围**: 10010/10010
   - **协议类型**: TCP
   - **授权对象**: 0.0.0.0/0 (或指定 IP)
   - **优先级**: 1
   - **描述**: Spring Boot 应用端口

### 其他云服务商

- **腾讯云**: 安全组 → 入站规则 → 添加规则
- **华为云**: 安全组 → 入站规则 → 添加规则
- **AWS**: Security Groups → Inbound Rules → Add Rule

---

## 监控和维护

### 日志监控

**实时查看日志**:
```bash
tail -f /www/wwwroot/project/cretas-backend.log
```

**查看最近 100 行**:
```bash
tail -n 100 /www/wwwroot/project/cretas-backend.log
```

**搜索错误日志**:
```bash
grep -i error /www/wwwroot/project/cretas-backend.log
```

### 性能监控

**查看 CPU 和内存使用**:
```bash
top -p $(ps aux | grep cretas-backend-system | grep -v grep | awk '{print $2}')
```

**查看 JVM 内存**:
```bash
jps -lvm | grep cretas-backend-system
```

### 定期重启

**创建定时任务** (可选):
```bash
# 编辑 crontab
crontab -e

# 添加每天凌晨 3 点重启应用
0 3 * * * cd /www/wwwroot/project && bash restart.sh
```

---

## 宝塔面板管理

### 使用宝塔面板 API

**本地脚本**:
- `upload-restart-script.sh` - 上传重启脚本
- `upload-java-simple.sh` - 上传 Java 安装脚本
- `upload-port-script.sh` - 上传端口开放脚本

**API 文档**: 参见 `bt-api-guide.md`

### 通过宝塔面板管理

1. 访问: https://139.196.165.140:17400
2. 使用账号密码登录
3. 进入 **文件管理** → `/www/wwwroot/project`
4. 可以直接查看、编辑文件和查看日志

---

## 更新部署流程

### 1. 编译新 JAR

```bash
# 在本地开发环境
cd /Users/jietaoxie/Downloads/cretas-backend-system-main
mvn clean package -DskipTests
```

### 2. 上传 JAR 到服务器

**方式 1: 使用宝塔面板上传**
1. 登录宝塔面板
2. 文件管理 → /www/wwwroot/project
3. 上传 `target/cretas-backend-system-1.0.0.jar`

**方式 2: 使用 SCP**
```bash
scp target/cretas-backend-system-1.0.0.jar root@139.196.165.140:/www/wwwroot/project/
```

**方式 3: 创建自动上传脚本** (类似其他上传脚本)

### 3. 重启应用

```bash
cd /www/wwwroot/project
bash restart.sh
```

### 4. 验证部署

```bash
curl http://139.196.165.140:10010/actuator/health
```

---

## 当前状态

- ✅ 宝塔面板配置完成
- ✅ restart.sh 已上传
- ✅ install-java-simple.sh 已上传
- ✅ open-port-10010.sh 已上传
- ⏳ 等待执行: 安装 Java 17
- ⏳ 等待执行: 开放端口 10010
- ⏳ 等待执行: 启动应用

---

## 下一步操作

请在服务器上依次执行以下命令:

```bash
# 1. 安装 Java 17
cd /www/wwwroot/project
sudo bash install-java-simple.sh
source /etc/profile

# 2. 开放端口 10010
sudo bash open-port-10010.sh

# 3. 启动应用
bash restart.sh

# 4. 检查应用状态
tail -f cretas-backend.log

# 5. 测试健康检查
curl http://139.196.165.140:10010/actuator/health
```

---

## 联系方式

如有问题，请检查:
1. 应用日志: `/www/wwwroot/project/cretas-backend.log`
2. 系统日志: `journalctl -xe`
3. 宝塔面板: https://139.196.165.140:17400
