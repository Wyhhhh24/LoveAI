package com.air.aiagent.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 消息元数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageMetadata {
    private String model;

    private String version;

    private Integer tokenCount;

    private Double confidence;

    private Integer responseTimeMs;

    private Map<String, Object> customData;
}
