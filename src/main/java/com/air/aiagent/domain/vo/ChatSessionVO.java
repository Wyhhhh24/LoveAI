package com.air.aiagent.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * @author WyH524
 * @since 2025/10/15 11:09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionVO {
    /**
     * 聊天会话ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String chatId;

    /**
     * 会话名称
     */
    private String sessionName;

    /**
     * 会话更新时间
     */
    private LocalDateTime updatedAt;
}
