SET @column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'work'
      AND COLUMN_NAME = 'difficulty_level'
);

SET @ddl := IF(
    @column_exists = 0,
    'ALTER TABLE work ADD COLUMN difficulty_level TINYINT DEFAULT NULL COMMENT ''难度等级 1:入门 2:进阶 3:大师'' AFTER is_hot',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
