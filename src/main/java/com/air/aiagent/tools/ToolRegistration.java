package com.air.aiagent.tools;

import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 集中的工具注册类
 */
@Configuration
public class ToolRegistration {

    @Value("${search-api.api-key}")
    private String searchApiKey;

    @Resource
    private PDFGenerationTool pdfGenerationTool;

    @Bean
    public ToolCallback[] allTools() {
        //FileOperationTool fileOperationTool = new FileOperationTool();
        //WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        //ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        //TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        //TerminateTool terminateTool = new TerminateTool();
        return ToolCallbacks.from(
//            fileOperationTool,
//            webSearchTool,
//            resourceDownloadTool,
//            terminalOperationTool,
            pdfGenerationTool
//                terminateTool
        );
    }
}
