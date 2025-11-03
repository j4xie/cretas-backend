#!/bin/bash

# ========================================
# Java 17 自动安装脚本
# 适用于 CentOS/AlmaLinux/Rocky Linux
# ========================================

echo "======================================"
echo "  Java 17 安装脚本"
echo "======================================"
echo ""

# 检查是否为 root 用户
if [ "$EUID" -ne 0 ]; then
    echo "⚠️  请使用 root 用户运行此脚本"
    echo "   使用命令: sudo bash install-java.sh"
    exit 1
fi

echo "🔍 检查现有 Java 安装..."
if command -v java &> /dev/null; then
    CURRENT_VERSION=$(java -version 2>&1 | head -n 1)
    echo "✓ 已安装: $CURRENT_VERSION"

    # 检查版本是否满足要求 (17+)
    VERSION_NUM=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}' | awk -F '.' '{print $1}')
    if [ "$VERSION_NUM" -ge 17 ]; then
        echo "✅ Java 版本满足要求 (>= 17)"
        echo ""
        echo "Java 路径: $(which java)"
        echo "JAVA_HOME: ${JAVA_HOME:-未设置}"
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

# 检测系统类型
if [ -f /etc/os-release ]; then
    . /etc/os-release
    OS=$ID
    VERSION=$VERSION_ID
else
    echo "❌ 无法检测系统类型"
    exit 1
fi

echo "系统信息: $OS $VERSION"
echo ""

# 根据系统类型安装
case $OS in
    centos|almalinux|rocky|alinux|anolis)
        echo "使用 YUM 安装 Java 17..."
        yum install -y java-17-openjdk java-17-openjdk-devel
        ;;
    ubuntu|debian)
        echo "使用 APT 安装 Java 17..."
        apt update
        apt install -y openjdk-17-jdk openjdk-17-jre
        ;;
    *)
        echo "❌ 不支持的系统: $OS"
        echo "   尝试使用 YUM 安装..."
        yum install -y java-17-openjdk java-17-openjdk-devel
        ;;
esac

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
    if ! grep -q "JAVA_HOME" /etc/profile; then
        echo "" >> /etc/profile
        echo "# Java Environment" >> /etc/profile
        echo "export JAVA_HOME=$JAVA_HOME_PATH" >> /etc/profile
        echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> /etc/profile
        echo "✅ 已添加 JAVA_HOME 到 /etc/profile"
    else
        echo "ℹ️  JAVA_HOME 已存在于 /etc/profile"
    fi

    # 使环境变量生效
    source /etc/profile

    echo ""
    echo "======================================"
    echo "  安装完成!"
    echo "======================================"
    echo ""
    echo "📝 后续步骤:"
    echo "   1. 重启终端或执行: source /etc/profile"
    echo "   2. 验证: java -version"
    echo "   3. 运行应用: cd /www/wwwroot/project && bash restart.sh"
    echo ""

else
    echo "❌ 安装失败，请检查错误信息"
    exit 1
fi
