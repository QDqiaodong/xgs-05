CREATE TABLE IF NOT EXISTS custom_invitation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    client_id BIGINT NOT NULL COMMENT '客户ID（发起邀约的用户）',
    creator_id BIGINT NOT NULL COMMENT '创作者ID（被邀约的用户）',
    work_id BIGINT COMMENT '参考作品ID',
    title VARCHAR(200) NOT NULL COMMENT '邀约标题',
    requirements TEXT COMMENT '定制需求描述',
    budget_min DECIMAL(10,2) COMMENT '预算下限',
    budget_max DECIMAL(10,2) COMMENT '预算上限',
    expected_days INT COMMENT '期望完成天数',
    status TINYINT DEFAULT 0 COMMENT '状态 0:待接受 1:已接受 2:已拒绝 3:进行中 4:已完成 5:已取消',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    reference_images TEXT COMMENT '参考图片(JSON数组)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
    INDEX idx_client_id (client_id),
    INDEX idx_creator_id (creator_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定制邀约表';

CREATE TABLE IF NOT EXISTS custom_invitation_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    invitation_id BIGINT NOT NULL COMMENT '邀约ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    content TEXT COMMENT '消息内容',
    images TEXT COMMENT '消息图片(JSON数组)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
    INDEX idx_invitation_id (invitation_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定制邀约沟通留言表';
