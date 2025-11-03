#!/bin/bash
# AIMS后端服务重启脚本

echo "=== 重启AIMS后端服务 ==="
echo ""

# 停止服务
/opt/aims/stop.sh

# 等待3秒
echo ""
echo "等待3秒..."
sleep 3

# 启动服务
/opt/aims/start.sh
