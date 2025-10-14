package com.air.aiagent.domain.vo;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @author WyH524
 * @since 2025/10/10 20:19
 */
@Data
@Builder
public class UserFileVO {
    /**
     * 文件主键ID
     */
    private Long id;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 创建时间
     */
    private Date createTime;
}
