# 宝塔面板配置指南

## 📋 当前配置信息

- **面板地址**: https://139.196.165.140:17400
- **API密钥**: `Fw3rqkRqAashK9uNDsFxvst31YSbBmUb`
- **应用服务器**: 139.196.165.140:10010

## ⚠️ IP白名单配置（重要）

为了使用宝塔API自动上传和管理文件，需要先添加IP白名单。

### 步骤1: 登录宝塔面板

1. 访问: https://139.196.165.140:17400
2. 输入您的宝塔面板用户名和密码

### 步骤2: 配置API接口

1. 点击左侧菜单 **"设置"**
2. 找到 **"API接口"** 选项卡
3. 确保 **"API接口开关"** 已开启（绿色）
4. 找到 **"接口密钥"** 字段，确认为: `Fw3rqkRqAashK9uNDsFxvst31YSbBmUb`

### 步骤3: 添加IP白名单

在 **"IP白名单"** 输入框中添加你的公网IP地址（可以在终端执行 `curl ifconfig.me` 查看当前IP）

**重要提示**:
- 每行一个IP地址
- 如果需要允许所有IP访问（不推荐生产环境），可以输入 `*`
- 建议只添加需要的IP地址以确保安全

### 步骤4: 保存配置

点击 **"保存"** 按钮

## ✅ 验证配置

配置完成后，在本地执行以下命令测试：

```bash
cd /Users/jietaoxie/Downloads/cretas-backend-system-main
bash upload-restart-script.sh
```

如果配置成功，您会看到：
```
✅ 文件上传成功
✅ 权限设置成功 (755)
```

## 🚀 自动上传重启脚本

配置完成后，运行以下命令即可自动上传并配置重启脚本：

```bash
cd /Users/jietaoxie/Downloads/cretas-backend-system-main
bash upload-restart-script.sh
```

脚本会自动：
1. 生成API签名
2. 上传 `restart.sh` 到服务器 `/www/wwwroot/project/`
3. 设置执行权限为 `755`

## 📝 手动上传方式（备选）

如果API上传失败，可以使用以下方式：

### 方式1: 宝塔面板文件管理器

1. 登录宝塔面板
2. 点击 **"文件"**
3. 导航到 `/www/wwwroot/project/`
4. 点击 **"上传"**
5. 选择 `restart.sh` 文件
6. 上传完成后，右键点击文件 -> **"权限"** -> 设置为 `755`

### 方式2: SSH上传

```bash
scp /Users/jietaoxie/Downloads/cretas-backend-system-main/restart.sh root@139.196.165.140:/www/wwwroot/project/
ssh root@139.196.165.140 "chmod +x /www/wwwroot/project/restart.sh"
```

## 🔧 使用重启脚本

上传完成后，在宝塔终端或SSH中执行：

```bash
cd /www/wwwroot/project
bash restart.sh
```

脚本会自动：
1. 停止现有的Java进程
2. 启动新的应用实例
3. 显示应用状态和PID
4. 提供日志查看命令

## 📊 预期输出

```
======================================
  Cretas Backend System 重启脚本
======================================

📍 当前目录: /www/wwwroot/project

🔍 查找现有Java进程...
🛑 停止现有进程: 12345

📦 JAR文件: cretas-backend-system-1.0.0.jar
📊 文件大小: 59M

🚀 启动应用...
✅ 应用启动成功!
   PID: 12346
   端口: 10010
   日志: /www/wwwroot/project/cretas-backend.log

📝 查看日志: tail -f /www/wwwroot/project/cretas-backend.log
🌐 访问地址: http://139.196.165.140:10010

======================================
  重启完成
======================================
```

## 🔐 安全建议

1. ✅ **限制IP白名单** - 只添加必要的IP地址
2. ✅ **定期更换API密钥** - 每3-6个月更换一次
3. ✅ **使用HTTPS** - 确保所有API调用使用HTTPS
4. ⚠️ **生产环境谨慎开启API** - API接口可能增加安全风险
5. ✅ **审计API调用日志** - 定期检查宝塔面板的API访问日志

## 🆘 故障排查

### 问题1: IP校验失败

**错误信息**: `IP校验失败，您的访问IP为[x.x.x.x]`

**解决方案**:
1. 检查宝塔面板是否已添加该IP到白名单
2. 确认IP白名单格式正确（每行一个IP）
3. 保存配置后等待1-2分钟生效

### 问题2: 密钥校验失败

**错误信息**: `密钥校验失败`

**解决方案**:
1. 检查API密钥是否正确: `Fw3rqkRqAashK9uNDsFxvst31YSbBmUb`
2. 确认API接口开关已开启
3. 重新生成API密钥并更新配置

### 问题3: 连接超时

**错误信息**: `Connection timeout` 或 `Connection refused`

**解决方案**:
1. 检查宝塔面板是否运行: `systemctl status bt`
2. 检查端口17400是否开放
3. 检查防火墙设置: `firewall-cmd --list-ports`

### 问题4: 文件上传失败

**解决方案**:
1. 检查目标路径是否存在: `/www/wwwroot/project/`
2. 检查文件权限
3. 使用备选的手动上传方式

## 📞 技术支持

- **宝塔官方文档**: https://www.bt.cn/bbs/
- **API文档**: https://www.bt.cn/bbs/thread-20376-1-1.html
- **配置指南**: `/Users/jietaoxie/my-prototype-logistics/.claude/bt-api-guide.md`
