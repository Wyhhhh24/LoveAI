package com.air.aiagent.domain.entity;

/**
 * 消息类型枚举
 */
public enum MessageType {
    TEXT("文本消息"),
    IMAGE("图片消息"),
    VOICE("语音消息"),
    FILE("文件消息"),
    SYSTEM("系统消息");

    private final String description;

    MessageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}



