ALTER TABLE sys_user ADD COLUMN creator_level TINYINT DEFAULT 1 COMMENT '创作者等级 1:学徒 2:匠人 3:熟练匠人 4:工艺师 5:工艺大师' AFTER role;

ALTER TABLE sys_user ADD COLUMN total_work_count INT DEFAULT 0 COMMENT '作品总数' AFTER creator_level;

ALTER TABLE sys_user ADD COLUMN total_view_count INT DEFAULT 0 COMMENT '总浏览量' AFTER total_work_count;

ALTER TABLE sys_user ADD COLUMN total_favorite_count INT DEFAULT 0 COMMENT '总收藏数' AFTER total_view_count;

ALTER TABLE sys_user ADD COLUMN total_like_count INT DEFAULT 0 COMMENT '总获赞数' AFTER total_favorite_count;

CREATE INDEX idx_creator_level ON sys_user(creator_level);
