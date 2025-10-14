package com.air.aiagent.aop;
import com.air.aiagent.annotation.ClearContext;
import com.air.aiagent.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author WyH524
 * @since 2025/9/9 下午1:13
 */
@Aspect
@Component
@Slf4j
public class ContextInterceptor {

    /**
     * 后置通知：在方法执行后清理当前线程的 ThreadLocal 避免内存泄漏或数据污染
     * 如下是一种绑定切入点的方法
     */
    @After("@annotation(clearContext)") // 切入点表达式：匹配所有被 @ClearContext 注解标记的方法。
    public void afterClearContext(ClearContext clearContext) {
        UserContext.clear();
        log.info("ThreadLocal context cleared after method execution");
    }
}
