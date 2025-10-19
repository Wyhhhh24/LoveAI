package com.air.aiagent.domain.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品推荐VO
 */
@Data
public class ProductRecommendVO {
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 商品分类
     */
    private String category;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 价格
     */
    private BigDecimal price;
    
    /**
     * 商品图片URL
     */
    private String imageUrl;
    
    /**
     * 跳转链接
     */
    private String jumpUrl;

    /**
     * AI推荐理由（可选）
     */
    private String recommendReason;
}