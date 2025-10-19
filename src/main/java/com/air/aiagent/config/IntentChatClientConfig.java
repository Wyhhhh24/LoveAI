package com.air.aiagent.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 意图识别专用模型配置
 */
@Configuration
public class IntentChatClientConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    /**
     * 创建意图识别专用的 ChatModel Bean
     * 注意：这里只创建了 ChatModel 实例，具体使用哪个模型在调用时指定
     */
    @Bean("intentChatModel")
    public ChatModel intentChatModel() {
        // 创建 DashScope API
        DashScopeApi dashScopeApi = new DashScopeApi(apiKey);

        // 创建 DashScopeChatModel（不指定模型，让调用者决定）
        return new DashScopeChatModel(dashScopeApi);
        // ChatModel intentChatModel = new DashScopeChatModel(dashScopeApi);
        // 这里只创建了一个到阿里云的连接
        // 并没有指定使用哪个模型
        // qwen3-max 主模型，不影响
    }
}