package com.air.aiagent.Enum;

import lombok.Getter;

/**
 * 验证码类型枚举
 */
@Getter
public enum VerificationCodeTypeEnum {

    REGISTER("0", "注册验证码"),
    LOGIN("1", "登录验证码");

    private final String value;

    private final String text;

    VerificationCodeTypeEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }


    /**
     * 根据 value 获取枚举实例
     */
    public static VerificationCodeTypeEnum getByValue(String value) {
        for (VerificationCodeTypeEnum type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }


    /**
     * 检查 value 是否有效,检查是否
     */
    public static boolean isValid(String value) {
        return getByValue(value) != null;
    }
}