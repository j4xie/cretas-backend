package com.cretas.aims.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务
 * 用于AI分析结果的缓存，减少重复调用AI服务
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-01-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * AI分析缓存前缀
     */
    private static final String AI_ANALYSIS_PREFIX = "ai:analysis:";

    /**
     * 会话历史缓存前缀
     */
    private static final String SESSION_HISTORY_PREFIX = "ai:session:";

    /**
     * 默认缓存时间：5分钟
     */
    private static final long DEFAULT_CACHE_MINUTES = 5;

    /**
     * 会话缓存时间：30分钟
     */
    private static final long SESSION_CACHE_MINUTES = 30;

    /**
     * 获取AI分析缓存
     *
     * @param factoryId 工厂ID
     * @param batchId   批次ID
     * @return 缓存的分析结果，如果不存在返回null
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getAIAnalysisCache(String factoryId, Long batchId) {
        try {
            String key = buildAIAnalysisKey(factoryId, batchId);
            Object cached = redisTemplate.opsForValue().get(key);

            if (cached != null && cached instanceof Map) {
                log.info("命中AI分析缓存: factoryId={}, batchId={}", factoryId, batchId);
                return (Map<String, Object>) cached;
            }

            log.debug("未命中AI分析缓存: factoryId={}, batchId={}", factoryId, batchId);
            return null;
        } catch (Exception e) {
            log.warn("获取AI分析缓存失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 保存AI分析缓存
     *
     * @param factoryId 工厂ID
     * @param batchId   批次ID
     * @param result    分析结果
     */
    public void setAIAnalysisCache(String factoryId, Long batchId, Map<String, Object> result) {
        try {
            String key = buildAIAnalysisKey(factoryId, batchId);
            redisTemplate.opsForValue().set(key, result, DEFAULT_CACHE_MINUTES, TimeUnit.MINUTES);
            log.info("保存AI分析缓存: factoryId={}, batchId={}, ttl={}分钟",
                    factoryId, batchId, DEFAULT_CACHE_MINUTES);
        } catch (Exception e) {
            log.warn("保存AI分析缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 获取会话历史
     *
     * @param sessionId 会话ID
     * @return 会话历史，如果不存在返回null
     */
    public Object getSessionHistory(String sessionId) {
        try {
            String key = buildSessionKey(sessionId);
            Object history = redisTemplate.opsForValue().get(key);

            if (history != null) {
                log.info("获取会话历史: sessionId={}", sessionId);
                return history;
            }

            return null;
        } catch (Exception e) {
            log.warn("获取会话历史失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 保存会话历史
     *
     * @param sessionId 会话ID
     * @param history   会话历史
     */
    public void setSessionHistory(String sessionId, Object history) {
        try {
            String key = buildSessionKey(sessionId);
            redisTemplate.opsForValue().set(key, history, SESSION_CACHE_MINUTES, TimeUnit.MINUTES);
            log.info("保存会话历史: sessionId={}, ttl={}分钟", sessionId, SESSION_CACHE_MINUTES);
        } catch (Exception e) {
            log.warn("保存会话历史失败: {}", e.getMessage());
        }
    }

    /**
     * 清除AI分析缓存
     *
     * @param factoryId 工厂ID
     * @param batchId   批次ID
     */
    public void clearAIAnalysisCache(String factoryId, Long batchId) {
        try {
            String key = buildAIAnalysisKey(factoryId, batchId);
            redisTemplate.delete(key);
            log.info("清除AI分析缓存: factoryId={}, batchId={}", factoryId, batchId);
        } catch (Exception e) {
            log.warn("清除AI分析缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 清除会话历史
     *
     * @param sessionId 会话ID
     */
    public void clearSessionHistory(String sessionId) {
        try {
            String key = buildSessionKey(sessionId);
            redisTemplate.delete(key);
            log.info("清除会话历史: sessionId={}", sessionId);
        } catch (Exception e) {
            log.warn("清除会话历史失败: {}", e.getMessage());
        }
    }

    /**
     * 构建AI分析缓存Key
     */
    private String buildAIAnalysisKey(String factoryId, Long batchId) {
        return AI_ANALYSIS_PREFIX + factoryId + ":" + batchId;
    }

    /**
     * 构建会话历史Key
     */
    private String buildSessionKey(String sessionId) {
        return SESSION_HISTORY_PREFIX + sessionId;
    }

    /**
     * 检查Redis连接状态
     *
     * @return true表示连接正常
     */
    public boolean isRedisAvailable() {
        try {
            redisTemplate.opsForValue().get("health_check");
            return true;
        } catch (Exception e) {
            log.warn("Redis连接异常: {}", e.getMessage());
            return false;
        }
    }
}
