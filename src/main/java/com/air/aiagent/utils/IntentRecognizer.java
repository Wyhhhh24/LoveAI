package com.air.aiagent.utils;

import com.air.aiagent.advisor.MyLoggerAdvisor;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 意图识别器 - 判断用户是否需要商品推荐
 */
@Component
@Slf4j
public class IntentRecognizer {

    private final ChatClient intentClient;

    @Value("${ai.intent.model:qwen-turbo}")
    private String intentModel;

    @Value("${ai.intent.temperature:0.1}")
    private Double temperature;

    @Value("${ai.intent.max-tokens:50}")
    private Integer maxTokens;

    /**
     * 注入意图识别专用的 ChatModel
     */
    public IntentRecognizer(@Qualifier("intentChatModel") ChatModel intentChatModel) {
        this.intentClient = ChatClient
                .builder(intentChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
    }


    /**
     * 判断用户是否有购买/礼物推荐需求
     * 
     * @param userMessage 用户消息
     * @return true-需要推荐商品，false-不需要
     */
    public boolean needProductRecommend(String userMessage) {
        try {
            // 构建全面的提示词
            String prompt = buildIntentPrompt(userMessage);

            // 创建 options 对象，指定使用 qwen-turbo 模型
            DashScopeChatOptions options = DashScopeChatOptions.builder()
                    .withModel(intentModel)      // qwen-turbo
                    .withTemperature(temperature)  // 0.1
                    .withMaxToken(maxTokens)    // 50
                    .build();

            
            String result = intentClient.prompt()
                .messages(List.of(
                    new SystemMessage("你是一个意图识别助手，只回答'是'或'否'，不要有任何解释。"),
                    new UserMessage(prompt)
                ))
                .options(options)
                .call()
                .content();
            
            boolean needRecommend = result.trim().contains("是");
            log.info("意图识别 - 用户消息: {}, AI判断结果: {}, 是否推荐商品: {}", userMessage, result, needRecommend);
            return needRecommend;
        } catch (Exception e) {
            log.error("意图识别失败，降级为不推荐商品", e);
            return false; // 失败时默认不推荐
        }
    }
    
    /**
     * 识别具体场景（用于精准推荐）
     * 
     * @param userMessage 用户消息
     * @return 场景类型：道歉/纪念日/表白/生日/学习提升/通用
     */
    public String recognizeScene(String userMessage) {
        try {
            String prompt = """
                判断以下用户提问属于哪个场景，只回答场景名称，不要有任何解释。
                
                可选场景：道歉、纪念日、表白、生日、学习提升、通用
                
                用户提问：%s
                """.formatted(userMessage);

            // 创建 options 对象，指定使用 qwen-turbo 模型
            DashScopeChatOptions options = DashScopeChatOptions.builder()
                    .withModel(intentModel)      // qwen-turbo
                    .withTemperature(temperature)  // 0.1
                    .withMaxToken(maxTokens)    // 50
                    .build();

            
            String result = intentClient.prompt()
                .user(prompt)
                .options(options)
                .call()
                .content();
            
            String scene = result.trim().replaceAll("[，。、]", "");
            log.info("场景识别 - 用户消息: {}, 识别场景: {}", userMessage, scene);
            
            return scene;
            
        } catch (Exception e) {
            log.error("场景识别失败，使用默认场景", e);
            return "通用";
        }
    }
    
    /**
     * 构建意图识别提示词
     */
    private String buildIntentPrompt(String userMessage) {
        return """
            判断以下用户提问是否有购买书籍、礼物或需要商品推荐的需求。
            
            判断标准：
            - 包含"送什么"、"买什么"、"推荐"、"礼物"等关键词 → 是
            - 询问生日、纪念日、道歉、表白相关的具体建议 → 是
            - 单纯咨询情感问题，不涉及购买行为 → 否
            - 明确表示"不需要"、"不想买" → 否
            
            用户提问：%s
            
            请只回答"是"或"否"。
            """.formatted(userMessage);
    }
}