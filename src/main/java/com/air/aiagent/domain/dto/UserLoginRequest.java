package com.air.aiagent.domain.dto;

import lombok.Data;

/**
 * @author WyH524
 * @since 2025/9/30 13:38
 */
@Data
public class UserLoginRequest {
    /**
     * qq邮箱
     */
    private String qqEmail;

    /**
     * 邮箱验证码
     */
    private String verificationCode;

    /**
     * 密码
     */
    private String password;
}
