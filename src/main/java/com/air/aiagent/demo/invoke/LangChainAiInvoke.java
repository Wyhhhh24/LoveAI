package com.air.aiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

/**
 * @author WyH524
 * @since 2025/6/16 下午5:11
 */
public class LangChainAiInvoke {

    private static final String APIKEY = "********";

    public static void main(String[] args) {
        ChatLanguageModel chatModel = QwenChatModel.builder()
                .apiKey(APIKEY)
                .modelName("qwen-max")
                .build();
        //可以直接拿到结果
        String response = chatModel.chat("你是谁？");
        System.out.println(response);
    }
}
