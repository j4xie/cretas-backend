#!/bin/bash

# ========================================
# MySQL 配置诊断脚本
# ========================================

echo "======================================"
echo "  MySQL 配置诊断"
echo "======================================"
echo ""

echo "🔍 检查 MySQL 服务..."
echo ""

# 检查本地MySQL服务
echo "1. 检查本地 MySQL 服务状态:"
if systemctl list-units --type=service | grep -i mysql &> /dev/null; then
    systemctl status mysqld 2>/dev/null || systemctl status mysql 2>/dev/null || echo "未找到 MySQL 服务"
else
    echo "❌ 本地未安装 MySQL 服务"
fi

echo ""

# 检查MySQL监听端口
echo "2. 检查 MySQL 监听端口:"
MYSQL_LISTEN=$(netstat -tulpn 2>/dev/null | grep 3306 || ss -tulpn 2>/dev/null | grep 3306)

if [ -n "$MYSQL_LISTEN" ]; then
    echo "✅ MySQL 正在监听:"
    echo "$MYSQL_LISTEN"

    # 判断是否只监听本地
    if echo "$MYSQL_LISTEN" | grep -q "127.0.0.1:3306"; then
        echo ""
        echo "⚠️  MySQL 只监听 localhost (127.0.0.1)"
        echo "   建议: 应用配置使用 localhost 连接"
    elif echo "$MYSQL_LISTEN" | grep -q "0.0.0.0:3306"; then
        echo ""
        echo "✓ MySQL 监听所有接口 (0.0.0.0)"
        echo "   可以接受远程连接"
    fi
else
    echo "❌ MySQL 未在监听端口 3306"
fi

echo ""

# 检查防火墙
echo "3. 检查防火墙配置:"
if systemctl is-active --quiet firewalld; then
    echo "防火墙类型: firewalld"
    echo "开放的端口:"
    firewall-cmd --list-ports 2>/dev/null

    if firewall-cmd --list-ports 2>/dev/null | grep -q "3306"; then
        echo "✅ 端口 3306 已开放"
    else
        echo "❌ 端口 3306 未开放"
    fi
elif command -v iptables &> /dev/null; then
    echo "防火墙类型: iptables"
    iptables -L -n | grep 3306
else
    echo "未检测到防火墙"
fi

echo ""

# 测试本地连接
echo "4. 测试本地 MySQL 连接:"
echo ""

if command -v mysql &> /dev/null; then
    # 测试 localhost 连接
    echo "   测试 localhost 连接..."
    if mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA -e "SELECT 1;" &> /dev/null; then
        echo "   ✅ localhost 连接成功"
    else
        echo "   ❌ localhost 连接失败"
    fi

    echo ""

    # 测试 127.0.0.1 连接
    echo "   测试 127.0.0.1 连接..."
    if mysql -h 127.0.0.1 -u cretas -psYyS6Jp3pyFMwLdA -e "SELECT 1;" &> /dev/null; then
        echo "   ✅ 127.0.0.1 连接成功"
    else
        echo "   ❌ 127.0.0.1 连接失败"
    fi

    echo ""

    # 测试远程 IP 连接（自己连自己）
    echo "   测试 139.196.165.140 连接..."
    if mysql -h 139.196.165.140 -u cretas -psYyS6Jp3pyFMwLdA -e "SELECT 1;" 2>&1 | tee /tmp/mysql_test.log | grep -q "ERROR"; then
        echo "   ❌ 139.196.165.140 连接失败"
        echo ""
        echo "   错误信息:"
        cat /tmp/mysql_test.log
    else
        echo "   ✅ 139.196.165.140 连接成功"
    fi
else
    echo "❌ MySQL 客户端未安装"
    echo "   安装命令: yum install -y mysql"
fi

echo ""
echo "======================================"
echo "  诊断建议"
echo "======================================"
echo ""

# 给出建议
if [ -n "$MYSQL_LISTEN" ]; then
    if echo "$MYSQL_LISTEN" | grep -q "127.0.0.1:3306"; then
        echo "📋 建议方案 A: 使用本地连接（推荐）"
        echo ""
        echo "修改 application.yml:"
        echo ""
        echo "  datasource:"
        echo "    url: jdbc:mysql://localhost:3306/cretas?..."
        echo ""
        echo "优点: 无需开放端口，更安全"
        echo ""
        echo "---"
        echo ""
        echo "📋 建议方案 B: 配置 MySQL 允许远程连接"
        echo ""
        echo "1. 修改 MySQL 配置文件 /etc/my.cnf:"
        echo "   bind-address = 0.0.0.0"
        echo ""
        echo "2. 重启 MySQL:"
        echo "   systemctl restart mysqld"
        echo ""
        echo "3. 开放防火墙端口:"
        echo "   firewall-cmd --permanent --add-port=3306/tcp"
        echo "   firewall-cmd --reload"
        echo ""
        echo "4. 配置 MySQL 用户远程访问权限:"
        echo "   GRANT ALL PRIVILEGES ON cretas.* TO 'cretas'@'%' IDENTIFIED BY 'sYyS6Jp3pyFMwLdA';"
        echo "   FLUSH PRIVILEGES;"
    elif echo "$MYSQL_LISTEN" | grep -q "0.0.0.0:3306"; then
        echo "📋 MySQL 已监听所有接口"
        echo ""
        echo "需要确认:"
        echo "1. 防火墙是否开放 3306 端口"
        echo "2. 云服务商安全组是否开放 3306 端口"
        echo "3. MySQL 用户是否允许远程连接"
    fi
else
    echo "❌ MySQL 服务未运行或未正确配置"
    echo ""
    echo "请先检查 MySQL 服务状态:"
    echo "  systemctl status mysqld"
fi

echo ""
