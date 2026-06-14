CREATE DATABASE IF NOT EXISTS handmade DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE handmade;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像',
    bio VARCHAR(500) COMMENT '个人简介',
    email VARCHAR(100) COMMENT '邮箱',
    role TINYINT DEFAULT 1 COMMENT '用户角色 1:普通用户 2:管理员',
    creator_level TINYINT DEFAULT 1 COMMENT '创作者等级 1:学徒 2:匠人 3:熟练匠人 4:工艺师 5:工艺大师',
    is_certified TINYINT DEFAULT 0 COMMENT '是否认证创作者 0:否 1:是',
    certified_time DATETIME DEFAULT NULL COMMENT '认证通过时间',
    total_work_count INT DEFAULT 0 COMMENT '作品总数',
    total_view_count INT DEFAULT 0 COMMENT '总浏览量',
    total_favorite_count INT DEFAULT 0 COMMENT '总收藏数',
    total_like_count INT DEFAULT 0 COMMENT '总获赞数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
    INDEX idx_username (username),
    INDEX idx_creator_level (creator_level),
    INDEX idx_is_certified (is_certified)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    description VARCHAR(255) COMMENT '分类描述',
    icon VARCHAR(255) COMMENT '分类图标',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
    INDEX idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

CREATE TABLE IF NOT EXISTS work (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    title VARCHAR(100) NOT NULL COMMENT '作品标题',
    description TEXT COMMENT '作品描述',
    cover_image VARCHAR(255) COMMENT '封面图片',
    images TEXT COMMENT '作品图片(JSON数组)',
    materials VARCHAR(500) COMMENT '用料清单',
    creation_idea TEXT COMMENT '创作思路',
    production_cycle VARCHAR(50) COMMENT '制作周期',
    steps TEXT COMMENT '制作步骤(JSON数组)',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    favorite_count INT DEFAULT 0 COMMENT '收藏数',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    status TINYINT DEFAULT 1 COMMENT '状态 0:下架 1:上架',
    is_hot TINYINT DEFAULT 0 COMMENT '是否热门 0:否 1:是',
    difficulty_level TINYINT DEFAULT NULL COMMENT '难度等级 1:入门 2:进阶 3:大师',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_create_time (create_time),
    INDEX idx_view_count (view_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作品表';

CREATE TABLE IF NOT EXISTS favorite_folder (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(50) NOT NULL COMMENT '收藏夹名称',
    description VARCHAR(255) COMMENT '收藏夹描述',
    cover_image VARCHAR(255) COMMENT '封面图片',
    sort INT DEFAULT 0 COMMENT '排序',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认收藏夹 0:否 1:是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏夹表';

CREATE TABLE IF NOT EXISTS favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    work_id BIGINT NOT NULL COMMENT '作品ID',
    folder_id BIGINT COMMENT '收藏夹ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
    UNIQUE KEY uk_user_work_folder (user_id, work_id, folder_id),
    INDEX idx_user_id (user_id),
    INDEX idx_work_id (work_id),
    INDEX idx_folder_id (folder_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

INSERT INTO category (name, description, icon, sort) VALUES
('编织', '一针一线，编织美好生活', '🧶', 1),
('陶艺', '泥土的艺术，指尖的温度', '🏺', 2),
('布艺', '布料的魔法，匠心的传承', '🧵', 3),
('木艺', '天然木材，手工雕刻', '🪵', 4);

INSERT INTO sys_user (username, password, nickname, avatar, bio, role) VALUES
('admin', 'admin123', '管理员', 'https://via.placeholder.com/100', '平台管理员', 2),
('xiaoshouqiao', '123456', '小手巧', 'https://via.placeholder.com/100', '专注手工编织5年，热爱所有美好的事物', 1),
('taoran', '123456', '陶然', 'https://via.placeholder.com/100', '陶艺爱好者，享受泥土带来的宁静', 1),
('bubu', '123456', '布布', 'https://via.placeholder.com/100', '布艺达人，喜欢制作各种可爱的小物件', 1);

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

CREATE TABLE IF NOT EXISTS activity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '活动主题',
    description TEXT COMMENT '活动介绍',
    rules TEXT COMMENT '参与规则',
    cover_image VARCHAR(255) COMMENT '活动封面图',
    banner_image VARCHAR(255) COMMENT '活动Banner图',
    category_id BIGINT COMMENT '关联分类ID（可选，限定某个分类参加）',
    start_time DATETIME NOT NULL COMMENT '活动开始时间',
    end_time DATETIME NOT NULL COMMENT '活动结束时间',
    vote_start_time DATETIME COMMENT '投票开始时间（默认等于开始时间）',
    vote_end_time DATETIME COMMENT '投票结束时间（默认等于结束时间）',
    max_submit_per_user INT DEFAULT 1 COMMENT '每人最多投稿数',
    max_vote_per_user INT DEFAULT 10 COMMENT '每人最多投票数',
    allow_same_work_multivote TINYINT DEFAULT 0 COMMENT '是否允许同一作品投多票 0:否 1:是',
    work_count INT DEFAULT 0 COMMENT '参赛作品总数',
    vote_count INT DEFAULT 0 COMMENT '总投票数',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    status TINYINT DEFAULT 1 COMMENT '状态 0:关闭 1:开启',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_end_time (end_time),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手作主题活动表';

CREATE TABLE IF NOT EXISTS activity_work (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    work_id BIGINT NOT NULL COMMENT '作品ID',
    user_id BIGINT NOT NULL COMMENT '投稿用户ID',
    submit_remark VARCHAR(500) COMMENT '投稿说明',
    vote_count INT DEFAULT 0 COMMENT '获得票数',
    rank_num INT DEFAULT 0 COMMENT '排名',
    submit_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '投稿时间',
    audit_status TINYINT DEFAULT 1 COMMENT '审核状态 0:待审核 1:已通过 2:已拒绝',
    audit_remark VARCHAR(500) COMMENT '审核备注',
    auditor_id BIGINT COMMENT '审核人ID',
    audit_time DATETIME COMMENT '审核时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
    UNIQUE KEY uk_activity_work (activity_id, work_id),
    INDEX idx_activity_id (activity_id),
    INDEX idx_work_id (work_id),
    INDEX idx_user_id (user_id),
    INDEX idx_vote_count (vote_count),
    INDEX idx_submit_time (submit_time),
    INDEX idx_audit_status (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动作品参赛表';

CREATE TABLE IF NOT EXISTS activity_vote (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    activity_work_id BIGINT NOT NULL COMMENT '活动作品ID',
    work_id BIGINT NOT NULL COMMENT '作品ID',
    user_id BIGINT NOT NULL COMMENT '投票用户ID',
    vote_count INT DEFAULT 1 COMMENT '投票数',
    vote_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '投票时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
    INDEX idx_activity_id (activity_id),
    INDEX idx_activity_work_id (activity_work_id),
    INDEX idx_user_id (user_id),
    INDEX idx_vote_time (vote_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动投票记录表';
