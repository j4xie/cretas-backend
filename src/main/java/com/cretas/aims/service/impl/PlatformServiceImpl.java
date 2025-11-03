package com.cretas.aims.service.impl;

import com.cretas.aims.dto.platform.FactoryAIQuotaDTO;
import com.cretas.aims.dto.platform.PlatformAIUsageStatsDTO;
import com.cretas.aims.entity.Factory;
import com.cretas.aims.exception.BusinessException;
import com.cretas.aims.exception.ResourceNotFoundException;
import com.cretas.aims.repository.AIUsageLogRepository;
import com.cretas.aims.repository.FactoryRepository;
import com.cretas.aims.service.PlatformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 平台管理服务实现
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-11-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformServiceImpl implements PlatformService {

    private final FactoryRepository factoryRepository;
    private final AIUsageLogRepository aiUsageLogRepository;

    @Override
    public List<FactoryAIQuotaDTO> getAllFactoryAIQuotas() {
        log.info("获取所有工厂AI配额");

        List<Factory> factories = factoryRepository.findAll();

        return factories.stream()
                .map(factory -> {
                    Long totalUsage = aiUsageLogRepository.countByFactoryId(factory.getId());
                    return FactoryAIQuotaDTO.builder()
                            .id(factory.getId())
                            .name(factory.getName())
                            .aiWeeklyQuota(factory.getAiWeeklyQuota() != null ? factory.getAiWeeklyQuota() : 50)
                            ._count(FactoryAIQuotaDTO.CountInfo.builder()
                                    .aiUsageLogs(totalUsage)
                                    .build())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FactoryAIQuotaDTO.CountInfo updateFactoryAIQuota(String factoryId, Integer weeklyQuota) {
        log.info("更新工厂AI配额: factoryId={}, weeklyQuota={}", factoryId, weeklyQuota);

        // 验证参数
        if (weeklyQuota == null) {
            throw new BusinessException("配额不能为空");
        }
        if (weeklyQuota < 0 || weeklyQuota > 1000) {
            throw new BusinessException("配额必须在0-1000之间");
        }

        // 查找工厂
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("工厂不存在: " + factoryId));

        // 更新配额
        factory.setAiWeeklyQuota(weeklyQuota);
        factoryRepository.save(factory);

        log.info("工厂AI配额已更新: factoryId={}, 新配额={}", factoryId, weeklyQuota);

        return FactoryAIQuotaDTO.CountInfo.builder()
                .aiUsageLogs(aiUsageLogRepository.countByFactoryId(factoryId))
                .build();
    }

    @Override
    public PlatformAIUsageStatsDTO getPlatformAIUsageStats() {
        log.info("获取平台AI使用统计");

        String currentWeek = getCurrentWeekNumber();
        log.debug("当前周次: {}", currentWeek);

        List<Factory> factories = factoryRepository.findAll();

        long totalUsed = 0;
        List<PlatformAIUsageStatsDTO.FactoryUsageInfo> factoryUsages = new java.util.ArrayList<>();

        for (Factory factory : factories) {
            Long weeklyUsed = aiUsageLogRepository.countByFactoryIdAndWeekNumber(
                    factory.getId(),
                    currentWeek
            );

            Integer quota = factory.getAiWeeklyQuota() != null ? factory.getAiWeeklyQuota() : 50;
            long used = weeklyUsed != null ? weeklyUsed : 0;
            long remaining = Math.max(0, quota - used);

            // 计算使用率（保留2位小数）
            String utilization = "0.00";
            if (quota > 0) {
                BigDecimal rate = BigDecimal.valueOf(used)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(quota), 2, RoundingMode.HALF_UP);
                utilization = rate.toString();
            }

            PlatformAIUsageStatsDTO.FactoryUsageInfo usageInfo = PlatformAIUsageStatsDTO.FactoryUsageInfo.builder()
                    .factoryId(factory.getId())
                    .factoryName(factory.getName())
                    .weeklyQuota(quota)
                    .used(used)
                    .remaining(remaining)
                    .utilization(utilization)
                    .build();

            factoryUsages.add(usageInfo);
            totalUsed += used;
        }

        return PlatformAIUsageStatsDTO.builder()
                .currentWeek(currentWeek)
                .totalUsed(totalUsed)
                .factories(factoryUsages)
                .build();
    }

    @Override
    public String getCurrentWeekNumber() {
        LocalDate now = LocalDate.now();

        // 使用ISO 8601周数计算（周一为一周的开始）
        WeekFields weekFields = WeekFields.of(Locale.CHINA);
        int year = now.get(weekFields.weekBasedYear());
        int week = now.get(weekFields.weekOfWeekBasedYear());

        return String.format("%d-W%02d", year, week);
    }
}
