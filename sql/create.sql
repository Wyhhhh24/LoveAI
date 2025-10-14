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