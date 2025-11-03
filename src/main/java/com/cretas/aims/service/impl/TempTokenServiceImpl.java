package com.cretas.aims.service.impl;

import com.cretas.aims.service.TempTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
/**
 * 临时令牌服务实现
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TempTokenServiceImpl implements TempTokenService {
    private static final String TEMP_TOKEN_PREFIX = "temp_token:";
    private final StringRedisTemplate redisTemplate;
    @Override
    public String generateTempToken(String phoneNumber, int durationMinutes) {
        // 生成唯一令牌
        String token = "temp_" + UUID.randomUUID().toString().replace("-", "");
        String key = TEMP_TOKEN_PREFIX + token;
        // 存储到Redis，设置过期时间
        redisTemplate.opsForValue().set(key, phoneNumber, durationMinutes, TimeUnit.MINUTES);
        log.debug("生成临时令牌: token={}, phone={}, duration={}min", token, phoneNumber, durationMinutes);
        return token;
    }
    public String validateAndGetPhone(String tempToken) {
        if (tempToken == null || tempToken.isEmpty()) {
            return null;
        }
        String key = TEMP_TOKEN_PREFIX + tempToken;
        String phoneNumber = redisTemplate.opsForValue().get(key);
        if (phoneNumber != null) {
            log.debug("临时令牌验证成功: token={}, phone={}", tempToken, phoneNumber);
        } else {
            log.warn("临时令牌验证失败: token={}", tempToken);
        }
        return phoneNumber;
    }

    @Override
    public void deleteTempToken(String tempToken) {
        if (tempToken == null || tempToken.isEmpty()) {
            return;
        }
        String key = TEMP_TOKEN_PREFIX + tempToken;
        Boolean deleted = redisTemplate.delete(key);
        log.debug("删除临时令牌: token={}, deleted={}", tempToken, deleted);
    }

    @Override
    public boolean exists(String tempToken) {
        if (tempToken == null || tempToken.isEmpty()) {
            return false;
        }
        String key = TEMP_TOKEN_PREFIX + tempToken;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
}
