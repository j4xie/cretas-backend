#!/bin/bash
echo "=== 重启后端服务 ==="
echo ""

# 1. 停止旧服务
echo "1. 停止旧服务..."
ps aux | grep "java.*10010" | grep -v grep | awk '{print $2}' | xargs kill 2>/dev/null
sleep 3

# 2. 启动新服务
echo "2. 启动新服务..."
cd ~/Downloads/cretas-backend-system-main
nohup java -jar target/cretas-backend-system-1.0.0.jar --server.port=10010 > logs/cretas-backend.log 2>&1 &
echo "服务启动中... PID: $!"

# 3. 等待服务启动
echo "3. 等待服务启动..."
sleep 10

# 4. 检查服务状态
echo "4. 检查服务状态..."
lsof -i :10010

echo ""
echo "✅ 重启完成"
