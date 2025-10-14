package com.air.aiagent.utils;

import com.air.aiagent.exception.BusinessException;
import com.air.aiagent.exception.ErrorCode;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BCrypt 密码工具类
 * 提供密码加密和验证功能
 * 注意：BCrypt 是单向哈希算法，无法解密
 * 
 * @author AI Agent
 * @version 1.0
 */
public final class BCryptUtils {
    
    private static final Logger log = LoggerFactory.getLogger(BCryptUtils.class);
    
    // 加密成本因子，推荐值：10-12
    // 10: 约100ms, 11: 约200ms, 12: 约400ms (在普通服务器上)
    private static final int BCRYPT_ROUNDS = 10;
    
    // 私有构造方法，防止实例化
    private BCryptUtils() {
        throw new UnsupportedOperationException("这是一个工具类，不需要实例化");
    }

    /**
     * 加密密码
     * 
     * @param plainPassword 明文密码
     * @return 加密后的密码
     * @throws IllegalArgumentException 如果密码为空或过短
     */
    public static String encrypt(String plainPassword) {
        validatePassword(plainPassword);
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * 验证密码
     * 
     * @param plainPassword 用户输入的明文密码
     * @param hashedPassword 数据库中存储的加密密码
     * @return 是否匹配
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            log.warn("密码验证失败: 输入参数为空");
            return false;
        }
        
        if (!isBCryptHash(hashedPassword)) {
            log.warn("密码验证失败: 哈希格式不正确");
            return false;
        }
        
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            log.error("密码验证异常: {}", e.getMessage());
            return false;
        }
    }


    /**
     * 检查字符串是否为有效的 BCrypt 哈希
     * 
     * @param hashedPassword 待检查的字符串
     * @return 是否为有效的 BCrypt 哈希
     */
    public static boolean isBCryptHash(String hashedPassword) {
        if (hashedPassword == null) {
            return false;
        }
        // BCrypt 哈希通常以 $2a$, $2b$, $2y$ 开头
        return hashedPassword.startsWith("$2a$") || 
               hashedPassword.startsWith("$2b$") || 
               hashedPassword.startsWith("$2y$");
    }


    /**
     * 验证密码并抛出详细异常
     */
    private static void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码不能为空");
        }
        
        if (password.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度不能少于6位");
        }
        
        if (password.length() > 72) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度不能超过72位");
        }
    }
}