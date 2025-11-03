#!/bin/bash

# ========================================
# 开放端口 10010 脚本
# 支持 firewalld 和 iptables
# ========================================

echo "======================================"
echo "  开放端口 10010"
echo "======================================"
echo ""

# 检查是否为 root 用户
if [ "$EUID" -ne 0 ]; then
    echo "⚠️  请使用 root 用户运行此脚本"
    echo "   使用命令: sudo bash open-port-10010.sh"
    exit 1
fi

PORT=10010

# 检测系统使用的防火墙
echo "🔍 检测防火墙类型..."

if systemctl is-active --quiet firewalld; then
    echo "✓ 检测到 firewalld"
    echo ""

    # 使用 firewalld
    echo "📝 当前防火墙规则:"
    firewall-cmd --list-ports
    echo ""

    echo "🔓 开放端口 $PORT..."

    # 添加端口
    firewall-cmd --permanent --add-port=${PORT}/tcp

    # 重载防火墙
    firewall-cmd --reload

    echo ""
    echo "✅ 端口开放成功!"
    echo ""
    echo "📋 当前开放的端口:"
    firewall-cmd --list-ports

elif command -v iptables &> /dev/null; then
    echo "✓ 检测到 iptables"
    echo ""

    echo "📝 当前防火墙规则:"
    iptables -L -n | grep ${PORT}
    echo ""

    echo "🔓 开放端口 $PORT..."

    # 添加规则
    iptables -I INPUT -p tcp --dport ${PORT} -j ACCEPT

    # 保存规则
    if command -v iptables-save &> /dev/null; then
        iptables-save > /etc/sysconfig/iptables 2>/dev/null || \
        iptables-save > /etc/iptables/rules.v4 2>/dev/null
        echo "✅ 规则已保存"
    fi

    echo ""
    echo "✅ 端口开放成功!"
    echo ""
    echo "📋 验证规则:"
    iptables -L -n | grep ${PORT}

else
    echo "⚠️  未检测到 firewalld 或 iptables"
    echo ""
    echo "请手动配置防火墙，或检查是否已安装防火墙"
fi

echo ""
echo "======================================"
echo "  配置完成"
echo "======================================"
echo ""
echo "📝 验证端口是否开放:"
echo "   netstat -tulpn | grep ${PORT}"
echo ""
echo "🌐 测试服务访问:"
echo "   curl http://139.196.165.140:${PORT}/actuator/health"
echo ""
