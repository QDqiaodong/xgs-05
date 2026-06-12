SET @column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'creator_level'
);

SET @ddl := IF(
    @column_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN creator_level TINYINT DEFAULT 1 COMMENT ''创作者等级 1:学徒 2:匠人 3:熟练匠人 4:工艺师 5:工艺大师'' AFTER role',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'total_work_count'
);

SET @ddl := IF(
    @column_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN total_work_count INT DEFAULT 0 COMMENT ''作品总数'' AFTER creator_level',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'total_view_count'
);

SET @ddl := IF(
    @column_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN total_view_count INT DEFAULT 0 COMMENT ''总浏览量'' AFTER total_work_count',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'total_favorite_count'
);

SET @ddl := IF(
    @column_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN total_favorite_count INT DEFAULT 0 COMMENT ''总收藏数'' AFTER total_view_count',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'total_like_count'
);

SET @ddl := IF(
    @column_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN total_like_count INT DEFAULT 0 COMMENT ''总获赞数'' AFTER total_favorite_count',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND INDEX_NAME = 'idx_creator_level'
);

SET @ddl := IF(
    @index_exists = 0,
    'CREATE INDEX idx_creator_level ON sys_user(creator_level)',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
