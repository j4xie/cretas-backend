-- 添加测试手机号到白名单
USE cretas;

INSERT INTO whitelist (factory_id, phone_number, name, department, position, status, expires_at, usage_count, max_usage_count, added_by, notes, created_at, updated_at)
VALUES
('F001', '13800001111', '测试用户A', '研发部', '工程师', 'ACTIVE', DATE_ADD(NOW(), INTERVAL 1 YEAR), 0, NULL, 2, '验证码注册测试账号', NOW(), NOW()),
('F002', '13800002222', '测试用户B', '技术部', '测试员', 'ACTIVE', DATE_ADD(NOW(), INTERVAL 1 YEAR), 0, NULL, 2, '验证码注册测试账号', NOW(), NOW());

SELECT '✅ 测试手机号已添加到白名单' AS message;
SELECT phone_number, factory_id, status, expires_at FROM whitelist WHERE phone_number IN ('13800001111', '13800002222');
