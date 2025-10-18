package com.air.aiagent.domain.vo;

import lombok.*;

/**
 * @author WyH524
 * @since 2025/10/18 14:49
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameChatVO {

    /**
     * 情绪
     */
    private String emo;

    /**
     * 会话 Id
     */
    private String sessionId;
}
