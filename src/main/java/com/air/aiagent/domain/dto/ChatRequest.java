package com.air.aiagent.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 聊天请求DTO
 */
@Data
public class ChatRequest {
    /**
     * 用户提示词
     */
    private String message;

    /**
     * 用户id
     */
    private String chatId;
}
