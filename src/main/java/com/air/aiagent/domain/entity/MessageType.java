package com.air.aiagent.domain.entity;

/**
 * 消息类型枚举
 */
public enum MessageType {
    TEXT("文本消息"),
    GAME("游戏消息");

    private final String description;

    MessageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}



