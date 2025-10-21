package com.air.aiagent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品信息表
 * @TableName product
 */
@TableName(value ="product")
@Data
public class Product implements Serializable {
    /**
     * 商品ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 商品名称
     */
    private String productName;


    /**
     * 商品分类：书籍/礼物/课程
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
     * 商品图片URL（MinIO）
     */
    private String imageUrl;


    /**
     * 跳转链接（淘宝/京东/自营）
     */
    private String jumpUrl;


    /**
     * 标签（逗号分隔）：道歉,生日,纪念日,表白,学习
     */
    private String tags;


    /**
     * 适用场景（逗号分隔）：吵架,纪念日,初次见面,冷战
     */
    private String scene;


    /**
     * 状态：1上架 0下架
     */
    private Integer status;


    /**
     * 创建时间
     */
    private Date createTime;


    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}