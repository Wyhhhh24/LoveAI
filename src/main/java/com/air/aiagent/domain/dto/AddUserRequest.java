package com.air.aiagent.domain.dto;

import lombok.Data;

/**
 * @author WyH524
 * @since 2025/9/12 下午1:09
 * 注册用户请求类
 */
@Data
public class AddUserRequest {
    /**
     * qq 邮箱
     */
    private String qqEmail;

    /**
     * 邮箱验证码
     */
    private String verificationCode;

    /**
     * "0" 注册时发送的验证码
     * "1" 登录时发送验证码
     */
    private String verificationCodeType;
}
