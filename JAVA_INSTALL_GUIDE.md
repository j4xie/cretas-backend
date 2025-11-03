# Java 安装和配置指南

## 🚨 问题诊断

根据日志显示：
```
nohup: failed to run command 'java': No such file or directory
```

**原因**: 服务器上没有安装 Java 或 Java 不在系统 PATH 中

## ✅ 解决方案

### 方案1: 安装 OpenJDK 17 (推荐)

#### CentOS / AlmaLinux / Rocky Linux

```bash
# 1. 安装 Java 17
sudo yum install -y java-17-openjdk java-17-openjdk-devel

# 2. 验证安装
java -version

# 3. 查找 Java 路径
which java
readlink -f $(which java)

# 4. 设置 JAVA_HOME (可选但推荐)
echo 'export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```

#### Ubuntu / Debian

```bash
# 1. 更新包列表
sudo apt update

# 2. 安装 Java 17
sudo apt install -y openjdk-17-jdk openjdk-17-jre

# 3. 验证安装
java -version

# 4. 配置默认 Java 版本 (如果有多个版本)
sudo update-alternatives --config java
```

### 方案2: 使用宝塔面板安装 Java

1. 登录宝塔面板: https://139.196.165.140:17400
2. 点击 **"软件商店"**
3. 搜索 **"Java"**
4. 选择 **"Java项目管理器"** 或 **"Tomcat"** (会自动安装 Java)
5. 点击 **"安装"**

### 方案3: 手动下载安装 (适用于无网络环境)

```bash
# 1. 下载 OpenJDK 17 (使用浏览器下载后上传到服务器)
# 下载地址: https://jdk.java.net/17/

# 2. 解压到 /usr/local
cd /usr/local
sudo tar -xzf openjdk-17_linux-x64_bin.tar.gz
sudo mv jdk-17* jdk-17

# 3. 配置环境变量
echo 'export JAVA_HOME=/usr/local/jdk-17' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc

# 4. 验证
java -version
```

## 🔧 安装完成后的验证

### 1. 检查 Java 版本

```bash
java -version
```

**预期输出**:
```
openjdk version "17.0.x" 2023-xx-xx
OpenJDK Runtime Environment (build 17.0.x+x)
OpenJDK 64-Bit Server VM (build 17.0.x+x, mixed mode, sharing)
```

### 2. 检查 Java 路径

```bash
which java
# 输出: /usr/bin/java

readlink -f $(which java)
# 输出: /usr/lib/jvm/java-17-openjdk-17.x.x.x/bin/java
```

### 3. 检查 JAVA_HOME

```bash
echo $JAVA_HOME
# 输出: /usr/lib/jvm/java-17-openjdk-17.x.x.x
```

## 🚀 更新重启脚本

如果 Java 不在默认 PATH 中，需要更新重启脚本使用绝对路径。

### 查找 Java 绝对路径

```bash
readlink -f $(which java)
```

假设输出为: `/usr/lib/jvm/java-17-openjdk/bin/java`

### 修改 restart.sh

编辑 `/www/wwwroot/project/restart.sh`，将：

```bash
nohup java -jar cretas-backend-system-1.0.0.jar \
    --server.port=10010 \
    > cretas-backend.log 2>&1 &
```

修改为：

```bash
nohup /usr/lib/jvm/java-17-openjdk/bin/java -jar cretas-backend-system-1.0.0.jar \
    --server.port=10010 \
    > cretas-backend.log 2>&1 &
```

## 📝 通过宝塔终端安装 (推荐)

1. 登录宝塔面板: https://139.196.165.140:17400
2. 点击 **"终端"**
3. 执行安装命令：

```bash
# AlmaLinux / CentOS
sudo yum install -y java-17-openjdk java-17-openjdk-devel

# 验证
java -version
```

4. 安装成功后，重新运行重启脚本：

```bash
cd /www/wwwroot/project
bash restart.sh
```

## 🔍 常见问题排查

### 问题1: 提示 "java: command not found"

**原因**: Java 未安装或不在 PATH 中

**解决方案**:
1. 检查是否已安装: `rpm -qa | grep java` (CentOS) 或 `dpkg -l | grep java` (Ubuntu)
2. 如果已安装但找不到，添加到 PATH: `export PATH=$PATH:/usr/lib/jvm/java-17-openjdk/bin`
3. 如果未安装，按照上述方案安装

### 问题2: 版本不匹配

**错误信息**: `UnsupportedClassVersionError` 或 `class file version xx.x`

**原因**: Java 版本过低，Spring Boot 3.x 需要 Java 17+

**解决方案**:
1. 卸载旧版本: `sudo yum remove java-*`
2. 安装 Java 17: `sudo yum install -y java-17-openjdk java-17-openjdk-devel`

### 问题3: 权限问题

**错误信息**: `Permission denied`

**解决方案**:
```bash
# 确保有执行权限
chmod +x /www/wwwroot/project/restart.sh

# 确保对目录有写权限
sudo chown -R $(whoami) /www/wwwroot/project
```

## 📊 验证应用启动

安装 Java 后，运行重启脚本：

```bash
cd /www/wwwroot/project
bash restart.sh
```

**预期输出**:
```
======================================
  Cretas Backend System 重启脚本
======================================

📍 当前目录: /www/wwwroot/project

🔍 查找现有Java进程...
ℹ️  未找到运行中的进程

📦 JAR文件: cretas-backend-system-1.0.0.jar
📊 文件大小: 60M

🚀 启动应用...
✅ 应用启动成功!
   PID: 12345
   端口: 10010
   日志: /www/wwwroot/project/cretas-backend.log

📝 查看日志: tail -f /www/wwwroot/project/cretas-backend.log
🌐 访问地址: http://139.196.165.140:10010
```

## 🌐 测试应用

```bash
# 测试健康检查端点
curl http://139.196.165.140:10010/actuator/health

# 测试登录API
curl -X POST http://139.196.165.140:10010/api/mobile/auth/unified-login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "123456"}'
```

## 📞 获取帮助

如果遇到问题，可以：

1. **查看日志**: `cat /www/wwwroot/project/cretas-backend.log`
2. **检查进程**: `ps aux | grep java`
3. **检查端口**: `netstat -tulpn | grep 10010`
4. **联系技术支持**: 提供日志文件内容

## 🔗 相关资源

- **OpenJDK 官网**: https://openjdk.org/
- **OpenJDK 下载**: https://jdk.java.net/17/
- **Spring Boot 文档**: https://spring.io/projects/spring-boot
- **宝塔面板文档**: https://www.bt.cn/bbs/
