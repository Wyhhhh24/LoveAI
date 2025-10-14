package com.air.aiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author WyH524
 * @since 2025/6/16 下午3:34
 */
public class HttpAiInvoke {

    private static final String APIKEY = "********";

    public static void main(String[] args) {
        //curl --location "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation" \
        //--header "Authorization: Bearer $DASHSCOPE_API_KEY" \
        //--header "Content-Type: application/json" \
        //--data '{
        //    "model": "qwen-plus",
        //    "input":{
        //        "messages":[
        //            {
        //                "role": "system",
        //                "content": "You are a helpful assistant."
        //            },
        //            {
        //                "role": "user",
        //                "content": "你是谁？"
        //            }
        //        ]
        //    },
        //    "parameters": {
        //        "result_format": "message"
        //    }
        //}'
        //将这个 curl 发送给大模型，让它生成hutool工具类发起请求

        // 请求的URL
        String apiUrl = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
        // 替换为实际的API密钥
        String apiKey = APIKEY; // 请替换为真实的API密钥

        // 构建消息数组
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a helpful assistant.");

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", "你是谁？");

        JSONArray messages = new JSONArray();
        messages.add(systemMessage);
        messages.add(userMessage);

        // 构建input对象
        JSONObject input = new JSONObject();
        input.put("messages", messages);

        // 构建parameters对象
        JSONObject parameters = new JSONObject();
        parameters.put("result_format", "message");

        // 构建完整请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "qwen-plus");
        requestBody.put("input", input);
        requestBody.put("parameters", parameters);

        try {
            // 发送HTTP POST请求
            HttpResponse response = HttpRequest.post(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .execute();

            // 检查响应状态
            if (response.isOk()) {
                String result = response.body();
                System.out.println("API响应: " + result);
            } else {
                System.err.println("请求失败，状态码: " + response.getStatus());
                System.err.println("错误信息: " + response.body());
            }
        } catch (Exception e) {
            System.err.println("请求过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
