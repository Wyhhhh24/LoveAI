package com.air.aiagent.utils;

import com.air.aiagent.exception.BusinessException;
import com.air.aiagent.exception.ErrorCode;

/**
 * 会话ID生成工具类 (简化版)
 * 生成规则：时间戳 + 用户ID
 * 适用于AI恋爱助手等需要全局唯一会话标识的场景
 */
public class SessionIdGenerator {

    /**
     * 生成会话ID
     * @param userId 用户ID
     * @return 格式: timestamp + "_" + userId
     */
    public static String generateSessionId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户 ID 不能为空");
        }
        // 获取当前时间戳
        long currentTimestamp = System.currentTimeMillis();
        // 组合生成最终ID：时间戳_用户ID
        return currentTimestamp + "_" + userId;
    }
}