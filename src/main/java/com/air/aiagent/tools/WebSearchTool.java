package com.air.aiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import java.util.HashMap;
import java.util.Map;

/**
 * 网页搜索工具
 * 基于 SearchAPI 实现百度搜索功能，返回格式化的搜索结果
 * 
 * @author WyH524
 */
@Slf4j
public class WebSearchTool {

    // SearchAPI 的搜索接口地址
    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    // 默认返回的搜索结果数量
    private static final int DEFAULT_RESULT_COUNT = 5;

    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * 搜索网页内容
     *
     * @param query 搜索关键词
     * @return 格式化的搜索结果字符串
     */
    @Tool(description = "百度搜索工具，根据关键词搜索互联网信息，返回格式化的搜索结果（标题、摘要、链接）。适用于查找最新恋爱技巧、情感咨询、约会建议等网络信息")
    public String searchWeb(
            @ToolParam(description = "搜索关键词，如：如何追女生、情侣吵架怎么办、恋爱心理学") String query) {

        if (query == null || query.trim().isEmpty()) {
            return "搜索关键词不能为空";
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("q", query);
        paramMap.put("api_key", apiKey);
        paramMap.put("engine", "baidu");

        try {
            log.info("开始搜索：{}", query);
            String response = HttpUtil.get(SEARCH_API_URL, paramMap);

            JSONObject jsonObject = JSONUtil.parseObj(response);
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");

            if (organicResults == null || organicResults.isEmpty()) {
                log.warn("搜索无结果：{}", query);
                return "未找到相关搜索结果，请尝试其他关键词";
            }

            // 提取前5条结果
            int resultCount = Math.min(organicResults.size(), DEFAULT_RESULT_COUNT);
            StringBuilder formattedResults = new StringBuilder();
            formattedResults.append(String.format("搜索「%s」找到以下结果：\n\n", query));

            for (int i = 0; i < resultCount; i++) {
                JSONObject result = organicResults.getJSONObject(i);

                String title = result.getStr("title", "无标题");
                String snippet = result.getStr("snippet", "暂无摘要");
                String link = result.getStr("link", "");

                formattedResults.append(String.format("%d. 【%s】\n", i + 1, title));
                formattedResults.append(String.format("   摘要：%s\n", snippet));

                if (!link.isEmpty()) {
                    formattedResults.append(String.format("   链接：%s\n", link));
                }

                formattedResults.append("\n");
            }

            log.info("搜索完成：{}，返回 {} 条结果", query, resultCount);
            return formattedResults.toString().trim();

        } catch (Exception e) {
            log.error("搜索失败：{}，错误：{}", query, e.getMessage(), e);
            return String.format("搜索服务暂时不可用，请稍后再试。错误信息：%s", e.getMessage());
        }
    }
}
