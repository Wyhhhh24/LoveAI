package com.air.aiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebScrapingToolTest {

    @Test
    void scrapeWebPage() {
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        String url = "https://www.msn.cn/zh-cn/news/other/%E5%A6%82%E4%BD%95%E8%A7%A3%E5%86%B3%E6%83%85%E4%BE%A3%E4%B9%8B%E9%97%B4%E7%9A%84%E7%9F%9B%E7%9B%BE-%E5%85%AD%E6%8B%9B%E5%B8%AE%E4%BD%A0%E8%A7%A3%E5%86%B3%E7%9F%9B%E7%9B%BE/ar-AA1qYyCu";
        String result = webScrapingTool.scrapeWebPage(url);
        System.out.println( result);
        Assertions.assertNotNull(result);
    }
}