USE cretas;

-- 查看最近的sessions记录
SELECT
    id, user_id, factory_id,
    LEFT(token, 30) as token_preview,
    LEFT(refresh_token, 36) as refresh_token_preview,
    is_revoked, created_at
FROM sessions
ORDER BY created_at DESC
LIMIT 5;
