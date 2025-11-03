#!/bin/bash

# ========================================
# Cretas后端系统重启脚本
# 部署路径: /www/wwwroot/project/
# 服务器: 139.196.165.140
# 端口: 10010
# ========================================

echo "======================================"
echo "  Cretas Backend System 重启脚本"
echo "======================================"
echo ""

# 切换到应用目录
cd /www/wwwroot/project || {
    echo "❌ 错误: 无法进入目录 /www/wwwroot/project"
    exit 1
}

echo "📍 当前目录: $(pwd)"
echo ""

# 查找并停止现有进程
echo "🔍 查找现有Java进程..."
PIDS=$(ps aux | grep cretas-backend-system | grep -v grep | awk '{print $2}')

if [ -n "$PIDS" ]; then
    echo "🛑 停止现有进程: $PIDS"
    echo "$PIDS" | xargs -r kill -9
    sleep 2
    echo "✅ 进程已停止"
else
    echo "ℹ️  未找到运行中的进程"
fi

echo ""

# 检查JAR文件是否存在
if [ ! -f "cretas-backend-system-1.0.0.jar" ]; then
    echo "❌ 错误: JAR文件不存在"
    echo "   期望位置: $(pwd)/cretas-backend-system-1.0.0.jar"
    exit 1
fi

echo "📦 JAR文件: cretas-backend-system-1.0.0.jar"
echo "📊 文件大小: $(du -h cretas-backend-system-1.0.0.jar | awk '{print $1}')"
echo ""

# 启动应用
echo "🚀 启动应用..."
nohup java -jar cretas-backend-system-1.0.0.jar \
    --server.port=10010 \
    > cretas-backend.log 2>&1 &

NEW_PID=$!

# 等待启动
sleep 3

# 验证进程是否运行
if ps -p $NEW_PID > /dev/null 2>&1; then
    echo "✅ 应用启动成功!"
    echo "   PID: $NEW_PID"
    echo "   端口: 10010"
    echo "   日志: $(pwd)/cretas-backend.log"
    echo ""
    echo "📝 查看日志: tail -f $(pwd)/cretas-backend.log"
    echo "🌐 访问地址: http://139.196.165.140:10010"
else
    echo "❌ 应用启动失败!"
    echo "   请检查日志: cat $(pwd)/cretas-backend.log"
    exit 1
fi

echo ""
echo "======================================"
echo "  重启完成"
echo "======================================"
