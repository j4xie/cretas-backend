#!/bin/bash

# ========================================
# 测试所有账号登录脚本
# 服务器: 139.196.165.140:10010
# 所有密码: 123456
# ========================================

API_URL="http://139.196.165.140:10010/api/auth/login"
PASSWORD="123456"

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo ""
echo "========================================"
echo "  测试所有账号登录功能"
echo "  API: $API_URL"
echo "  统一密码: $PASSWORD"
echo "========================================"
echo ""

# 测试函数
test_login() {
    local username=$1
    local description=$2

    echo -e "${BLUE}测试: $username${NC} - $description"

    response=$(curl -s -X POST "$API_URL" \
        -H "Content-Type: application/json" \
        -d "{\"username\": \"$username\", \"password\": \"$PASSWORD\"}" \
        -w "\nHTTP_CODE:%{http_code}")

    http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_CODE:/d')

    if [ "$http_code" = "200" ]; then
        # 提取accessToken（如果存在）
        token=$(echo "$body" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4 | head -c 20)
        if [ -n "$token" ]; then
            echo -e "${GREEN}✓ 登录成功${NC} (Token: ${token}...)"
        else
            echo -e "${GREEN}✓ 登录成功${NC}"
        fi
        echo "  响应: $(echo "$body" | head -c 200)..."
    else
        echo -e "${RED}✗ 登录失败 (HTTP $http_code)${NC}"
        echo "  错误: $(echo "$body" | head -c 200)"
    fi

    echo ""
}

# ========================================
# 测试平台管理员
# ========================================

echo -e "${YELLOW}=== 平台管理员账号 ===${NC}"
echo ""

test_login "admin" "超级管理员"
test_login "developer" "开发者"
test_login "platform_admin" "平台管理员"

# ========================================
# 测试工厂用户
# ========================================

echo -e "${YELLOW}=== 工厂用户账号 (F001) ===${NC}"
echo ""

test_login "perm_admin" "权限管理员"
test_login "proc_admin" "加工管理员"
test_login "farm_admin" "养殖管理员"
test_login "logi_admin" "物流管理员"
test_login "proc_user" "加工操作员"

# ========================================
# 统计结果
# ========================================

echo "========================================"
echo -e "${GREEN}测试完成！${NC}"
echo "========================================"
echo ""
echo "📝 说明："
echo "  - 所有账号密码统一为: 123456"
echo "  - 工厂用户属于工厂: F001"
echo "  - 登录不需要传入 factoryId 参数"
echo ""
echo "如需查看完整响应，可以单独测试某个账号："
echo "  curl -X POST $API_URL \\"
echo "    -H \"Content-Type: application/json\" \\"
echo "    -d '{\"username\": \"admin\", \"password\": \"123456\"}' | jq"
echo ""
