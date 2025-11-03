#!/bin/bash
# 一键修复生产环境问题
# 使用方法: bash fix-all-production.sh

set -e

echo "======================================"
echo "开始修复生产环境问题"
echo "======================================"
echo ""

DB_HOST="47.251.121.76"
DB_PORT="3307"
DB_USER="root"
DB_PASS="Zyh123456"
DB_NAME="cretas"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}步骤 1: 修复 sessions 表${NC}"
echo "-------------------------------------"

mysql -h ${DB_HOST} -P ${DB_PORT} -u ${DB_USER} -p${DB_PASS} ${DB_NAME} << 'EOF'

SET @query = (
    SELECT CONCAT('ALTER TABLE sessions DROP FOREIGN KEY ', CONSTRAINT_NAME, ';')
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = 'cretas'
      AND TABLE_NAME = 'sessions'
      AND COLUMN_NAME = 'factory_id'
      AND CONSTRAINT_NAME != 'PRIMARY'
    LIMIT 1
);

SET @query = IFNULL(@query, 'SELECT "No foreign key to drop" AS info;');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE sessions MODIFY COLUMN factory_id VARCHAR(50) NULL;

ALTER TABLE sessions
ADD CONSTRAINT FK_sessions_factory_id
FOREIGN KEY (factory_id) REFERENCES factories(id);

SELECT 'Sessions表修复完成' AS status;

EOF

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Sessions 表修复成功${NC}"
else
    echo -e "${RED}❌ Sessions 表修复失败${NC}"
    exit 1
fi

echo ""
echo -e "${YELLOW}步骤 2: 检查 Redis 连接${NC}"
echo "-------------------------------------"

if command -v redis-cli > /dev/null 2>&1; then
    redis-cli ping > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Redis 连接正常${NC}"
    else
        echo -e "${RED}❌ Redis 连接失败${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  redis-cli 未安装${NC}"
fi

echo ""
echo -e "${YELLOW}步骤 3: 停止旧的应用进程${NC}"
echo "-------------------------------------"

OLD_PID=$(ps -ef | grep 'cretas-backend-system' | grep -v grep | awk '{print $2}')
if [ ! -z "$OLD_PID" ]; then
    echo "找到旧进程: $OLD_PID"
    kill -9 $OLD_PID
    sleep 2
    echo -e "${GREEN}✅ 旧进程已停止${NC}"
else
    echo "未找到运行中的进程"
fi

echo ""
echo -e "${YELLOW}步骤 4: 启动新的应用${NC}"
echo "-------------------------------------"

nohup java -jar -Xms512m -Xmx1024m -Dserver.port=10010 cretas-backend-system-1.0.0.jar > nohup.out 2>&1 &

NEW_PID=$!
echo "应用已启动，PID: $NEW_PID"
echo -e "${GREEN}✅ 应用启动成功${NC}"

echo ""
echo -e "${YELLOW}步骤 5: 等待服务启动 (30秒)${NC}"
echo "-------------------------------------"

sleep 30

echo ""
echo -e "${YELLOW}步骤 6: 测试关键接口${NC}"
echo "-------------------------------------"

echo "测试 1: 平台管理员登录..."
RESPONSE=$(curl -s -X POST "http://localhost:10010/api/auth/platform-login" -H "Content-Type: application/json" -d '{"username":"platform_admin","password":"123456"}')

if echo "$RESPONSE" | grep -q '"success":true'; then
    echo -e "${GREEN}✅ 平台管理员登录成功${NC}"
    REFRESH_TOKEN=$(echo "$RESPONSE" | grep -o '"refreshToken":"[^"]*"' | cut -d'"' -f4)
else
    echo -e "${RED}❌ 平台管理员登录失败${NC}"
fi

if [ ! -z "$REFRESH_TOKEN" ]; then
    echo ""
    echo "测试 2: Token 刷新..."
    REFRESH_RESPONSE=$(curl -s -X POST "http://localhost:10010/api/auth/refresh" -H "Content-Type: application/json" -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}")
    
    if echo "$REFRESH_RESPONSE" | grep -q '"success":true'; then
        echo -e "${GREEN}✅ Token 刷新成功${NC}"
    else
        echo -e "${RED}❌ Token 刷新失败${NC}"
    fi
fi

echo ""
echo "测试 3: 验证码发送..."
CODE_RESPONSE=$(curl -s -X POST "http://localhost:10010/api/auth/send-code" -H "Content-Type: application/json" -d '{"phoneNumber":"13800001111","factoryId":"F001"}')

if echo "$CODE_RESPONSE" | grep -q '"success":true'; then
    echo -e "${GREEN}✅ 验证码发送成功${NC}"
else
    echo -e "${RED}❌ 验证码发送失败${NC}"
fi

echo ""
echo "======================================"
echo -e "${GREEN}修复流程完成！${NC}"
echo "======================================"
echo ""
echo "应用进程 PID: $NEW_PID"
echo "查看日志: tail -f /opt/aims/nohup.out"
