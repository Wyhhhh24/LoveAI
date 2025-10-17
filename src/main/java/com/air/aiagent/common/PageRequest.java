package com.air.aiagent.common;

import lombok.Data;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author WyH524
 * @since 2025/10/14 18:48
 */
@Data
public class PageRequest {
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 当前页码
     */
    private int page;

    /**
     * 展示条数
     */
    private int size;

    /**
     * 排序字段
     */
    private String sortBy;

    /**
     * 升序还是降序
     */
    private String sortDir;
}
