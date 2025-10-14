package com.air.aiagent.advisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;
/**
 * 自定义 MyLoggerAdvisor
 * 记录日志
 */
@Slf4j                                 //首先这个拦截器是需要实现这两个接口的
public class MyLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    //重写这个方法，也就是拦截器的名称，这里直接是类名
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    // 这个拦截器在拦截器链中执行的顺序，值越小优先级越高，越先执行。最后添加的顾问会自动将请求发送到 LLM。
    public int getOrder() {
        return 0;
    }

    @Override
    public String toString() {
        return SimpleLoggerAdvisor.class.getSimpleName();
    }

    // 调用大模型前会先调用这个方法
    // 我们就可以自己实现这个方法来记录请求日志
    private AdvisedRequest before(AdvisedRequest request) {
        log.info("AI request: {}", request.userText()); //这个从当前请求中获取用户提示词
        return request;
        // 当加入了结构化输出转化器之后，这个request对像里面的一个advisorContext拦截器上下文，中有这么一个字段
        // 这个字段里面的就是发送给大模型的提示词，就是FormatProvider实现的功能，将我们自己输入的提示词拼接了这样一段话
    }

    //我们就可以自己实现这个方法来记录响应日志
    private void observeAfter(AdvisedResponse advisedResponse) {
        log.info("AI response: {}", advisedResponse.response().getResult().getOutput().getText()); //这里获取响应的结果，而且获得的更精简没有乱七八糟的东西了
    }


    // 下面这两个方法 aroundCall 和 aroundStream 就是我们要实现的两个方法
    // 对于非流式处理 (CallAroundAdvisor)，实现 aroundCall 方法：
    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        //1.先去调用before来处理请求，处理请求（前置处理）
        advisedRequest = this.before(advisedRequest);

        //2. 调用链中的下一个 Advisor
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);

        //2.observeAfter来处理响应，处理响应（后置处理）
        this.observeAfter(advisedResponse);

        return advisedResponse;
    }


    //对于流式处理 (StreamAroundAdvisor)，实现 aroundStream 方法：
    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        // 1. 处理请求
        advisedRequest = before(advisedRequest);

        // 2. 调用链中的下一个Advisor并处理流式响应
        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);
        return (new MessageAggregator()).aggregateAdvisedResponse(advisedResponses, this::observeAfter);
    }
}
