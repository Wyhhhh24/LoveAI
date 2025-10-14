package com.air.aiagent.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 恋爱报告响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoveReportResponse {
    
    private String title;
    
    private List<String> suggestions;
}
