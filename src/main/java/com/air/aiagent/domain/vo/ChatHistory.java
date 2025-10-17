package com.air.aiagent.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author WyH524
 * @since 2025/10/15 15:28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistory {
    /**
     * 最新会话中的消息历史
     */
    private List<ChatMessageVO> chatMessageVOList;

    /**
     * 最新会话的会话ID
     */
    private String sessionId;
}
