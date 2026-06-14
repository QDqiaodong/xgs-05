ALTER TABLE sys_user ADD COLUMN is_certified TINYINT DEFAULT 0 COMMENT '是否认证创作者 0:否 1:是' AFTER creator_level;
ALTER TABLE sys_user ADD COLUMN certified_time DATETIME DEFAULT NULL COMMENT '认证通过时间' AFTER is_certified;
ALTER TABLE sys_user ADD INDEX idx_is_certified (is_certified);

CREATE TABLE IF NOT EXISTS creator_verification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    portfolio_links TEXT COMMENT '作品集链接(JSON数组)',
    creation_experience TEXT COMMENT '创作经历',
    expertise_field VARCHAR(500) COMMENT '擅长领域',
    real_name VARCHAR(50) COMMENT '真实姓名',
    contact_info VARCHAR(200) COMMENT '联系方式',
    additional_materials TEXT COMMENT '补充材料说明',
    status TINYINT DEFAULT 0 COMMENT '状态 0:待审核 1:已通过 2:已拒绝',
    review_remark VARCHAR(500) COMMENT '审核备注',
    reviewer_id BIGINT COMMENT '审核人ID',
    review_time DATETIME COMMENT '审核时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='创作者认证申请表';
