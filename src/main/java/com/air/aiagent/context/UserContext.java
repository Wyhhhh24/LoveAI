package com.air.aiagent.context;

import java.util.Optional;

/**
 * 用户上下文管理器（基于ThreadLocal）
 */
public class UserContext {

    // 使用ThreadLocal保存用户ID
    private static final ThreadLocal<String> currentUserId = new ThreadLocal<>();


    /**
     * 设置当前用户ID
     */
    public static void setUserId(String userId) {
        currentUserId.set(userId);
    }


    /**
     * 获取当前用户ID
     */
    public static String getUserId() {
        return currentUserId.get();
    }


    /**
     * 获取当前用户ID（安全方式）
     * 获取 currentUserId 的值，如果值为 null 则返回默认值 "SYSTEM"
     */
    public static String getSafeUserId() {
        return Optional.ofNullable(currentUserId.get()).orElse("SYSTEM");
    }


    /**
     * 清除所有ThreadLocal数据
     */
    public static void clear() {
        currentUserId.remove();
    }
}