#!/bin/bash
# AIMS后端服务启动脚本

APP_NAME="cretas-backend-system"
APP_VERSION="1.0.0"
APP_JAR="/opt/aims/${APP_NAME}-${APP_VERSION}.jar"
LOG_DIR="/opt/aims/logs"
PID_FILE="/opt/aims/${APP_NAME}.pid"

# 确保日志目录存在
mkdir -p ${LOG_DIR}

# 检查是否已经在运行
if [ -f "$PID_FILE" ]; then
    PID=$(cat $PID_FILE)
    if ps -p $PID > /dev/null 2>&1; then
        echo "❌ 应用已经在运行 (PID: $PID)"
        exit 1
    else
        echo "⚠️  删除过期的PID文件"
        rm -f $PID_FILE
    fi
fi

# 启动应用
echo "🚀 启动 ${APP_NAME}..."
nohup java -jar \
    -Xms512m \
    -Xmx1024m \
    -Dserver.port=10010 \
    -Dspring.profiles.active=prod \
    ${APP_JAR} \
    > ${LOG_DIR}/application.log 2>&1 &

# 保存PID
echo $! > ${PID_FILE}
echo "✅ 应用已启动 (PID: $(cat $PID_FILE))"
echo "📋 日志文件: ${LOG_DIR}/application.log"
echo ""
echo "查看日志: tail -f ${LOG_DIR}/application.log"
echo "停止服务: /opt/aims/stop.sh"
