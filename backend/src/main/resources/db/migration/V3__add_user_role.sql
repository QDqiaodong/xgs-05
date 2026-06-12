SET @column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'role'
);

SET @ddl := IF(
    @column_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN role TINYINT DEFAULT 1 COMMENT ''用户角色 1:普通用户 2:管理员'' AFTER email',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE sys_user SET role = 2 WHERE id = 1;
