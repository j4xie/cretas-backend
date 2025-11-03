#!/bin/bash

# ========================================
# 上传简化版 Java 安装脚本
# ========================================

echo "======================================"
echo "  上传简化版 Java 安装脚本"
echo "======================================"
echo ""

# 宝塔API配置
BT_PANEL="https://139.196.165.140:17400"
API_KEY="Fw3rqkRqAashK9uNDsFxvst31YSbBmUb"
TARGET_PATH="/www/wwwroot/project"
SCRIPT_FILE="install-java-simple.sh"

# 检查本地脚本是否存在
if [ ! -f "$SCRIPT_FILE" ]; then
    echo "❌ 错误: 本地脚本文件不存在: $SCRIPT_FILE"
    exit 1
fi

echo "📁 本地脚本: $SCRIPT_FILE"
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
FILE_SIZE=$(stat -f%z "$SCRIPT_FILE" 2>/dev/null || stat -c%s "$SCRIPT_FILE" 2>/dev/null)
echo "📊 文件大小: $FILE_SIZE bytes"
echo ""

# 上传文件
echo "📤 上传文件..."
UPLOAD_RESPONSE=$(curl -k -s -X POST "${BT_PANEL}/files?action=upload" \
  -F "request_time=$REQUEST_TIME" \
  -F "request_token=$REQUEST_TOKEN" \
  -F "f_path=$TARGET_PATH" \
  -F "f_name=$SCRIPT_FILE" \
  -F "f_size=$FILE_SIZE" \
  -F "f_start=0" \
  -F "blob=@$SCRIPT_FILE")

echo "响应: $UPLOAD_RESPONSE"
echo ""

if echo "$UPLOAD_RESPONSE" | grep -q '"status":\s*true'; then
    echo "✅ 文件上传成功"
else
    echo "❌ 文件上传失败"
    exit 1
fi

echo ""

# 设置执行权限
echo "🔧 设置执行权限..."
PERMISSION_RESPONSE=$(curl -k -s -X POST "${BT_PANEL}/files?action=SetFileAccess" \
  -d "request_time=$REQUEST_TIME" \
  -d "request_token=$REQUEST_TOKEN" \
  -d "filename=${TARGET_PATH}/${SCRIPT_FILE}" \
  -d "user=root" \
  -d "access=755")

echo "响应: $PERMISSION_RESPONSE"
echo ""

if echo "$PERMISSION_RESPONSE" | grep -q '"status":\s*true'; then
    echo "✅ 权限设置成功 (755)"
else
    echo "⚠️  权限设置可能失败，但不影响使用"
fi

echo ""
echo "======================================"
echo "  上传完成"
echo "======================================"
echo ""
echo "📝 请在服务器上执行:"
echo ""
echo "   cd /www/wwwroot/project"
echo "   sudo bash install-java-simple.sh"
echo ""
