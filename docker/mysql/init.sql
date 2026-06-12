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
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
    INDEX idx_username (username)
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
