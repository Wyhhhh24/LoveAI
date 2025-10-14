package com.air.aiagent.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AsyncTaskService {
    /**
     * 执行异步任务
     * 无返回值，异常自动记录日志，不影响主线程
     */
    @Async("asyncTaskExecutor") // 指定使用我们配置的线程池
    public void executeAsyncTask(Runnable task, String taskDis) {
        try {
            log.info("开始执行异步任务: {}", taskDis);
            task.run();
            log.info("异步任务执行完成: {}", taskDis);
        } catch (Exception e) {
            log.error("异步任务执行失败: {}, 错误信息: {}", taskDis, e.getMessage(), e);
            // 这里可以添加额外的错误处理，比如发送通知等
        }
    }
}