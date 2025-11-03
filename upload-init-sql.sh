#!/bin/bash

# ========================================
# 上传数据库初始化脚本
# ========================================

echo "======================================"
echo "  上传数据库初始化 SQL"
echo "======================================"
echo ""

# 宝塔API配置
BT_PANEL="https://139.196.165.140:17400"
API_KEY="Fw3rqkRqAashK9uNDsFxvst31YSbBmUb"
TARGET_PATH="/www/wwwroot/project"
SQL_FILE="fix-document/init-final-users.sql"

# 检查SQL文件是否存在
if [ ! -f "$SQL_FILE" ]; then
    echo "❌ 错误: SQL文件不存在: $SQL_FILE"
    exit 1
fi

echo "📁 SQL文件: $SQL_FILE"
echo "🎯 目标路径: $TARGET_PATH"
echo ""

# 生成API签名
echo "🔐 生成API签名..."
TIME_TOKEN=$(python3 << 'PYTHON_EOF'
import hashlib
import time

api_sk = "Fw3rqkRqAashK9uNDsFxvst31YSbBmUb"
request_time = str(int(time.time()))
md5_api_sk = hashlib.md5(api_sk.encode()).hexdigest()
request_token = hashlib.md5((request_time + md5_api_sk).encode()).hexdigest()
print(f"{request_time}|{request_token}")
PYTHON_EOF
)

REQUEST_TIME=$(echo $TIME_TOKEN | cut -d'|' -f1)
REQUEST_TOKEN=$(echo $TIME_TOKEN | cut -d'|' -f2)

echo "✅ 签名生成成功"
echo ""

# 获取文件大小
FILE_SIZE=$(stat -f%z "$SQL_FILE" 2>/dev/null || stat -c%s "$SQL_FILE" 2>/dev/null)
echo "📊 文件大小: $FILE_SIZE bytes"
echo ""

# 上传文件
echo "📤 上传文件..."
UPLOAD_RESPONSE=$(curl -k -s -X POST "${BT_PANEL}/files?action=upload" \
  -F "request_time=$REQUEST_TIME" \
  -F "request_token=$REQUEST_TOKEN" \
  -F "f_path=$TARGET_PATH" \
  -F "f_name=init-final-users.sql" \
  -F "f_size=$FILE_SIZE" \
  -F "f_start=0" \
  -F "blob=@$SQL_FILE")

echo "响应: $UPLOAD_RESPONSE"
echo ""

if echo "$UPLOAD_RESPONSE" | grep -q '"status":\s*true'; then
    echo "✅ SQL文件上传成功"
else
    echo "❌ SQL文件上传失败"
    exit 1
fi

echo ""
echo "======================================"
echo "  上传完成"
echo "======================================"
echo ""
echo "📝 SQL文件说明:"
echo "   - 所有账号密码: 123456"
echo "   - 平台管理员: admin, developer, platform_admin"
echo "   - 工厂用户: perm_admin, proc_admin, farm_admin, logi_admin, proc_user"
echo "   - 工厂ID: F001 (测试工厂)"
echo ""
echo "📝 下一步操作:"
echo ""
echo "1. 在服务器上执行SQL:"
echo "   cd /www/wwwroot/project"
echo "   mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas < init-final-users.sql"
echo ""
echo "2. 验证数据:"
echo "   mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas -e \"SELECT username, real_name FROM platform_admins;\""
echo ""
echo "3. 重启应用:"
echo "   bash restart.sh"
echo ""
echo "4. 测试登录:"
echo "   curl -X POST http://139.196.165.140:10010/api/auth/login \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{\"username\":\"admin\",\"password\":\"123456\"}'"
echo ""
