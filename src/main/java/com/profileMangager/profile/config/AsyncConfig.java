package com.profileMangager.profile.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "profileExecutor")
    public ThreadPoolTaskExecutor profileExecutor() {

        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();

        ex.setCorePoolSize(4);          // số thread ban đầu
        ex.setMaxPoolSize(8);          // Số thread tối đa
        ex.setQueueCapacity(10);      //số lượng phần tử của queue // ựa vào downtime của DB
        ex.setThreadNamePrefix("async-worker-");
        ex.setKeepAliveSeconds(60);
        ex.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
//quueee nghẽn do db -> xài thread chính k ý nghĩa -> báo lỗi
        // 40p -> báo lỗi -> nêếu xài thread chính -> nghẽn toàn bộ
        // thread chính báo lỗi
        //xài abort
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.setAwaitTerminationSeconds(30);

        ex.initialize();
        return ex;
    }
}
