#!/bin/bash

# 检查数据库表是否存在的脚本

echo "======================================"
echo "检查数据库表"
echo "======================================"
echo ""

echo "1. 检查所有表:"
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas -e "SHOW TABLES;"

echo ""
echo "2. 检查关键表是否存在:"
for table in factories users platform_admins sessions whitelist; do
    COUNT=$(mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='cretas' AND table_name='$table';")
    if [ "$COUNT" -eq "1" ]; then
        echo "✓ $table 表存在"
    else
        echo "✗ $table 表不存在"
    fi
done

echo ""
echo "======================================"
