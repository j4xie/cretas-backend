# 应用访问问题诊断指南

## 🔍 快速诊断步骤

### 步骤 1: 检查应用是否运行

**在服务器上执行（通过宝塔终端或SSH）**:

```bash
# 方法1: 检查进程
ps aux | grep cretas-backend-system | grep -v grep

# 方法2: 检查端口是否监听
netstat -tuln | grep 10010
# 或
ss -tuln | grep 10010

# 方法3: 检查Java进程
ps aux | grep java | grep cretas
```

**预期结果**:
- 如果看到进程，说明应用在运行
- 如果看到 `10010` 端口在监听，说明应用已启动

---

### 步骤 2: 检查应用日志

```bash
cd /www/wwwroot/project
tail -50 cretas-backend.log
```

**查看关键信息**:
- ✅ `Started CretasApplication` - 应用启动成功
- ❌ `APPLICATION FAILED TO START` - 应用启动失败
- ❌ `Port 10010 was already in use` - 端口被占用
- ❌ `Failed to connect to database` - 数据库连接失败

---

### 步骤 3: 检查防火墙和端口

**在服务器上执行**:

```bash
# 检查防火墙端口
firewall-cmd --list-ports
# 或
iptables -L -n | grep 10010

# 检查端口是否开放
netstat -tuln | grep 10010
```

**如果端口未开放，执行**:
```bash
cd /www/wwwroot/project
bash open-port-10010.sh
```

---

### 步骤 4: 检查云服务商安全组

**重要**: 除了服务器防火墙，还需要在云服务商控制台配置安全组！

#### 阿里云ECS
1. 登录阿里云控制台
2. 进入 **云服务器 ECS** → **实例**
3. 找到服务器 `139.196.165.140`
4. 点击 **安全组** → **配置规则**
5. 添加入站规则:
   - **端口范围**: 10010/10010
   - **协议类型**: TCP
   - **授权对象**: 0.0.0.0/0（或指定IP）
   - **优先级**: 1
   - **描述**: Spring Boot应用端口

---

### 步骤 5: 本地测试连接

**在本地终端执行**:

```bash
# 测试端口是否可达
telnet 139.196.165.140 10010
# 或
nc -zv 139.196.165.140 10010

# 测试HTTP连接
curl -v http://139.196.165.140:10010/actuator/health
# 或
curl http://139.196.165.140:10010/swagger-ui.html
```

---

## 🚀 如果应用未运行，启动它

### 方法1: 使用重启脚本（推荐）

```bash
cd /www/wwwroot/project
bash restart.sh
```

### 方法2: 手动启动

```bash
cd /www/wwwroot/project
nohup java -jar cretas-backend-system-1.0.0.jar \
    --server.port=10010 \
    > cretas-backend.log 2>&1 &
```

### 方法3: 通过宝塔面板

1. 登录宝塔面板: `https://139.196.165.140:17400`
2. 点击 **"文件"** → 进入 `/www/wwwroot/project`
3. 点击右上角 **"终端"**
4. 执行:
   ```bash
   cd /www/wwwroot/project
   bash restart.sh
   ```

---

## ⚠️ 常见问题及解决方案

### 问题1: 端口被占用

**错误信息**: `Port 10010 was already in use`

**解决方案**:
```bash
# 查找占用端口的进程
lsof -i :10010
# 或
netstat -tuln | grep 10010

# 停止进程
kill -9 <PID>
```

### 问题2: 数据库连接失败

**错误信息**: `Failed to connect to database`

**解决方案**:
1. 检查数据库是否运行:
   ```bash
   systemctl status mysql
   # 或
   systemctl status mysqld
   ```

2. 检查数据库配置:
   ```bash
   mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA -e "SELECT 1;"
   ```

