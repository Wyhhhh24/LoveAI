package com.air.aiagent.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
public class ChatMessage {

    /**
     * 消息ID
     */
    @Id
    private String id;

    /**
     * 用户ID
     */
    @Field("chat_id")
    private String chatId;

    /**
     * 会话ID
     */
    @Field("session_id")
    private String sessionId;

    /**
     * 消息类型
     */
    @Field("message_type")
    private MessageType messageType;

    /**
     * 消息内容
     */
    @Field("content")
    private String content;

    /**
     * 消息发送时间
     */
    @Field("timestamp")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 是否为AI回复
     * 在展示消息列表的时候，可以根据该字段来判断消息是AI回复还是用户发送的
     * 还有很多作用
     */
    @Field("is_ai_response")
    private Boolean isAiResponse;

    /**
     * 消息元数据
     * 消耗的token那些
     */
    @Field("metadata")
    private MessageMetadata metadata;
}
