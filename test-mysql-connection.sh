#!/bin/bash

# ========================================
# MySQL 连接测试脚本
# ========================================

echo "======================================"
echo "  MySQL 连接测试"
echo "======================================"
echo ""

# MySQL配置
MYSQL_HOST="139.196.165.140"
MYSQL_PORT="3306"
MYSQL_USER="cretas"
MYSQL_PASSWORD="sYyS6Jp3pyFMwLdA"
MYSQL_DATABASE="cretas"

echo "📋 测试配置:"
echo "   主机: $MYSQL_HOST"
echo "   端口: $MYSQL_PORT"
echo "   用户: $MYSQL_USER"
echo "   数据库: $MYSQL_DATABASE"
echo ""

# 测试1: 检查MySQL客户端
echo "1. 检查 MySQL 客户端..."
if command -v mysql &> /dev/null; then
    MYSQL_VERSION=$(mysql --version)
    echo "✅ MySQL 客户端已安装: $MYSQL_VERSION"
else
    echo "❌ MySQL 客户端未安装"
    echo "   安装命令: yum install -y mysql"
    exit 1
fi

echo ""

# 测试2: 检查端口连通性
echo "2. 检查端口连通性..."
if command -v telnet &> /dev/null; then
    echo "测试 telnet $MYSQL_HOST $MYSQL_PORT ..."
    timeout 5 bash -c "echo '' | telnet $MYSQL_HOST $MYSQL_PORT" 2>&1 | head -5
elif command -v nc &> /dev/null; then
    echo "测试 nc -zv $MYSQL_HOST $MYSQL_PORT ..."
    nc -zv $MYSQL_HOST $MYSQL_PORT 2>&1
else
    echo "⚠️  telnet 和 nc 都未安装，跳过端口测试"
fi

echo ""

# 测试3: 尝试连接MySQL
echo "3. 尝试连接 MySQL 数据库..."
MYSQL_CONNECT_TEST=$(mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" -e "SELECT 1 as test;" 2>&1)

if echo "$MYSQL_CONNECT_TEST" | grep -q "test"; then
    echo "✅ MySQL 连接成功!"
    echo ""
    echo "📊 连接信息:"
    mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" -e "SELECT VERSION() as version, DATABASE() as current_db;" 2>/dev/null
    echo ""
    echo "📋 数据库列表:"
    mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" -e "SHOW DATABASES;" 2>/dev/null
else
    echo "❌ MySQL 连接失败!"
    echo ""
    echo "错误信息:"
    echo "$MYSQL_CONNECT_TEST"
    echo ""

    echo "可能的原因:"
    echo "1. MySQL 服务未启动"
    echo "2. 防火墙阻止了端口 3306"
    echo "3. MySQL 未配置允许远程连接"
    echo "4. 用户名或密码错误"
    echo "5. 数据库不存在"
    echo ""

    echo "排查步骤:"
    echo "1. 检查 MySQL 服务状态:"
    echo "   systemctl status mysqld"
    echo ""
    echo "2. 检查 MySQL 监听端口:"
    echo "   netstat -tulpn | grep 3306"
    echo ""
    echo "3. 检查防火墙规则:"
    echo "   firewall-cmd --list-ports"
    echo ""
    echo "4. 检查 MySQL 远程访问配置:"
    echo "   mysql -u root -p"
    echo "   SELECT host, user FROM mysql.user WHERE user='$MYSQL_USER';"
    exit 1
fi

echo ""

# 测试4: 检查cretas数据库
echo "4. 检查 cretas 数据库..."
DB_EXISTS=$(mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" -e "USE cretas; SELECT 'exists' as result;" 2>&1)

if echo "$DB_EXISTS" | grep -q "exists"; then
    echo "✅ cretas 数据库存在"
    echo ""
    echo "📊 数据库表列表:"
    mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -e "SHOW TABLES;" 2>/dev/null
else
    echo "❌ cretas 数据库不存在或无法访问"
    echo ""
    echo "创建数据库命令:"
    echo "   CREATE DATABASE cretas CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
fi

echo ""
echo "======================================"
echo "  测试完成"
echo "======================================"
