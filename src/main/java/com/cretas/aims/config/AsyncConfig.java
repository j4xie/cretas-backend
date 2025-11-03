package com.cretas.aims.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 *
 * @author Cretas Team
 * @version 1.0.0
 * @since 2025-11-02
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Dashboard统计专用线程池
     * 用于并行计算Dashboard的各个统计模块
     */
    @Bean(name = "dashboardExecutor")
    public Executor dashboardExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数：CPU核心数
        int processors = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(processors);

        // 最大线程数：CPU核心数 * 2
        executor.setMaxPoolSize(processors * 2);

        // 队列容量
        executor.setQueueCapacity(100);

        // 线程名称前缀
        executor.setThreadNamePrefix("dashboard-async-");

        // 线程池关闭时等待任务完成
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间
        executor.setAwaitTerminationSeconds(60);

        // 拒绝策略：调用者运行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 异常处理
        executor.setTaskDecorator(runnable -> () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                log.error("Dashboard异步任务执行失败", e);
            }
        });

        executor.initialize();

        log.info("Dashboard线程池初始化完成: corePoolSize={}, maxPoolSize={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());

        return executor;
    }

    /**
     * 通用异步任务线程池
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int processors = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(processors);
        executor.setMaxPoolSize(processors * 2);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("async-task-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();

        log.info("通用线程池初始化完成: corePoolSize={}, maxPoolSize={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());

        return executor;
    }
}
