package com.fjq.login.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync // 1. 开启异步任务支持
public class AsyncConfig {

    @Bean(name = "mailExecutor")
    public Executor mailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：线程池空闲时保持的线程数
        executor.setCorePoolSize(5);
        // 最大线程数：线程池允许的最大线程数
        executor.setMaxPoolSize(10);
        // 缓冲队列：用来缓冲等待执行任务的队列长度
        executor.setQueueCapacity(100);
        // 线程存活时间：超过核心线程数的物理线程空闲多长时间后被销毁
        executor.setKeepAliveSeconds(60);
        // 线程名前缀：方便在日志中定位问题
        executor.setThreadNamePrefix("mail-async-");

        // 拒绝策略：当队列和最大线程都满了，由调用方主线程处理（保证邮件不丢失）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}
