package com.air.aiagent.utils;

import com.air.aiagent.domain.dto.AddUserRequest;
import com.air.aiagent.exception.BusinessException;
import com.air.aiagent.exception.ErrorCode;
import org.apache.commons.lang3.StringUtils;
import java.util.regex.Pattern;
/**
 * 工具类
 * 验证 QQ 邮箱、验证码是否有效
 */
public class ValidationUtils {

    // QQ 邮箱正则：数字5-12位@qq.com
    private static final Pattern QQ_EMAIL_PATTERN = Pattern.compile("^[1-9]\\d{4,11}@qq\\.com$", Pattern.CASE_INSENSITIVE);
    
    // 验证码正则：6 位数字
    private static final Pattern VERIFICATION_CODE_PATTERN = Pattern.compile("^\\d{6}$");

    /**
     * 验证请求参数合法性
     * @param request 用户请求
     */
    public static void validateAddUserRequest(AddUserRequest request) {
        // 验证整体参数
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数不能为空");
        }

        // 验证 QQ 邮箱是否符合要求
        if (StringUtils.isBlank(request.getQqEmail())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"QQ 邮箱不能为空");
        }
        if (!QQ_EMAIL_PATTERN.matcher(request.getQqEmail()).matches()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"QQ 邮箱格式错误");
        }

        // 验证验证码是否正确
        if (StringUtils.isBlank(request.getVerificationCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码不能为空");
        }
        if (!VERIFICATION_CODE_PATTERN.matcher(request.getVerificationCode()).matches()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码格式错误");
        }
    }

    /**
     * 验证 QQ 邮箱格式是否正确
     * @param email 待验证的邮箱字符串
     * @return true-符合QQ邮箱格式, false-不符合或为空
     */
    public static boolean isValidQqEmail(String email) {
        // 1. 判空处理（同时排除纯空格的情况）
        if (StringUtils.isBlank(email)) {
            return false;
        }
        // 2. 正则匹配（已预编译 Pattern ，线程安全）
        return QQ_EMAIL_PATTERN.matcher(email.trim()).matches();
    }
}