package com.air.aiagent.aop;
import com.air.aiagent.annotation.LoginCheck;
import com.air.aiagent.domain.dto.ChatRequest;
import com.air.aiagent.domain.entity.User;
import com.air.aiagent.exception.BusinessException;
import com.air.aiagent.exception.ErrorCode;
import com.air.aiagent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author WyH524
 * @since 2025/10/10 19:01
 */
@Aspect  // 声明这是一个切面类
@Component  // 由 Spring 容器管理
public class LoginCheckInterceptor {

    @Resource
    private UserService userService;  // 可注入其他依赖

    /**
     * 执行拦截，用户登录了才可以访问，且前端传来的 userId 与 session 存的 userId 一致
     *
     * @param joinPoint 切入点
     * 这是一个切点，就是你想要在哪些地方去执行这里面的代码
     */
    @Around("@annotation(loginCheck)")
    public Object checkLogin(ProceedingJoinPoint joinPoint, LoginCheck loginCheck) throws Throwable {
        // 1.直接获取当前 HTTP 请求的 HttpServletRequest 对象
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 2.获取存储到 session 中的用户信息，这个方法里面如果未获取到是会报错的
        User user = userService.getLoginUser(request);

        // 3.判断获取到的 userId 与前端传过来的 userId 是否一致
        Object[] args = joinPoint.getArgs();
        ChatRequest chatRequest =(ChatRequest)args[0];
        if(chatRequest == null || chatRequest.getChatId() == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数错误");
        }
        if(!Long.valueOf(chatRequest.getChatId()).equals(user.getId())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户登录信息错误");
        }

        // 4.放行
        return joinPoint.proceed();
    }
}
