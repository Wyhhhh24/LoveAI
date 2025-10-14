package com.air.aiagent.common;

import com.air.aiagent.exception.ErrorCode;
import lombok.Data;

/**
 * @author WyH524
 * @since 2025/9/12 下午1:02
 * 统一响应类
 */
@Data
public class BaseResponse<T> {
    private int code;

    private T data;

    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
