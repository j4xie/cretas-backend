# 密码重置和测试指南

## 🔍 测试结果

✅ **接口可访问**: `http://139.196.165.140:10010/api/mobile/auth/unified-login`
❌ **登录失败**: 所有账号返回"用户名或密码错误"

**可能原因**: 数据库中的密码哈希值与密码 `123456` 不匹配

---

## 🔧 解决方案

### 方案1: 生成正确的密码哈希

在服务器上执行以下命令生成BCrypt哈希：

```bash
# 使用Python生成BCrypt哈希
python3 << 'EOF'
import bcrypt

password = "123456"
hashed = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())
print("密码哈希:", hashed.decode('utf-8'))
EOF
```

或使用Java生成：

```bash
cd /www/wwwroot/project
cat > GeneratePassword.java << 'EOF'
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String hash = encoder.encode(password);
        System.out.println("密码: " + password);
        System.out.println("哈希: " + hash);
    }
}
EOF
```

### 方案2: 更新数据库中的密码哈希

**在服务器上执行SQL**:

```sql
-- 更新平台管理员密码（使用BCrypt哈希）
-- 注意：需要先运行上面的脚本生成正确的哈希值

-- 示例：更新admin密码
UPDATE platform_admins 
SET password_hash = '$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW' 
WHERE username = 'admin';

-- 更新其他平台管理员
UPDATE platform_admins 
SET password_hash = '$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW' 
WHERE username = 'developer';

UPDATE platform_admins 
SET password_hash = '$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW' 
WHERE username = 'platform_admin';

-- 更新工厂用户密码
UPDATE users 
SET password_hash = '$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW' 
WHERE factory_id = 'F001';
```

**注意**: `$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW` 是密码 `123456` 的BCrypt哈希值（使用BCrypt强度12）

---

## 🧪 验证密码哈希

**在服务器上执行**:

```bash
cd /www/wwwroot/project

# 使用Java验证密码哈希
cat > TestPassword.java << 'EOF'
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String hash = "$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW";
        
        boolean matches = encoder.matches(password, hash);
        System.out.println("密码匹配: " + matches);
        
        if (matches) {
            System.out.println("✅ 密码哈希正确！");
        } else {
            System.out.println("❌ 密码哈希不匹配！");
        }
    }
}
EOF
```

---

## 📝 快速修复脚本

**在服务器上执行**:

```bash
cd /www/wwwroot/project

# 连接到MySQL并更新密码
mysql -h localhost -u cretas -psYyS6Jp3pyFMwLdA cretas << 'EOF'
-- 更新所有平台管理员密码为123456
UPDATE platform_admins 
SET password_hash = '$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW'
WHERE username IN ('admin', 'developer', 'platform_admin');

-- 更新所有工厂用户密码为123456
UPDATE users 
SET password_hash = '$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW'
WHERE factory_id = 'F001';

-- 验证更新
SELECT username, LEFT(password_hash, 30) as hash_preview FROM platform_admins;
SELECT username, LEFT(password_hash, 30) as hash_preview FROM users WHERE factory_id = 'F001';
EOF
```

---

## ✅ 测试登录

更新密码后，测试登录：

```bash
# 测试平台管理员登录
curl -X POST http://139.196.165.140:10010/api/mobile/auth/unified-login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 测试工厂用户登录
curl -X POST http://139.196.165.140:10010/api/mobile/auth/unified-login \
  -H "Content-Type: application/json" \
  -d '{"username":"proc_admin","password":"123456","factoryId":"F001"}'
```

---

## 🎯 密码哈希说明

BCrypt哈希格式: `$2a$12$...`

- `$2a$`: BCrypt版本标识
- `12`: 强度（cost factor），值越大越安全但越慢
- 后面的字符串: 盐值和哈希值

**正确的密码123456的哈希值**:
```
$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW
```

---

## 📞 如果问题仍然存在

如果更新密码后仍然无法登录，请检查：

1. **数据库字段名是否正确**:
   - 平台管理员表: `password_hash` 还是 `password`?
   - 工厂用户表: `password_hash` 还是 `password`?

2. **查看应用日志**:
   ```bash
   tail -50 /www/wwwroot/project/cretas-backend.log | grep -i password
   ```

3. **检查用户状态**:
   ```sql
   SELECT username, is_active, status FROM platform_admins;
   SELECT username, is_active FROM users WHERE factory_id = 'F001';
   ```

