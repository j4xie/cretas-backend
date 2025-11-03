#!/bin/bash
# 生成BCrypt密码哈希的脚本

# 创建临时Java文件
cat > /tmp/PasswordGenerator.java << 'EOF'
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java PasswordGenerator <password>");
            return;
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode(args[0]);
        System.out.println("Password: " + args[0]);
        System.out.println("BCrypt Hash: " + encoded);
    }
}
EOF

echo "密码哈希生成脚本已创建在 /tmp/PasswordGenerator.java"
echo "使用方法："
echo "1. 编译: javac -cp target/classes:/Users/eason/.m2/repository/org/springframework/security/spring-security-crypto/5.7.5/spring-security-crypto-5.7.5.jar /tmp/PasswordGenerator.java"
echo "2. 运行: java -cp /tmp:target/classes:/Users/eason/.m2/repository/org/springframework/security/spring-security-crypto/5.7.5/spring-security-crypto-5.7.5.jar PasswordGenerator admin123"