3. 如果连接失败，检查 `application.yml` 配置:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/cretas
       username: cretas
       password: sYyS6Jp3pyFMwLdA
   ```

### 问题3: Redis连接失败

**错误信息**: `Unable to connect to Redis`

**解决方案**:
```bash
# 检查Redis是否运行
systemctl status redis
# 或
redis-cli -h localhost -p 6379 -a 123456 ping
```

### 问题4: 防火墙阻止

**症状**: 本地无法访问，但服务器上可以访问

**解决方案**:
1. 检查服务器防火墙:
   ```bash
   firewall-cmd --list-ports
   ```

2. 开放端口:
   ```bash
   firewall-cmd --permanent --add-port=10010/tcp
   firewall-cmd --reload
   ```

3. **重要**: 检查云服务商安全组是否已配置

### 问题5: JAR文件损坏

**症状**: 应用启动立即退出

**解决方案**:
1. 检查JAR文件:
   ```bash
   cd /www/wwwroot/project
   ls -lh cretas-backend-system-1.0.0.jar
   ```

2. 验证JAR文件:
   ```bash
   java -jar cretas-backend-system-1.0.0.jar --version
   ```

3. 如果损坏，重新上传JAR文件

---

## 📋 完整诊断脚本

**在服务器上执行以下脚本进行完整诊断**:

```bash
#!/bin/bash
echo "======================================"
echo "  应用状态诊断"
echo "======================================"
echo ""

# 1. 检查进程
echo "1. 检查应用进程..."
if ps aux | grep cretas-backend-system | grep -v grep > /dev/null; then
    echo "✅ 应用进程存在"
    ps aux | grep cretas-backend-system | grep -v grep
else
    echo "❌ 应用进程不存在"
fi
echo ""

# 2. 检查端口
echo "2. 检查端口10010..."
if netstat -tuln | grep 10010 > /dev/null; then
    echo "✅ 端口10010正在监听"
    netstat -tuln | grep 10010
else
    echo "❌ 端口10010未监听"
fi
echo ""

# 3. 检查防火墙
echo "3. 检查防火墙..."
if command -v firewall-cmd > /dev/null; then
    if firewall-cmd --list-ports | grep 10010 > /dev/null; then
        echo "✅ 防火墙已开放端口10010"
    else
        echo "⚠️  防火墙未开放端口10010"
        echo "   执行: firewall-cmd --permanent --add-port=10010/tcp && firewall-cmd --reload"
    fi
else
    echo "ℹ️  未检测到firewalld"
fi
echo ""

# 4. 检查日志
echo "4. 检查应用日志..."
LOG_FILE="/www/wwwroot/project/cretas-backend.log"
if [ -f "$LOG_FILE" ]; then
    echo "✅ 日志文件存在"
    echo "最近10行日志:"
    tail -10 "$LOG_FILE"
    
    if grep -q "Started CretasApplication" "$LOG_FILE"; then
        echo "✅ 应用已成功启动"
    elif grep -q "APPLICATION FAILED TO START" "$LOG_FILE"; then
        echo "❌ 应用启动失败"
    else
        echo "⚠️  无法确定应用状态"
    fi
else
    echo "❌ 日志文件不存在"
fi
echo ""

# 5. 检查JAR文件
echo "5. 检查JAR文件..."
JAR_FILE="/www/wwwroot/project/cretas-backend-system-1.0.0.jar"
if [ -f "$JAR_FILE" ]; then
    echo "✅ JAR文件存在"
    ls -lh "$JAR_FILE"
else
    echo "❌ JAR文件不存在"
fi
echo ""

# 6. 本地测试
echo "6. 本地测试连接..."
if curl -s --connect-timeout 5 http://localhost:10010/actuator/health > /dev/null; then
    echo "✅ 本地连接成功"
else
    echo "❌ 本地连接失败"
fi
echo ""

echo "======================================"
echo "  诊断完成"
echo "======================================"
```

---

## ✅ 快速修复命令

**如果应用未运行，一键修复**:

```bash
cd /www/wwwroot/project

# 1. 停止旧进程
pkill -f cretas-backend-system

# 2. 开放端口
firewall-cmd --permanent --add-port=10010/tcp 2>/dev/null
firewall-cmd --reload 2>/dev/null

# 3. 启动应用
bash restart.sh

# 4. 等待5秒
sleep 5

# 5. 检查状态
ps aux | grep cretas-backend-system | grep -v grep
netstat -tuln | grep 10010
```

---

## 📞 需要帮助？

如果以上步骤都无法解决问题，请提供：
1. 应用日志的最后50行: `tail -50 /www/wwwroot/project/cretas-backend.log`
2. 进程检查结果: `ps aux | grep cretas`
3. 端口检查结果: `netstat -tuln | grep 10010`
4. 防火墙状态: `firewall-cmd --list-ports`

