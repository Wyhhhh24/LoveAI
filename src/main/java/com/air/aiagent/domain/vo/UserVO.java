package com.air.aiagent.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author WyH524
 * @since 2025/9/27 13:17
 * 添加用户响应类
 */
@Data
public class UserVO {
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * QQ邮箱
     */
    private String qqEmail;

    /**
     * 恋爱状态：0-单身，1-恋爱中
     */
    private Integer relationshipStatus;
}
