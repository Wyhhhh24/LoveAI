package com.air.aiagent.domain.vo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author WyH524
 * @since 2025/10/15 14:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageVO {

    /**
     * 消息ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String chatId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 推荐的商品列表（如果有）
     */
    private List<ProductVO> recommendedProducts;

    /**
     * 是否为AI回复
     * 在展示消息列表的时候，可以根据该字段来判断消息是AI回复还是用户发送的
     * 还有很多作用
     */
    private Boolean isAiResponse;
}
