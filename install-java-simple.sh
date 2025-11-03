#!/bin/bash

# ========================================
# Java 17 简化安装脚本
# 适用于所有基于 YUM 的系统
# ========================================

echo "======================================"
echo "  Java 17 安装脚本 (通用版)"
echo "======================================"
echo ""

# 检查是否为 root 用户
if [ "$EUID" -ne 0 ]; then
    echo "⚠️  请使用 root 用户运行此脚本"
    echo "   使用命令: sudo bash install-java-simple.sh"
    exit 1
fi

echo "🔍 检查现有 Java 安装..."
if command -v java &> /dev/null; then
    CURRENT_VERSION=$(java -version 2>&1 | head -n 1)
    echo "✓ 已安装: $CURRENT_VERSION"

    # 检查版本是否满足要求 (17+)
    VERSION_NUM=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}' | awk -F '.' '{print $1}')
    if [ "$VERSION_NUM" -ge 17 ] 2>/dev/null; then
        echo "✅ Java 版本满足要求 (>= 17)"
        echo ""
        echo "Java 路径: $(which java)"
        echo "JAVA_HOME: ${JAVA_HOME:-未设置}"

        # 配置 JAVA_HOME
        JAVA_PATH=$(which java)
        JAVA_REAL_PATH=$(readlink -f $JAVA_PATH)
        JAVA_HOME_PATH=$(dirname $(dirname $JAVA_REAL_PATH))

        if [ -z "$JAVA_HOME" ]; then
            echo ""
            echo "🔧 配置 JAVA_HOME..."
            if ! grep -q "JAVA_HOME" /etc/profile 2>/dev/null; then
                echo "" >> /etc/profile
                echo "# Java Environment" >> /etc/profile
                echo "export JAVA_HOME=$JAVA_HOME_PATH" >> /etc/profile
                echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> /etc/profile
                echo "✅ 已添加 JAVA_HOME 到 /etc/profile"
                echo "   请执行: source /etc/profile"
            fi
        fi

        exit 0
    else
        echo "⚠️  当前版本过低，需要 Java 17+"
        echo "   将安装 Java 17"
    fi
else
    echo "ℹ️  未检测到 Java 安装"
fi

echo ""
echo "📦 开始安装 Java 17..."
echo ""

# 尝试使用 YUM 安装
echo "使用 YUM 安装 Java 17 OpenJDK..."
echo ""

# 先尝试安装
yum install -y java-17-openjdk java-17-openjdk-devel

# 检查安装结果
echo ""
echo "🔍 验证安装..."
if command -v java &> /dev/null; then
    echo "✅ Java 安装成功!"
    echo ""

    # 显示版本信息
    java -version
    echo ""

    # 显示路径信息
    JAVA_PATH=$(which java)
    JAVA_REAL_PATH=$(readlink -f $JAVA_PATH)
    JAVA_HOME_PATH=$(dirname $(dirname $JAVA_REAL_PATH))

    echo "Java 路径: $JAVA_PATH"
    echo "实际路径: $JAVA_REAL_PATH"
    echo "建议 JAVA_HOME: $JAVA_HOME_PATH"
    echo ""

    # 配置环境变量
    echo "🔧 配置环境变量..."

    # 添加到 /etc/profile
    if ! grep -q "JAVA_HOME" /etc/profile 2>/dev/null; then
        echo "" >> /etc/profile
        echo "# Java Environment" >> /etc/profile
        echo "export JAVA_HOME=$JAVA_HOME_PATH" >> /etc/profile
        echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> /etc/profile
        echo "✅ 已添加 JAVA_HOME 到 /etc/profile"
    else
        echo "ℹ️  JAVA_HOME 已存在于 /etc/profile"
    fi

    # 使环境变量生效
    source /etc/profile 2>/dev/null || true

    echo ""
    echo "======================================"
    echo "  安装完成!"
    echo "======================================"
    echo ""
    echo "📝 后续步骤:"
    echo "   1. 重新登录终端或执行: source /etc/profile"
    echo "   2. 验证: java -version"
    echo "   3. 运行应用: cd /www/wwwroot/project && bash restart.sh"
    echo ""

else
    echo "❌ 安装失败"
    echo ""
    echo "可能的原因:"
    echo "1. YUM 源中没有 Java 17 包"
    echo "2. 网络连接问题"
    echo ""
    echo "解决方案:"
    echo "1. 检查 YUM 源配置: yum repolist"
    echo "2. 更新 YUM 缓存: yum clean all && yum makecache"
    echo "3. 查找可用的 Java 版本: yum search openjdk"
    echo "4. 或手动下载安装: https://jdk.java.net/17/"
    exit 1
fi
