#!/bin/bash
# AIMS后端服务停止脚本

APP_NAME="cretas-backend-system"
PID_FILE="/opt/aims/${APP_NAME}.pid"

if [ ! -f "$PID_FILE" ]; then
    echo "❌ PID文件不存在，尝试通过端口停止..."
    lsof -ti:10010 | xargs kill -15 2>/dev/null && echo "✅ 已停止服务" || echo "ℹ️ 没有运行中的服务"
    exit 0
fi

PID=$(cat $PID_FILE)

if ! ps -p $PID > /dev/null 2>&1; then
    echo "⚠️  进程不存在，删除PID文件"
    rm -f $PID_FILE
    exit 0
fi

echo "🛑 停止 ${APP_NAME} (PID: $PID)..."
kill -15 $PID

# 等待进程优雅关闭
for i in {1..30}; do
    if ! ps -p $PID > /dev/null 2>&1; then
        echo "✅ 应用已停止"
        rm -f $PID_FILE
        exit 0
    fi
    echo -n "."
    sleep 1
done

echo ""
# 如果30秒后还没停止，强制kill
echo "⚠️  强制停止应用"
kill -9 $PID
rm -f $PID_FILE
echo "✅ 应用已强制停止"
