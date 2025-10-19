create table user
(
    id                  bigint auto_increment comment '主键ID'
        primary key,
    username            varchar(20)                        not null comment '用户名',
    qq_email            varchar(50)                        not null comment 'QQ邮箱',
    relationship_status tinyint  default 0                 null comment '恋爱状态：0-单身，1-恋爱中',
    create_time         datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time         datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted          tinyint  default 0                 not null comment '逻辑删除：0-未删除，1-已删除',
    constraint idx_qq_email
        unique (qq_email),
    constraint idx_username
        unique (username)
)
    comment '用户表';

CREATE TABLE `file` (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件主键ID',
                        `file_url` varchar(255) NOT NULL COMMENT '文件访问URL',
                        `user_id` bigint NOT NULL COMMENT '关联用户ID',
                        `file_name` varchar(100) NOT NULL COMMENT '原始文件名',
                        `is_deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0-未删除 1-已删除)',
                        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                        PRIMARY KEY (`id`),
                        KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件存储表';

ALTER TABLE `file` RENAME TO `userfile`;

-- 或者如果允许为空（根据业务需求）
ALTER TABLE user
    ADD COLUMN password varchar(100) NULL COMMENT '密码（BCrypt加密）' AFTER username;

-- 商品信息表
CREATE TABLE `product` (
                       `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
                       `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
                       `category` VARCHAR(50) NOT NULL COMMENT '商品分类：书籍/礼物/课程',
                       `description` TEXT COMMENT '商品描述',
                       `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
                       `image_url` VARCHAR(500) COMMENT '商品图片URL（MinIO）',
                       `jump_url` VARCHAR(500) NOT NULL COMMENT '跳转链接（淘宝/京东/自营）',
                       `tags` VARCHAR(200) COMMENT '标签（逗号分隔）：道歉,生日,纪念日,表白,学习',
                       `scene` VARCHAR(200) COMMENT '适用场景（逗号分隔）：吵架,纪念日,初次见面,冷战',
                       `status` TINYINT DEFAULT 1 COMMENT '状态：1上架 0下架',
                       `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                       `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                       PRIMARY KEY (`id`),
                       KEY `idx_category` (`category`),
                       KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品信息表';

-- 插入示例商品数据
INSERT INTO `product` (`product_name`, `category`, `description`, `price`, `image_url`, `jump_url`, `tags`, `scene`) VALUES
         ('《亲密关系》', '书籍', '深度解析恋爱心理学经典著作，帮助理解和建立健康的亲密关系', 49.80,
          'https://your-minio.com/products/book-qinmi.jpg',
          'https://s.taobao.com/search?q=亲密关系',
          '心理学,沟通技巧', '吵架,冷战,学习提升'),

         ('星空投影灯', '礼物', '浪漫星空投影灯，营造温馨氛围，适合纪念日送礼', 129.00,
          'https://your-minio.com/products/star-light.jpg',
          'https://item.jd.com/100012345678.html',
          '浪漫,纪念日,惊喜', '纪念日,生日,表白,道歉'),

         ('定制刻字情侣手链', '礼物', '925银情侣手链，可刻字定制，精美礼盒包装', 299.00,
          'https://your-minio.com/products/bracelet.jpg',
          'https://s.taobao.com/search?q=情侣手链刻字',
          '定制,纪念品,仪式感', '纪念日,生日,表白');