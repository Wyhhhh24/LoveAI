package com.air.aiagent.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件存储表
 * @TableName file
 */
@TableName(value ="userfile")
@Data
@Builder
public class UserFile implements Serializable {
    /**
     * 文件主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 逻辑删除标记(0-未删除 1-已删除)
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}