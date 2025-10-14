package com.air.aiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

/**
 * 网页抓取工具
 * 就是根据网址来解析网页的内容
 */
public class WebScrapingTool {

    // 网页抓取工具的作用是根据网址解析到网页的内容。
    // 请求网页，然后得到HTML文件，它可以快速的帮你解析其中一部分的内容，或者返回文档
    @Tool(description = "Scrape the content of a web page")
    public String scrapeWebPage(@ToolParam(description = "URL of the web page to scrape") String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            return doc.html();
        } catch (IOException e) {
            return "Error scraping web page: " + e.getMessage();
        }
    }
}
