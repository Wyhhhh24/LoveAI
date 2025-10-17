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
 * 聊天会话实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_sessions")
public class ChatSession {

    /**
     * 聊天会话ID
     */
    @Id
    private String id;

    /**
     * 用户ID
     */
    @Field("chat_id")
    private String chatId;

    /**
     * 会话名称
     */
    @Field("session_name")
    private String sessionName;

    /**
     * 会话创建时间
     * 。@Builder.Default 注解
     * 。@Builder.Default通过一个标记位来记录你在构建对象时是否显式地为某个字段赋值了
     * 。
     * 。当你没有为这个字段调用设置方法时（例如，没有调用 .isActive(true)），Lombok 在最终构建对象（调用 build()方法）时，会使用你定义的默认值（在你的例子中是 true）。
     * 。当你显式调用了设置方法时（例如，调用了 .isActive(false)），Lombok 则会使用你提供的值，从而覆盖掉默认值
     * 。
     */
    @Field("created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 会话更新时间
     */
    @Field("updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * 会话是否活跃，可以在前端只展示活跃的会话，优化性能
     */
    @Field("is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 会话消息数量
     */
    @Field("message_count")
    @Builder.Default
    private Integer messageCount = 0;
}
